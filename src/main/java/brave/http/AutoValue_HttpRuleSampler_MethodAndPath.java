
package brave.http;

final class AutoValue_HttpRuleSampler_MethodAndPath extends HttpRuleSampler.MethodAndPath {

	private final String method;
	private final String path;

	AutoValue_HttpRuleSampler_MethodAndPath(String method, String path) {
		if (method == null) {
			throw new NullPointerException("Null method");
		}
		this.method = method;
		if (path == null) {
			throw new NullPointerException("Null path");
		}
		this.path = path;
	}

	@Override
	String method() {
		return method;
	}

	@Override
	String path() {
		return path;
	}

	@Override
	public String toString() {
		return "MethodAndPath{" + "method=" + method + ", " + "path=" + path + "}";
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof HttpRuleSampler.MethodAndPath) {
			HttpRuleSampler.MethodAndPath that = (HttpRuleSampler.MethodAndPath) o;
			return (this.method.equals(that.method())) && (this.path.equals(that.path()));
		}
		return false;
	}

	@Override
	public int hashCode() {
		int h = 1;
		h *= 1000003;
		h ^= this.method.hashCode();
		h *= 1000003;
		h ^= this.path.hashCode();
		return h;
	}

}
