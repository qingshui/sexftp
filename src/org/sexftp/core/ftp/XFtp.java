package org.sexftp.core.ftp;

import java.util.List;
import org.sexftp.core.ftp.bean.FtpFile;
import sexftp.views.IFtpStreamMonitor;

public abstract interface XFtp {
	public abstract void prepareConnect(String paramString1, int paramInt, String paramString2, String paramString3,
			String paramString4);

	public abstract void connect();

	public abstract void disconnect();

	public abstract void upload(String paramString, IFtpStreamMonitor paramIFtpStreamMonitor);

	public abstract void cdOrMakeIfNotExists(String paramString);

	public abstract void download(String paramString1, String paramString2, IFtpStreamMonitor paramIFtpStreamMonitor);

	public abstract void cd(String paramString);

	public abstract void delete(String paramString);

	public abstract List<FtpFile> listFiles();

	public abstract boolean isConnect();
}