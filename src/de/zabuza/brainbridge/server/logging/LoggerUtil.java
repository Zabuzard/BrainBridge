package de.zabuza.brainbridge.server.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Class that provides utility methods for {@link ILogger} and logging messages
 * in general.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class LoggerUtil {

	/**
	 * Gets the stack trace of a given exception as {@link String} instead of
	 * printing it to the error console.
	 * 
	 * @param e
	 *            The exception to get the stack trace of
	 * @return The stack trace of the given exception
	 */
	public static final String getStackTrace(final Exception e) {
		final StringWriter target = new StringWriter();
		e.printStackTrace(new PrintWriter(target));
		return target.toString();
	}

	/**
	 * Utility class. No implementation.
	 */
	private LoggerUtil() {

	}
}
