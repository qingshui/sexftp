package sexftp.uils;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.ui.console.MessageConsoleStream;
import org.sexftp.core.utils.StringUtil;

public class FtpExchangeOutputStream extends PrintWriter {
	private String encode;
	private StringBuffer listenStr = null;
	private String preStr = "";

	public FtpExchangeOutputStream(MessageConsoleStream out, String encode) {
		super(out);
		this.encode = encode;
	}

	public void println(String str) {
		String isobk = StringUtil.bakFromiso88591(str, this.encode);
		if (!isobk.equals(str)) {
			str = str + " [" + isobk + "]";
		}
		super.println(str);
	}

	public void print(String str) {
		String isobk = StringUtil.bakFromiso88591(str, this.encode);
		StringBuffer sb = new StringBuffer();
		StringBuffer gb = new StringBuffer();
		for (char c : isobk.toCharArray()) {
			if ((c >= 0) && (c <= '­')) {
				if (gb.length() > 0) {
					String iso88591 = StringUtil.iso88591(gb.toString(), this.encode);
					sb.append(iso88591 + "[" + gb.toString() + "]");
					gb = new StringBuffer();
				}
				sb.append(c);
			} else {
				gb.append(c);
			}
		}
		if (this.listenStr != null) {
			this.listenStr.append(sb.toString());
			this.listenStr.append("\r\n");
		}

		String p = String.format("%s %s %s", new Object[] {
				new SimpleDateFormat("yyyy-M-d HH:mm:ss").format(new Date()), this.preStr, sb.toString() });
		super.print(p);
	}

	public StringBuffer getListenStr() {
		return this.listenStr;
	}

	public void setListenStr(StringBuffer listenStr) {
		this.listenStr = listenStr;
	}

	public String getPreStr() {
		return this.preStr;
	}

	public void setPreStr(String preStr) {
		this.preStr = preStr;
	}
}
