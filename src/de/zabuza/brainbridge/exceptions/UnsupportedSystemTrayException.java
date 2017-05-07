package de.zabuza.brainbridge.exceptions;

/**
 * Exception that is thrown whenever the operating system does not support a
 * system tray though it is needed for the service.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class UnsupportedSystemTrayException extends IllegalStateException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

}
