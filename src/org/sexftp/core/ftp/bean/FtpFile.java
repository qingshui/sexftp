package org.sexftp.core.ftp.bean;

import java.util.Calendar;

public class FtpFile {
	private String name;
	private boolean isfolder;
	private long size;
	private Calendar timeStamp;

	public FtpFile(String name, boolean isfolder, long size, Calendar timeStamp) {
		this.name = name;
		this.isfolder = isfolder;
		this.size = size;
		this.timeStamp = timeStamp;
	}

	public String getName() {
		return this.name;
	}

	public boolean isIsfolder() {
		return this.isfolder;
	}

	public long getSize() {
		return this.size;
	}

	public Calendar getTimeStamp() {
		return this.timeStamp;
	}
}
