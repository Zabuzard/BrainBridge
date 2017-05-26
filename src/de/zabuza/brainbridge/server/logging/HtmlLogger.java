package de.zabuza.brainbridge.server.logging;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import de.zabuza.brainbridge.server.exceptions.LoggerCanNotReadException;
import de.zabuza.brainbridge.server.exceptions.LoggerCanNotWriteException;

/**
 * Logger implementation that writes to a HTML format which is nicely readable
 * for humans. The output is in a log-file specified by {@link #FILEPATH}. The
 * logger automatically buffers queries, a write action can be enforced by using
 * {@link #flush()}. Note that the buffer is automatically flushed when calling
 * {@link #close()}.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class HtmlLogger implements ILogger {
	/**
	 * CSS class to use for the content of a log message.
	 */
	private static final String CLASS_LOG_CONTENT = "logContent";
	/**
	 * CSS class to use for a log message that contains debugging information.
	 */
	private static final String CLASS_LOG_DEBUG = "debugLog";
	/**
	 * CSS class to use for a log message that contains error information.
	 */
	private static final String CLASS_LOG_ERROR = "errorLog";
	/**
	 * CSS class to use for a log message that contains general information.
	 */
	private static final String CLASS_LOG_INFO = "infoLog";
	/**
	 * CSS class to use for the timestamp of a log message.
	 */
	private static final String CLASS_LOG_TIMESTAMP = "logTimestamp";
	/**
	 * The path to the file where to save the logging content.
	 */
	private static final String FILEPATH = "log.html";
	/**
	 * Size of the message buffer, i.e. amount of messages when to automatically
	 * flush the buffer by using {@link #flush()}.
	 */
	private static final int LOG_EVERY = 200;
	/**
	 * The maximal size of the logging file, i.e. the amount of messages when
	 * the logger deletes old messages.
	 */
	private static final int LOG_MESSAGES_MAX = 2_000;
	/**
	 * The value to use for separating log messages.
	 */
	private static final String MESSAGE_SEPARATOR = "<br />";
	/**
	 * Indicator to use for lines in the log file that are no logging messages.
	 */
	private static final String NO_LOG_MESSAGE_INDICATOR = "<!--NO_MESSAGE-->";
	/**
	 * The value of the title tag for the HTML logging content.
	 */
	private static final String TITLE = "BrainBridge Log";

	/**
	 * Escapes HTML problematic symbols in the given text by replacing them with
	 * their corresponding entities.
	 * 
	 * @param text
	 *            The text to escape
	 * @return The HTML escaped text
	 */
	private static String escapeHtml(final String text) {
		final String lineSeparator = System.lineSeparator();
		String result = text.replaceAll("\"", "&quot;");
		result = result.replaceAll("&", "&amp;");
		result = result.replaceAll("<", "&lt;");
		result = result.replaceAll(">", "&gt;");
		result = result.replaceAll(lineSeparator, "<br />");
		return result;
	}

	/**
	 * Converts the given log message to the HTML format.
	 * 
	 * @param message
	 *            The message to convert to the HTML format
	 * @return The given message in the HTML format
	 */
	private static String messageToHtml(final LogMessage message) {
		final String content = message.getMessage();
		final ELogLevel level = message.getLogLevel();
		final long timestamp = message.getTimestamp();
		final Date date = new Date(timestamp);
		final String timestampFormat = DateFormat.getDateTimeInstance().format(date);

		final String cssLogClass;
		if (level == ELogLevel.INFO) {
			cssLogClass = CLASS_LOG_INFO;
		} else if (level == ELogLevel.DEBUG) {
			cssLogClass = CLASS_LOG_DEBUG;
		} else if (level == ELogLevel.ERROR) {
			cssLogClass = CLASS_LOG_ERROR;
		} else {
			throw new AssertionError();
		}

		final StringBuilder sb = new StringBuilder();
		sb.append("<span class=\"").append(cssLogClass).append("\">");

		sb.append("<span class=\"").append(CLASS_LOG_TIMESTAMP).append("\">");
		sb.append(timestampFormat);
		sb.append(":</span>");

		sb.append("<span class=\"").append(CLASS_LOG_CONTENT).append("\">");
		sb.append(escapeHtml(content));
		sb.append("</span>");

		sb.append("</span>");
		sb.append(MESSAGE_SEPARATOR);

		return sb.toString();
	}

	/**
	 * Mixes the given buffered messages into the already logged messages by
	 * ensuring that in total there are only {@link #LOG_MESSAGES_MAX} messages.
	 * 
	 * @param loggedMessages
	 *            Already logged messages
	 * @param bufferedMessages
	 *            Buffered message to mix with the already logged messages
	 * @return A queue of messages that has a maximum of
	 *         {@link #LOG_MESSAGES_MAX} messages where the buffered messages
	 *         come after the already logged messages
	 */
	private static Queue<String> mixBufferIntoMessages(final Queue<String> loggedMessages,
			final Queue<LogMessage> bufferedMessages) {
		for (final LogMessage logMessage : bufferedMessages) {
			loggedMessages.add(messageToHtml(logMessage));
		}

		while (loggedMessages.size() > LOG_MESSAGES_MAX) {
			loggedMessages.remove();
		}
		return loggedMessages;
	}

	/**
	 * Reads all already logged messages from the log-file.
	 * 
	 * @return A queue containing all already logged messages of the log-file.
	 */
	private static Queue<String> readLoggedMessages() {
		final Queue<String> messages = new LinkedList<>();
		try (final BufferedReader br = new BufferedReader(new FileReader(FILEPATH))) {
			while (br.ready()) {
				final String line = br.readLine();
				if (line == null) {
					break;
				}

				if (line.endsWith(NO_LOG_MESSAGE_INDICATOR)) {
					continue;
				}

				messages.add(line);
			}
		} catch (final FileNotFoundException e) {
			// There are no messages
			return messages;
		} catch (final IOException e) {
			throw new LoggerCanNotReadException(e);
		}

		return messages;
	}

	/**
	 * The head message that comes before any log messages in the HTML format.
	 * It contains meta information for the HTML to get displayed correctly like
	 * stylesheet data.
	 */
	private String mHeadMessage;
	/**
	 * Whether the logger has the debugging channel enabled or not.
	 */
	private boolean mIsDebugEnabled;
	/**
	 * The symbol to use for separating lines.
	 */
	private final String mLineSeparator;
	/**
	 * The queue that buffers log messages of queries. It can be flushed into
	 * the log-file by using {@link #flush()}.
	 */
	private final Queue<LogMessage> mMessageBuffer;
	/**
	 * The trailing message that comes after all log messages in the HTML
	 * format. It contains meta information for the HTML to get displayed
	 * correctly like stylesheet data.
	 */
	private String mTailMessage;

	/**
	 * Creates a new instance of this logger that has the debugging channel
	 * disabled. Note that this can later be changed by
	 * {@link #setDebugEnabled(boolean)}.
	 */
	public HtmlLogger() {
		this(false);
	}

	/**
	 * Creates a new instance of this logger.
	 * 
	 * @param isDebugEnabled
	 *            <tt>True</tt> if the debugging channel of this logger should
	 *            get enabled, <tt>false</tt> if not. Note that this can later
	 *            be changed by {@link #setDebugEnabled(boolean)}.
	 */
	public HtmlLogger(final boolean isDebugEnabled) {
		this.mLineSeparator = System.lineSeparator();
		this.mHeadMessage = null;
		this.mTailMessage = null;
		this.mIsDebugEnabled = isDebugEnabled;

		this.mMessageBuffer = new LinkedList<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zabuza.beedlebot.logging.ILogger#close()
	 */
	@Override
	public void close() {
		flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zabuza.beedlebot.logging.ILogger#flush()
	 */
	@Override
	public void flush() {
		if (this.mMessageBuffer.isEmpty()) {
			return;
		}

		final Queue<String> loggedMessages = readLoggedMessages();
		try (final FileWriter writer = new FileWriter(FILEPATH, false)) {
			writer.write(getHeadMessage());
			writer.write(this.mLineSeparator);

			final Queue<String> messagesToWrite = mixBufferIntoMessages(loggedMessages, this.mMessageBuffer);
			this.mMessageBuffer.clear();
			while (!messagesToWrite.isEmpty()) {
				final String message = messagesToWrite.poll();
				writer.write(message);
				if (!message.endsWith(this.mLineSeparator)) {
					writer.write(this.mLineSeparator);
				}
			}

			writer.write(getTailMessage());
			writer.flush();
		} catch (final IOException e) {
			throw new LoggerCanNotWriteException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zabuza.beedlebot.logging.ILogger#isDebugEnabled()
	 */
	@Override
	public boolean isDebugEnabled() {
		return this.mIsDebugEnabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zabuza.beedlebot.logging.ILogger#log(java.lang.String,
	 * de.zabuza.beedlebot.logging.ELogLevel)
	 */
	@Override
	public void log(final String message, final ELogLevel level) {
		// Do not log debug if not enabled
		if (level == ELogLevel.DEBUG && !isDebugEnabled()) {
			return;
		}

		final long timestampNow = System.currentTimeMillis();
		final LogMessage logMessage = new LogMessage(message, level, timestampNow);
		this.mMessageBuffer.add(logMessage);

		if (this.mMessageBuffer.size() >= LOG_EVERY) {
			flush();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zabuza.beedlebot.logging.ILogger#setDebugEnabled(boolean)
	 */
	@Override
	public void setDebugEnabled(final boolean isDebugEnabled) {
		this.mIsDebugEnabled = isDebugEnabled;
	}

	/**
	 * Gets the head message that comes before any log messages in the HTML
	 * format. It contains meta information for the HTML to get displayed
	 * correctly like stylesheet data. Note that this method uses a singleton
	 * pattern to not create the head message unnecessarily often.
	 * 
	 * @return The head message
	 */
	private String getHeadMessage() {
		if (this.mHeadMessage != null) {
			return this.mHeadMessage;
		}

		final StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>");
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>").append(TITLE).append("</title>");
		sb.append("<style>");
		sb.append(".").append(CLASS_LOG_INFO).append(" .").append(CLASS_LOG_TIMESTAMP).append(" { }");
		sb.append(".").append(CLASS_LOG_DEBUG).append(" .").append(CLASS_LOG_TIMESTAMP)
				.append(" { background-color: #CFE4F3; }");
		sb.append(".").append(CLASS_LOG_ERROR).append(" .").append(CLASS_LOG_TIMESTAMP)
				.append(" { background-color: #F2CEC8; }");
		sb.append(".").append(CLASS_LOG_CONTENT).append(" { margin-left: 10px;}");
		sb.append("</style>");
		sb.append("</head><body>");
		sb.append(NO_LOG_MESSAGE_INDICATOR);

		this.mHeadMessage = sb.toString();
		return this.mHeadMessage;
	}

	/**
	 * Gets the trailing message that comes after all log messages in the HTML
	 * format. It contains meta information for the HTML to get displayed
	 * correctly like stylesheet data. Note that this method uses a singleton
	 * pattern to not create the tail message unnecessarily often.
	 * 
	 * @return The trailing message
	 */
	private String getTailMessage() {
		if (this.mTailMessage != null) {
			return this.mTailMessage;
		}

		final StringBuilder sb = new StringBuilder();
		sb.append("</body>");
		sb.append("</html>");
		sb.append(NO_LOG_MESSAGE_INDICATOR);

		this.mTailMessage = sb.toString();
		return this.mTailMessage;
	}

}
