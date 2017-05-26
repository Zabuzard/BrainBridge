package de.zabuza.brainbridge.server.exceptions;

import java.io.UnsupportedEncodingException;

/**
 * Exception that is thrown whenever trying to use an encoding that is
 * unsupported though it was expected to be supported.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class UnexpectedUnsupportedEncodingException extends IllegalStateException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of this exception to be thrown whenever trying to
	 * use an encoding that is unsupported though it was expected to be
	 * supported.
	 * 
	 * @param cause
	 *            The exact cause that lead to this problem
	 */
	public UnexpectedUnsupportedEncodingException(final UnsupportedEncodingException cause) {
		super(cause);
	}

}
