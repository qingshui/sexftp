package org.sexftp.core.utils;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.UnicodeDetector;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.sexftp.core.exceptions.SRuntimeException;

public class Cpdetector {
	private static Set<Character> ZH_SBC_CASE = new HashSet<Character>(Arrays.asList(
			new Character[] { Character.valueOf((char)65280), Character.valueOf((char) 65281), Character.valueOf((char) 65282),
					Character.valueOf((char) 65283), Character.valueOf((char) 65284), Character.valueOf((char) 65285),
					Character.valueOf((char) 65286), Character.valueOf((char) 65287), Character.valueOf((char) 65288),
					Character.valueOf((char) 65289), Character.valueOf((char) 65290), Character.valueOf((char) 65291),
					Character.valueOf((char) 65292), Character.valueOf((char) 65293), Character.valueOf((char) 65294),
					Character.valueOf((char) 65295), Character.valueOf((char) 65296), Character.valueOf((char) 65297),
					Character.valueOf((char) 65298), Character.valueOf((char) 65299), Character.valueOf((char) 65300),
					Character.valueOf((char) 65301), Character.valueOf((char) 65302), Character.valueOf((char) 65303),
					Character.valueOf((char) 65304), Character.valueOf((char) 65305), Character.valueOf((char) 65306),
					Character.valueOf((char) 65307), Character.valueOf((char) 65308), Character.valueOf((char) 65309),
					Character.valueOf((char) 65310), Character.valueOf((char) 65311), Character.valueOf((char) 65312),
					Character.valueOf((char) 65313), Character.valueOf((char) 65314), Character.valueOf((char) 65315),
					Character.valueOf((char) 65316), Character.valueOf((char) 65317), Character.valueOf((char) 65318),
					Character.valueOf((char) 65319), Character.valueOf((char) 65320), Character.valueOf((char) 65321),
					Character.valueOf((char) 65322), Character.valueOf((char) 65323), Character.valueOf((char) 65324),
					Character.valueOf((char) 65325), Character.valueOf((char) 65326), Character.valueOf((char) 65327),
					Character.valueOf((char) 65328), Character.valueOf((char) 65329), Character.valueOf((char) 65330),
					Character.valueOf((char) 65331), Character.valueOf((char) 65332), Character.valueOf((char) 65333),
					Character.valueOf((char) 65334), Character.valueOf((char) 65335), Character.valueOf((char) 65336),
					Character.valueOf((char) 65337), Character.valueOf((char) 65338), Character.valueOf((char) 65339),
					Character.valueOf((char) 65340), Character.valueOf((char) 65341), Character.valueOf((char) 65342),
					Character.valueOf((char) 65343), Character.valueOf((char) 65344), Character.valueOf((char) 65345),
					Character.valueOf((char) 65346), Character.valueOf((char) 65347), Character.valueOf((char) 65348),
					Character.valueOf((char) 65349), Character.valueOf((char) 65350), Character.valueOf((char) 65351),
					Character.valueOf((char) 65352), Character.valueOf((char) 65353), Character.valueOf((char) 65354),
					Character.valueOf((char) 65355), Character.valueOf((char) 65356), Character.valueOf((char) 65357),
					Character.valueOf((char) 65358), Character.valueOf((char) 65359), Character.valueOf((char) 65360),
					Character.valueOf((char) 65361), Character.valueOf((char) 65362), Character.valueOf((char) 65363),
					Character.valueOf((char) 65364), Character.valueOf((char) 65365), Character.valueOf((char) 65366),
					Character.valueOf((char) 65367), Character.valueOf((char) 65368), Character.valueOf((char) 65369),
					Character.valueOf((char) 65370), Character.valueOf((char) 65371), Character.valueOf((char) 65372),
					Character.valueOf((char) 65373), Character.valueOf((char) 65374) }));
	
	public static final int sta = 161;
	public static final String[] SUPORT_CHARSET = { "GBK", "UTF-8", "GB2312", "GB18030" };
	//private static final int PS = 134217728;

	public static void main(String[] args) throws Exception {
		byte[] data = FileUtil.readBytesFromInStream(new FileInputStream(
				"E:/coynn/workc/MyEclipse 8.5/productActivation/src/com/kkfun/productactive/monitor/ProductActivationMonitor.java"));
		new String(data, "GBK");
		new String(data, "x-EUC-TW");
		data = delASCIIdata(data);
		System.out.println(encode(new ByteArrayInputStream(data, 0, 2)));
		System.out.println(new String(data));
	}

	public static byte[] delASCIIdata(byte[] data) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (int i = 0; i < data.length; ++i) {
			byte b = data[i];
			char c = (char) b;
			if ((c < '') && (c > '\b')) {
				continue;
			}

			bos.write(b);
			++i;

			if (i >= data.length)
				continue;
			bos.write(data[i]);
		}

		return bos.toByteArray();
	}

	public static String isOnlyASC(byte[] data) throws UnsupportedEncodingException {
		for (String suportchar : SUPORT_CHARSET) {
			boolean isallasc = true;
			String encode = "";
			for (char c : new String(data, suportchar).toCharArray()) {
				if ((c <= '') && (c >= '\b'))
					continue;
				if (ZH_SBC_CASE.contains(Character.valueOf(c))) {
					encode = suportchar;
				} else {
					isallasc = false;
					break;
				}
			}

			if (!isallasc)
				continue;
			if (encode.length() > 0) {
				return "US-ASCII_" + encode;
			}

			return "US-ASCII";
		}

		return "";
	}

	public static String onlyNoneASCII(String str) {
		StringBuffer sb = new StringBuffer();
		for (char c : str.toCharArray()) {
			if ((c <= '') && (c >= '\b'))
				continue;
			sb.append(c);
		}

		return sb.toString();
	}

	public static Charset richencode(InputStream in) throws UnsupportedEncodingException {
		byte[] data = FileUtil.readBytesFromInStream(in);
		String isallasc = isOnlyASC(data);
		String encode = null;
		if (isallasc.length() > 0) {
			if (isallasc.startsWith("US-ASCII_")) {
				encode = isallasc.replace("US-ASCII_", "");
			} else {
				encode = isallasc;
			}
		}
		if (encode == null) {
			Charset c = encode(new ByteArrayInputStream(data));
			encode = (c != null) ? c.toString() : null;
		}

		if (encode != null) {
			if (("x-EUC-TW".equalsIgnoreCase(encode)) || ("windows-1252".equalsIgnoreCase(encode))
					|| ("EUC-KR".equalsIgnoreCase(encode))) {
				byte[] newdata = delASCIIdata(data);
				Charset c = encode(new ByteArrayInputStream(newdata));
				String newcode = (c != null) ? c.toString() : null;
				if (newcode != null) {
					if (newcode.startsWith("GB")) {
						encode = newcode;
					} else {
						encode = "GB18030";
					}
				}
			}
			try {
				return Charset.forName(encode);
			} catch (Exception e) {
				throw new SRuntimeException(e);
			}
		}
		return null;
	}

	public static Charset encode(InputStream in) {
		return encode(in, 134217728);
	}

	public static Charset xencode(InputStream in) {
		return null;
	}

	public static Charset encode(InputStream in, int length) {
		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();

		detector.add(ASCIIDetector.getInstance());
		detector.add(JChardetFacade.getInstance());

		detector.add(UnicodeDetector.getInstance());
		Charset charset = null;
		BufferedInputStream bis = new BufferedInputStream(in);
		try {
			charset = detector.detectCodepage(bis, length);
		} catch (IllegalArgumentException localIllegalArgumentException) {
		} catch (IOException localIOException) {
		}
		return charset;
	}
}