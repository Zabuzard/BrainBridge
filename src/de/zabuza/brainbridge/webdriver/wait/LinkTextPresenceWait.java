package de.zabuza.brainbridge.webdriver.wait;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Class for waiting until an element, given by a partial link text, is present.
 * Start waiting using the {@link #waitUntilCondition()} method.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public class LinkTextPresenceWait extends AConditionalWait<WebElement> {
	/**
	 * Condition to wait for.
	 */
	private final ExpectedCondition<WebElement> m_Condition;

	/**
	 * Creates a new instance of this object using a given web driver and CSS
	 * selector.
	 * 
	 * @param driver
	 *            Driver to use for waiting
	 * @param partialLinkText
	 *            Partial link text that corresponds to the element to wait for
	 *            its presence
	 */
	public LinkTextPresenceWait(final WebDriver driver, final String partialLinkText) {
		super(driver);
		this.m_Condition = ExpectedConditions.presenceOfElementLocated(By.partialLinkText(partialLinkText));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zabuza.brainbridge.webdriver.wait.AConditionalWait#getCondition()
	 */
	@Override
	protected ExpectedCondition<WebElement> getCondition() {
		return this.m_Condition;
	}
}
