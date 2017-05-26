package de.zabuza.brainbridge.server.logging;

/**
 * Class that represents logging messages.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class LogMessage {
	/**
	 * The log level of this message.
	 */
	private final ELogLevel mLogLevel;
	/**
	 * The actual content of this message.
	 */
	private final String mMessage;
	/**
	 * The timestamp when this message arrived.
	 */
	private final long mTimestamp;

	/**
	 * Creates a new logging message with the given data.
	 * 
	 * @param message
	 *            The actual content of the message
	 * @param logLevel
	 *            The log level of this message
	 * @param timestamp
	 *            The timestamp when this message arrived
	 */
	public LogMessage(final String message, final ELogLevel logLevel, final long timestamp) {
		this.mMessage = message;
		this.mLogLevel = logLevel;
		this.mTimestamp = timestamp;
	}

	/**
	 * Gets the log level of this message.
	 * 
	 * @return The log level of this message
	 */
	public ELogLevel getLogLevel() {
		return this.mLogLevel;
	}

	/**
	 * Gets the actual content of this message.
	 * 
	 * @return The actual content of this message
	 */
	public String getMessage() {
		return this.mMessage;
	}

	/**
	 * Gets the timestamp when this message arrived
	 * 
	 * @return The timestamp when this message arrived
	 */
	public long getTimestamp() {
		return this.mTimestamp;
	}
}
