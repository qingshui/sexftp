package org.sexftp.core.ftp.bean;

import org.sexftp.core.Tosimpleable;
import org.sexftp.core.utils.StringUtil;

public class FtpUploadConf implements Tosimpleable {
	private String clientPath;
	private String serverPath;
	private String fileMd5;
	private String[] excludes;
	private String[] includes;

	public String getClientPath() {
		return this.clientPath;
	}

	public void setClientPath(String clientPath) {
		this.clientPath = clientPath;
	}

	public String getServerPath() {
		return this.serverPath;
	}

	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}

	public String getFileMd5() {
		return this.fileMd5;
	}

	public void setFileMd5(String fileMd5) {
		this.fileMd5 = fileMd5;
	}

	public String[] getExcludes() {
		return this.excludes;
	}

	public void setExcludes(String[] excludes) {
		this.excludes = excludes;
	}

	public String toString() {
		return String.format("%s <-> %s", new Object[] { this.clientPath, this.serverPath });
	}

	public String[] getIncludes() {
		return this.includes;
	}

	public void setIncludes(String[] includes) {
		this.includes = includes;
	}

	public String toSimpleString() {
		return toSimpleString(60);
	}

	public String toSimpleString(int maxlen) {
		return String.format("%s <-> %s", new Object[] { StringUtil.simpString(this.clientPath, maxlen),
				StringUtil.simpString(this.serverPath, maxlen) });
	}
}
