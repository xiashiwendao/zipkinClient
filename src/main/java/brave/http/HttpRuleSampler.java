package brave.http;

import brave.Tracing;
import brave.internal.Nullable;
import brave.sampler.ParameterizedSampler;
import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Assigns sample rates to http routes.
 *
 * <p>Ex. Here's a sampler that traces 80% requests to /foo and 10% of POST requests to /bar. Other
 * requests will use a global rate provided by the {@link Tracing tracing component}.
 * <pre>{@code
 * httpTracingBuilder.serverSampler(HttpRuleSampler.newBuilder()
 *   .addRule(null, "/foo", 0.8f)
 *   .addRule("POST", "/bar", 0.1f)
 *   .build());
 * }</pre>
 *
 * <p>Note that the path is a prefix, so "/foo" will match "/foo/abcd".
 */
public final class HttpRuleSampler extends HttpSampler {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {
    final List<MethodAndPathRule> rules = new ArrayList<MethodAndPathRule>();

    /**
     * Assigns a sample rate to all requests that match the input.
     *
     * @param method if null, any method is accepted
     * @param path all paths starting with this string are accepted
     * @param rate percentage of requests to start traces for. 1.0 is 100%
     */
    public Builder addRule(@Nullable String method, String path, float rate) {
      rules.add(new MethodAndPathRule(method, path, rate));
      return this;
    }

    public HttpSampler build() {
      return new HttpRuleSampler(rules);
    }

    Builder() {
    }
  }

  final ParameterizedSampler<MethodAndPath> sampler;

  HttpRuleSampler(List<MethodAndPathRule> rules) {
    this.sampler = ParameterizedSampler.create(rules);
  }

  @Override public <Req> Boolean trySample(HttpAdapter<Req, ?> adapter, Req request) {
    String method = adapter.method(request);
    String path = adapter.path(request);
    if (method == null || path == null) return null; // use default if we couldn't parse
    return sampler.sample(MethodAndPath.create(method, path)).sampled();
  }

  @AutoValue
  static abstract class MethodAndPath {
    static MethodAndPath create(String method, String path) {
      return new AutoValue_HttpRuleSampler_MethodAndPath(method, path);
    }

    abstract String method();

    abstract String path();
  }

  static final class MethodAndPathRule extends ParameterizedSampler.Rule<MethodAndPath> {
    @Nullable final String method;
    final String path;

    MethodAndPathRule(@Nullable String method, String path, float rate) {
      super(rate);
      this.method = method;
      this.path = path;
    }

    @Override public boolean matches(MethodAndPath parameters) {
      if (method != null && !method.equals(parameters.method())) return false;
      return parameters.path().startsWith(path);
    }
  }
}
