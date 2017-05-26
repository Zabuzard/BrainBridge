package de.zabuza.brainbridge.server.service;

/**
 * Enumeration of valid HTTP/1.0 states.
 * 
 * @author Zabuza {@literal <zabuza.dev@gmail.com>}
 *
 */
public enum EHttpStatus {
	/**
	 * The request was in a wrong format.
	 */
	BAD_REQUEST,
	/**
	 * If the requested resource is not allowed to get accessed.
	 */
	FORBIDDEN,
	/**
	 * An unexpected server error occurred.
	 */
	INTERNAL_SERVER_ERROR,
	/**
	 * The request was processed successfully but the response does not contain
	 * any content.
	 */
	NO_CONTENT,
	/**
	 * If the requested resource could not be found.
	 */
	NOT_FOUND,
	/**
	 * The functionality to serve the given request is not supported by the
	 * server.
	 */
	NOT_IMPLEMENTED,
	/**
	 * If everything was valid and went okay.
	 */
	OK,
	/**
	 * If the service is currently not available. The client should try it later
	 * again.
	 */
	SERVICE_UNAVAILABLE,
	/**
	 * If a request could not be served because it contains invalid data though
	 * it is in a correct format.
	 */
	UNPROCESSABLE_ENTITY
}
