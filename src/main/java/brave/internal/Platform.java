package brave.internal;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.animal_sniffer.IgnoreJRERequirement;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.reporter.Reporter;
import zipkinClient.Clock;
import zipkinClient.Tracer;
import zipkinClient.Tracing;

/**
 * Access to platform-specific features and implements a default logging
 * spanReporter.
 *
 * <p>
 * Originally designed by OkHttp team, derived from
 * {@code okhttp3.internal.platform.Platform}
 */
public abstract class Platform {
	static final Logger logger = Logger.getLogger(Tracer.class.getName());

	private static final Platform PLATFORM = findPlatform();

	volatile Endpoint endpoint;

	public Reporter<zipkin2.Span> reporter() {
		return LoggingReporter.INSTANCE;
	}

	enum LoggingReporter implements Reporter<zipkin2.Span> {
		INSTANCE;

		@Override
		public void report(Span span) {
			if (!logger.isLoggable(Level.INFO))
				return;
			if (span == null)
				throw new NullPointerException("span == null");
			logger.info(span.toString());
		}

		@Override
		public String toString() {
			return "LoggingReporter{name=" + logger.getName() + "}";
		}
	}

	public Endpoint endpoint() {
		// uses synchronized variant of double-checked locking as getting the
		// endpoint can be expensive
		if (endpoint == null) {
			synchronized (this) {
				if (endpoint == null) {
					endpoint = produceEndpoint();
				}
			}
		}
		return endpoint;
	}

	Endpoint produceEndpoint() {
		Endpoint.Builder builder = Endpoint.newBuilder().serviceName("unknown");
		try {
			Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
			if (nics == null)
				return builder.build();
			while (nics.hasMoreElements()) {
				NetworkInterface nic = nics.nextElement();
				Enumeration<InetAddress> addresses = nic.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (address.isSiteLocalAddress()) {
						builder.ip(address);
						break;
					}
				}
			}
		} catch (Exception e) {
			// don't crash the caller if there was a problem reading nics.
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "error reading nics", e);
			}
		}
		return builder.build();
	}

	public static Platform get() {
		return PLATFORM;
	}

	/**
	 * Attempt to match the host runtime to a capable Platform implementation.
	 */
	static Platform findPlatform() {
		Platform jre9 = Jre9.buildIfSupported();

		if (jre9 != null)
			return jre9;

		Platform jre7 = Jre7.buildIfSupported();

		if (jre7 != null)
			return jre7;

		// compatible with JRE 6
		return new Jre6();
	}

	/**
	 * This class uses pseudo-random number generators to provision IDs.
	 *
	 * <p>
	 * This optimizes speed over full coverage of 64-bits, which is why it
	 * doesn't share a {@link SecureRandom}. It will use
	 * {@link java.util.concurrent.ThreadLocalRandom} unless used in JRE 6 which
	 * doesn't have the class.
	 */
	public abstract long randomLong();

	/**
	 * Returns the high 8-bytes for use in {@link Tracing.Builder#traceId128Bit
	 * 128-bit trace IDs}.
	 *
	 * <p>
	 * The upper 4-bytes are epoch seconds and the lower 4-bytes are random.
	 * This makes it convertible to <a href=
	 * "http://docs.aws.amazon.com/elasticloadbalancing/latest/application/load-balancer-request-tracing.html"></a>Amazon
	 * X-Ray trace ID format v1</a>.
	 */
	public abstract long nextTraceIdHigh();

	public Clock clock() {
		return new Clock() {
			@Override
			public long currentTimeMicroseconds() {
				return System.currentTimeMillis() * 1000;
			}

			@Override
			public String toString() {
				return "System.currentTimeMillis()";
			}
		};
	}

	static class Jre9 extends Jre7 {

		static Jre9 buildIfSupported() {
			// Find JRE 9 new methods
			try {
				Class zoneId = Class.forName("java.time.ZoneId");
				Class.forName("java.time.Clock").getMethod("tickMillis", zoneId);
				return new Jre9();
			} catch (ClassNotFoundException e) {
				// pre JRE 8
			} catch (NoSuchMethodException e) {
				// pre JRE 9
			}
			return null;
		}

		@IgnoreJRERequirement
		@Override
		public Clock clock() {
			return new Clock() {
				// we could use jdk.internal.misc.VM to do this more
				// efficiently, but it is internal
				@Override
				public long currentTimeMicroseconds() {
					return System.currentTimeMillis();
				}

				@Override
				public String toString() {
					return "Clock.systemUTC().instant()";
				}
			};
		}

		@Override
		public String toString() {
			return "Jre9{}";
		}
	}

	static class Jre7 extends Platform {

		static Jre7 buildIfSupported() {
			// Find JRE 7 new methods
			try {
				Class.forName("java.util.concurrent.ThreadLocalRandom");
				return new Jre7();
			} catch (ClassNotFoundException e) {
				// pre JRE 7
			}
			return null;
		}

		@IgnoreJRERequirement
		@Override
		public long randomLong() {
			return System.currentTimeMillis();
		}

		@IgnoreJRERequirement
		@Override
		public long nextTraceIdHigh() {
			return System.currentTimeMillis();
		}

		@Override
		public String toString() {
			return "Jre7{}";
		}
	}

	static long nextTraceIdHigh(int random) {
		long epochSeconds = System.currentTimeMillis() / 1000;
		return (epochSeconds & 0xffffffffL) << 32 | (random & 0xffffffffL);
	}

	static class Jre6 extends Platform {

		@Override
		public long randomLong() {
			return prng.nextLong();
		}

		@Override
		public long nextTraceIdHigh() {
			return nextTraceIdHigh(prng.nextInt());
		}

		final Random prng;

		Jre6() {
			this.prng = new Random(System.nanoTime());
		}

		@Override
		public String toString() {
			return "Jre6{}";
		}
	}
}
