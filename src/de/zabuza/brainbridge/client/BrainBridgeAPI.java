package de.zabuza.brainbridge.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import de.zabuza.brainbridge.client.exceptions.UnexpectedUnsupportedEncodingException;

/**
 * Client-side API for the BrainBridge service. It interacts with the
 * server-side API.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public final class BrainBridgeAPI {
	/**
	 * The keyword which every create request begins with.
	 */
	private static final String CREATE_REQUEST = "/create";
	/**
	 * The keyword which every get message request begins with.
	 */
	private static final String GET_MESSAGE_REQUEST = "/get?";
	/**
	 * The name of the id parameter.
	 */
	private static final String ID_PARAMETER = "id=";
	/**
	 * The name of the message parameter.
	 */
	private static final String MESSAGE_PARAMETER = "msg=";
	/**
	 * Value that separates parameters in a query.
	 */
	private static final String PARAMETER_SEPARATOR = "&";
	/**
	 * The keyword which every post message request begins with.
	 */
	private static final String POST_MESSAGE_REQUEST = "/post?";
	/**
	 * The keyword which every shutdown request begins with.
	 */
	private static final String SHUTDOWN_REQUEST = "/shutdown?";
	/**
	 * The charset to use for encoding and decoding text.
	 */
	private static final Charset TEXT_CHARSET = StandardCharsets.UTF_8;
	/**
	 * Delimiter between an URL and a port.
	 */
	private static final String URL_PORT_DELIMITER = ":";

	/**
	 * Gets the content of a web site and returns it as list of lines.
	 * 
	 * @param url
	 *            URL pointing to the web site
	 * @return List of lines from the content
	 * @throws IOException
	 *             If an I/O-Exception occurs
	 */
	private static List<String> getWebContent(final String url) throws IOException {
		try (final BufferedReader site = new BufferedReader(
				new InputStreamReader(new URL(url).openStream(), TEXT_CHARSET));) {
			final List<String> content = new LinkedList<>();
			while (site.ready()) {
				final String line = site.readLine();
				if (line == null) {
					break;
				}
				content.add(line);
			}
			return content;
		}
	}

	/**
	 * The full URL to the service that runs the server.
	 */
	private final String mServiceUrl;

	/**
	 * Creates a new client-side BrainBridge API which interacts with the
	 * server-side API at the given address and port.
	 * 
	 * @param serverAddress
	 *            The full address to the server that offers the BrainBridge API
	 *            including protocol. For example
	 *            <tt>http://www.example.org</tt>.
	 * @param port
	 *            The port at the server address that offers the BrainBridge API
	 */
	public BrainBridgeAPI(final String serverAddress, final int port) {
		this.mServiceUrl = serverAddress + URL_PORT_DELIMITER + port;
	}

	/**
	 * Creates a new chat instance.
	 * 
	 * @return The unique id that identifies the created instance
	 */
	public String createInstance() {
		final String query = this.mServiceUrl + CREATE_REQUEST;
		try {
			final List<String> content = getWebContent(query);
			// If the answer is empty there was some error
			if (content.isEmpty()) {
				return null;
			}

			// The first line contains the id
			final String firstLine = content.iterator().next();
			if (firstLine.trim().isEmpty()) {
				return null;
			}
			return StringEscapeUtils.unescapeHtml4(firstLine);
		} catch (final IOException e) {
			// Ignore the exception and return null
			return null;
		}
	}

	/**
	 * Gets the last answer of the chat bot for the instance with the given id.
	 * 
	 * @param id
	 *            The id of the instance
	 * @return The last answer of the chat bot for the given instance or
	 *         <tt>null</tt> if there was no or the server experienced an error
	 */
	public String getLastMessage(final String id) {
		final String query = this.mServiceUrl + GET_MESSAGE_REQUEST + ID_PARAMETER + id;
		try {
			final List<String> content = getWebContent(query);
			// If the answer is empty there was some error
			if (content.isEmpty()) {
				return null;
			}

			// The first line contains the message
			final String firstLine = content.iterator().next();
			if (firstLine.trim().isEmpty()) {
				return null;
			}
			return StringEscapeUtils.unescapeHtml4(firstLine);
		} catch (final IOException e) {
			// Ignore the exception and return null
			return null;
		}
	}

	/**
	 * Posts the given message for the instance with the given id.
	 * 
	 * @param id
	 *            The id of the instance to post with
	 * @param message
	 *            The message to post
	 */
	public void postMessage(final String id, final String message) {
		String encodedMessage;
		try {
			encodedMessage = URLEncoder.encode(message, TEXT_CHARSET.name());
		} catch (final UnsupportedEncodingException e) {
			// Re-throw new exception
			throw new UnexpectedUnsupportedEncodingException(e);
		}
		final String query = this.mServiceUrl + POST_MESSAGE_REQUEST + ID_PARAMETER + id + PARAMETER_SEPARATOR
				+ MESSAGE_PARAMETER + encodedMessage;
		try {
			// Simply open the web page, we are not interested in its content
			getWebContent(query);
		} catch (final IOException e) {
			// Just ignore the exception and continue
		}
	}

	/**
	 * Shuts the instance with the given id down.
	 * 
	 * @param id
	 *            The id of the instance to shutdown
	 */
	public void shutdownInstance(final String id) {
		final String query = this.mServiceUrl + SHUTDOWN_REQUEST + ID_PARAMETER + id;
		try {
			// Simply open the web page, we are not interested in its content
			getWebContent(query);
		} catch (final IOException e) {
			// Just ignore the exception and continue
		}
	}
}
