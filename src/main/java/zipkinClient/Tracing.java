package zipkinClient;

import brave.internal.Nullable;
import brave.internal.Platform;
import brave.propagation.B3Propagation;
import brave.propagation.CurrentTraceContext;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.sampler.Sampler;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;
import zipkin2.Endpoint;
import zipkin2.zipkin;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.Sender;

/**
 * This provides utilities needed for trace instrumentation. For example, a {@link Tracer}.
 *
 * <p>Instances built via {@link #newBuilder()} are registered automatically such that statically
 * configured instrumentation like JDBC drivers can use {@link #current()}.
 *
 * <p>This type can be extended so that the object graph can be built differently or overridden, for
 * example via spring or when mocking.
 */
public abstract class Tracing implements Closeable {

  public static Builder newBuilder() {
    return new Builder();
  }

  /** All tracing commands start with a {@link Span}. Use a tracer to create spans. */
  abstract public Tracer tracer();

  /**
   * When a trace leaves the process, it needs to be propagated, usually via headers. This utility
   * is used to inject or extract a trace context from remote requests.
   */
  // Implementations should override and cache this as a field.
  public Propagation<String> propagation() {
    return propagationFactory().create(Propagation.KeyFactory.STRING);
  }

  /** This supports edge cases like GRPC Metadata propagation which doesn't use String keys. */
  abstract public Propagation.Factory propagationFactory();

  /**
   * This supports in-process propagation, typically across thread boundaries. This includes
   * utilities for concurrent types like {@linkplain java.util.concurrent.ExecutorService}.
   */
  abstract public CurrentTraceContext currentTraceContext();

  /** @deprecated use {@link #clock(TraceContext)} */
  @Deprecated abstract public Clock clock();

  /**
   * This exposes the microsecond clock used by operations such as {@link Span#finish()}. This is
   * helpful when you want to time things manually. Notably, this clock will be coherent for all
   * child spans in this trace (that use this tracing component). For example, NTP or system clock
   * changes will not affect the result.
   *
   * @param context references a potentially unstarted span you'd like a clock correlated with
   */
  public final Clock clock(TraceContext context) {
    return tracer().recorder.clock(context);
  }

  // volatile for visibility on get. writes guarded by Tracing.class
  static volatile Tracing current = null;

  /**
   * Returns the most recently created tracer if its component hasn't been closed. null otherwise.
   *
   * <p>This object should not be cached.
   */
  @Nullable public static Tracer currentTracer() {
    Tracing tracing = current;
    return tracing != null ? tracing.tracer() : null;
  }

  final AtomicBoolean noop = new AtomicBoolean(false);

  /**
   * When true, no recording is done and nothing is reported to zipkin. However, trace context is
   * still injected into outgoing requests.
   *
   * @see Span#isNoop()
   */
  public boolean isNoop() {
    return noop.get();
  }

  /**
   * Set true to drop data and only return {@link Span#isNoop() noop spans} regardless of sampling
   * policy. This allows operators to stop tracing in risk scenarios.
   *
   * @see #isNoop()
   */
  public void setNoop(boolean noop) {
    this.noop.set(noop);
  }

  /**
   * Returns the most recently created tracing component iff it hasn't been closed. null otherwise.
   *
   * <p>This object should not be cached.
   */
  @Nullable public static Tracing current() {
    return current;
  }

  /** Ensures this component can be garbage collected, by making it not {@link #current()} */
  @Override abstract public void close();

  public static final class Builder {
    String localServiceName;
    Endpoint endpoint;
    Reporter<zipkin2.Span> reporter;
    Clock clock;
    Sampler sampler = Sampler.ALWAYS_SAMPLE;
    CurrentTraceContext currentTraceContext = CurrentTraceContext.Default.inheritable();
    boolean traceId128Bit = false;
    boolean supportsJoin = true;
    Propagation.Factory propagationFactory = B3Propagation.FACTORY;

    /**
     * Controls the name of the service being traced, while still using a default site-local IP.
     * This is an alternative to {@link #endpoint(Endpoint)}.
     *
     * @param localServiceName name of the service being traced. Defaults to "unknown".
     */
    public Builder localServiceName(String localServiceName) {
      if (localServiceName == null) throw new NullPointerException("localServiceName == null");
      this.localServiceName = localServiceName;
      return this;
    }

    /** @deprecated use {@link #endpoint(Endpoint)}, possibly with {@link zipkin.Endpoint#toV2()} */
    @Deprecated
    public Builder localEndpoint(zipkin.Endpoint localEndpoint) {
      if (localEndpoint == null) throw new NullPointerException("localEndpoint == null");
      return endpoint(localEndpoint.toV2());
    }

    /** @deprecated use {@link #endpoint(Endpoint)} which compiles without io.zipkin.java:zipkin */
    // compiling a call to an overloaded method requires all types on the classpath.
    @Deprecated
    public Builder localEndpoint(Endpoint localEndpoint) {
      return endpoint(localEndpoint);
    }

    /**
     * Sets the {@link zipkin2.Span#localEndpoint Endpoint of the local service} being traced.
     * Defaults to a site local IP.
     *
     * <p>Use {@link #localServiceName} when only effecting the service name.
     */
    public Builder endpoint(Endpoint endpoint) {
      if (endpoint == null) throw new NullPointerException("endpoint == null");
      this.endpoint = endpoint;
      return this;
    }

