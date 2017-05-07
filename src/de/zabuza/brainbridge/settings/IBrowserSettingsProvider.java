package de.zabuza.brainbridge.settings;

import de.zabuza.brainbridge.webdriver.EBrowser;

/**
 * Interface for objects that provide settings for browsers.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public interface IBrowserSettingsProvider {
	/**
	 * Gets the browser to use.
	 * 
	 * @return The browser to use or <tt>null</tt> if not set
	 */
	public EBrowser getBrowser();

	/**
	 * Gets the binary for the browser to use.
	 * 
	 * @return The binary for the browser to use or <tt>null</tt> if not set
	 */
	public String getBrowserBinary();

	/**
	 * Gets the driver for a given browser if set.
	 * 
	 * @param browser
	 *            Browser to get driver for
	 * @return The driver for the browser or <tt>null</tt> if not set
	 */
	public String getDriverForBrowser(final EBrowser browser);

	/**
	 * Gets the path to the user profile to use.
	 * 
	 * @return The path to the user profile to use or <tt>null</tt> if not set
	 */
	public String getUserProfile();
}
