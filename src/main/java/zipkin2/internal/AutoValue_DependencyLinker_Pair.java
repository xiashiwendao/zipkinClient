
package zipkin2.internal;

final class AutoValue_DependencyLinker_Pair extends DependencyLinker.Pair {

	private final String left;
	private final String right;

	AutoValue_DependencyLinker_Pair(String left, String right) {
		if (left == null) {
			throw new NullPointerException("Null left");
		}
		this.left = left;
		if (right == null) {
			throw new NullPointerException("Null right");
		}
		this.right = right;
	}

	@Override
	String left() {
		return left;
	}

	@Override
	String right() {
		return right;
	}

	@Override
	public String toString() {
		return "Pair{" + "left=" + left + ", " + "right=" + right + "}";
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof DependencyLinker.Pair) {
			DependencyLinker.Pair that = (DependencyLinker.Pair) o;
			return (this.left.equals(that.left())) && (this.right.equals(that.right()));
		}
		return false;
	}

	@Override
	public int hashCode() {
		int h = 1;
		h *= 1000003;
		h ^= this.left.hashCode();
		h *= 1000003;
		h ^= this.right.hashCode();
		return h;
	}

}
