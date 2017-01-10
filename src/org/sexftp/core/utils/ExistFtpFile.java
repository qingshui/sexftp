package org.sexftp.core.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sexftp.core.exceptions.FtpNoSuchFileException;
import org.sexftp.core.exceptions.SRuntimeException;
import org.sexftp.core.ftp.XFtp;
import org.sexftp.core.ftp.bean.FtpFile;

public class ExistFtpFile {
	private Map<String, List<FtpFile>> cFiles = new HashMap<String, List<FtpFile>>();

	private XFtp ftp = null;

	public ExistFtpFile(XFtp ftp) {
		this.ftp = ftp;
	}

	public FtpFile existsFtpFile(String serverPath) {
		String cserverPath = serverPath;
		if (!cserverPath.endsWith("/")) {
			cserverPath = cserverPath.substring(0, cserverPath.lastIndexOf("/") + 1);
			if (!this.cFiles.containsKey(cserverPath)) {
				try {
					this.ftp.cd(cserverPath);
					this.cFiles.put(cserverPath, this.ftp.listFiles());
				} catch (FtpNoSuchFileException localFtpNoSuchFileException) {
					this.cFiles.put(cserverPath, new ArrayList<FtpFile>());
				}
			}

			for (FtpFile ffile : (ArrayList<FtpFile>) this.cFiles.get(cserverPath)) {
				if (ffile.getName().equals(new File(serverPath).getName())) {
					return ffile;
				}

			}

			return null;
		}
		throw new SRuntimeException("Cann't Check,[" + serverPath + "] Not File!");
	}
}
