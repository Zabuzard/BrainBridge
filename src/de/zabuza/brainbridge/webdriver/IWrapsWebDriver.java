package de.zabuza.brainbridge.webdriver;

import org.openqa.selenium.WebDriver;

/**
 * Interface for objects that wrap around a {@link WebDriver}.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public interface IWrapsWebDriver {

	/**
	 * Gets the underlying wrapped web driver.
	 * 
	 * @return The underlying wrapped web driver
	 */
	public WebDriver getRawDriver();
}
