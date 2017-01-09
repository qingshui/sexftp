package org.sexftp.core.exceptions;

public class SFConnectionException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SFConnectionException() {
	}

	public SFConnectionException(String message) {
		super(message);
	}

	public SFConnectionException(Throwable cause) {
		super(cause);
	}

	public SFConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
