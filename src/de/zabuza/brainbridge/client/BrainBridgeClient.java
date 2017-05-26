package de.zabuza.brainbridge.client;

/**
 * A client implementation that uses the BrainBridge server API.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class BrainBridgeClient {
	/**
	 * Demonstrates the usage of the client. Make sure that the server is
	 * started.
	 * 
	 * @param args
	 *            Not supported
	 */
	public static void main(final String[] args) {
		System.out.println("Starting");
		final int port = 8110;
		final String serverAddress = "http://www.example.org";

		final BrainBridgeClient client = new BrainBridgeClient(serverAddress, port);

		final BrainInstance instance = client.createInstance();
		if (instance == null) {
			System.out.println("Is the server running?");
			System.out.println("Shutdown");
			return;
		}

		System.out.println(instance.post("hallo brain"));
		System.out.println(instance.post("wie geht es dir?"));
		System.out.println(instance.post("okay, bin wieder weg"));
		instance.shutdown();

		System.out.println("Shutdown");
	}

	/**
	 * The API to use for interaction with the server.
	 */
	private final BrainBridgeAPI mApi;
	/**
	 * The port at the server address that offers the BrainBridge API.
	 */
	private final int mPort;
	/**
	 * The full address to the server that offers the BrainBridge API including
	 * protocol. For example <tt>http://www.example.org</tt>.
	 */
	private final String mServerAddress;

	/**
	 * Creates a new brain bridge client with given server address and port.
	 * 
	 * @param serverAddress
	 *            The full address to the server that offers the BrainBridge API
	 *            including protocol. For example
	 *            <tt>http://www.example.org</tt>.
	 * @param port
	 *            The port at the server address that offers the BrainBridge API
	 */
	public BrainBridgeClient(final String serverAddress, final int port) {
		this.mServerAddress = serverAddress;
		this.mPort = port;
		this.mApi = new BrainBridgeAPI(this.mServerAddress, this.mPort);
	}

	/**
	 * Creates a new instance that offers chat interaction with the BrainBridge
	 * API.
	 * 
	 * @return The created instance or <tt>null</tt> if that was not possible,
	 *         for example if the server was not reachable or experienced an
	 *         error.
	 */
	public BrainInstance createInstance() {
		final String id = this.mApi.createInstance();
		// There was some error
		if (id == null) {
			return null;
		}

		return new BrainInstance(id, this.mApi);
	}
}
