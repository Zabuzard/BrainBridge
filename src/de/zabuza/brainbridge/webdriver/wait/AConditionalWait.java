package de.zabuza.brainbridge.webdriver.wait;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Abstract class for waiting for a given condition. By using
 * {@link #waitUntilCondition()} the method returns as soon as the condition
 * given by {@link #getCondition()} resolves to <tt>true</tt>.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 * @param <V>
 *            Class the condition returns when resolving to <tt>true</tt>, as
 *            specified by {@link ExpectedCondition}.
 */
public abstract class AConditionalWait<V> {
	/**
	 * Default time out the object waits for a condition to resolve to
	 * <tt>true</tt> until a {@link TimeoutException} is thrown.
	 */
	protected static final int STANDARD_TIMEOUT = 6;

	/**
	 * Web driver to use for waiting.
	 */
	private final WebDriverWait m_Wait;

	/**
	 * Creates a new conditional wait object that uses a given web driver and
	 * the default timeout of {@link #STANDARD_TIMEOUT}.
	 * 
	 * @param driver
	 *            Web driver to use for waiting
	 */
	public AConditionalWait(final WebDriver driver) {
		this(driver, STANDARD_TIMEOUT);
	}

	/**
	 * Creates a new conditional wait object that uses a given web driver and a
	 * given timeout.
	 * 
	 * @param driver
	 *            Web driver to use for waiting
	 * @param timeOutInSeconds
	 *            Timeout in seconds to wait for the condition to resolve to
	 *            <tt>true</tt> until a {@link TimeoutException} is thrown.
	 */
	public AConditionalWait(final WebDriver driver, final long timeOutInSeconds) {
		this.m_Wait = new WebDriverWait(driver, timeOutInSeconds);
	}

	/**
	 * Waits for the condition given by {@link #getCondition()} and returns as
	 * soon as it resolves to <tt>true</tt>.
	 * 
	 * @return The object specified by {@link ExpectedCondition}.
	 */
	public V waitUntilCondition() {
		return this.m_Wait.until(getCondition());
	}

	/**
	 * Gets the condition this object should wait for when using
	 * {@link #waitUntilCondition()}.
	 * 
	 * @return The condition this object should wait for
	 */
	protected abstract ExpectedCondition<V> getCondition();
}
