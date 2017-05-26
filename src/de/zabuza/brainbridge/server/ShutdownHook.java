package de.zabuza.brainbridge.server;

/**
 * Thread that once started shuts the parent tool down.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class ShutdownHook extends Thread {
	/**
	 * The parent tool to shutdown on execution.
	 */
	private final BrainBridge mParent;

	/**
	 * Creates a new shutdown hook that will shutdown the given parent tool once
	 * started.
	 * 
	 * @param parent
	 *            The parent tool to shutdown on execution
	 */
	public ShutdownHook(final BrainBridge parent) {
		this.mParent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		if (!this.mParent.wasShutdown()) {
			this.mParent.shutdown();
		}
	}
}
