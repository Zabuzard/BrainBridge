package de.zabuza.brainbridge.server.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import de.zabuza.brainbridge.server.BrainBridge;
import de.zabuza.brainbridge.server.exceptions.DriverNewWindowUnsupportedException;
import de.zabuza.brainbridge.server.exceptions.UnexpectedUnsupportedEncodingException;
import de.zabuza.brainbridge.server.exceptions.WindowHandleNotFoundException;
import de.zabuza.brainbridge.server.logging.ILogger;
import de.zabuza.brainbridge.server.logging.LoggerFactory;
import de.zabuza.brainbridge.server.logging.LoggerUtil;
import de.zabuza.brainbridge.server.webdriver.IWrapsWebDriver;

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
	 * Time after when an instance is declared as abandoned and will get
	 * automatically shutdown in milliseconds.
	 */
	private final static long ABANDONED_INSTANCE_INTERVAL = 300_000L;
	/**
	 * The pattern which matches the id argument in a request. It can be
	 * accessed by the group 1.
	 */
	private static final String ARGUMENT_ID_PATTERN = "(?:^|.+&)id=([A-Za-z0-9]+)(?:$|&.+)";
	/**
	 * The pattern which matches the message argument in a request. It can be
	 * accessed by the group 1.
	 */
	private static final String ARGUMENT_MESSAGE_PATTERN = "(?:^|.+&)msg=(.+)(?:$|&.+)";
	/**
	 * The keyword which every create request begins with.
	 */
	private static final String CREATE_REQUEST = "/create";
	/**
	 * Time in seconds a driver waits for a page to fully load until throwing a
	 * {@link TimeoutException}.
	 */
	private final static int DRIVER_PAGE_LOAD_TIMEOUT = 3;
	/**
	 * The keyword which every get message request begins with.
	 */
	private static final String GET_MESSAGE_REQUEST = "/get?";
	/**
	 * The pattern which every GET request matches. Group 1 holds the content of
	 * the GET request.
	 */
	private static final String GET_REQUEST_PATTERN = "GET (.+) HTTP/?[\\d\\.]*";
	/**
	 * The maximal amount of instances that can be served at the same time. If
	 * the limit is reached incoming create requests will be rejected.
	 */
	private final static int MAX_INSTANCES = 20;
	/**
	 * The keyword which every post message request begins with.
	 */
	private static final String POST_MESSAGE_REQUEST = "/post?";
	/**
	 * The time in milliseconds to wait for the next iteration of the life
	 * cycle.
	 */
	private final static long SERVICE_INTERVAL = 200L;
	/**
	 * The keyword which every shutdown request begins with.
	 */
	private static final String SHUTDOWN_REQUEST = "/shutdown?";
	/**
	 * The time in milliseconds to wait for a client to connect.
	 */
	private final static int SOCKET_WAIT_TIMEOUT = 10_000;

	/**
	 * The unique window handle for the blank control window.
	 */
	private String mControlWindowHandle;
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
	 * Data-structure that maps ids to their corresponding brain instances.
	 */
	private final Map<String, BrainInstance> mIdToBrainInstance;
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
	 * Set that contains all assigned window handles.
	 */
	private final Set<String> mWindowHandles;

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

		this.mWindowHandles = new HashSet<>();
		this.mIdToBrainInstance = new HashMap<>();
		this.mControlWindowHandle = null;
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

			this.mDriver.manage().timeouts().pageLoadTimeout(DRIVER_PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS);

			this.mControlWindowHandle = this.mDriver.getWindowHandle();
			this.mWindowHandles.add(this.mControlWindowHandle);
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

				// Clean up abandoned instances
				cleanAbandonedInstance();

				// Wait for a client to connect
				try (final Socket clientSocket = this.mServerSocket.accept();
						final BufferedReader br = new BufferedReader(
								new InputStreamReader(clientSocket.getInputStream()))) {
					final InetAddress clientIp = clientSocket.getInetAddress();
					this.mLogger.logInfo("Connected with " + clientIp);

					// Serve the request
					final String request = br.readLine();
					serveRequest(request, clientSocket);
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
	 * Shuts abandoned instances down and removes them from the pool.
	 */
	private void cleanAbandonedInstance() {
		final long timeNow = System.currentTimeMillis();
		final Set<String> idsToRemove = new HashSet<>();
		for (final BrainInstance instance : this.mIdToBrainInstance.values()) {
			if (timeNow - instance.getLastUsage() > ABANDONED_INSTANCE_INTERVAL) {
				// Instance is abandoned
				final String id = instance.getId();
				final String windowHandle = instance.getWindowHandle();

				// Shut it down
				instance.shutdown();
				this.mWindowHandles.remove(windowHandle);
				idsToRemove.add(id);
			}
		}

		for (final String id : idsToRemove) {
			this.mIdToBrainInstance.remove(id);
		}
	}

	/**
	 * Serves the given create request of the given client
	 * 
	 * @param clientSocket
	 *            Client to serve
	 * @throws WindowHandleNotFoundException
	 *             If a window handle could not be found
	 * @throws IOException
	 *             If an I/O-Exception occurs
	 */
	private void serveCreateRequest(final Socket clientSocket) throws WindowHandleNotFoundException, IOException {
		if (this.mLogger.isDebugEnabled()) {
			this.mLogger.logDebug("Serving create request.");
		}

		// Check if limit is reached
		if (this.mIdToBrainInstance.size() >= MAX_INSTANCES) {
			HttpUtil.sendError(EHttpStatus.SERVICE_UNAVAILABLE, clientSocket);
			this.mLogger.logInfo("Rejected create request, limit reached.");
			return;
		}

		// Create a window handle for a new brain instance
		String windowHandle = null;

		// Create a new blank window
		WebDriver rawDriver = this.mDriver;
		while (rawDriver instanceof IWrapsWebDriver) {
			rawDriver = ((IWrapsWebDriver) rawDriver).getRawDriver();
		}

		if (!(rawDriver instanceof JavascriptExecutor)) {
			throw new DriverNewWindowUnsupportedException(rawDriver);
		}
		final JavascriptExecutor executor = (JavascriptExecutor) rawDriver;
		this.mDriver.switchTo().window(this.mControlWindowHandle);
		executor.executeScript("window.open();");

		// Find the window
		for (final String windowHandleCandidate : this.mDriver.getWindowHandles()) {
			if (!this.mWindowHandles.contains(windowHandleCandidate)) {
				windowHandle = windowHandleCandidate;
				this.mWindowHandles.add(windowHandleCandidate);
				break;
			}
		}

		if (windowHandle == null) {
			throw new WindowHandleNotFoundException();
		}

		// Create a new brain instance
		final BrainInstance instance = new BrainInstance(this.mDriver, windowHandle);
		instance.initialize();
		final String id = instance.getId();

		if (id == null) {
			// Instance is invalid, throw it away
			instance.shutdown();
			this.mWindowHandles.remove(windowHandle);

			this.mLogger.logError("Instance can not be created since id is null: " + id);
			HttpUtil.sendError(EHttpStatus.INTERNAL_SERVER_ERROR, clientSocket);
			return;
		}

		this.mIdToBrainInstance.put(id, instance);

		this.mLogger.logInfo("Created instance: " + id);
		HttpUtil.sendHttpAnswer(id, EHttpContentType.TEXT, EHttpStatus.OK, clientSocket);
	}

	/**
	 * Serves the given get message request of the given client.
	 * 
	 * @param requestContent
	 *            The content of the request
	 * @param clientSocket
	 *            The client to serve
	 * @throws IOException
	 *             If an I/O-Exception occurs
	 */
	private void serveGetMessageRequest(final String requestContent, final Socket clientSocket) throws IOException {
		if (this.mLogger.isDebugEnabled()) {
			this.mLogger.logDebug("Serving get message request.");
		}

		// Extract the arguments
		final String arguments = requestContent.substring(GET_MESSAGE_REQUEST.length());

		// Id Argument
		final Pattern idPattern = Pattern.compile(ARGUMENT_ID_PATTERN);
		final Matcher idMatcher = idPattern.matcher(arguments);
		if (!idMatcher.matches()) {
			HttpUtil.sendError(EHttpStatus.BAD_REQUEST, clientSocket);
			return;
		}
		final String id = idMatcher.group(1);

		if (!this.mIdToBrainInstance.containsKey(id)) {
			HttpUtil.sendError(EHttpStatus.UNPROCESSABLE_ENTITY, clientSocket);
			return;
		}

		// Get the brain instance corresponding to the requested id
		final BrainInstance instance = this.mIdToBrainInstance.get(id);
		String latestAnswer = instance.getLatestAnswer();
		if (latestAnswer == null) {
			this.mLogger.logInfo("Get for " + id + " has returned no answer.");
			HttpUtil.sendHttpAnswer(EHttpContentType.TEXT, EHttpStatus.NO_CONTENT, clientSocket);
			return;
		}

		this.mLogger.logInfo("Get for " + id + ": " + latestAnswer);
		HttpUtil.sendHttpAnswer(latestAnswer, EHttpContentType.TEXT, EHttpStatus.OK, clientSocket);
	}

	/**
	 * Serves the given post message request of the given client.
	 * 
	 * @param requestContent
	 *            The content of the request
	 * @param clientSocket
	 *            The client to serve
	 * @throws IOException
	 *             If an I/O-Exception occurs
	 */
	private void servePostMessageRequest(final String requestContent, final Socket clientSocket) throws IOException {
		if (this.mLogger.isDebugEnabled()) {
			this.mLogger.logDebug("Serving post message request.");
		}

		// Extract the arguments
		final String arguments = requestContent.substring(POST_MESSAGE_REQUEST.length());

		// Id Argument
		final Pattern idPattern = Pattern.compile(ARGUMENT_ID_PATTERN);
		final Matcher idMatcher = idPattern.matcher(arguments);
		if (!idMatcher.matches()) {
			HttpUtil.sendError(EHttpStatus.BAD_REQUEST, clientSocket);
			return;
		}
		final String id = idMatcher.group(1);

		// Message argument
		final Pattern messagePattern = Pattern.compile(ARGUMENT_MESSAGE_PATTERN);
		final Matcher messageMatcher = messagePattern.matcher(arguments);
		if (!messageMatcher.matches()) {
			HttpUtil.sendError(EHttpStatus.BAD_REQUEST, clientSocket);
			return;
		}
		final String message = HttpUtil.decodeUrlToUtf8(messageMatcher.group(1));

		if (!this.mIdToBrainInstance.containsKey(id)) {
			HttpUtil.sendError(EHttpStatus.UNPROCESSABLE_ENTITY, clientSocket);
			return;
		}

		// Get the brain instance corresponding to the requested id
		final BrainInstance instance = this.mIdToBrainInstance.get(id);
		instance.postMessage(message);

		this.mLogger.logInfo("Post for " + id + ": " + message);

		HttpUtil.sendHttpAnswer(EHttpContentType.TEXT, EHttpStatus.NO_CONTENT, clientSocket);
	}

	/**
	 * Serves the given request of the given client.
	 * 
	 * @param request
	 *            The request to serve
	 * @param clientSocket
	 *            The client to serve
	 * @throws IOException
	 *             If an I/O-Exception occurs
	 * @throws WindowHandleNotFoundException
	 *             If a window handle could not be found
	 */
	private void serveRequest(final String request, final Socket clientSocket) throws IOException {
		try {
			// Reject the request if empty
			if (request == null || request.trim().length() <= 0) {
				HttpUtil.sendError(EHttpStatus.BAD_REQUEST, clientSocket);
				return;
			}

			// Reject the request if not a GET request
			final Pattern requestPattern = Pattern.compile(GET_REQUEST_PATTERN);
			final Matcher requestMatcher = requestPattern.matcher(request);
			if (!requestMatcher.matches()) {
				HttpUtil.sendError(EHttpStatus.BAD_REQUEST, clientSocket);
				return;
			}

			// Strip the content of the GET request
			final String requestContent = requestMatcher.group(1);

			// Serve create requests
			final boolean isCreateRequest = requestContent.startsWith(CREATE_REQUEST);
			if (isCreateRequest) {
				serveCreateRequest(clientSocket);
				return;
			}

			// Serve post message requests
			final boolean isPostMessageRequest = requestContent.startsWith(POST_MESSAGE_REQUEST);
			if (isPostMessageRequest) {
				servePostMessageRequest(requestContent, clientSocket);
				return;
			}

			// Serve get message requests
			final boolean isGetMessageRequest = requestContent.startsWith(GET_MESSAGE_REQUEST);
			if (isGetMessageRequest) {
				serveGetMessageRequest(requestContent, clientSocket);
				return;
			}

			// Serve shutdown requests
			final boolean isShutdownRequest = requestContent.startsWith(SHUTDOWN_REQUEST);
			if (isShutdownRequest) {
				serveShutdownRequest(requestContent, clientSocket);
				return;
			}

			// Request type not supported
			HttpUtil.sendError(EHttpStatus.NOT_IMPLEMENTED, clientSocket);
		} catch (final WindowHandleNotFoundException | StaleElementReferenceException | TimeoutException
				| NoSuchElementException | NoSuchFrameException | UnexpectedUnsupportedEncodingException e) {
			// Log the error and reject the request
			this.mLogger.logError("Server error while serving request: " + LoggerUtil.getStackTrace(e));
			HttpUtil.sendError(EHttpStatus.INTERNAL_SERVER_ERROR, clientSocket);
		}
	}

	/**
	 * Serves the given shutdown request of the given client.
	 * 
	 * @param requestContent
	 *            The content of the request
	 * @param clientSocket
	 *            The client to serve
	 * @throws IOException
	 *             If an I/O-Exception occurs
	 */
	private void serveShutdownRequest(final String requestContent, final Socket clientSocket) throws IOException {
		if (this.mLogger.isDebugEnabled()) {
			this.mLogger.logDebug("Serving shutdown request.");
		}

		// Extract the arguments
		final String arguments = requestContent.substring(SHUTDOWN_REQUEST.length());

		// Id Argument
		final Pattern idPattern = Pattern.compile(ARGUMENT_ID_PATTERN);
		final Matcher idMatcher = idPattern.matcher(arguments);
		if (!idMatcher.matches()) {
			HttpUtil.sendError(EHttpStatus.BAD_REQUEST, clientSocket);
			return;
		}
		final String id = idMatcher.group(1);

		if (!this.mIdToBrainInstance.containsKey(id)) {
			HttpUtil.sendError(EHttpStatus.UNPROCESSABLE_ENTITY, clientSocket);
			return;
		}

		// Get the brain instance corresponding to the requested id
		final BrainInstance instance = this.mIdToBrainInstance.get(id);
		final String windowHandle = instance.getWindowHandle();

		instance.shutdown();
		this.mWindowHandles.remove(windowHandle);
		this.mIdToBrainInstance.remove(id);

		this.mLogger.logInfo("Shutdown instance: " + id);

		HttpUtil.sendHttpAnswer(EHttpContentType.TEXT, EHttpStatus.NO_CONTENT, clientSocket);
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

	/**
	 * Waits a given time before executing the next iteration of the services
	 * life cycle.
	 */
	private void waitToNextIteration() {
		try {
			sleep(SERVICE_INTERVAL);
		} catch (final InterruptedException e) {
			// Log the error but continue
			this.mLogger.logError("Service wait got interrupted: " + LoggerUtil.getStackTrace(e));
		}
	}
}
