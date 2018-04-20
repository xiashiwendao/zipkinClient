
package brave;

import brave.internal.recorder.Recorder;
import brave.propagation.TraceContext;

final class AutoValue_RealSpan extends RealSpan {

	private final TraceContext context;
	private final SpanCustomizer customizer;
	private final Recorder recorder;

	AutoValue_RealSpan(TraceContext context, SpanCustomizer customizer, Recorder recorder) {
		if (context == null) {
			throw new NullPointerException("Null context");
		}
		this.context = context;
		if (customizer == null) {
			throw new NullPointerException("Null customizer");
		}
		this.customizer = customizer;
		if (recorder == null) {
			throw new NullPointerException("Null recorder");
		}
		this.recorder = recorder;
	}

	@Override
	public TraceContext context() {
		return context;
	}

	@Override
	public SpanCustomizer customizer() {
		return customizer;
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
		if (o instanceof RealSpan) {
			RealSpan that = (RealSpan) o;
			return (this.context.equals(that.context())) && (this.customizer.equals(that.customizer()))
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
		h ^= this.customizer.hashCode();
		h *= 1000003;
		h ^= this.recorder.hashCode();
		return h;
	}

}
