package org.sexftp.core.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.sexftp.core.exceptions.AbortException;
import org.sexftp.core.exceptions.FtpNoSuchFileException;
import org.sexftp.core.exceptions.SRuntimeException;
import org.sexftp.core.ftp.bean.FtpFile;
import sexftp.views.IFtpStreamMonitor;

public class MyFile implements XFtp {
	private String curDir;

	public void prepareConnect(String host, int port, String username, String password, String encode) {
	}

	public void delete(String deleteFile) {
	}

	public void disconnect() {
	}

	public List<FtpFile> listFiles() {
		try {
			List<FtpFile> list = new ArrayList<FtpFile>();
			File[] listFiles = new File(this.curDir).listFiles();
			if (listFiles != null) {
				for (File file : listFiles) {
					Calendar instance = Calendar.getInstance();
					instance.setTime(new Date(file.lastModified()));
					long available = 0L;
					if (file.isFile()) {
						available = file.length();
					}

					FtpFile ftpfile = new FtpFile(file.getName(), file.isDirectory(), available, instance);
					list.add(ftpfile);
				}
			}
			return list;
		} catch (Exception e) {
			throw new SRuntimeException(e);
		}
	}

	public void cd(String directory) {
		if (!new File(directory).exists()) {
			throw new FtpNoSuchFileException("No Such Folder:" + directory);
		}
		this.curDir = directory;
	}

	public void download(String downloadFile, String saveFile, IFtpStreamMonitor monitor) {
		OutputStream os = null;
		FileInputStream is = null;
		try {
			File file_in = new File(this.curDir + "/" + downloadFile);
			os = new FileOutputStream(saveFile);
			is = new FileInputStream(file_in);
			long avi = file_in.length();

			byte[] bytes = new byte[1024];

			int t = 0;
			int c;
			while ((c = is.read(bytes)) != -1) {
				os.write(bytes, 0, c);
				t += c;
				monitor.printStreamString(null, t, avi, "");
			}

			monitor.printSimple("      download success :" + file_in.getName());

			is.close();

			os.close();
		} catch (AbortException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				is.close();
			} catch (Exception localException1) {
			}
			try {
				os.close();
			} catch (Exception localException2) {
			}
		}
	}

	public boolean isConnect() {
		return true;
	}

	public void cdOrMakeIfNotExists(String directory) {
		this.curDir = directory;
		if (new File(this.curDir).exists())
			return;
		new File(this.curDir).mkdirs();
	}

	public void upload(String uploadFile, IFtpStreamMonitor monitor) {
		OutputStream os = null;
		FileInputStream is = null;
		try {
			File file_in = new File(uploadFile);

			if (new File(this.curDir + "/" + file_in.getName()).exists())
				;
			os = new FileOutputStream(this.curDir + "/" + file_in.getName());
			is = new FileInputStream(file_in);
			long avi = file_in.length();

			byte[] bytes = new byte[1024];

			int t = 0;
			int c;
			while ((c = is.read(bytes)) != -1) {
				os.write(bytes, 0, c);
				t += c;
				monitor.printStreamString(null, t, avi, "");
			}

			monitor.printSimple("      upload success :" + file_in.getName());

			is.close();

			os.close();
		} catch (AbortException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				is.close();
			} catch (Exception localException1) {
			}
			try {
				os.close();
			} catch (Exception localException2) {
			}
		}
	}

	public void connect() {
	}

	public void completed() {
	}
}
