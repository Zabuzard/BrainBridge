package de.zabuza.brainbridge.server.exceptions;

import org.openqa.selenium.StaleElementReferenceException;

import de.zabuza.brainbridge.server.webdriver.StaleRefresherWebElement;

/**
 * Utility class that provides error messages for the tool.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 * 
 */
public final class ErrorMessages {
	/**
	 * Thrown when a {@link StaleRefresherWebElement} tries to resolve a
	 * {@link StaleElementReferenceException} but could not succeed.
	 */
	public static final String STALE_REFRESHER_STALED_STATE_NOT_SOLVED = "Element is in a staled state. Could not resolve the issue.";

	/**
	 * Utility class. No implementation.
	 */
	private ErrorMessages() {

	}
}
