package org.sexftp.core.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.sexftp.core.exceptions.AbortException;
import org.sexftp.core.exceptions.FtpCDFailedException;
import org.sexftp.core.exceptions.FtpConnectClosedException;
import org.sexftp.core.exceptions.FtpIOException;
import org.sexftp.core.exceptions.FtpNoSuchFileException;
import org.sexftp.core.exceptions.NeedRetryException;
import org.sexftp.core.ftp.bean.FtpFile;
import org.sexftp.core.utils.StringUtil;
import sexftp.uils.FtpExchangeOutputStream;
import sexftp.uils.LogUtil;
import sexftp.uils.PluginUtil;
import sexftp.views.IFtpStreamMonitor;

public class MyFtp implements XFtp {
	FtpExchangeOutputStream ftpExchangeOutputStream = null;
	private FTPClient ftpclient = null;
	private String host;
	private int port;
	private String username;
	private String password;
	private String encode = "gbk";

	public MyFtp() {
		this.ftpclient = myInstance();
	}

	protected FTPClient myInstance() {
		return new FTPClient();
	}

	public void cd(String directory) {
		try {
			this.ftpExchangeOutputStream.setListenStr(new StringBuffer());
			boolean isOk = this.ftpclient.changeWorkingDirectory(iso88591(directory));
			if (isOk)
				return;
			String reseason = this.ftpExchangeOutputStream.getListenStr().toString();
			this.ftpExchangeOutputStream.setListenStr(null);
			if (reseason.toLowerCase().indexOf("No such file or directory".toLowerCase()) >= 0) {
				throw new FtpNoSuchFileException(reseason);
			}
			if (reseason.toLowerCase().indexOf("550".toLowerCase()) > 0) {
				throw new FtpNoSuchFileException(reseason);
			}

			throw new FtpCDFailedException(directory + " change failed." + reseason);
		} catch (FTPConnectionClosedException e) {
			throw new FtpConnectClosedException(e.getMessage(), e);
		} catch (IOException e) {
			throw new FtpIOException("cd " + directory + " error!" + e.getMessage(), e);
		}
	}

	public void cdOrMakeIfNotExists(String directory) {
		try {
			if (directory.length() == 0)
				return;
			cd(directory);
		} catch (FtpNoSuchFileException localFtpNoSuchFileException) {
			try {
				mkdirs(directory);
			} catch (IOException e1) {
				throw new FtpIOException("mkdirs " + directory + " fail!" + e1, e1);
			}
		}
	}

	private void mkdirs(String directory) throws IOException {
		String[] dirs = directory.split("/");

		List<String> plist = new ArrayList<String>();
		for (int i = 0; i < dirs.length; ++i) {
			String path = "/";
			for (int j = 0; j <= i; ++j) {
				if (dirs[j].length() != 0)
					path = path + dirs[j] + "/";
			}
			plist.add(path);
		}

		int i = 0;
		for (i = plist.size() - 1; i >= 0; --i) {
			try {
				cd((String) plist.get(i));
			} catch (FtpNoSuchFileException localFtpNoSuchFileException) {
			}
		}

		++i;
		for (; i < plist.size(); ++i) {
			String dir = (String) plist.get(i);
			this.ftpclient.makeDirectory(iso88591(dir));
			cd(dir);
		}
	}

	private String iso88591(String str) {
		return StringUtil.iso88591(str, this.encode);
	}

