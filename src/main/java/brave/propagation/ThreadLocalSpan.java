package brave.propagation;

import brave.internal.Nullable;
import zipkinClient.Span;
import zipkinClient.Tracer;
import zipkinClient.Tracing;
import zipkinClient.Tracer.SpanInScope;

import com.google.auto.value.AutoValue;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * This type allows you to place a span in scope in one method and access it in another without
 * using an explicit request parameter.
 *
 * <p>Many libraries expose a callback model as opposed to an interceptor one. When creating new
 * instrumentation, you may find places where you need to place a span in scope in one callback
 * (like `onStart()`) and end the scope in another callback (like `onFinish()`).
 *
 * <p>Provided the library guarantees these run on the same thread, you can simply propagate the
 * result of {@link Tracer#withSpanInScope(Span)} from the starting callback to the closing one.
 * This is typically done with a request-scoped attribute.
 *
 * Here's an example:
 * <pre>{@code
 * class MyFilter extends Filter {
 *   public void onStart(Request request, Attributes attributes) {
 *     // Assume you have code to start the span and add relevant tags...
 *
 *     // We now set the span in scope so that any code between here and
 *     // the end of the request can see it with Tracer.currentSpan()
 *     SpanInScope spanInScope = tracer.withSpanInScope(span);
 *
 *     // We don't want to leak the scope, so we place it somewhere we can
 *     // lookup later
 *     attributes.put(SpanInScope.class, spanInScope);
 *   }
 *
 *   public void onFinish(Response response, Attributes attributes) {
 *     // as long as we are on the same thread, we can read the span started above
 *     Span span = tracer.currentSpan();
 *
 *     // Assume you have code to complete the span
 *
 *     // We now remove the scope (which implicitly detaches it from the span)
 *     attributes.remove(SpanInScope.class).close();
 *   }
 * }
 * }</pre>
 *
 * <p>Sometimes you have to instrument a library where There's no attribute namespace shared across
 * request and response. For this scenario, you can use {@link ThreadLocalSpan} to temporarily store
 * the span between callbacks.
 *
 * Here's an example:
 * <pre>{@code
 * class MyFilter extends Filter {
 *   final ThreadLocalSpan threadLocalSpan;
 *
 *   public void onStart(Request request) {
 *     // Allocates a span and places it in scope so that code between here and onFinish can see it
 *     Span span = threadLocalSpan.next();
 *     if (span == null || span.isNoop()) return; // skip below logic on noop
 *
 *     // Assume you have code to start the span and add relevant tags...
 *   }
 *
 *   public void onFinish(Response response, Attributes attributes) {
 *     // as long as we are on the same thread, we can read the span started above
 *     Span span = threadLocalSpan.remove();
 *     if (span == null || span.isNoop()) return; // skip below logic on noop
 *
 *     // Assume you have code to complete the span
 *   }
 * }
 * }</pre>
 */
@AutoValue
public abstract class ThreadLocalSpan {
  /**
   * This uses the {@link Tracing#currentTracer()}, which means calls to {@link #next()} may return
   * null. Use this when you have no other means to get a reference to the tracer. For example, JDBC
   * connections, as they often initialize prior to the tracing component.
   */
  public static final ThreadLocalSpan CURRENT_TRACER = new ThreadLocalSpan() {
    @Override Tracer tracer() {
      return Tracing.currentTracer();
    }
  };

  public static ThreadLocalSpan create(Tracer tracer) {
    return new AutoValue_ThreadLocalSpan(tracer);
  }

  /**
   * This keeps track of a stack with a normal array dequeue. Redundant stacking of the same span is
   * not possible because there is no api to place an arbitrary span in scope using this api.
   */
  final ThreadLocal<Deque<SpanAndScope>> currentSpanInScope =
      new ThreadLocal<Deque<SpanAndScope>>() {
        @Override protected Deque<SpanAndScope> initialValue() {
          return new ArrayDeque<SpanAndScope>();
        }
      };

  abstract Tracer tracer();

  /**
   * Returns the {@link Tracer#nextSpan(TraceContextOrSamplingFlags)} or null if {@link #CURRENT_TRACER}
   * and tracing isn't available.
   */
  @Nullable public Span next(TraceContextOrSamplingFlags extracted) {
    Tracer tracer = tracer();
    if (tracer == null) return null;
    Span next = tracer.nextSpan(extracted);
    SpanAndScope spanAndScope = SpanAndScope.create(next, tracer.withSpanInScope(next));
    currentSpanInScope.get().addFirst(spanAndScope);
    return next;
  }

  /**
   * Returns the {@link Tracer#nextSpan()} or null if {@link #CURRENT_TRACER} and tracing isn't
   * available.
   */
  @Nullable public Span next() {
    Tracer tracer = tracer();
    if (tracer == null) return null;
    Span next = tracer.nextSpan();
    SpanAndScope spanAndScope = SpanAndScope.create(next, tracer.withSpanInScope(next));
    currentSpanInScope.get().addFirst(spanAndScope);
    return next;
  }

  /**
   * Returns the span set in scope via {@link #next()} or null if there was none.
   *
   * <p>When assertions are on, this will throw an assertion error if the span returned was not the
   * one currently in context. This could happen if someone called {@link Tracer#withSpanInScope(Span)}
   * or {@link CurrentTraceContext#newScope(TraceContext)} outside a try/finally block.
   */
  @Nullable public Span remove() {
    Tracer tracer = tracer();
    Span currentSpan = tracer != null ? tracer.currentSpan() : null;
    SpanAndScope scope = currentSpanInScope.get().pollFirst();
    if (scope == null) return currentSpan;

    scope.scope().close();
    assert scope.span().equals(currentSpan) :
        "Misalignment: scoped span " + scope.span() + " !=  current span " + currentSpan;
    return currentSpan;
  }

  ThreadLocalSpan() {
  }

  /** Allows state checks when nesting spans */
  @AutoValue
  static abstract class SpanAndScope {
    static SpanAndScope create(Span span, SpanInScope scope) {
      return new AutoValue_ThreadLocalSpan_SpanAndScope(span, scope);
    }

    abstract Span span();

    abstract SpanInScope scope();
  }
}
