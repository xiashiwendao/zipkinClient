
package brave.propagation;

import zipkinClient.Span;
import zipkinClient.Tracer;

final class AutoValue_ThreadLocalSpan_SpanAndScope extends ThreadLocalSpan.SpanAndScope {

	private final Span span;
	private final Tracer.SpanInScope scope;

	AutoValue_ThreadLocalSpan_SpanAndScope(Span span, Tracer.SpanInScope scope) {
		if (span == null) {
			throw new NullPointerException("Null span");
		}
		this.span = span;
		if (scope == null) {
			throw new NullPointerException("Null scope");
		}
		this.scope = scope;
	}

	@Override
	Span span() {
		return span;
	}

	@Override
	Tracer.SpanInScope scope() {
		return scope;
	}

	@Override
	public String toString() {
		return "SpanAndScope{" + "span=" + span + ", " + "scope=" + scope + "}";
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof ThreadLocalSpan.SpanAndScope) {
			ThreadLocalSpan.SpanAndScope that = (ThreadLocalSpan.SpanAndScope) o;
			return (this.span.equals(that.span())) && (this.scope.equals(that.scope()));
		}
		return false;
	}

	@Override
	public int hashCode() {
		int h = 1;
		h *= 1000003;
		h ^= this.span.hashCode();
		h *= 1000003;
		h ^= this.scope.hashCode();
		return h;
	}

}
