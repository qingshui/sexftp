package sexftp.views;

import org.sexftp.core.ftp.bean.FtpDownloadPro;

public abstract interface MySexftpServerDownload {
	public abstract void afterDownload(final FtpDownloadPro paramFtpDownloadPro) throws Exception;

	public abstract boolean exceptionNotExits(final FtpDownloadPro paramFtpDownloadPro) throws Exception;

	public abstract String trustFolder(final FtpDownloadPro paramFtpDownloadPro) throws Exception;
}