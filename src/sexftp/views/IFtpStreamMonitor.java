package sexftp.views;

import org.sexftp.core.ftp.bean.FtpUploadConf;

public abstract interface IFtpStreamMonitor {
	public abstract void printSimple(String paramString);

	public abstract void printStreamString(FtpUploadConf paramFtpUploadConf, long paramLong1, long paramLong2,
			String paramString);
}