package de.zabuza.brainbridge;

import java.io.File;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;

import de.zabuza.brainbridge.logging.ILogger;
import de.zabuza.brainbridge.logging.LoggerFactory;
import de.zabuza.brainbridge.logging.LoggerUtil;
import de.zabuza.brainbridge.service.Service;
import de.zabuza.brainbridge.settings.IBrowserSettingsProvider;
import de.zabuza.brainbridge.settings.SettingsController;
import de.zabuza.brainbridge.webdriver.EBrowser;
import de.zabuza.brainbridge.webdriver.StaleRefresherWebDriver;

/**
 * The entry class of the BrainBridge service. After creation and initialization
 * via {@link #initialize()} the tool can be started by {@link #start()} and
 * ended by {@link #shutdown()} or {@link #stop()}.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class BrainBridge {
	/**
	 * The default port to use.
	 */
	private static final int DEFAULT_PORT = 8110;

	/**
	 * Starts the BrainBridge service and ensures that all thrown and not caught
	 * exceptions create log messages and shutdown the service.
	 * 
	 * @param args
	 *            Not supported
	 */
	public static void main(final String[] args) {
		BrainBridge brainBridge = null;
		try {
			brainBridge = new BrainBridge();
			brainBridge.initialize();
			brainBridge.start();
		} catch (final Exception e) {
			LoggerFactory.getLogger().logError("Error, shutting down: " + LoggerUtil.getStackTrace(e));
			// Try to shutdown
			if (brainBridge != null) {
				brainBridge.shutdown();
			}
		}
	}

	/**
	 * Creates the capabilities to use with a browser for the given arguments.
	 * 
	 * @param browser
	 *            Browser to create capabilities for
	 * @param driverPath
	 *            Path to the driver or <tt>null</tt> if not set
	 * @param binaryPath
	 *            Path to the binary or <tt>null</tt> if not set
	 * @param userProfile
	 *            The name or the path to the user profile, depending on the
	 *            browser, or <tt>null</tt> if not set
	 * @return The capabilities to use or <tt>null</tt> if there are no
	 */
	private static DesiredCapabilities createCapabilities(final EBrowser browser, final String driverPath,
			final String binaryPath, final String userProfile) {
		DesiredCapabilities capabilities = null;

		if (browser == EBrowser.FIREFOX) {
			capabilities = DesiredCapabilities.firefox();
			final FirefoxOptions options = new FirefoxOptions();

			// Set the driver
			if (driverPath != null) {
				System.setProperty("webdriver.gecko.driver", driverPath);
				System.setProperty("webdriver.firefox.marionette", driverPath);
				capabilities.setCapability(FirefoxDriver.MARIONETTE, true);
			}

			// Set the binary
			if (binaryPath != null) {
				final File pathToBinary = new File(binaryPath);
				final FirefoxBinary binary = new FirefoxBinary(pathToBinary);
				options.setBinary(binary);
			}

			// Set the user profile
			if (userProfile != null) {
				final FirefoxProfile profile = new ProfilesIni().getProfile(userProfile);
				options.setProfile(profile);
			}

			options.addTo(capabilities);
		} else if (browser == EBrowser.CHROME) {
			capabilities = DesiredCapabilities.chrome();
			final ChromeOptions options = new ChromeOptions();

			// Set the driver
			if (driverPath != null) {
				System.setProperty("webdriver.chrome.driver", driverPath);
			}

			// Set the binary
			if (binaryPath != null) {
				options.setBinary(binaryPath);
			}

			// Set the user profile
			if (userProfile != null) {
				options.addArguments("user-data-dir=" + userProfile);
			}
			options.addArguments("disable-infobars");

			capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		} else if (browser == EBrowser.SAFARI) {
			capabilities = DesiredCapabilities.internetExplorer();

			// Set the driver
			if (driverPath != null) {
				System.setProperty("webdriver.safari.driver", driverPath);
			}

			// Set the binary
			if (binaryPath != null) {
				capabilities.setCapability("safari.binary", binaryPath);
			}
		} else if (browser == EBrowser.INTERNET_EXPLORER) {
			capabilities = DesiredCapabilities.internetExplorer();

			// Set the driver
			if (driverPath != null) {
				System.setProperty("webdriver.ie.driver", driverPath);
			}

			// Set the binary
			if (binaryPath != null) {
				capabilities.setCapability("ie.binary", binaryPath);
			}
		} else if (browser == EBrowser.OPERA) {
			capabilities = DesiredCapabilities.internetExplorer();

			// Set the driver
			if (driverPath != null) {
				System.setProperty("webdriver.opera.driver", driverPath);
			}

			// Set the binary
			if (binaryPath != null) {
				capabilities.setCapability("opera.binary", binaryPath);
			}

			// Set the user profile
			if (userProfile != null) {
				capabilities.setCapability("opera.profile", userProfile);
			}
		} else if (browser == EBrowser.MS_EDGE) {
			capabilities = DesiredCapabilities.internetExplorer();

			// Set the driver
			if (driverPath != null) {
				System.setProperty("webdriver.edge.driver", driverPath);
			}

			// Set the binary
			if (binaryPath != null) {
				capabilities.setCapability("edge.binary", binaryPath);
			}
		} else {
			throw new IllegalArgumentException("The given browser is not supported: " + browser);
		}

		return capabilities;
	}

	/**
	 * Creates a {@link WebDriver} that uses the given browser.
	 * 
	 * @param browser
	 *            Browser to use for the driver
	 * @param capabilities
	 *            The desired capabilities for the driver or <tt>null</tt> if
	 *            not used
	 * @return Webdriver that uses the given browser
	 */
	private static WebDriver createWebDriver(final EBrowser browser, final DesiredCapabilities capabilities) {
		WebDriver driver;
		if (browser == EBrowser.FIREFOX) {
			if (capabilities != null) {
				driver = new FirefoxDriver(capabilities);
			} else {
				driver = new FirefoxDriver();
			}
		} else if (browser == EBrowser.CHROME) {
			if (capabilities != null) {
				driver = new ChromeDriver(capabilities);
			} else {
				driver = new ChromeDriver();
			}
		} else if (browser == EBrowser.SAFARI) {
			if (capabilities != null) {
				driver = new SafariDriver(capabilities);
			} else {
				driver = new SafariDriver();
			}
		} else if (browser == EBrowser.INTERNET_EXPLORER) {
			if (capabilities != null) {
				driver = new InternetExplorerDriver(capabilities);
			} else {
				driver = new InternetExplorerDriver();
			}
		} else if (browser == EBrowser.OPERA) {
			if (capabilities != null) {
				driver = new OperaDriver(capabilities);
			} else {
				driver = new OperaDriver();
			}
		} else if (browser == EBrowser.MS_EDGE) {
			if (capabilities != null) {
				driver = new EdgeDriver(capabilities);
			} else {
				driver = new EdgeDriver();
			}
		} else {
			throw new IllegalArgumentException("The given browser is not supported: " + browser);
		}

		// Wrap a stale refresher driver around
		driver = new StaleRefresherWebDriver(driver);

		return driver;

	}

	/**
	 * The driver to use for interaction with the browser.
	 */
	private WebDriver mDriver;
	/**
	 * The logger to use for logging.
	 */
	private final ILogger mLogger;

	/**
	 * The main service of the tool.
	 */
	private Service mService;

	/**
	 * The controller of the settings.
	 */
	private final SettingsController mSettingsController;

	/**
	 * Creates a new instance of the service. After creation call
	 * {@link #initialize()} and then {@link #start()}. To end the service call
	 * {@link #shutdown()} or {@link #stop()}.
	 * 
	 */
	public BrainBridge() {
		this.mService = null;
		this.mSettingsController = new SettingsController();
		this.mLogger = LoggerFactory.getLogger();
	}

	/**
	 * Initializes the service. Call this method prior to {@link #start()}.
	 */
	public void initialize() {
		if (this.mLogger.isDebugEnabled()) {
			this.mLogger.logDebug("Initializing BrainBridge");
		}
		this.mSettingsController.initialize();

		// Add shutdown hook for a controlled shutdown when killed
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));
	}

	/**
	 * Shuts the service down and frees all used resources. The object instance
	 * can not be used anymore after calling this method, instead create a new
	 * one. If the service should only get restarted consider using
	 * {@link #stop()} instead of this method.
	 */
	public void shutdown() {
		this.mLogger.flush();
		if (this.mLogger.isDebugEnabled()) {
			this.mLogger.logDebug("Shutting down BrainBridge");
		}
		try {
			stop();
		} catch (final Exception e) {
			this.mLogger.logError("Error while stopping: " + LoggerUtil.getStackTrace(e));
		}

		this.mLogger.logInfo("BrainBridge shutdown");
		this.mLogger.close();
	}

	/**
	 * Starts the service. Prior to this call {@link #initialize()}. To end the
	 * tool call {@link #shutdown()} or {@link #stop()}.
	 */
	public void start() {
		this.mLogger.logInfo("BrainBridge start");

		final Integer portFromSettings = this.mSettingsController.getPort();
		final int port;
		if (portFromSettings == null) {
			port = DEFAULT_PORT;
		} else {
			port = portFromSettings.intValue();
		}
		startService(port, this.mSettingsController);
	}

	/**
	 * Starts the actual main service of the tool. The method tries to catch all
	 * not caught exceptions to ensure a proper shutdown of the tool.
	 * 
	 * @param port
	 *            The port to use for communication
	 * @param browserSettingsProvider
	 *            Object that provides settings about the browser to use for the
	 *            tool
	 */
	public void startService(final int port, final IBrowserSettingsProvider browserSettingsProvider) {
		try {
			this.mLogger.logInfo("Starting service");

			// Set options
			final EBrowser browser = browserSettingsProvider.getBrowser();
			final DesiredCapabilities capabilities = createCapabilities(browserSettingsProvider.getBrowser(),
					browserSettingsProvider.getDriverForBrowser(browser), browserSettingsProvider.getBrowserBinary(),
					browserSettingsProvider.getUserProfile());

			// Create an instance of a web driver
			this.mDriver = createWebDriver(browser, capabilities);

			// Create and start all services
			this.mService = new Service(port, this.mDriver, this);
			this.mService.start();
		} catch (final Exception e) {
			this.mLogger.logError("Error while starting service, shutting down: " + LoggerUtil.getStackTrace(e));
			// Try to shutdown and free all resources
			if (this.mDriver != null) {
				this.mDriver.quit();
			}

			shutdown();
		}
	}

	/**
	 * Stops the service. In contrast to {@link #shutdown()} the service object
	 * can be restarted with {@link #start()} after this method.
	 */
	public void stop() {
		try {
			stopService();
		} catch (final Exception e) {
			this.mLogger.logError("Error while stopping service: " + LoggerUtil.getStackTrace(e));
		}
	}

	/**
	 * Stops the actual main service of the tool if present and active. The
	 * service can not be used anymore after calling this method. Instead
	 * restart the tool by calling {@link #stop()} and {@link #start()}.
	 */
	private void stopService() {
		if (this.mService != null && this.mService.isActive()) {
			try {
				this.mService.stopService();
				// Wait for the service to stop
				this.mService.join();
			} catch (final Exception e) {
				this.mLogger.logError("Error while stopping service: " + LoggerUtil.getStackTrace(e));
			}

			this.mLogger.flush();
		}
	}
}
