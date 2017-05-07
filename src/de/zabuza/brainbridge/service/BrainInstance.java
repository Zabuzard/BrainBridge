package de.zabuza.brainbridge.service;

import org.openqa.selenium.WebDriver;

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

		// TODO Implement
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
		// TODO Implement
	}

	/**
	 * Posts the given message to the brain chat.
	 * 
	 * @param message
	 *            The message to post
	 */
	public void postMessage(final String message) {
		updateLastUsage();

		// TODO Implement
	}

	/**
	 * Shuts this instance down and frees all used resources.
	 */
	public void shutdown() {
		// TODO Implement;
	}

	/**
	 * Updates the value of the last usage timestamp by setting it to the
	 * current time.
	 */
	private void updateLastUsage() {
		this.mLastUsage = System.currentTimeMillis();
	}

}
