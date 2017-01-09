package org.sexftp.core.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

public class HtmlCharsetDetector {
	public static boolean found = false;

	public static void main(String[] argv) throws Exception {
		nsDetector det = new nsDetector(3);

		det.Init(new nsICharsetDetectionObserver() {
			public void Notify(String charset) {
				HtmlCharsetDetector.found = true;
				System.out.println("CHARSET FIND = " + charset);
			}
		});
		InputStream imp;
		String str = "你好繁体字";
		for (int i = 0; i < 50; ++i)
			str = str + "你好繁体字";
		ByteArrayInputStream bins = new ByteArrayInputStream(str.getBytes("utf-8"));
		imp = bins;

		byte[] buf = new byte[1024];

		boolean done = false;
		boolean isAscii = true;
		int len;
		while ((len = imp.read(buf, 0, buf.length)) != -1) {
			if (isAscii) {
				isAscii = det.isAscii(buf, len);
			}

			if ((!isAscii) && (!done))
				done = det.DoIt(buf, len, false);
		}
		det.DataEnd();

		if (isAscii) {
			System.out.println("CHARSET = ASCII");
			found = true;
		}

		if (!found) {
			String[] prob = det.getProbableCharsets();
			for (int i = 0; i < prob.length; ++i)
				System.out.println("Probable Charset = " + prob[i]);
		}
	}
}
