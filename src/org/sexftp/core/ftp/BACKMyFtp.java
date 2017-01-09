package org.sexftp.core.ftp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.sexftp.core.exceptions.AbortException;
import org.sexftp.core.ftp.bean.FtpFile;
import sexftp.views.IFtpStreamMonitor;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class BACKMyFtp implements XFtp {
	FTPClient ftpClient;

	public void delete(String deleteFile) {
	}

	public void download(String downloadFile, String saveFile, IFtpStreamMonitor monitor) {
	}

	public List<FtpFile> listFiles() {
		return null;
	}

	public void prepareConnect(String host, int port, String username, String password, String encode) {
		try {
			this.ftpClient = new FTPClient();
			System.out.println(host + ":" + port + "@" + username + " logining ...");
			this.ftpClient.connect(host, port);
			this.ftpClient.login(username, password);

			System.out.println("login success!");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void delete(String directory, String deleteFile) {
	}

	public boolean isConnect() {
		if (this.ftpClient == null)
			return false;
		return this.ftpClient.isConnected();
	}

	public void disconnect() {
		try {
			this.ftpClient.disconnect();

			System.out.println("disconnect success");
		} catch (IOException ex) {
			System.out.println("not disconnect");

			System.out.println(ex);
		}
	}

	public void download(String directory, String downloadFile, String saveFile) {
	}

	public List<String> listFiles(String directory) {
		cd(directory);
		ArrayList<String> downList = new ArrayList<String>();
		try {
			FTPFile[] ftplist = this.ftpClient.listFiles();
			for (FTPFile file : ftplist) {
				downList.add(file.getName());
			}
		} catch (Exception localException1) {
		}
		return downList;
	}

	public void cd(String directory) {
		try {
			if (directory.length() != 0) {
				this.ftpClient.changeWorkingDirectory(directory);
			}
			this.ftpClient.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
		} catch (IOException e) {
			throw new RuntimeException("mkdirs " + directory + " fail!" + e, e);
		}
	}

	public void cdOrMakeIfNotExists(String directory) {
		try {
			if (directory.length() != 0) {
				this.ftpClient.changeWorkingDirectory(directory);
			}
			this.ftpClient.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
		} catch (IOException localIOException1) {
			try {
				mkdirs(directory);
			} catch (IOException e1) {
				throw new RuntimeException("mkdirs " + directory + " fail!" + e1, e1);
			}
		}
	}

	private void mkdirs(String directory) throws IOException {
		String[] dirs = directory.split("/");
		if (directory.startsWith("/")) {
			this.ftpClient.changeWorkingDirectory("/");
		}
		for (String dir : dirs) {
			if (dir.length() <= 0)
				continue;
			try {
				this.ftpClient.changeWorkingDirectory(dir);
			} catch (IOException localIOException) {
				this.ftpClient.makeDirectory(dir);
			}
		}
		this.ftpClient.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
	}

	public void upload(String uploadFile, IFtpStreamMonitor monitor) {
		try {
			this.ftpClient.storeFileStream(uploadFile);
			System.out.println("upload success :" + uploadFile);
		} catch (AbortException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
		}
	}

	public void upload(String[] fileList) {
		for (int i = 0; i < fileList.length; ++i) {
			upload(fileList[i], null);
		}
	}

	public void completed() {
	}

	public static void main(String[] args) {
	}

	public void connect() {
	}
}
