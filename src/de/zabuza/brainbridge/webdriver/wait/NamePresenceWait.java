package de.zabuza.brainbridge.webdriver.wait;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Class for waiting until an element, given its name, is present. Start waiting
 * using the {@link #waitUntilCondition()} method.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public class NamePresenceWait extends AConditionalWait<WebElement> {
	/**
	 * Condition to wait for.
	 */
	private final ExpectedCondition<WebElement> m_Condition;

	/**
	 * Creates a new instance of this object using a given web driver and name.
	 * 
	 * @param driver
	 *            Driver to use for waiting
	 * @param name
	 *            Name of the element to wait for its presence
	 */
	public NamePresenceWait(final WebDriver driver, final String name) {
		super(driver);
		this.m_Condition = ExpectedConditions.presenceOfElementLocated(By.name(name));
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
