package org.sexftp.core.exceptions;

public class FtpNoSuchFileException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FtpNoSuchFileException() {
	}

	public FtpNoSuchFileException(String message) {
		super(message);
	}

	public FtpNoSuchFileException(Throwable cause) {
		super(cause);
	}

	public FtpNoSuchFileException(String message, Throwable cause) {
		super(message, cause);
	}
}
