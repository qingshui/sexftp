package org.sexftp.core.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

public class MyFtpes extends MyFtp {
	protected FTPClient myInstance() {
		return new FTPSClient(false);
	}
}
