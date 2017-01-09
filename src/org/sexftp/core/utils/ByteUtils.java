package org.sexftp.core.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class ByteUtils {
	public static String getHexString(byte[] b) throws Exception {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < b.length; ++i) {
			result.append(Integer.toString((b[i] & 0xFF) + 256, 16).substring(1));
		}
		return result.toString();
	}

	public static byte[] getByteArray(String str) {
		if (str.length() % 2 == 1) {
			str = str + 'F';
		}

		byte[] ret = new byte[str.length() / 2];

		for (int i = 0; i < ret.length; ++i) {
			String bs = str.substring(2 * i, 2 * i + 2);
			ret[i] = (byte) Integer.parseInt(bs, 16);
		}

		return ret;
	}

	public static void main(String[] args) {
		byte[] d = getByteArray("ad0e");
		System.out.println(Arrays.toString(d));
	}

	public static byte[] encryption(byte[] data) {
		byte[] cods = { 54, 8, -10, 19, -30, 13, 71, -96 };
		int codsi = 0;

		for (int i = 0; i < data.length; ++i) {
			int tmp56_55 = i;
			data[tmp56_55] = (byte) (data[tmp56_55] ^ cods[codsi]);
			++codsi;
			if (codsi < cods.length)
				continue;
			codsi = 0;
		}
		return data;
	}

	public static void writeByte2Stream(byte[] data, OutputStream os) {
		try {
			os.write(data);
			os.flush();
		} catch (IOException e) {
			if ((e == null) || (e.toString().indexOf("ClientAbortException") < 0)) {
				throw new RuntimeException(e);
			}
			try {
				os.close();
			} catch (IOException e2) {
				if (e2 != null)
					e2.toString().indexOf("ClientAbortException");
				throw new RuntimeException(e2);
			}
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					if (e != null)
						e.toString().indexOf("ClientAbortException");

					throw new RuntimeException(e);
				}
			}
		}
	}
}
