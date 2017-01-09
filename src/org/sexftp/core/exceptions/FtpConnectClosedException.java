package org.sexftp.core.exceptions;

public class FtpConnectClosedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FtpConnectClosedException() {
	}

	public FtpConnectClosedException(String message) {
		super(message);
	}

	public FtpConnectClosedException(Throwable cause) {
		super(cause);
	}

	public FtpConnectClosedException(String message, Throwable cause) {
		super(message, cause);
	}
}