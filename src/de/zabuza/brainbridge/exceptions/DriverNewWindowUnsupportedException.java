package de.zabuza.brainbridge.exceptions;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Exception that is thrown whenever a driver does not support opening new
 * windows. That is the case if the driver does not implement
 * {@link JavascriptExecutor}.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class DriverNewWindowUnsupportedException extends IllegalArgumentException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of this exception. Indicates that the given driver
	 * does not support opening new windows. That is the case if it does not
	 * implement {@link JavascriptExecutor}.
	 * 
	 * @param driver
	 *            The driver that does not support opening new windows
	 */
	public DriverNewWindowUnsupportedException(final WebDriver driver) {
		super(driver.toString());
	}

}
