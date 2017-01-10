package org.sexftp.core.exceptions;

public class FtpCDFailedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FtpCDFailedException() {
	}

	public FtpCDFailedException(String message) {
		super(message);
	}

	public FtpCDFailedException(Throwable cause) {
		super(cause);
	}

	public FtpCDFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}