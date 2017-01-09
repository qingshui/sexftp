package org.sexftp.core.ftp;

import java.io.File;
import java.net.SocketTimeoutException;
import java.util.List;
import org.sexftp.core.exceptions.NeedRetryException;
import org.sexftp.core.ftp.bean.FtpFile;
import sexftp.views.AbstractSexftpView;
import sexftp.views.IFtpStreamMonitor;

public class MyftpProxy implements XFtp {
	//private static final int RETRY_TIMES = 8;
	private XFtp xftp;
	private Consoleable console;
	private String curDir = null;

	private String connectstr = "";
	private String host;
	private int port;
	private String username;
	//private String password;
	//private String encode = "gbk";

	public boolean isCanTryException(Exception ex) {
		if ("User Disconnected".equals(this.connectstr)) {
			return false;
		}

		Throwable ce = ex;
		for (int i = 0; i < 5; ++i) {
			if (ce instanceof SocketTimeoutException) {
				return true;
			}
			if (ce instanceof NeedRetryException) {
				return true;
			}
			if ((ce.getMessage() != null) && (ce.getMessage().toLowerCase().indexOf("timeout") >= 0)) {
				return true;
			}
			ce = ce.getCause();
			if (ce == null) {
				break;
			}

		}

		return false;
	}

	public MyftpProxy(XFtp xftp, Consoleable console) {
		this.xftp = xftp;
		this.console = console;
	}

	public void cd(String directory) {
		RuntimeException er = null;
		for (int i = 0; i < 8; ++i) {
			try {
				this.xftp.cd(directory);
				this.curDir = directory;
				return;
			} catch (RuntimeException e) {
				if (isCanTryException(e)) {
					er = e;
				} else
					throw e;
			}
			if (er != null) {
				this.console.console("cd " + directory + " failed!" + er.getMessage() + " try " + i + " again...");
			}

			if (!reconnect(i + 1 < 8, null))
				continue;
			i = 0;
		}

		if (er != null)
			throw er;
	}

	public void cdOrMakeIfNotExists(String directory) {
		RuntimeException er = null;
		for (int i = 0; i < 8; ++i) {
			try {
				this.xftp.cdOrMakeIfNotExists(directory);
				this.curDir = directory;
				return;
			} catch (RuntimeException e) {
				if (isCanTryException(e)) {
					er = e;
				} else
					throw e;
			}
			if (er != null) {
				this.console.console("cd " + directory + " failed!" + er.getMessage() + " try again...");
			}

			if (!reconnect(i + 1 < 8, null))
				continue;
			i = 0;
		}

		if (er != null)
			throw er;
	}

	public void connect() {
		this.connectstr = "User connecte";
		RuntimeException er = null;
		for (int i = 0; i < 8; ++i) {
			try {
				this.xftp.connect();
				finallyed();

				return;
			} catch (RuntimeException e) {
				if (isCanTryException(e)) {
					er = e;
				} else
					throw e;
			}
			if (er != null) {
				this.console.console("connect failed!" + er.getMessage() + " try " + i + " again...");
			}

			if ((i + 1 >= 8) && (this.console instanceof AbstractSexftpView)) {
				AbstractSexftpView view = (AbstractSexftpView) this.console;
				if (view.showQuestion("Server Operation Timeout,Retry [8] Times,Retry Again?")) {
					i = 0;
				}

			}

			finallyed();
		}
		if (er != null)
			throw er;
	}

	public void delete(String deleteFile) {
		this.xftp.delete(deleteFile);
	}

	public void disconnect() {
		this.xftp.disconnect();
		finallyed();
		this.connectstr = "User Disconnected";
	}

	public void download(String downloadFile, String saveFile, IFtpStreamMonitor monitor) {
		String curDir = this.curDir;
		RuntimeException er = null;
		for (int i = 0; i < 8; ++i) {
			try {
				File sf = new File(saveFile);
				if (!sf.getParentFile().exists()) {
					sf.getParentFile().mkdirs();
				}
				this.xftp.download(downloadFile, saveFile, monitor);
				return;
			} catch (RuntimeException e) {
				if (isCanTryException(e)) {
					er = e;
				} else
					throw e;
			}
			if (er != null) {
				this.console
						.console("download " + downloadFile + " failed!" + er.getMessage() + " try " + i + " again...");
			}

			if (!reconnect(i + 1 < 8, curDir))
				continue;
			i = 0;
		}

		if (er != null)
			throw er;
	}

	public boolean isConnect() {
		boolean isConnect = this.xftp.isConnect();
		if (!isConnect)
			finallyed();
		return isConnect;
	}

	public List<FtpFile> listFiles() {
		String curDir = this.curDir;
		RuntimeException er = null;
		for (int i = 0; i < 8; ++i) {
			try {
				return this.xftp.listFiles();
			} catch (RuntimeException e) {
				if (isCanTryException(e)) {
					er = e;
				} else
					throw e;
			}
			if (er != null) {
				this.console
						.console("listFiles Of " + curDir + " failed!" + er.getMessage() + " try again " + i + "...");
			}

			if (!reconnect(i + 1 < 8, curDir))
				continue;
			i = 0;
		}

		if (er != null) {
			throw er;
		}
		return null;
	}

	public void prepareConnect(String host, int port, String username, String password, String encode) {
		this.host = host;
		this.port = port;
		//this.password = password;
		this.username = username;
		//this.encode = ((encode != null) ? encode : "gbk");
		this.xftp.prepareConnect(host, port, username, password, encode);
	}

	public void upload(String uploadFile, IFtpStreamMonitor monitor) {
		String curDir = this.curDir;
		RuntimeException er = null;
		for (int i = 0; i < 8; ++i) {
			try {
				this.xftp.upload(uploadFile, monitor);
				return;
			} catch (RuntimeException e) {
				if (isCanTryException(e)) {
					er = e;
				} else
					throw e;
			}
			if (er != null) {
				this.console.console("upload " + uploadFile + " failed!" + er.getMessage() + " try " + i + " again...");
			}

			if (!reconnect(i + 1 < 8, curDir))
				continue;
			i = 0;
		}

		if (er != null)
			throw er;
	}

	private void finallyed() {
		this.curDir = null;
	}

	private boolean reconnect(boolean isok, String curdir) {
		boolean retryAgain = false;
		this.xftp.disconnect();
		if ((!isok) && (this.console instanceof AbstractSexftpView)) {
			AbstractSexftpView view = (AbstractSexftpView) this.console;
			if (view.showQuestion("Server Operation Timeout,Retry [8] Times,Retry Again?")) {
				isok = true;
				retryAgain = true;
			}

		}

		if (isok) {
			connect();
			if (curdir != null) {
				cd(curdir);
			}
		}
		return retryAgain;
	}

	public String toString() {
		return String.format("MyftpProxy [curDir=%s, host=%s, port=%s, username=%s, xftp=%s]",
				new Object[] { this.curDir, this.host, Integer.valueOf(this.port), this.username, this.xftp });
	}
}