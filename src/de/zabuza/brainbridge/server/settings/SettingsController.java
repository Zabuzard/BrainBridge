package de.zabuza.brainbridge.server.settings;

import java.util.HashMap;
import java.util.Map;

import de.zabuza.brainbridge.server.logging.ILogger;
import de.zabuza.brainbridge.server.logging.LoggerFactory;
import de.zabuza.brainbridge.server.logging.LoggerUtil;
import de.zabuza.brainbridge.server.webdriver.EBrowser;

/**
 * The controller of the settings.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class SettingsController implements ISettingsProvider, IBrowserSettingsProvider {
	/**
	 * Text to save for a value if a key is unknown.
	 */
	public static final String UNKNOWN_KEY_VALUE = "";
	/**
	 * Key identifier for binary setting.
	 */
	private static final String KEY_IDENTIFIER_BINARY = "binary";
	/**
	 * Key identifier for the selected browser.
	 */
	private static final String KEY_IDENTIFIER_BROWSER = "browser";
	/**
	 * Key identifier for driver settings.
	 */
	private static final String KEY_IDENTIFIER_DRIVER = "driver";
	/**
	 * Key identifier for port settings.
	 */
	private static final String KEY_IDENTIFIER_PORT = "port";
	/**
	 * Key identifier for user profile setting.
	 */
	private static final String KEY_IDENTIFIER_USER_PROFILE = "userProfile";
	/**
	 * Separator which separates several information in a key.
	 */
	private static final String KEY_INFO_SEPARATOR = "@";

	/**
	 * Utility main method to create settings.
	 * 
	 * @param args
	 *            Not supported
	 */
	public static void main(final String[] args) {
		final SettingsController settings = new SettingsController();
		settings.initialize();

		settings.setPort(8110);
		settings.setBrowser(EBrowser.CHROME);
		settings.setBinary("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
		settings.setDriverForBrowser(
				"D:\\Program Files\\Eclipse\\Workspaces\\workspace_neon\\BrainBridge\\lib\\driver\\chromedriver.exe",
				EBrowser.CHROME);
		settings.setUserProfile("");

		settings.saveSettings();
	}

	/**
	 * The logger to use for logging.
	 */
	private final ILogger mLogger;
	/**
	 * The object for the settings.
	 */
	private final Settings mSettings;

	/**
	 * Structure which saves all currently loaded settings.
	 */
	private final Map<String, String> mSettingsStore;

	/**
	 * Creates a new controller of the settings.
	 */
	public SettingsController() {
		this.mLogger = LoggerFactory.getLogger();

		this.mSettingsStore = new HashMap<>();
		this.mSettings = new Settings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zabuza.brainbridge.settings.ISettingsProvider#getAllSettings()
	 */
	@Override
	public Map<String, String> getAllSettings() {
		return this.mSettingsStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zabuza.brainbridge.settings.IBrowserSettingsProvider#getBrowser()
	 */
	@Override
	public EBrowser getBrowser() {
		final String value = getSetting(KEY_IDENTIFIER_BROWSER);
		if (value != null) {
			return EBrowser.valueOf(value);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.zabuza.brainbridge.settings.IBrowserSettingsProvider#getBrowserBinary(
	 * )
	 */
	@Override
	public String getBrowserBinary() {
		final String binary = getSetting(KEY_IDENTIFIER_BINARY);
		if (binary.equals(UNKNOWN_KEY_VALUE)) {
			return null;
		}
		return binary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zabuza.brainbridge.settings.IBrowserSettingsProvider#
	 * getDriverForBrowser(de.zabuza.brainbridge.webdriver.EBrowser)
	 */
	@Override
	public String getDriverForBrowser(final EBrowser browser) {
		final String key = KEY_IDENTIFIER_DRIVER + KEY_INFO_SEPARATOR + browser;
		final String driver = getSetting(key);
		if (driver.equals(UNKNOWN_KEY_VALUE)) {
			return null;
		}
		return driver;
	}

	/**
	 * Gets the set port.
	 * 
	 * @return The set port or <tt>null</tt> if there is no
	 */
	public Integer getPort() {
		final String value = getSetting(KEY_IDENTIFIER_PORT);
		if (value != null) {
			return Integer.valueOf(value);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.zabuza.brainbridge.settings.ISettingsProvider#getSetting(java.lang.
	 * String)
	 */
	@Override
	public String getSetting(final String key) {
		String value = this.mSettingsStore.get(key);
		if (value == null) {
			value = UNKNOWN_KEY_VALUE;
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.zabuza.brainbridge.settings.IBrowserSettingsProvider#getUserProfile()
	 */
	@Override
	public String getUserProfile() {
		final String userProfile = getSetting(KEY_IDENTIFIER_USER_PROFILE);
		if (userProfile.equals(UNKNOWN_KEY_VALUE)) {
			return null;
		}
		return userProfile;
	}

	/**
	 * Initializes the controller.
	 */
	public void initialize() {
		if (this.mLogger.isDebugEnabled()) {
			this.mLogger.logDebug("Initializing SettingsController");
		}

		this.mSettings.loadSettings(this);
	}

	/**
	 * Call whenever the save action is to be executed. This will save all
	 * settings.
	 */
	public void saveSettings() {
		try {
			// Save settings
			this.mSettings.saveSettings(this);
		} catch (final Exception e) {
			// Log the error but continue
			this.mLogger.logError("Error while saving settings: " + LoggerUtil.getStackTrace(e));
		}
	}

	/**
	 * Sets the path to the executable binary to use for the browser.
	 * 
	 * @param binary
	 *            The path to the executable binary to use for the browser
	 */
	public void setBinary(final String binary) {
		if (!binary.equals(UNKNOWN_KEY_VALUE)) {
			final String key = KEY_IDENTIFIER_BINARY;
			setSetting(key, binary);
		}
	}

	/**
	 * Sets the browser to use.
	 * 
	 * @param browser
	 *            The browser to use
	 */
	public void setBrowser(final EBrowser browser) {
		if (browser != null) {
			final String key = KEY_IDENTIFIER_BROWSER;
			setSetting(key, browser.toString());
		}
	}

	/**
	 * Sets the path to the driver to use for the given browser.
	 * 
	 * @param driver
	 *            The path to the driver to use for the given browser
	 * @param browser
	 *            The browser to set a driver for
	 */
	public void setDriverForBrowser(final String driver, final EBrowser browser) {
		if (!driver.equals(UNKNOWN_KEY_VALUE)) {
			final String key = KEY_IDENTIFIER_DRIVER + KEY_INFO_SEPARATOR + browser;
			setSetting(key, driver);
		}
	}

	/**
	 * Sets the port to use for communication.
	 * 
	 * @param port
	 *            The port to use for communication
	 */
	public void setPort(final int port) {
		if (port > 0) {
			final String key = KEY_IDENTIFIER_PORT;
			setSetting(key, Integer.toString(port));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.zabuza.brainbridge.settings.ISettingsProvider#setSetting(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public void setSetting(final String key, final String value) {
		this.mSettingsStore.put(key, value);
	}

	/**
	 * Sets the user profile to use.
	 * 
	 * @param userProfile
	 *            The user profile to use
	 */
	public void setUserProfile(final String userProfile) {
		if (!userProfile.equals(UNKNOWN_KEY_VALUE)) {
			final String key = KEY_IDENTIFIER_USER_PROFILE;
			setSetting(key, userProfile);
		}
	}
}
