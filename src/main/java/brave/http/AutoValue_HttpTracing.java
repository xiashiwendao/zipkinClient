
package brave.http;

import brave.Tracing;

 final class AutoValue_HttpTracing extends HttpTracing {

  private final Tracing tracing;
  private final HttpClientParser clientParser;
  private final String serverName;
  private final HttpServerParser serverParser;
  private final HttpSampler clientSampler;
  private final HttpSampler serverSampler;

  private AutoValue_HttpTracing(
      Tracing tracing,
      HttpClientParser clientParser,
      String serverName,
      HttpServerParser serverParser,
      HttpSampler clientSampler,
      HttpSampler serverSampler) {
    this.tracing = tracing;
    this.clientParser = clientParser;
    this.serverName = serverName;
    this.serverParser = serverParser;
    this.clientSampler = clientSampler;
    this.serverSampler = serverSampler;
  }

  @Override
  public Tracing tracing() {
    return tracing;
  }

  @Override
  public HttpClientParser clientParser() {
    return clientParser;
  }

  @Override
  public String serverName() {
    return serverName;
  }

  @Override
  public HttpServerParser serverParser() {
    return serverParser;
  }

  @Override
  public HttpSampler clientSampler() {
    return clientSampler;
  }

  @Override
  public HttpSampler serverSampler() {
    return serverSampler;
  }

  @Override
  public String toString() {
    return "HttpTracing{"
         + "tracing=" + tracing + ", "
         + "clientParser=" + clientParser + ", "
         + "serverName=" + serverName + ", "
         + "serverParser=" + serverParser + ", "
         + "clientSampler=" + clientSampler + ", "
         + "serverSampler=" + serverSampler
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof HttpTracing) {
      HttpTracing that = (HttpTracing) o;
      return (this.tracing.equals(that.tracing()))
           && (this.clientParser.equals(that.clientParser()))
           && (this.serverName.equals(that.serverName()))
           && (this.serverParser.equals(that.serverParser()))
           && (this.clientSampler.equals(that.clientSampler()))
           && (this.serverSampler.equals(that.serverSampler()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= this.tracing.hashCode();
    h *= 1000003;
    h ^= this.clientParser.hashCode();
    h *= 1000003;
    h ^= this.serverName.hashCode();
    h *= 1000003;
    h ^= this.serverParser.hashCode();
    h *= 1000003;
    h ^= this.clientSampler.hashCode();
    h *= 1000003;
    h ^= this.serverSampler.hashCode();
    return h;
  }

  @Override
  public HttpTracing.Builder toBuilder() {
    return new Builder(this);
  }

  static final class Builder extends HttpTracing.Builder {
    private Tracing tracing;
    private HttpClientParser clientParser;
    private String serverName;
    private HttpServerParser serverParser;
    private HttpSampler clientSampler;
    private HttpSampler serverSampler;
    Builder() {
    }
    private Builder(HttpTracing source) {
      this.tracing = source.tracing();
      this.clientParser = source.clientParser();
      this.serverName = source.serverName();
      this.serverParser = source.serverParser();
      this.clientSampler = source.clientSampler();
      this.serverSampler = source.serverSampler();
    }
    @Override
    public HttpTracing.Builder tracing(Tracing tracing) {
      if (tracing == null) {
        throw new NullPointerException("Null tracing");
      }
      this.tracing = tracing;
      return this;
    }
    @Override
    public HttpTracing.Builder clientParser(HttpClientParser clientParser) {
      if (clientParser == null) {
        throw new NullPointerException("Null clientParser");
      }
      this.clientParser = clientParser;
      return this;
    }
    @Override
    HttpTracing.Builder serverName(String serverName) {
      if (serverName == null) {
        throw new NullPointerException("Null serverName");
      }
      this.serverName = serverName;
      return this;
    }
    @Override
    public HttpTracing.Builder serverParser(HttpServerParser serverParser) {
      if (serverParser == null) {
        throw new NullPointerException("Null serverParser");
      }
      this.serverParser = serverParser;
      return this;
    }
    @Override
    public HttpTracing.Builder clientSampler(HttpSampler clientSampler) {
      if (clientSampler == null) {
        throw new NullPointerException("Null clientSampler");
      }
      this.clientSampler = clientSampler;
      return this;
    }
    @Override
    public HttpTracing.Builder serverSampler(HttpSampler serverSampler) {
      if (serverSampler == null) {
        throw new NullPointerException("Null serverSampler");
      }
      this.serverSampler = serverSampler;
      return this;
    }
    @Override
    public HttpTracing build() {
      String missing = "";
      if (this.tracing == null) {
        missing += " tracing";
      }
      if (this.clientParser == null) {
        missing += " clientParser";
      }
      if (this.serverName == null) {
        missing += " serverName";
      }
      if (this.serverParser == null) {
        missing += " serverParser";
      }
      if (this.clientSampler == null) {
        missing += " clientSampler";
      }
      if (this.serverSampler == null) {
        missing += " serverSampler";
      }
      if (!missing.isEmpty()) {
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new AutoValue_HttpTracing(
          this.tracing,
          this.clientParser,
          this.serverName,
          this.serverParser,
          this.clientSampler,
          this.serverSampler);
    }
  }

}
