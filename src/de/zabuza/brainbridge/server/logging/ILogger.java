package de.zabuza.brainbridge.server.logging;

/**
 * Interface for logger.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public interface ILogger extends AutoCloseable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close();

	/**
	 * Flushes all buffered message.
	 */
	public void flush();

	/**
	 * Whether the debugging channel of this logger is currently enabled or not.
	 * Note that this can be changed by using {@link #setDebugEnabled(boolean)}.
	 * 
	 * @return <tt>True</tt> if the debugging channel of this logger is
	 *         currently enabled, <tt>false</tt> if not
	 */
	public boolean isDebugEnabled();

	/**
	 * Logs the given message to the given logging level.
	 * 
	 * @param message
	 *            The message to log
	 * @param level
	 *            The logging level to log the message to
	 */
	public void log(final String message, final ELogLevel level);

	/**
	 * Logs the given message to the debugging channel. If the debugging channel
	 * is currently disabled the method will not log any message. The channel
	 * can be enabled by using {@link #setDebugEnabled(boolean)} and the current
	 * state can be accessed with {@link #isDebugEnabled()}.
	 * 
	 * @param message
	 *            The message to log
	 */
	public default void logDebug(final String message) {
		log(message, ELogLevel.DEBUG);
	}

	/**
	 * Logs the given message to the error channel.
	 * 
	 * @param message
	 *            The message to log
	 */
	public default void logError(final String message) {
		log(message, ELogLevel.ERROR);
	}

	/**
	 * Logs the given message to the info channel.
	 * 
	 * @param message
	 *            The message to log
	 */
	public default void logInfo(final String message) {
		log(message, ELogLevel.INFO);
	}

	/**
	 * Sets whether the debugging channel of this logger is currently enabled or
	 * not.
	 * 
	 * @param isDebugEnabled
	 *            <tt>True</tt> if the debugging channel should get enabled,
	 *            <tt>false</tt> if not
	 */
	public void setDebugEnabled(final boolean isDebugEnabled);
}
