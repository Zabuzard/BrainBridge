package de.zabuza.brainbridge.server.webdriver.wait;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Class for waiting until an element, given by a CSS selector, is present.
 * Start waiting using the {@link #waitUntilCondition()} method.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public class CSSSelectorPresenceWait extends AConditionalWait<WebElement> {
	/**
	 * Condition to wait for.
	 */
	private final ExpectedCondition<WebElement> mCondition;

	/**
	 * Creates a new instance of this object using a given web driver and CSS
	 * selector.
	 * 
	 * @param driver
	 *            Driver to use for waiting
	 * @param cssSelector
	 *            CSS selector that corresponds to the element to wait for its
	 *            presence
	 */
	public CSSSelectorPresenceWait(final WebDriver driver, final String cssSelector) {
		super(driver);
		this.mCondition = ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zabuza.brainbridge.webdriver.wait.AConditionalWait#getCondition()
	 */
	@Override
	protected ExpectedCondition<WebElement> getCondition() {
		return this.mCondition;
	}
}
