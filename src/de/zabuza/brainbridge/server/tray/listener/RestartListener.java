package de.zabuza.brainbridge.server.tray.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.zabuza.brainbridge.server.BrainBridge;
import de.zabuza.brainbridge.server.logging.ILogger;
import de.zabuza.brainbridge.server.logging.LoggerFactory;

/**
 * Listener to use for restarting the tool. When the event arrives it performs
 * {@link BrainBridge#stop()} and {@link BrainBridge#start()}.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class RestartListener implements ActionListener {
	/**
	 * The logger to use for logging.
	 */
	private final ILogger mLogger;
	/**
	 * The parent tool to restart when the event arrives.
	 */
	private final BrainBridge mParent;

	/**
	 * Creates a new restart listener that restarts the given tool when an
	 * action event arrives. Therefore it performs {@link BrainBridge#stop()}
	 * and {@link BrainBridge#start()}.
	 * 
	 * @param parent
	 *            The tool to shutdown when the event arrives
	 */
	public RestartListener(final BrainBridge parent) {
		this.mParent = parent;
		this.mLogger = LoggerFactory.getLogger();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		this.mLogger.logInfo("Executing restart action");
		this.mParent.stop();
		this.mParent.start();
	}

}
