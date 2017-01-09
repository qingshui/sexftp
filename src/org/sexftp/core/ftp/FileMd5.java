package org.sexftp.core.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import org.eclipse.core.runtime.IProgressMonitor;
import org.sexftp.core.exceptions.AbortException;
import org.sexftp.core.utils.StringUtil;

public class FileMd5 {
	public static String getMD5(File file) {
		return getInerMD5(file, null, null);
	}

	public static String getMD5(File file, IProgressMonitor monitor) {
		return getInerMD5(file, monitor, null);
	}

	public static String getMD5(File file, IProgressMonitor monitor, String semd5) {
		return getInerMD5(file, monitor, semd5);
	}

	private static String getInerMD5(File file, IProgressMonitor monitor, String semd5) {
		FileInputStream fis = null;
		String fileNameAndBody = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			MessageDigest mdxor = MessageDigest.getInstance("SHA1");
			byte[] fb = (semd5 != null) ? semd5.getBytes("gbk") : file.getName().getBytes("gbk");
			fis = new FileInputStream(file);
			String filename = file.getAbsolutePath();
			long dilled = 0L;
			long total = file.length();

			byte[] buffer = new byte[8192];
			int length = -1;
			while ((length = fis.read(buffer)) != -1) {
				md.update(buffer, 0, length);
				for (int i = 0; i < fb.length; ++i) {
					int tmp97_95 = i;
					byte[] tmp97_93 = buffer;
					tmp97_93[tmp97_95] = (byte) (tmp97_93[tmp97_95] ^ fb[i]);
				}
				mdxor.update(buffer, 0, length);

				if (monitor == null)
					continue;
				if (monitor.isCanceled())
					throw new AbortException("User Canceled.");
				if (total <= 20000000L)
					continue;
				dilled += length;

				monitor.subTask(String.format("(%s in %s) \r\nAnalyzing Content Of %s",
						new Object[] { StringUtil.getHumanSize(dilled), StringUtil.getHumanSize(total), filename }));
			}

			String filebody = bytesToString(md.digest());
			fileNameAndBody = bytesToString(mdxor.digest());
			fileNameAndBody += filebody;
		} catch (RuntimeException ex) {
		} catch (Exception ex) {
		} finally {
			try {
				fis.close();
			} catch (IOException localIOException2) {
			}
		}
		return fileNameAndBody;
	}

	public static String bytesToString(byte[] data) {
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] temp = new char[data.length * 2];
		for (int i = 0; i < data.length; ++i) {
			byte b = data[i];
			temp[(i * 2)] = hexDigits[(b >>> 4 & 0xF)];
			temp[(i * 2 + 1)] = hexDigits[(b & 0xF)];
		}
		return new String(temp);
	}

	public static void main(String[] args) {
		File f = new File("d:/out.t");
		System.out.println(getMD5(f));
		String s1 = "93ee04140d60241002da80756a8eda07";
		String s2 = "93ee04140d60241002da80756a8eda07";
		System.out.println(s1.equals(s2));
	}
}