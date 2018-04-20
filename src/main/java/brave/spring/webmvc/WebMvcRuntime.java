package brave.spring.webmvc;

import brave.http.HttpTracing;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * Access to Spring WebMvc version-specific features
 *
 * <p>Originally designed by OkHttp team, derived from {@code okhttp3.internal.platform.Platform}
 */
abstract class WebMvcRuntime {
  private static final WebMvcRuntime WEBMVC_RUNTIME = findWebMvcRuntime();

  abstract HttpTracing httpTracing(ApplicationContext ctx);

  abstract boolean isHandlerMethod(Object handler);

  WebMvcRuntime() {
  }

  static WebMvcRuntime get() {
    return WEBMVC_RUNTIME;
  }

  /** Attempt to match the host runtime to a capable Platform implementation. */
  static WebMvcRuntime findWebMvcRuntime() {
    // compatible with spring-webmvc 2.5
    return new WebMvc25();
  }

  static final class WebMvc25 extends WebMvcRuntime {
    @Override HttpTracing httpTracing(ApplicationContext ctx) {
      // Spring 2.5 does not have a get bean by type interface. To remain compatible, lookup by name
      if (ctx.containsBean("httpTracing")) {
        Object bean = ctx.getBean("httpTracing");
        if (bean instanceof HttpTracing) return (HttpTracing) bean;
      }
      throw new NoSuchBeanDefinitionException(HttpTracing.class, "httpTracing");
    }

    @Override boolean isHandlerMethod(Object handler) {
      return false;
    }
  }
}
