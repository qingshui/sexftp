package sexftp.uils;

import java.io.IOException;
import java.io.PrintWriter;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.console.MessageConsoleStream;
import org.sexftp.core.utils.StringUtil;
import sexftp.Activator;

public class LogUtil {
	public static void info(String msg) {
		try {
			Activator.getDefault().getLog().log(new Status(1, "sexftp", "sexftp_info_log:" + msg));
		} catch (NullPointerException localNullPointerException) {
		}
		System.out.println(msg);
	}

	public static void error(String msg, Throwable e) {
		try {
			Activator.getDefault().getLog().log(new Status(4, "sexftp", "sexftp_erorr_log:" + msg, e));
		} catch (NullPointerException localNullPointerException) {
		}
		System.out.println(msg + "\r\n" + StringUtil.readExceptionDetailInfo(e));
	}

	public static MessageConsoleStream initSexftpChangeConsole() {
		return Console.createConsole("SexFtpServerCmd", "repository_rep.gif").getConsoleStream();
	}

	public static void main(String[] args) throws Exception {
		FTPClient ftpclient = new FTPClient();

		ftpclient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

		ftpclient.connect("59.151.39.39", 9898);
		boolean isit = ftpclient.login("kkfunapp", "BEIJINGkkfun2007?><|\":}{P+_)");
		System.out.println(isit);
		ftpclient.setFileType(2);
		ftpclient.enterLocalPassiveMode();

		boolean wk = ftpclient.changeWorkingDirectory("/");
		System.out.println(wk);
		System.out.println(ftpclient.printWorkingDirectory());

		FTPFile[] listFiles = ftpclient.listFiles();
		for (FTPFile ftpFile : listFiles) {
			System.out.println(ftpFile.getName());
		}
		System.out.println(listFiles.length);

		ftpclient.noop();

		ftpclient.logout();

		if (!ftpclient.isConnected())
			return;
		try {
			ftpclient.disconnect();
		} catch (IOException localIOException) {
		}
	}
}
