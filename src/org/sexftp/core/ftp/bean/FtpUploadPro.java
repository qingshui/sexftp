package org.sexftp.core.ftp.bean;

public class FtpUploadPro {
	private FtpUploadConf ftpUploadConf;
	private FtpConf ftpConf;

	public FtpUploadPro(FtpUploadConf ftpUploadConf, FtpConf ftpConf) {
		this.ftpUploadConf = ftpUploadConf;
		this.ftpConf = ftpConf;
	}

	public FtpUploadConf getFtpUploadConf() {
		return this.ftpUploadConf;
	}

	public void setFtpUploadConf(FtpUploadConf ftpUploadConf) {
		this.ftpUploadConf = ftpUploadConf;
	}

	public FtpConf getFtpConf() {
		return this.ftpConf;
	}

	public void setFtpConf(FtpConf ftpConf) {
		this.ftpConf = ftpConf;
	}

	public String toString() {
		return this.ftpUploadConf.toString();
	}
}
