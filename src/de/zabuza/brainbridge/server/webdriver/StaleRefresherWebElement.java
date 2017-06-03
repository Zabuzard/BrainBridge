package de.zabuza.brainbridge.server.webdriver;

import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import de.zabuza.brainbridge.server.exceptions.ErrorMessages;

/**
 * Wrapper for web element objects to automatically handle staled states that
 * would otherwise throw {@link StaleElementReferenceException}s when used.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class StaleRefresherWebElement implements WebElement {

	/**
	 * Maximal tries to exchange a staled web element reference.
	 */
	private static final int MAX_STALED_EXCHANGE_TRIES = 10;
	/**
	 * Time to wait before exchanging a staled reference in milliseconds.
	 */
	private static final int STALE_EXCHANGE_WAIT = 1000;
	/**
	 * Context of the element to find itself again if staled
	 */
	private final By mContext;
	/**
	 * Web element to wrap for staled state handling.
	 */
	private volatile WebElement mElement;
	/**
	 * Tag of the underlying element to wrap.
	 */
	private final String mElementTag;
	/**
	 * The position of this element if it occurs in a list of elements, a
	 * negative value else.
	 */
	private final int mIndex;
	/**
	 * The parent element of this element as driver if used in its context, null
	 * else.
	 */
	private final WebDriver mParentAsDriver;

	/**
	 * The parent element of this element as element if used in its context,
	 * null else.
	 */
	private final WebElement mParentAsElement;

	/**
	 * Creates a new instance of this object with a given web element object and
	 * its {@link By}-context.
	 * 
	 * @param element
	 *            Web element object to wrap for staled state handling
	 * @param context
	 *            Context of the element to find itself again if staled
	 * @param parent
	 *            The parent element of this element if used in its context,
	 *            null else.
	 */
	public StaleRefresherWebElement(final WebElement element, final By context, final WebDriver parent) {
		this(element, context, parent, null, -1);
	}

	/**
	 * Creates a new instance of this object with a given web element object and
	 * its {@link By}-context.
	 * 
	 * @param element
	 *            Web element object to wrap for staled state handling
	 * @param context
	 *            Context of the element to find itself again if staled
	 * @param parent
	 *            The parent element of this element if used in its context,
	 *            null else.
	 * @param index
	 *            The position of this element if it occurs in a list of
	 *            elements, a negative value else.
	 */
	public StaleRefresherWebElement(final WebElement element, final By context, final WebDriver parent,
			final int index) {
		this(element, context, parent, null, index);
	}

	/**
	 * Creates a new instance of this object with a given web element object and
	 * its {@link By}-context.
	 * 
	 * @param element
	 *            Web element object to wrap for staled state handling
	 * @param context
	 *            Context of the element to find itself again if staled
	 * @param parent
	 *            The parent element of this element if used in its context,
	 *            null else.
	 */
	public StaleRefresherWebElement(final WebElement element, final By context, final WebElement parent) {
		this(element, context, null, parent, -1);
	}

	/**
	 * Creates a new instance of this object with a given web element object and
	 * its {@link By}-context.
	 * 
	 * @param element
	 *            Web element object to wrap for staled state handling
	 * @param context
	 *            Context of the element to find itself again if staled
	 * @param parent
	 *            The parent element of this element if used in its context,
	 *            null else.
	 * @param index
	 *            The position of this element if it occurs in a list of
	 *            elements, a negative value else.
	 */
	public StaleRefresherWebElement(final WebElement element, final By context, final WebElement parent,
			final int index) {
		this(element, context, null, parent, index);
	}

	/**
	 * Creates a new instance of this object with a given web element object and
	 * its {@link By}-context.
	 * 
	 * @param element
	 *            Web element object to wrap for staled state handling
	 * @param context
	 *            Context of the element to find itself again if staled
	 * @param parentAsDriver
	 *            The parent element of this element as driver if used in its
	 *            context, null else.
	 * @param parentAsElement
	 *            The parent element of this element as element if used in its
	 *            context, null else.
	 * @param index
	 *            The position of this element if it occurs in a list of
	 *            elements, a negative value else.
	 */
	private StaleRefresherWebElement(final WebElement element, final By context, final WebDriver parentAsDriver,
			final WebElement parentAsElement, final int index) {
		this.mElement = element;
		this.mElementTag = this.mElement.getTagName();
		this.mContext = context;
		this.mParentAsDriver = parentAsDriver;
		this.mParentAsElement = parentAsElement;
		this.mIndex = index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#clear()
	 */
	@Override
	public void clear() {
		ensureReferenceNotStaled();
		this.mElement.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#click()
	 */
	@Override
	public void click() {
		ensureReferenceNotStaled();
		this.mElement.click();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#findElement(org.openqa.selenium.By)
	 */
	@Override
	public WebElement findElement(final By by) {
		ensureReferenceNotStaled();
		return new StaleRefresherWebElement(this.mElement.findElement(by), by, this.mElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#findElements(org.openqa.selenium.By)
	 */
	@Override
	public List<WebElement> findElements(final By by) {
		ensureReferenceNotStaled();
		final List<WebElement> elements = this.mElement.findElements(by);
		final List<WebElement> staleRefresherElements = new LinkedList<>();
		int i = 0;
		for (final WebElement element : elements) {
			staleRefresherElements.add(new StaleRefresherWebElement(element, by, this.mElement, i));
			i++;
		}

		return staleRefresherElements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#getAttribute(java.lang.String)
	 */
	@Override
	public String getAttribute(final String name) {
		ensureReferenceNotStaled();
		return this.mElement.getAttribute(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#getCssValue(java.lang.String)
	 */
	@Override
	public String getCssValue(final String propertyName) {
		ensureReferenceNotStaled();
		return this.mElement.getCssValue(propertyName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#getLocation()
	 */
	@Override
	public Point getLocation() {
		ensureReferenceNotStaled();
		return this.mElement.getLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#getRect()
	 */
	@Override
	public Rectangle getRect() {
		ensureReferenceNotStaled();
		return this.mElement.getRect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openqa.selenium.TakesScreenshot#getScreenshotAs(org.openqa.selenium.
	 * OutputType)
	 */
	@Override
	public <X> X getScreenshotAs(final OutputType<X> target) throws WebDriverException {
		ensureReferenceNotStaled();
		return this.mElement.getScreenshotAs(target);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#getSize()
	 */
	@Override
	public Dimension getSize() {
		ensureReferenceNotStaled();
		return this.mElement.getSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#getTagName()
	 */
	@Override
	public String getTagName() {
		ensureReferenceNotStaled();
		return this.mElement.getTagName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#getText()
	 */
	@Override
	public String getText() {
		ensureReferenceNotStaled();
		return this.mElement.getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#isDisplayed()
	 */
	@Override
	public boolean isDisplayed() {
		ensureReferenceNotStaled();
		return this.mElement.isDisplayed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		ensureReferenceNotStaled();
		return this.mElement.isEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#isSelected()
	 */
	@Override
	public boolean isSelected() {
		ensureReferenceNotStaled();
		return this.mElement.isSelected();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#sendKeys(java.lang.CharSequence[])
	 */
	@Override
	public void sendKeys(final CharSequence... keysToSend) {
		ensureReferenceNotStaled();
		this.mElement.sendKeys(keysToSend);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openqa.selenium.WebElement#submit()
	 */
	@Override
	public void submit() {
		ensureReferenceNotStaled();
		this.mElement.submit();
	}

	/**
	 * Tries to exchange the underlying web element if it is in a staled state.
	 * 
	 * @throws StaleElementReferenceException
	 *             When the element is staled and resolving was unsuccessful.
	 */
	private void ensureReferenceNotStaled() throws StaleElementReferenceException {
		int tries = 0;
		boolean resolvedIssue = false;
		StaleElementReferenceException latestException = null;

		while (!resolvedIssue && tries < MAX_STALED_EXCHANGE_TRIES) {
			try {
				// Check if element is staled by provoking the exception
				this.mElement.getTagName();

				// Element is not staled if exception got not thrown
				resolvedIssue = true;
			} catch (final StaleElementReferenceException e) {
				resolvedIssue = false;
				latestException = e;
				try {
					Thread.sleep(STALE_EXCHANGE_WAIT);
				} catch (final InterruptedException interrupt) {
					// Just ignore the interrupt and continue with the next
					// iteration
				}
				exchangeStaledReference();
			}
			tries++;
		}

		if (!resolvedIssue) {
			throw new StaleElementReferenceException(ErrorMessages.STALE_REFRESHER_STALED_STATE_NOT_SOLVED,
					latestException);
		}
	}

	/**
	 * Exchanges a staled reference to the underlying web element by finding it
	 * again with its context. If the element could not be find again it will
	 * not exchange the reference.
	 */
	private void exchangeStaledReference() {
		final WebElement element;
		// Element is not contained in a list of elements
		if (this.mIndex < 0) {
			if (this.mParentAsDriver != null) {
				element = this.mParentAsDriver.findElement(this.mContext);
			} else {
				element = this.mParentAsElement.findElement(this.mContext);
			}
		} else {
			final List<WebElement> elements;
			if (this.mParentAsDriver != null) {
				elements = this.mParentAsDriver.findElements(this.mContext);
			} else {
				elements = this.mParentAsElement.findElements(this.mContext);
			}

			// Assume element stayed at its index
			if (this.mIndex < elements.size()) {
				element = elements.get(this.mIndex);
			} else {
				// List is to small so issue can not be resolved
				return;
			}
		}

		// Compare some attributes to ensure false positive elements
		if (this.mElementTag.equals(element.getTagName())) {
			// Exchange the underlying reference to the element
			this.mElement = element;
		}
	}
}
