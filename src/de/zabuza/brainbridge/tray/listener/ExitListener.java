package de.zabuza.brainbridge.tray.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.zabuza.brainbridge.BrainBridge;
import de.zabuza.brainbridge.logging.ILogger;
import de.zabuza.brainbridge.logging.LoggerFactory;

/**
 * Listener to use for exiting the tool. When the event arrives it performs
 * {@link BrainBridge#shutdown()}.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class ExitListener implements ActionListener {
	/**
	 * The logger to use for logging.
	 */
	private final ILogger mLogger;
	/**
	 * The parent tool to shutdown when the event arrives.
	 */
	private final BrainBridge mParent;

	/**
	 * Creates a new exit listener that shutdowns the given tool when an action
	 * event arrives. Therefore it performs {@link BrainBridge#shutdown()}.
	 * 
	 * @param parent
	 *            The tool to shutdown when the event arrives
	 */
	public ExitListener(final BrainBridge parent) {
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
		this.mLogger.logInfo("Executing exit action");
		this.mParent.shutdown();
	}

}
