package de.zabuza.brainbridge.server.exceptions;

import java.io.IOException;

/**
 * Exception that is thrown whenever a logger can not write to a log file.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class LoggerCanNotWriteException extends IllegalStateException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of this exception to be thrown whenever a logger
	 * can not write to a log file.
	 * 
	 * @param cause
	 *            The exact cause that lead to this problem
	 */
	public LoggerCanNotWriteException(final IOException cause) {
		super(cause);
	}

}
