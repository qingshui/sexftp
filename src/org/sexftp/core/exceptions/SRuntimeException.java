package org.sexftp.core.exceptions;

public class SRuntimeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SRuntimeException() {
	}

	public SRuntimeException(String message) {
		super(message);
	}

	public SRuntimeException(Throwable cause) {
		super(cause);
	}

	public SRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