    /**
     * Controls how spans are reported. Defaults to logging, but often an {@link AsyncReporter}
     * which batches spans before sending to Zipkin.
     *
     * The {@link AsyncReporter} includes a {@link Sender}, which is a driver for transports like
     * http, kafka and scribe.
     *
     * <p>For example, here's how to batch send spans via http:
     *
     * <pre>{@code
     * spanReporter = AsyncReporter.create(URLConnectionSender.create("http://localhost:9411/api/v2/spans"));
     *
     * tracingBuilder.spanReporter(spanReporter);
     * }</pre>
     *
     * <p>See https://github.com/openzipkin/zipkin-reporter-java
     */
    public Builder spanReporter(Reporter<zipkin2.Span> reporter) {
      if (reporter == null) throw new NullPointerException("spanReporter == null");
      this.reporter = reporter;
      return this;
    }

    /** @deprecated use {@link #spanReporter(Reporter)} */
    @Deprecated
    public Builder reporter(final zipkin.reporter.Reporter<zipkin.Span> reporter) {
      if (reporter == null) throw new NullPointerException("spanReporter == null");
      if (reporter == zipkin.reporter.Reporter.NOOP) {
        this.reporter = Reporter.NOOP;
        return this;
      }
      this.reporter = new Reporter<zipkin2.Span>() {
        @Override public void report(zipkin2.Span span) {
          reporter.report(brave.internal.V2SpanConverter.toSpan(span));
        }

        @Override public String toString() {
          return reporter.toString();
        }
      };
      return this;
    }

    /**
     * Assigns microsecond-resolution timestamp source for operations like {@link Span#start()}.
     * Defaults to JRE-specific platform time.
     *
     * <p>Note: timestamps are read once per trace, then {@link System#nanoTime() ticks} thereafter.
     * This ensures there's no clock skew problems inside a single trace.
     *
     * See {@link Tracing#clock(TraceContext)}
     */
    public Builder clock(Clock clock) {
      if (clock == null) throw new NullPointerException("clock == null");
      this.clock = clock;
      return this;
    }

    /**
     * Sampler is responsible for deciding if a particular trace should be "sampled", i.e. whether
     * the overhead of tracing will occur and/or if a trace will be reported to Zipkin.
     */
    public Builder sampler(Sampler sampler) {
      if (sampler == null) throw new NullPointerException("sampler == null");
      this.sampler = sampler;
      return this;
    }

    /**
     * Responsible for implementing {@link Tracer#currentSpanCustomizer()}, {@link Tracer#currentSpan()}
     * and {@link Tracer#withSpanInScope(Span)}. By default a simple thread-local is used. Override
     * to support other mechanisms or to synchronize with other mechanisms such as SLF4J's MDC.
     */
    public Builder currentTraceContext(CurrentTraceContext currentTraceContext) {
      if (currentTraceContext == null) throw new NullPointerException("currentTraceContext == null");
      this.currentTraceContext = currentTraceContext;
      return this;
    }

    /**
     * Controls how trace contexts are injected or extracted from remote requests, such as from http
     * headers. Defaults to {@link B3Propagation#FACTORY}
     */
    public Builder propagationFactory(Propagation.Factory propagationFactory) {
      if (propagationFactory == null) throw new NullPointerException("propagationFactory == null");
      this.propagationFactory = propagationFactory;
      return this;
    }

    /** When true, new root spans will have 128-bit trace IDs. Defaults to false (64-bit) */
    public Builder traceId128Bit(boolean traceId128Bit) {
      this.traceId128Bit = traceId128Bit;
      return this;
    }

    /**
     * True means the tracing system supports sharing a span ID between a {@link Span.Kind#CLIENT}
     * and {@link Span.Kind#SERVER} span. Defaults to true.
     *
     * <p>Set this to false when the tracing system requires the opposite. For example, if
     * ultimately spans are sent to Amazon X-Ray or Google Stackdriver Trace, you should set this to
     * false.
     *
     * <p>This is implicitly set to false when {@link Propagation.Factory#supportsJoin()} is false,
     * as in that case, sharing IDs isn't possible anyway.
     *
     * @see Propagation.Factory#supportsJoin()
     */
    public Builder supportsJoin(boolean supportsJoin) {
      this.supportsJoin = supportsJoin;
      return this;
    }

    public Tracing build() {
      if (clock == null) clock = Platform.get().clock();
      if (endpoint == null) {
        endpoint = Platform.get().endpoint();
        if (localServiceName != null) {
          endpoint = endpoint.toBuilder().serviceName(localServiceName).build();
        }
      }
      if (reporter == null) reporter = Platform.get().reporter();
      return new Default(this);
    }

    Builder() {
    }
  }

  static final class Default extends Tracing {
    final Tracer tracer;
    final Propagation.Factory propagationFactory;
    final Propagation<String> stringPropagation;
    final CurrentTraceContext currentTraceContext;
    final Clock clock;

    Default(Builder builder) {
      this.clock = builder.clock;
      this.tracer = new Tracer(builder, clock, noop);
      this.propagationFactory = builder.propagationFactory;
      this.stringPropagation = builder.propagationFactory.create(Propagation.KeyFactory.STRING);
      this.currentTraceContext = builder.currentTraceContext;
      maybeSetCurrent();
    }

    @Override public Tracer tracer() {
      return tracer;
    }

    @Override public Propagation<String> propagation() {
      return stringPropagation;
    }

    @Override public Propagation.Factory propagationFactory() {
      return propagationFactory;
    }

    @Override public CurrentTraceContext currentTraceContext() {
      return currentTraceContext;
    }

    @Override public Clock clock() {
      return clock;
    }

    private void maybeSetCurrent() {
      if (current != null) return;
      synchronized (Tracing.class) {
        if (current == null) current = this;
      }
    }

    @Override public void close() {
      if (current != this) return;
      // don't blindly set most recent to null as there could be a race
      synchronized (Tracing.class) {
        if (current == this) current = null;
      }
    }
  }
}
