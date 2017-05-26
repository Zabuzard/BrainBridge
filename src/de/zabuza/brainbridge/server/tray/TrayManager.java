package de.zabuza.brainbridge.server.tray;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import de.zabuza.brainbridge.server.BrainBridge;
import de.zabuza.brainbridge.server.exceptions.UnsupportedSystemTrayException;
import de.zabuza.brainbridge.server.logging.ILogger;
import de.zabuza.brainbridge.server.logging.LoggerFactory;
import de.zabuza.brainbridge.server.tray.listener.ExitListener;
import de.zabuza.brainbridge.server.tray.listener.RestartListener;

/**
 * Manages the tray icon of the tool. After creation use {@link #addTrayIcon()}
 * to add the icon and {@link #removeTrayIcon()} to remove it. It has a context
 * menu that will automatically use callbacks to the tool.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class TrayManager {
	/**
	 * The name of the context menu entry for exiting the tool.
	 */
	private static final String NAME_EXIT = "Exit";
	/**
	 * The name of the context menu entry for restarting the tool.
	 */
	private static final String NAME_RESTART = "Restart";
	/**
	 * The name of the tray icon in the system tray.
	 */
	private static final String NAME_TRAY = "BrainBridge";
	/**
	 * The logger to use for logging.
	 */
	private final ILogger mLogger;
	/**
	 * The parent tool to use for context menu action callbacks.
	 */
	private final BrainBridge mParent;
	/**
	 * The systems tray.
	 */
	private SystemTray mSystemTray;
	/**
	 * The tray icon of the tool which is managed by this object.
	 */
	private TrayIcon mTrayIcon;
	/**
	 * The image of the tray icon which is managed by this object.
	 */
	private final Image mTrayIconImage;

	/**
	 * Creates a new tray manager that manages the tray icon of the tool. After
	 * creation use {@link #addTrayIcon()} to add the icon and
	 * {@link #removeTrayIcon()} to remove it. It has a context menu that will
	 * automatically use callbacks to the tool.
	 * 
	 * @param parent
	 *            The parent tool to use for context menu action callbacks
	 * @param trayIconImage
	 *            The image of the tray icon which is managed by this object
	 */
	public TrayManager(final BrainBridge parent, final Image trayIconImage) {
		this.mParent = parent;
		this.mTrayIconImage = trayIconImage;
		this.mLogger = LoggerFactory.getLogger();
		initialize();
	}

	/**
	 * Adds the tray icon of the tool to the systems tray.
	 * 
	 * @throws AWTException
	 *             If the desktop system tray is missing
	 */
	public void addTrayIcon() throws AWTException {
		if (this.mLogger.isDebugEnabled()) {
			this.mLogger.logDebug("Adding tray icon");
		}
		this.mSystemTray.add(this.mTrayIcon);
	}

	/**
	 * Removes the tray icon of the tool from the systems tray.
	 */
	public void removeTrayIcon() {
		if (this.mLogger.isDebugEnabled()) {
			this.mLogger.logDebug("Removing tray icon");
		}
		this.mSystemTray.remove(this.mTrayIcon);
	}

	/**
	 * Intializes the tray manager and creates the tray icon and its context
	 * menu.
	 * 
	 * @throws UnsupportedSystemTrayException
	 *             If the current platform does not support a system tray
	 */
	private void initialize() throws UnsupportedSystemTrayException {
		if (this.mLogger.isDebugEnabled()) {
			this.mLogger.logDebug("Initializing TrayManager");
		}

		// If try is not supported, abort
		if (!SystemTray.isSupported()) {
			throw new UnsupportedSystemTrayException();
		}

		this.mSystemTray = SystemTray.getSystemTray();

		this.mTrayIcon = new TrayIcon(this.mTrayIconImage, NAME_TRAY);
		this.mTrayIcon.setImageAutoSize(true);

		final MenuItem restartItem = new MenuItem(NAME_RESTART);
		final MenuItem exitItem = new MenuItem(NAME_EXIT);

		final PopupMenu popup = new PopupMenu();
		popup.add(restartItem);
		popup.add(exitItem);
		this.mTrayIcon.setPopupMenu(popup);

		restartItem.addActionListener(new RestartListener(this.mParent));
		exitItem.addActionListener(new ExitListener(this.mParent));
	}
}
