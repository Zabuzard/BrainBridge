package de.zabuza.brainbridge.client;

/**
 * A client-side instance of a chat over the BrainBridge.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class BrainInstance {
	/**
	 * The time to wait until trying to access an answer in milliseconds.
	 */
	private static final long WAIT_FOR_ANSWER_INTERVAL = 1_000;
	/**
	 * The maximal time to wait for trying to access an answer in milliseconds.
	 */
	private static final long WAIT_FOR_ANSWER_MAXIMAL = 15_000;
	/**
	 * The client-side API to use for interaction with the server-side API.
	 */
	private final BrainBridgeAPI mApi;
	/**
	 * The unique id of this instance.
	 */
	private final String mId;

	/**
	 * Creates a new client-side instance of a chat over the BrainBridge. Do not
	 * forget to call {@link #shutdown()} once finished with this instance.
	 * 
	 * @param id
	 *            The id that uniquely identifies this instance
	 * @param api
	 *            The client-side API to use for interaction with the
	 *            server-side API.
	 */
	public BrainInstance(final String id, final BrainBridgeAPI api) {
		this.mId = id;
		this.mApi = api;
	}

	/**
	 * Posts the given message to the chat and returns the answer of the chat
	 * bot.
	 * 
	 * @param message
	 *            The message to post
	 * @return The answer of the chat bot or <tt>null</tt> if the server
	 *         experienced an error or the maximal time to wait for an answer
	 *         was exceeded.
	 */
	public String post(final String message) {
		final String answerBefore = this.mApi.getLastMessage(this.mId);

		this.mApi.postMessage(this.mId, message);

		final long waitForAnswerStartingTime = System.currentTimeMillis();
		while (true) {
			final String answerAfter = this.mApi.getLastMessage(this.mId);
			if (answerAfter != null && !answerAfter.equals(answerBefore)) {
				return answerAfter;
			}

			try {
				Thread.sleep(WAIT_FOR_ANSWER_INTERVAL);
			} catch (final InterruptedException e) {
				// Simply ignore the interrupt and continue
			}
			final long waitForAnswerCurrentTime = System.currentTimeMillis();
			if (waitForAnswerCurrentTime - waitForAnswerStartingTime >= WAIT_FOR_ANSWER_MAXIMAL) {
				return null;
			}
		}
	}

	/**
	 * Shuts the given instance down. After calling this method the instance is
	 * invalid and should not be used anymore.
	 */
	public void shutdown() {
		this.mApi.shutdownInstance(this.mId);
	}
}
