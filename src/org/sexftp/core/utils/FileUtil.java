package org.sexftp.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.sexftp.core.exceptions.AbortException;
import org.sexftp.core.exceptions.BizException;
import org.sexftp.core.exceptions.SRuntimeException;

public class FileUtil {
	public static String getTextFromFile(String filePath, String encode) {
		String s = null;
		File f = new File(filePath);
		if (f.length() > 10000000L) {
			throw new BizException(
					String.format("[%s] File Total Size [%s] , More Than [%s], Can't Go Ahead!", new Object[] {
							filePath, StringUtil.getHumanSize(f.length()), StringUtil.getHumanSize(10000000L) }));
		}
		InputStream ins = null;
		try {
			ins = new FileInputStream(filePath);
			byte[] data = new byte[ins.available()];
			ins.read(data);
			s = new String(data, encode);
		} catch (Exception e) {
		} finally {
			try {
				ins.close();
			} catch (Exception localException2) {
			}
		}
		return s;
	}

	public static void writeByte2File(String fielname, byte[] data) {
		OutputStream os = null;
		try {
			new File(fielname).getParentFile().mkdirs();
			os = new FileOutputStream(fielname);
			os.write(data);
			os.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				os.close();
			} catch (Exception localException1) {
			}
		}
	}

	public static void deleteFolder(File folder) {
		if (folder.isFile()) {
			folder.delete();
		} else {
			for (File subfile : folder.listFiles()) {
				deleteFolder(subfile);
			}
			folder.delete();
		}
	}

	@SuppressWarnings("resource")
	public static void copyFile(String srcPath, String desPath) {
		InputStream is = null;
		OutputStream os = null;
		int t = 0;
		try {
			is = new FileInputStream(srcPath);
			os = new FileOutputStream(desPath);
			byte[] bytes = new byte[8192];

			t = 0;
			int c;
			while ((c = is.read(bytes)) != -1) {
				try {
					os.write(bytes, 0, c);
				} catch (Exception e) {
					throw new RuntimeException("error:" + t + " " + e.getMessage(), e);
				}
				t += c;
			}

		} catch (AbortException e) {
			throw e;
		} catch (Exception e) {
			throw new SRuntimeException(e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception localException1) {
			}
			try {
				if (os != null)
					os.close();
			} catch (Exception localException2) {
			}
		}
	}

	public static byte[] readBytesFromInStream(InputStream in) {
		byte[] ret = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
			byte[] buf = new byte[2048];
			int len = 0;
			try {
				while ((len = in.read(buf)) != -1) {
					baos.write(buf, 0, len);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			ret = baos.toByteArray();
		} catch (Exception e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return ret;
	}

	public static List<File> searchFile(File f, IProgressMonitor monitor) {
		List<File> list = new ArrayList<File>();
		if (f.isFile()) {
			list.add(f);
		} else {
			File[] subfiles = f.listFiles();
			if (subfiles != null) {
				if (monitor != null) {
					if (monitor.isCanceled()) {
						throw new AbortException();
					}

					monitor.subTask("scanning in " + f.getAbsolutePath());
				}

				for (File subfile : subfiles) {
					list.addAll(searchFile(subfile, monitor));
				}
			}
		}
		return list;
	}

	public static Set<String> unionUpFilePath(Set<String> pathList) {
		Set<String> newPathList = new HashSet<String>();
		for (String path : pathList) {
			File file = new File(path);
			String addPath = (file.isDirectory()) ? file.getAbsolutePath() : file.getParent();
			newPathList.add(addPath);
			for (String oldPath : (String[]) newPathList.toArray(new String[0])) {
				if ((oldPath.equals(addPath)) || (!oldPath.startsWith(addPath)))
					continue;
				newPathList.remove(oldPath);
			}
		}

		return newPathList;
	}
}