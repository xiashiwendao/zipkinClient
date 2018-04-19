
package zipkinClient;

import brave.internal.recorder.Recorder;
import brave.propagation.TraceContext;

 final class AutoValue_RealSpanCustomizer extends RealSpanCustomizer {

  private final TraceContext context;
  private final Recorder recorder;

  AutoValue_RealSpanCustomizer(
      TraceContext context,
      Recorder recorder) {
    if (context == null) {
      throw new NullPointerException("Null context");
    }
    this.context = context;
    if (recorder == null) {
      throw new NullPointerException("Null recorder");
    }
    this.recorder = recorder;
  }

  @Override
  TraceContext context() {
    return context;
  }

  @Override
  Recorder recorder() {
    return recorder;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof RealSpanCustomizer) {
      RealSpanCustomizer that = (RealSpanCustomizer) o;
      return (this.context.equals(that.context()))
           && (this.recorder.equals(that.recorder()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= this.context.hashCode();
    h *= 1000003;
    h ^= this.recorder.hashCode();
    return h;
  }

}
