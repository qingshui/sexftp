package org.sexftp.core.exceptions;

public class AbortException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AbortException() {
	}

	public AbortException(String message, Throwable cause) {
		super(message, cause);
	}

	public AbortException(String message) {
		super(message);
	}

	public AbortException(Throwable cause) {
		super(cause);
	}
}