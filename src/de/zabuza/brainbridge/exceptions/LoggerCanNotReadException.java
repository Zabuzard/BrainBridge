package de.zabuza.brainbridge.exceptions;

import java.io.IOException;

/**
 * Exception that is thrown whenever a logger can not read a log file.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class LoggerCanNotReadException extends IllegalStateException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of this exception to be thrown whenever a logger
	 * can not read a log file.
	 * 
	 * @param cause
	 *            The exact cause that lead to this problem
	 */
	public LoggerCanNotReadException(final IOException cause) {
		super(cause);
	}

}
