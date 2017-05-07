package de.zabuza.brainbridge.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.openqa.selenium.WebDriver;

import de.zabuza.brainbridge.BrainBridge;
import de.zabuza.brainbridge.logging.ILogger;
import de.zabuza.brainbridge.logging.LoggerFactory;
import de.zabuza.brainbridge.logging.LoggerUtil;

/**
 * Actual service thread of the tool. Call {@link #start()} to start the service
 * and {@link #stopService()} to stop it. If the service leaves its life cycle
 * abnormally it will request the parent tool to also shutdown.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class Service extends Thread {
	/**
	 * The time in milliseconds to wait for the next iteration of the life
	 * cycle.
	 */
	private final static long SERVICE_INTERVAL = 200;
	/**
	 * The time in milliseconds to wait for a client to connect.
	 */
	private final static int SOCKET_WAIT_TIMEOUT = 10_000;
	/**
	 * Internal flag whether the service should run or not. If set to
	 * <tt>false</tt> the service will not enter the next iteration of its life
	 * cycle and shutdown.
	 */
	private boolean mDoRun;
	/**
	 * The driver to use for accessing browsers contents.
	 */
	private final WebDriver mDriver;
	/**
	 * The logger to use for logging.
	 */
	private final ILogger mLogger;
	/**
	 * The parent object that controls the service. If the service shuts down it
	 * will request its parent to also shutdown.
	 */
	private final BrainBridge mParent;
	/**
	 * The port to use for communication.
	 */
	private final int mPort;
	/**
	 * The server socket used to listen for requests.
	 */
	private ServerSocket mServerSocket;
	/**
	 * Whether the service should stop or not. If set to <tt>true</tt> the
	 * service will try to leave its life cycle in a normal way and shutdown.
	 */
	private boolean mShouldStopService;

	/**
	 * Creates a new Service instance. Call {@link #start()} to start the
	 * service and {@link #stopService()} to stop it.
	 * 
	 * @param port
	 *            The port to use for communication
	 * @param driver
	 *            The driver to use for accessing browsers contents
	 * @param parent
	 *            The parent object that controls the service. If the service
	 *            shuts down in an abnormal way it will request its parent to
	 *            also shutdown.
	 */
	public Service(final int port, final WebDriver driver, final BrainBridge parent) {
		this.mPort = port;
		this.mDriver = driver;
		this.mParent = parent;
		this.mServerSocket = null;
		this.mLogger = LoggerFactory.getLogger();

		this.mDoRun = true;
		this.mShouldStopService = false;
	}

	/**
	 * Whether the service is alive and running.
	 * 
	 * @return <tt>True</tt> if the service is alive and running, <tt>false</tt>
	 *         otherwise
	 */
	public boolean isActive() {
		return this.mDoRun;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		boolean terminateParent = false;
		try {
			this.mServerSocket = new ServerSocket(this.mPort);
			this.mServerSocket.setSoTimeout(SOCKET_WAIT_TIMEOUT);
		} catch (final Exception e) {
			// Do not enter the service loop
			this.mLogger.logError("Error while starting service, not entering: " + LoggerUtil.getStackTrace(e));
			this.mDoRun = false;
			terminateParent = true;
		}

		// Enter the life cycle
		while (this.mDoRun) {
			try {
				if (this.mShouldStopService) {
					this.mDoRun = false;
				}

				// Wait for a client to connect
				try (final Socket clientSocket = this.mServerSocket.accept();
						final BufferedReader br = new BufferedReader(
								new InputStreamReader(clientSocket.getInputStream()))) {
					final InetAddress clientIp = clientSocket.getInetAddress();
					this.mLogger.logInfo("Connected with " + clientIp);
					final String request = br.readLine();

					// TODO Serve request
				} catch (final SocketTimeoutException e) {
					// Ignore the exception and continue with the next iteration
				} catch (final IOException e) {
					// Log the error but continue
					this.mLogger.logError("I/O error with client: " + LoggerUtil.getStackTrace(e));
				}

				// Delay the next iteration
				waitToNextIteration();
			} catch (final Exception e) {
				this.mLogger.logError("Error while running service, shutting down: " + LoggerUtil.getStackTrace(e));
				// Try to shutdown
				this.mDoRun = false;
				terminateParent = true;
			}
		}

		// If the service is leaved shut it down
		shutdown();

		// Request parent to terminate
		if (terminateParent) {
			this.mParent.shutdown();
		}
	}

	/**
	 * Requests the service to stop. It will try to end its life cycle in a
	 * normal way and shutdown.
	 */
	public void stopService() {
		this.mShouldStopService = true;
	}

	/**
	 * Waits a given time before executing the next iteration of the services
	 * life cycle.
	 */
	public void waitToNextIteration() {
		try {
			sleep(SERVICE_INTERVAL);
		} catch (final InterruptedException e) {
			// Log the error but continue
			this.mLogger.logError("Service wait got interrupted: " + LoggerUtil.getStackTrace(e));
		}
	}

	/**
	 * Shuts the service down. Afterwards this instance can not be used anymore,
	 * instead create a new one.
	 */
	private void shutdown() {
		this.mLogger.logInfo("Shutting down service");
		try {
			this.mDriver.quit();
		} catch (final Exception e) {
			// Log the error but continue
			this.mLogger.logError("Error while shutting down driver: " + LoggerUtil.getStackTrace(e));
		}
	}
}
