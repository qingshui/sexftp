package org.sexftp.core.ftp.bean;

public class FtpDownloadPro {
	private FtpUploadConf ftpUploadConf;
	private FtpConf ftpConf;
	private FtpFile ftpfile;

	public FtpDownloadPro(FtpUploadConf ftpUploadConf, FtpConf ftpConf, FtpFile ftpfile) {
		this.ftpUploadConf = ftpUploadConf;
		this.ftpConf = ftpConf;
		this.ftpfile = ftpfile;
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

	public FtpFile getFtpfile() {
		return this.ftpfile;
	}

	public void setFtpfile(FtpFile ftpfile) {
		this.ftpfile = ftpfile;
	}

	public String toString() {
		return this.ftpUploadConf.toString();
	}
}