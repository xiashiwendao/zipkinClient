
package brave.propagation;

import brave.Tracer;

 final class AutoValue_ThreadLocalSpan extends ThreadLocalSpan {

  private final Tracer tracer;

  AutoValue_ThreadLocalSpan(
      Tracer tracer) {
    if (tracer == null) {
      throw new NullPointerException("Null tracer");
    }
    this.tracer = tracer;
  }

  @Override
  Tracer tracer() {
    return tracer;
  }

  @Override
  public String toString() {
    return "ThreadLocalSpan{"
         + "tracer=" + tracer
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ThreadLocalSpan) {
      ThreadLocalSpan that = (ThreadLocalSpan) o;
      return (this.tracer.equals(that.tracer()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= this.tracer.hashCode();
    return h;
  }

}
