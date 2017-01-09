package org.sexftp.core.ftp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import org.sexftp.core.exceptions.AbortException;
import org.sexftp.core.exceptions.FtpNoSuchFileException;
import org.sexftp.core.exceptions.SRuntimeException;
import org.sexftp.core.ftp.bean.FtpFile;
import sexftp.uils.PluginUtil;
import sexftp.views.IFtpStreamMonitor;

public class MySFTP implements XFtp {
	private ChannelSftp channelSftp;
	private String host;
	private int port;
	private String username;
	private String password;
	//private String encode = "gbk";

	public void connect() {
		ChannelSftp sftp = null;
		try {
			int serverTimeout = PluginUtil.getServerTimeout();
			JSch jsch = new JSch();

			Session sshSession = jsch.getSession(this.username, this.host, this.port);
			sshSession.setPassword(this.password);
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			sshSession.setTimeout(serverTimeout);
			sshSession.setConfig(sshConfig);
			sshSession.connect();
			Channel channel = sshSession.openChannel("sftp");
			channel.connect();
			sftp = (ChannelSftp) channel;
		} catch (Exception e) {
			throw new SRuntimeException(String.format("%s \r\n using %s : %s",
					new Object[] { e.getMessage(), this.username, this.password }), e);
		}
		this.channelSftp = sftp;
	}

	public void prepareConnect(String host, int port, String username, String password, String encode) {
		this.host = host;
		this.port = port;
		this.password = password;
		this.username = username;
		//this.encode = ((encode != null) ? encode : "gbk");
	}

	public void disconnect() {
		this.channelSftp.disconnect();
		this.channelSftp.exit();
	}

	public void cd(String directory) {
		ChannelSftp sftp = this.channelSftp;
		try {
			sftp.cd(directory);
		} catch (SftpException e) {
			if ((e.getMessage() != null) && (e.getMessage().toLowerCase().startsWith("No such file".toLowerCase()))) {
				throw new FtpNoSuchFileException(e.toString());
			}
			if (e.id == 2) {
				throw new FtpNoSuchFileException(e.toString());
			}

			throw new RuntimeException(directory + e.getMessage(), e);
		} catch (Exception e) {
			throw new RuntimeException(directory + e.getMessage(), e);
		}
	}

	public void cdOrMakeIfNotExists(String directory) {
		ChannelSftp sftp = this.channelSftp;
		try {
			sftp.cd(directory);
		} catch (Exception localException1) {
			try {
				mkdirs(directory);
			} catch (Exception e2) {
				throw new RuntimeException(directory, e2);
			}
		}
	}

	private void mkdirs(String directory) throws Exception {
		ChannelSftp sftp = this.channelSftp;
		String[] dirs = directory.split("/");
		if (directory.startsWith("/")) {
			sftp.cd("/");
		}
		for (String dir : dirs) {
			if (dir.length() <= 0)
				continue;
			try {
				sftp.cd(dir);
			} catch (Exception localException1) {
				try {
					sftp.mkdir(dir);
					sftp.cd(dir);
				} catch (Exception e1) {
					throw new SRuntimeException("Make directory " + directory + " Failed! (" + dir + ")", e1);
				}
			}
		}
	}

	public void upload(String uploadFile, final IFtpStreamMonitor monitor) {
		ChannelSftp sftp = this.channelSftp;
		final File file = new File(uploadFile);
		FileInputStream useIns = null;
		try {
			useIns = new FileInputStream(file);
			useIns.close();
			FileInputStream ins = new FileInputStream(file) {
				private int t = 0;
				public int read(byte[] b) throws IOException {
					return super.read(b);
				}

				public int read() throws IOException {
					return super.read();
				}

				public int read(byte[] b, int off, int len) throws IOException {
					int av = super.read(b, off, len);
					if (av >= 0) {
						this.t += av;
						monitor.printStreamString(null, this.t, file.length(), "");
					}
					return av;
				}
			};
			useIns = ins;
			sftp.put(ins, file.getName());
		} catch (AbortException e) {
			throw e;
		} catch (Exception e) {
			if (e.getCause() instanceof AbortException) {
				throw ((AbortException) e.getCause());
			}
			throw new RuntimeException(e);
		} finally {
			try {
				useIns.close();
			} catch (Exception localException1) {
			}
		}
		monitor.printSimple("      upload success :" + new File(uploadFile).getName());
	}

	public void download(String downloadFile, String saveFile, final IFtpStreamMonitor monitor) {
		ChannelSftp sftp = this.channelSftp;
		try {
			File file = new File(saveFile);
			String exists = (file.exists()) ? " (overwrite exists:" + file.getName() + ")" : "";
			FileOutputStream fos = new FileOutputStream(file) {
				private int t = 0;
				public void write(byte[] b, int off, int len) throws IOException {
					if (len >= 0) {
						this.t += len;
						monitor.printStreamString(null, this.t, 0L, "");
					}
					super.write(b, off, len);
				}
			};
			sftp.get(downloadFile, fos);
			monitor.printSimple("      download success :" + new File(downloadFile).getName() + exists);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delete(String deleteFile) {
		ChannelSftp sftp = this.channelSftp;
		try {
			sftp.rm(deleteFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<FtpFile> listFiles() {
		List<FtpFile> listfiles = new ArrayList<FtpFile>();
		ChannelSftp sftp = this.channelSftp;
		try {
			Vector<?> ls = sftp.ls(sftp.pwd());
			for (Iterator<?> localIterator = ls.iterator(); localIterator.hasNext();) {
				Object f = localIterator.next();

				ChannelSftp.LsEntry lsentry = (ChannelSftp.LsEntry) f;
				if (lsentry.getFilename().equals("."))
					continue;
				if (!lsentry.getFilename().equals("..")) {
					StringTokenizer s = new StringTokenizer(lsentry.getLongname(), "\t ");
					boolean isFolder = false;
					while (s.hasMoreElements()) {
						String i = (String) s.nextElement();
						try {
							Integer valueOf = Integer.valueOf(i);
							isFolder = valueOf.intValue() != 1;
						} catch (NumberFormatException localNumberFormatException) {
						}
					}
					Calendar instance = Calendar.getInstance();

					instance.setTime(new Date(lsentry.getAttrs().getMTime() * 1000L));
					FtpFile file = new FtpFile(lsentry.getFilename(), isFolder, lsentry.getAttrs().getSize(), instance);
					listfiles.add(file);
				}
			}
			return listfiles;
		} catch (SftpException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		String[] arrayOfString = args;
		int j = args.length;
		for (int i = 0; i < j; ++i) {
			String arg = arrayOfString[i];
			System.out.println(arg);
		}

		XFtp sf = new MySFTP();
		String host = args[1];
		String port = args[2];
		String username = args[3];
		String password = args[4];
		String directory = args[5];
		sf.prepareConnect(host, new Integer(port).intValue(), username, password, null);
		sf.cdOrMakeIfNotExists(directory);
		try {
			sf.disconnect();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isConnect() {
		return (this.channelSftp.isConnected()) && (!this.channelSftp.isClosed()) && (!this.channelSftp.isEOF());
	}
}
