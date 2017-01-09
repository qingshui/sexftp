package org.sexftp.core.bean;

public class FileZilla {
	private FileZillaServer[] Server = null;

	public FileZillaServer[] getServer() {
		return this.Server;
	}

	public void setServer(FileZillaServer[] server) {
		this.Server = server;
	}
}