	private String gbfrom88591(String str) {
		try {
			return new String(str.getBytes("iso-8859-1"), this.encode);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public void connect() {
		try {
			int serverTimeout = PluginUtil.getServerTimeout();
			this.ftpclient.setDefaultTimeout(serverTimeout);
			this.ftpclient.setConnectTimeout(serverTimeout);
			this.ftpclient.setDataTimeout(serverTimeout);
			this.ftpExchangeOutputStream = new FtpExchangeOutputStream(LogUtil.initSexftpChangeConsole(), this.encode);
			this.ftpExchangeOutputStream.setPreStr(
					String.format("%s:%d@%s", new Object[] { this.host, Integer.valueOf(this.port), this.username }));
			this.ftpclient.addProtocolCommandListener(new PrintCommandListener(this.ftpExchangeOutputStream, true));
			this.ftpclient.connect(this.host, this.port);
			if (!FTPReply.isPositiveCompletion(this.ftpclient.getReplyCode())) {
				this.ftpclient.disconnect();
				return;
			}
			boolean loginSuc = this.ftpclient.login(this.username, this.password);
			if (!loginSuc) {
				throw new FtpIOException(
						String.format("Login Failed Using %s : %s!", new Object[] { this.username, this.password }));
			}
			this.ftpclient.setFileType(2);
			this.ftpclient.enterLocalPassiveMode();

			LogUtil.info("connected.");
		} catch (Exception e) {
			disconnect();
			throw new FtpIOException(e);
		}
	}

	public void prepareConnect(String host, int port, String username, String password, String encode) {
		this.host = host;
		this.port = port;
		this.password = password;
		this.username = username;
		this.encode = ((encode != null) ? encode : "gbk");
	}

	public void delete(String deleteFile) {
		try {
			this.ftpclient.deleteFile(deleteFile);
		} catch (IOException e) {
			throw new FtpIOException("delete " + deleteFile + " fail!" + e, e);
		}
	}

	public void disconnect() {
		try {
			this.ftpclient.logout();
		} catch (Exception localException1) {
		}
		try {
			this.ftpclient.disconnect();
		} catch (Exception localException2) {
		} finally {
			this.ftpclient = myInstance();

			LogUtil.info("new Ftpclient created.");
		}
	}

	@SuppressWarnings("resource")
	public void download(String downloadFile, String saveFile, IFtpStreamMonitor monitor) {
		InputStream is = null;
		OutputStream os = null;
		int t = 0;
		try {
			File sf = new File(saveFile);
			if (!sf.getParentFile().exists()) {
				sf.getParentFile().mkdirs();
			}
			String exists = (sf.exists()) ? "(overwrite exists:" + sf.getName() + ")" : "";

			is = this.ftpclient.retrieveFileStream(iso88591(downloadFile));
			if (is == null) {
				throw new NeedRetryException(downloadFile + " down reseted,please try again!");
			}
			os = new FileOutputStream(saveFile);

			int avi = 0;
			byte[] bytes = new byte[1024];

			t = 0;
			int c;
			while ((c = is.read(bytes)) != -1) {
				try {
					os.write(bytes, 0, c);
				} catch (Exception localException1) {
					throw new RuntimeException("error:" + t);
				}
				t += c;
				monitor.printStreamString(null, t, avi, "");
			}
			monitor.printSimple(
					"      download success :" + ((!downloadFile.equals(sf.getName())) ? downloadFile + " -> " : "")
							+ sf.getName() + " " + saveFile + " " + exists);
		} catch (AbortException e) {
			throw e;
		} catch (Exception e) {
			try {
				disconnect();
			} catch (Exception localException2) {
			}
			throw new FtpIOException("Exception when downloaded downloadFile " + StringUtil.getHumanSize(t) + e, e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception localException3) {
			}
			try {
				os.close();
			} catch (Exception localException4) {
			}
			try {
				if (this.ftpclient.isConnected())
					this.ftpclient.completePendingCommand();
			} catch (Exception localException5) {
			}
		}
	}

	public boolean isConnect() {
		if (this.ftpclient.isConnected()) {
			try {
				this.ftpclient.noop();
				return true;
			} catch (FTPConnectionClosedException localFTPConnectionClosedException) {
			} catch (SocketException localSocketException) {
			} catch (IOException e) {
				throw new FtpIOException(e);
			}
			disconnect();
		}
		return false;
	}

	public List<FtpFile> listFiles() {
		List<FtpFile> ftpfileLIst = new ArrayList<FtpFile>();
		try {
			FTPFile[] listFiles = this.ftpclient.listFiles();
			for (FTPFile ftpFile : listFiles) {
				if (".".equals(ftpFile.getName()))
					continue;
				if ("..".equals(ftpFile.getName())) {
					continue;
				}

				boolean isfolder = ftpFile.isDirectory();

				FtpFile newftpfile = new FtpFile(gbfrom88591(ftpFile.getName()), isfolder, ftpFile.getSize(),
						ftpFile.getTimestamp());
				ftpfileLIst.add(newftpfile);
			}
		} catch (IOException e) {
			throw new FtpIOException(e);
		}
		return ftpfileLIst;
	}

	public void upload(String uploadFile, IFtpStreamMonitor monitor) {
		FileInputStream is = null;
		OutputStream os = null;
		try {
			File file = new File(uploadFile);

			os = this.ftpclient.storeFileStream(iso88591(file.getName()));

			is = new FileInputStream(uploadFile);
			long avi = file.length();

			byte[] bytes = new byte[1024];

			long t = 0L;
			int c;
			while ((c = is.read(bytes)) != -1) {
				try {
					os.write(bytes, 0, c);
				} catch (Exception e) {
					throw new RuntimeException("error:" + t, e);
				}
				t += c;
				monitor.printStreamString(null, t, avi, "");
			}
			monitor.printSimple("      upload success :" + file.getName());
		} catch (IOException e) {
			try {
				disconnect();
			} catch (Exception localException1) {
			}
			throw new FtpIOException(e);
		} finally {
			try {
				is.close();
			} catch (Exception localException2) {
			}
			try {
				os.close();
			} catch (Exception localException3) {
			}
			try {
				if (this.ftpclient.isConnected())
					this.ftpclient.completePendingCommand();
			} catch (IOException e) {
				throw new FtpIOException(e);
			}
		}
	}

	public void completed() {
	}

	public static void main(String[] args) {
		System.out.println("coy".substring(3));
	}
}