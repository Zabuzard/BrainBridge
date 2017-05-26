package de.zabuza.brainbridge.server.logging;

/**
 * Factory that provides instances of {@link ILogger}.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class LoggerFactory {

	/**
	 * Singleton for a global logger instance.
	 */
	private static ILogger loggerInstance = null;

	/**
	 * Gets the reference to a global logger instance that can be used for
	 * logging messages.
	 * 
	 * @return The reference to a global logger instance that can be used for
	 *         logging messages
	 */
	public static ILogger getLogger() {
		if (loggerInstance == null) {
			loggerInstance = new HtmlLogger();
		}

		return loggerInstance;
	}
}
