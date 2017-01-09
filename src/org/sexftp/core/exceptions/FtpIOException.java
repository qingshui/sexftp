package org.sexftp.core.exceptions;

public class FtpIOException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FtpIOException() {
	}

	public FtpIOException(String message) {
		super(message);
	}

	public FtpIOException(Throwable cause) {
		super(cause);
	}

	public FtpIOException(String message, Throwable cause) {
		super(message, cause);
	}
}