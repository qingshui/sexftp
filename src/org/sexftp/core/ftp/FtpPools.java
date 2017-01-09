package org.sexftp.core.ftp;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.sexftp.core.exceptions.BizException;
import org.sexftp.core.exceptions.SRuntimeException;
import org.sexftp.core.ftp.bean.FtpConf;

public class FtpPools implements Consoleable {
	private static Map<String, XFtp> CASH = new Hashtable<String, XFtp>();
	public static Map<String, Class<? extends XFtp>> FTP_MAP = new LinkedHashMap<String, Class<? extends XFtp>>();
	private FtpConf conf;
	private Consoleable console;

	static {
		FTP_MAP.put("ftp", MyFtp.class);
		FTP_MAP.put("sftp", MySFTP.class);
		FTP_MAP.put("ftps", MyFtps.class);
		FTP_MAP.put("ftpes", MyFtpes.class);
		FTP_MAP.put("file", MyFile.class);
	}

	public FtpPools(FtpConf conf, Consoleable console) {
		this.conf = conf;
		this.console = ((console != null) ? console : this);
	}

	public XFtp getNewFtp() {
		XFtp xftp = null;
		Class<?> xftpcls = (Class<?>) FTP_MAP.get(this.conf.getServerType());
		if (xftpcls == null) {
			throw new BizException(
					"Server Type:" + this.conf.getServerType() + " in " + this.conf.getName() + " Not Supported!");
		}
		try {
			xftp = new MyftpProxy((XFtp) xftpcls.newInstance(), this.console);
		} catch (Exception e) {
			throw new SRuntimeException(e);
		}
		xftp.prepareConnect(this.conf.getHost(), this.conf.getPort().intValue(), this.conf.getUsername(),
				this.conf.getPassword(), null);
		return xftp;
	}

	public XFtp getFtp() {
		synchronized (FtpPools.class) {
			XFtp xftp = null;
			String key = String.format("%s:%d@%s@%s", new Object[] { this.conf.getHost(), this.conf.getPort(),
					this.conf.getUsername(), this.conf.getServerType() });
			if (CASH.containsKey(key)) {
				xftp = (XFtp) CASH.get(key);
			} else {
				try {
					Class<?> xftpcls = (Class<?>) FTP_MAP.get(this.conf.getServerType());
					if (xftpcls == null) {
						throw new BizException(
								"Server Type [" + this.conf.getServerType() + "] in [" + this.conf.getName()
										+ "] Not Supported!\r\n" + "We Supported " + FTP_MAP.keySet().toString());
					}
					xftp = new MyftpProxy((XFtp) xftpcls.newInstance(), this.console);
					xftp.prepareConnect(this.conf.getHost(), this.conf.getPort().intValue(), this.conf.getUsername(),
							this.conf.getPassword(), null);
					CASH.put(key, xftp);
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new SRuntimeException(e);
				}

			}

			return xftp;
		}
	}

	public void disconnectAll() {
		for (Map.Entry<String, XFtp> entry : CASH.entrySet()) {
			try {
				((XFtp) entry.getValue()).disconnect();
				if (this.console != null)
					this.console.console("disconceted " + entry.getValue());
			} catch (Exception e) {
				if (this.console != null)
					this.console.console(e.getMessage());
			}
		}
		CASH.clear();
	}

	public XFtp getConnectedFtp() {
		XFtp xftp = getFtp();
		if (!xftp.isConnect()) {
			this.console.console("Connecting... " + this.conf.toString());
			xftp.connect();
			this.console.console("Connected.");
		}
		return xftp;
	}

	public void console(String str) {
	}
}
