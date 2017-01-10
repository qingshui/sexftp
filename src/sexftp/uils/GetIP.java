package sexftp.uils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;

public class GetIP {
	public static void main(String[] args) {
		boolean bHasNoArgs = false;
		if (args.length <= 0)
			bHasNoArgs = true;

		StringBuffer sbFileContent = new StringBuffer();
		boolean bGetSuccess = true;
		try {
			InetAddress host = InetAddress.getLocalHost();

			String hostName = host.getHostName();
			String hostAddr = host.getHostAddress();
			host.getCanonicalHostName();

			Date da = new Date();
			String osname = System.getProperty("os.name");
			String osversion = System.getProperty("os.version");
			String username = System.getProperty("user.name");
			String userhome = System.getProperty("user.home");
			String userdir = System.getProperty("user.dir");

			if (bHasNoArgs) {
				System.out.println("hostName is:" + hostName);
				System.out.println("hostAddr is:" + hostAddr);

				System.out.println("Current Date is:" + da.toString());
				System.out.println("osname is:" + osname);
				System.out.println("osversion is:" + osversion);
				System.out.println("username is:" + username);
				System.out.println("userhome is:" + userhome);
				System.out.println("userdir is:" + userdir);
			} else {
				sbFileContent.append("hostName is:" + hostName + "\n");
				sbFileContent.append("hostAddr is:" + hostAddr + "\n");

				sbFileContent.append("Current Date is:" + da.toString() + "\n");
				sbFileContent.append("osname is:" + osname + "\n");
				sbFileContent.append("osversion is:" + osversion + "\n");
				sbFileContent.append("username is:" + username + "\n");
				sbFileContent.append("userhome is:" + userhome + "\n");
				sbFileContent.append("userdir is:" + userdir + "\n");
			}

			StringBuffer url = new StringBuffer();
			if ((bHasNoArgs) || (args[0].equals(null)) || (args[0].equals(""))) {
				url.append("http://www.cz88.net/ip/viewip778.aspx");
			} else
				url.append(args[0]);
			StringBuffer strForeignIP = new StringBuffer("strForeignIPUnkown");
			StringBuffer strLocation = new StringBuffer("strLocationUnkown");

			if (getWebIp(url.toString(), strForeignIP, strLocation)) {
				if (bHasNoArgs) {
					System.out.println("Foreign IP is:" + strForeignIP);
					System.out.println("Location is:" + strLocation);
				} else {
					sbFileContent.append("Foreign IP is:" + strForeignIP + "\n");
					sbFileContent.append("Location is:" + strLocation + "\n");
				}

			} else if (bHasNoArgs) {
				System.out.println("Failed to connect:" + url);
			} else {
				bGetSuccess = false;
				sbFileContent.append("Failed to connect:" + url + "\n");
			}

		} catch (UnknownHostException e) {
			if (bHasNoArgs) {
				e.printStackTrace();
			} else {
				bGetSuccess = false;
				sbFileContent.append(e.getStackTrace() + "\n");
			}

		}

		if (bGetSuccess)
			sbFileContent.insert(0, "sucess\n");
		else {
			sbFileContent.insert(0, "fail\n");
		}
		if (bHasNoArgs)
			return;
		write2file(sbFileContent);
	}

	public static boolean getWebIp(String strUrl, StringBuffer strForeignIP, StringBuffer strLocation) {
		try {
			URL url = new URL(strUrl);

			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

			String s = "";
			StringBuffer sb = new StringBuffer("");
			while ((s = br.readLine()) != null) {
				sb.append(s + "\r\n");
			}
			br.close();

			String webContent = "";
			webContent = sb.toString();

			if ((webContent.equals(null)) || (webContent.equals("")))
				return false;

			String flagofForeignIPString = "IPMessage";
			int startIP = webContent.indexOf(flagofForeignIPString) + flagofForeignIPString.length() + 2;
			int endIP = webContent.indexOf("</span>", startIP);
			strForeignIP.delete(0, webContent.length());
			strForeignIP.append(webContent.substring(startIP, endIP));

			String flagofLocationString = "AddrMessage";
			int startLoc = webContent.indexOf(flagofLocationString) + flagofLocationString.length() + 2;
			int endLoc = webContent.indexOf("</span>", startLoc);
			strLocation.delete(0, webContent.length());
			strLocation.append(webContent.substring(startLoc, endLoc));

			return true;
		} catch (Exception localException) {
		}
		return false;
	}

	public static void write2file(StringBuffer content) {
		if (content.length() <= 0)
			return;
		try {
			FileOutputStream fos = new FileOutputStream("GETIP.sys");
			OutputStreamWriter osr = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osr);
			try {
				int index = 0;
				while (index >= 0) {
					int preIndex = index;
					index = content.indexOf("\n", preIndex + 2);

					if (index > 0) {
						String str = new String(content.substring(preIndex, index));
						bw.write(str);
						bw.newLine();
					} else {
						String str = new String(content.substring(preIndex, content.length() - 1));
						bw.write(str);
					}
				}
			} catch (IOException localIOException1) {
			}

			try {
				bw.close();
			} catch (IOException localIOException2) {
			}
		} catch (FileNotFoundException localFileNotFoundException) {
		}
	}
}
