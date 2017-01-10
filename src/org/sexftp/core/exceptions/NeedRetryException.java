package org.sexftp.core.exceptions;

public class NeedRetryException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NeedRetryException() {
	}

	public NeedRetryException(String message, Throwable cause) {
		super(message, cause);
	}

	public NeedRetryException(String message) {
		super(message);
	}

	public NeedRetryException(Throwable cause) {
		super(cause);
	}
}
