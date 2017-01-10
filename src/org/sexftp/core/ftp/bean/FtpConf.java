package org.sexftp.core.ftp.bean;

import java.util.List;

public class FtpConf {
	String host = "localhost";
	Integer port = Integer.valueOf(21);
	String username = "root";
	String password;
	String serverType = "ftp";
	String name;
	private List<FtpUploadConf> ftpUploadConfList;

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServerType() {
		return this.serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public List<FtpUploadConf> getFtpUploadConfList() {
		return this.ftpUploadConfList;
	}

	public void setFtpUploadConfList(List<FtpUploadConf> ftpUploadConfList) {
		this.ftpUploadConfList = ftpUploadConfList;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return String.format("%s[%s - %s:%s@%s]",
				new Object[] { this.name, this.serverType, this.host, this.port, this.username });
	}
}
