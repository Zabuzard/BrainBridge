package de.zabuza.brainbridge.server.webdriver.wait;

import org.openqa.selenium.WebDriver;

/**
 * Class for waiting until a given frame is present. Start waiting using the
 * {@link #waitUntilCondition()} method.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class FramePresenceWait extends NamePresenceWait {
	/**
	 * Creates a new instance of this object using a given web driver and a
	 * frame name.
	 * 
	 * @param driver
	 *            Driver to use for waiting
	 * @param frameName
	 *            Name of the frame to wait for its presence
	 */
	public FramePresenceWait(final WebDriver driver, final String frameName) {
		super(driver, frameName);
	}
}
