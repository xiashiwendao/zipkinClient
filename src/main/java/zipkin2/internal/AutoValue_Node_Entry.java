
package zipkin2.internal;

import javax.annotation.Generated;

 final class AutoValue_Node_Entry<V> extends Node.Entry<V> {

  private final String parentId;
  private final String id;
  private final V value;

  AutoValue_Node_Entry(
      @Nullable String parentId,
      String id,
      V value) {
    this.parentId = parentId;
    if (id == null) {
      throw new NullPointerException("Null id");
    }
    this.id = id;
    if (value == null) {
      throw new NullPointerException("Null value");
    }
    this.value = value;
  }

  @Nullable
  @Override
  String parentId() {
    return parentId;
  }

  @Override
  String id() {
    return id;
  }

  @Override
  V value() {
    return value;
  }

  @Override
  public String toString() {
    return "Entry{"
         + "parentId=" + parentId + ", "
         + "id=" + id + ", "
         + "value=" + value
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Node.Entry) {
      Node.Entry<?> that = (Node.Entry<?>) o;
      return ((this.parentId == null) ? (that.parentId() == null) : this.parentId.equals(that.parentId()))
           && (this.id.equals(that.id()))
           && (this.value.equals(that.value()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (parentId == null) ? 0 : this.parentId.hashCode();
    h *= 1000003;
    h ^= this.id.hashCode();
    h *= 1000003;
    h ^= this.value.hashCode();
    return h;
  }

}
