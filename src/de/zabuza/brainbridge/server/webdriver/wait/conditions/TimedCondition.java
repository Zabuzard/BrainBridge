package de.zabuza.brainbridge.server.webdriver.wait.conditions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * Condition that outputs whether a given time period has passed. The timer
 * automatically starts by the first time {@link #apply(WebDriver)} is called.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public class TimedCondition implements ExpectedCondition<Boolean> {
	/**
	 * If the timer has started.
	 */
	private boolean m_ConditionActivated;
	/**
	 * A system time stamp from that point where the timer has started.
	 */
	private long m_TimeStampStarted;
	/**
	 * Time period to wait for to pass, in milliseconds.
	 */
	private final long m_TimeToWait;

	/**
	 * Creates a new instance of this object with a given time to wait. The
	 * timer automatically starts by the first time {@link #apply(WebDriver)} is
	 * called.
	 * 
	 * @param timeToWait
	 *            Time period to wait for to pass, in milliseconds
	 */
	public TimedCondition(final long timeToWait) {
		this.m_TimeToWait = timeToWait;
		this.m_ConditionActivated = false;
		this.m_TimeStampStarted = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.common.base.Function#apply(java.lang.Object)
	 */
	@Override
	public Boolean apply(final WebDriver driver) {
		if (!this.m_ConditionActivated) {
			this.m_TimeStampStarted = System.currentTimeMillis();
			this.m_ConditionActivated = true;
		}
		return Boolean.valueOf(System.currentTimeMillis() - this.m_TimeStampStarted > this.m_TimeToWait);
	}

}
