package zipkin2;

import javax.annotation.Generated;
import zipkin2.internal.Nullable;

final class AutoValue_CheckResult extends CheckResult {

	private final boolean ok;
	private final Throwable error;

	AutoValue_CheckResult(boolean ok, @Nullable Throwable error) {
		this.ok = ok;
		this.error = error;
	}

	@Override
	public boolean ok() {
		return ok;
	}

	@Nullable
	@Override
	public Throwable error() {
		return error;
	}

	@Override
	public String toString() {
		return "CheckResult{" + "ok=" + ok + ", " + "error=" + error + "}";
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof CheckResult) {
			CheckResult that = (CheckResult) o;
			return (this.ok == that.ok())
					&& ((this.error == null) ? (that.error() == null) : this.error.equals(that.error()));
		}
		return false;
	}

	@Override
	public int hashCode() {
		int h = 1;
		h *= 1000003;
		h ^= this.ok ? 1231 : 1237;
		h *= 1000003;
		h ^= (error == null) ? 0 : this.error.hashCode();
		return h;
	}

}
