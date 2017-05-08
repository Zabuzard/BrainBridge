package de.zabuza.brainbridge.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import de.zabuza.brainbridge.webdriver.wait.CSSSelectorPresenceWait;
import de.zabuza.brainbridge.webdriver.wait.FramePresenceWait;
import de.zabuza.brainbridge.webdriver.wait.NamePresenceWait;

/**
 * Instance for an active chat with brain. Once created use
 * {@link #initialize()} to initialize it. Afterwards get the assigned unique id
 * of the instance by {@link #getId()} and use the provided methods for chat
 * interaction. Always use {@link #shutdown()} when not using this instance
 * anymore.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class BrainInstance {
	/**
	 * The pattern every answer from the chat service matches. The message
	 * content can be accessed by group 1.
	 */
	private static final String CHAT_ANSWER_PATTERN = "^.*<font color=\"(?:#\\d{3,6}|[A-Za-z]+)\"><b>Brain\\s*:\\s*<\\/b><\\/font>(.+)$";
	/**
	 * Name of the frame that contains the chat input.
	 */
	private final static String CHAT_INPUT_FRAME_NAME = "frin";
	/**
	 * The name of the input element that allows inputting chat messages.
	 */
	private static final String CHAT_INPUT_NAME = "editMsg";
	/**
	 * Pattern that matches line separators in the chat content.
	 */
	private static final String CHAT_LINE_SEPARATOR = "<br[\\s\\/]*>";
	/**
	 * Name of the frame that contains the chat output.
	 */
	private final static String CHAT_OUTPUT_FRAME_NAME = "frout";
	/**
	 * The URL to the chat service.
	 */
	private final static String CHAT_SERVICE = "http://www.thebot.de/";
	/**
	 * The pattern which matches the id argument in an URL. It can be accessed
	 * by the group 1.
	 */
	private static final String ID_PATTERN = "(?:^|.+&|.+\\?)id=([A-Za-z0-9]+)(?:$|&.+)";
	/**
	 * Class of the anchor that logs in to the chat service.
	 */
	private final static String LOGIN_ANCHOR = "a.btnAls_Gast";
	/**
	 * Class of the anchor that logs out from the chat service.
	 */
	private final static String LOGOUT_ANCHOR = "a.btnChat_beenden";
	/**
	 * The driver to use for accessing browser contents.
	 */
	private final WebDriver mDriver;
	/**
	 * The unique id assigned to this instance.
	 */
	private String mId;
	/**
	 * Timestamp of when the instance was last used in milliseconds.
	 */
	private long mLastUsage;
	/**
	 * The unique window handle assigned to this instance.
	 */
	private final String mWindowHandle;

	/**
	 * Creates a new instance for an active chat with brain. Once created use
	 * {@link #initialize()} to initialize it. Afterwards get the assigned
	 * unique id of the instance by {@link #getId()} and use the provided
	 * methods for chat interaction. Always use {@link #shutdown()} when not
	 * using this instance anymore.
	 * 
	 * @param driver
	 *            The driver to use for accessing browser contents
	 * @param windowHandle
	 *            The unique window handle assigned to this instance
	 */
	public BrainInstance(final WebDriver driver, final String windowHandle) {
		this.mDriver = driver;
		this.mWindowHandle = windowHandle;
		this.mId = null;

		updateLastUsage();
	}

	/**
	 * Gets the unique id assigned to this chat instance.
	 * 
	 * @return The unique id assigned to this chat instance or <tt>null</tt> if
	 *         not yet initialized with {@link #initialize()}
	 */
	public String getId() {
		return this.mId;
	}

	/**
	 * Gets the timestamp of when the instance was last used in milliseconds.
	 * 
	 * @return The timestamp of when the instance was last used in milliseconds
	 */
	public long getLastUsage() {
		return this.mLastUsage;
	}

	/**
	 * Gets the latest answer of brain in the chat.
	 * 
	 * @return The latest answer of brain in the chat or <tt>null</tt> if there
	 *         is no
	 */
	public String getLatestAnswer() {
		updateLastUsage();
		switchToWindow();
		switchToFrame(CHAT_OUTPUT_FRAME_NAME);
		final String pageContent = this.mDriver.getPageSource();

		final String[] lines = pageContent.split(CHAT_LINE_SEPARATOR);
		// Latest message is always the second to last entry
		if (lines.length < 2) {
			return null;
		}
		final String latestMessage = lines[lines.length - 2].trim();

		final Pattern answerPattern = Pattern.compile(CHAT_ANSWER_PATTERN);
		final Matcher answerMatcher = answerPattern.matcher(latestMessage);
		if (answerMatcher.matches()) {
			return answerMatcher.group(1);
		}

		return null;
	}

	/**
	 * Gets the unique window handle assigned to this instance.
	 * 
	 * @return The unique window handle assigned to this instance
	 */
	public String getWindowHandle() {
		return this.mWindowHandle;
	}

	/**
	 * Initializes the instance. Call this method prior to chat interaction.
	 */
	public void initialize() {
		switchToWindow();
		this.mDriver.get(CHAT_SERVICE);
		final WebElement loginAnchor = new CSSSelectorPresenceWait(this.mDriver, LOGIN_ANCHOR).waitUntilCondition();
		try {
			loginAnchor.click();
		} catch (final TimeoutException e) {
			// Ignore the error as it comes from the aborted page load
		}

		final WebElement outputFrame = new FramePresenceWait(this.mDriver, CHAT_OUTPUT_FRAME_NAME).waitUntilCondition();
		final String frameSrc = outputFrame.getAttribute("src");

		final Pattern idPattern = Pattern.compile(ID_PATTERN);
		final Matcher idMatcher = idPattern.matcher(frameSrc);
		if (idMatcher.matches()) {
			this.mId = idMatcher.group(1);
		}
	}

	/**
	 * Posts the given message to the brain chat.
	 * 
	 * @param message
	 *            The message to post
	 */
	public void postMessage(final String message) {
		updateLastUsage();
		switchToWindow();
		switchToFrame(CHAT_INPUT_FRAME_NAME);

		final WebElement input = new NamePresenceWait(this.mDriver, CHAT_INPUT_NAME).waitUntilCondition();
		input.sendKeys(message);
		input.sendKeys(Keys.ENTER);
	}

	/**
	 * Shuts this instance down and frees all used resources.
	 */
	public void shutdown() {
		switchToWindow();
		switchToFrame(CHAT_INPUT_FRAME_NAME);

		final WebElement logoutAnchor = new CSSSelectorPresenceWait(this.mDriver, LOGOUT_ANCHOR).waitUntilCondition();
		logoutAnchor.click();

		this.mDriver.close();
	}

	/**
	 * Switches the context of the driver instance to the frame with the given
	 * name. The frame is always searched in the context of the whole document
	 * instead of the current set context.
	 * 
	 * @param name
	 *            The name of the frame to switch to
	 */
	private void switchToFrame(final String name) {
		this.mDriver.switchTo().parentFrame();
		this.mDriver.switchTo().frame(name);
	}

	/**
	 * Switches the driver instance to the window of this chat instance.
	 */
	private void switchToWindow() {
		this.mDriver.switchTo().window(this.mWindowHandle);
	}

	/**
	 * Updates the value of the last usage timestamp by setting it to the
	 * current time.
	 */
	private void updateLastUsage() {
		this.mLastUsage = System.currentTimeMillis();
	}

}
