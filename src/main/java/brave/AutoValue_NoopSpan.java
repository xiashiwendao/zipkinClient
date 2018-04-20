package brave;

import brave.propagation.TraceContext;

 final class AutoValue_NoopSpan extends NoopSpan {

  private final TraceContext context;

  AutoValue_NoopSpan(
      TraceContext context) {
    if (context == null) {
      throw new NullPointerException("Null context");
    }
    this.context = context;
  }

  @Override
  public TraceContext context() {
    return context;
  }

  @Override
  public String toString() {
    return "NoopSpan{"
         + "context=" + context
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof NoopSpan) {
      NoopSpan that = (NoopSpan) o;
      return (this.context.equals(that.context()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= this.context.hashCode();
    return h;
  }

}
