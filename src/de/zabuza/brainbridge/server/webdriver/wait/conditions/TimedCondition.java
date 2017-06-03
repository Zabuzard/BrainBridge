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
	private boolean mConditionActivated;
	/**
	 * A system time stamp from that point where the timer has started.
	 */
	private long mTimeStampStarted;
	/**
	 * Time period to wait for to pass, in milliseconds.
	 */
	private final long mTimeToWait;

	/**
	 * Creates a new instance of this object with a given time to wait. The
	 * timer automatically starts by the first time {@link #apply(WebDriver)} is
	 * called.
	 * 
	 * @param timeToWait
	 *            Time period to wait for to pass, in milliseconds
	 */
	public TimedCondition(final long timeToWait) {
		this.mTimeToWait = timeToWait;
		this.mConditionActivated = false;
		this.mTimeStampStarted = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.common.base.Function#apply(java.lang.Object)
	 */
	@Override
	public Boolean apply(final WebDriver driver) {
		if (!this.mConditionActivated) {
			this.mTimeStampStarted = System.currentTimeMillis();
			this.mConditionActivated = true;
		}
		return Boolean.valueOf(System.currentTimeMillis() - this.mTimeStampStarted > this.mTimeToWait);
	}

}
