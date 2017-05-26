package de.zabuza.brainbridge.server.webdriver.wait;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import de.zabuza.brainbridge.server.webdriver.wait.conditions.TimedCondition;

/**
 * Class for waiting until a given period of time has passed. Start waiting
 * using the {@link #waitUntilCondition()} method.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public class TimedWait extends AConditionalWait<Boolean> {
	/**
	 * Factor to convert seconds to milliseconds, if multiplied with.
	 */
	private static final int SECONDS_TO_MILLIS = 1000;
	/**
	 * Offset to the given time period, for which the condition should wait to
	 * pass, when a {@TimeoutException} should be thrown.
	 */
	private static final int TIMEOUT_OFFSET = 2;

	/**
	 * Condition to wait for.
	 */
	private final ExpectedCondition<Boolean> m_Condition;

	/**
	 * Creates a new instance of this object with a given web driver and time to
	 * wait for.
	 * 
	 * @param driver
	 *            Driver to use for waiting
	 * @param timeToWait
	 *            Time period in milliseconds to wait for it to pass
	 */
	public TimedWait(final WebDriver driver, final long timeToWait) {
		super(driver, (timeToWait / SECONDS_TO_MILLIS) + TIMEOUT_OFFSET);
		this.m_Condition = new TimedCondition(timeToWait);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zabuza.brainbridge.webdriver.wait.AConditionalWait#getCondition()
	 */
	@Override
	protected ExpectedCondition<Boolean> getCondition() {
		return this.m_Condition;
	}
}
