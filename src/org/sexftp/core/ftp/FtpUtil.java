package org.sexftp.core.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.desy.xbean.XbeanUtil;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.sexftp.core.exceptions.AbortException;
import org.sexftp.core.exceptions.BizException;
import org.sexftp.core.exceptions.SRuntimeException;
import org.sexftp.core.ftp.bean.FtpConf;
import org.sexftp.core.ftp.bean.FtpFile;
import org.sexftp.core.ftp.bean.FtpUploadConf;
import org.sexftp.core.utils.ExistFtpFile;
import org.sexftp.core.utils.FileUtil;
import sexftp.SexftpRun;
import sexftp.uils.PluginUtil;
import sexftp.views.AbstractSexftpView;
import sexftp.views.IFtpStreamMonitor;

public class FtpUtil {
	public static Map<String, XFtp> FTP_MAP = new HashMap<String, XFtp>();

	static {
		FTP_MAP.put("ftp", new MyFtp());
		FTP_MAP.put("file", new MyFile());
		FTP_MAP.put("sftp", new MySFTP());
	}

	public static void main(String[] args) {
		XFtp f = new MySFTP();
		f.prepareConnect("10.0.0.250", 2010, "root", "20101207kkfunserver!@#$%^&*()_+", null);
	}

	public static List<FtpConf> getAllConf(String confFoloder) {
		List<FtpConf> ftpConfList = new ArrayList<FtpConf>();
		File FileCurDir = new File(confFoloder);
		File[] confFiles = FileCurDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});
		Arrays.sort(confFiles);
		for (File file : confFiles) {
			String xmlconf = FileUtil.getTextFromFile(file.getAbsolutePath(), "utf-8");
			FtpConf conf = null;
			try {
				conf = (FtpConf) XbeanUtil.xml2Bean(FtpConf.class, xmlconf);
			} catch (Exception e) {
				conf = new FtpConf();
				conf.setHost("Load Config File Error :" + e.getMessage());
			}
			conf.setName(file.getName());
			ftpConfList.add(conf);
		}
		return ftpConfList;
	}

	public static void initWkDir(String workBaseDir, String configFilePath) {
		String wkdir = workBaseDir + "/" + new File(configFilePath).getName();
		File wkdirFile = new File(wkdir);
		if (wkdirFile.exists())
			return;
		wkdirFile.mkdirs();
	}

	public static List<FtpUploadConf> expandFtpUploadConf(FtpConf ftpconf, FtpUploadConf ftpUploadConf) {
		List<FtpUploadConf> expandFtpUploadConfList = new ArrayList<FtpUploadConf>();
		if (ftpUploadConf != null) {
			expandFtpUploadConfList.addAll(expandFtpUploadConf(ftpUploadConf));
		} else {
			for (FtpUploadConf tftpUploadConf : ftpconf.getFtpUploadConfList()) {
				expandFtpUploadConfList.addAll(expandFtpUploadConf(tftpUploadConf));
			}
		}
		return expandFtpUploadConfList;
	}

	public static void formater(String workBaseDir, String configFilePath) throws Exception {
		String wkdir = workBaseDir + "/" + new File(configFilePath).getName();
		initWkDir(workBaseDir, configFilePath);

		String xmlconf = FileUtil.getTextFromFile(configFilePath, "utf-8");
		FtpConf conf = (FtpConf) XbeanUtil.xml2Bean(FtpConf.class, xmlconf);
		List<FtpUploadConf> expandFtpUploadConfList = expandFtpUploadConf(conf, null);
		Map<String, String> lastModMap = new HashMap<String, String>();
		for (FtpUploadConf expandFtpUploadConf : (ArrayList<FtpUploadConf>) expandFtpUploadConfList) {
			lastModMap.put(expandFtpUploadConf.getClientPath(), expandFtpUploadConf.getFileMd5());
		}

		writeLastModMap(wkdir, lastModMap);
	}

	public static void formaterSel(String workBaseDir, String configFilePath, List<FtpUploadConf> ftpUploadConfList)
			throws Exception {
		String wkdir = workBaseDir + "/" + new File(configFilePath).getName();
		initWkDir(workBaseDir, configFilePath);

		String xmlconf = FileUtil.getTextFromFile(configFilePath, "utf-8");
		FtpConf conf = (FtpConf) XbeanUtil.xml2Bean(FtpConf.class, xmlconf);
		Map<String, String> lastModMap = null;
		try {
			lastModMap = readLastModMap(wkdir);
		} catch (RuntimeException e) {
			if (e.getCause() instanceof FileNotFoundException) {
				formater(workBaseDir, configFilePath);
				return;
			}

			throw e;
		}

		for (FtpUploadConf ftpUploadConf : ftpUploadConfList) {
			for (FtpUploadConf expandFtpUploadConf : expandFtpUploadConf(conf, ftpUploadConf)) {
				lastModMap.put(expandFtpUploadConf.getClientPath(), expandFtpUploadConf.getFileMd5());
			}
		}

		writeLastModMap(wkdir, lastModMap);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> readLastModMap(String wkdir) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(wkdir + "/lastModMap.d"));
			Object o = ois.readObject();
			if (o == null) {
				return new HashMap<String, String>();
			}
			return (Map<String, String>) o;
		} catch (FileNotFoundException localFileNotFoundException) {
		} catch (Exception e) {
		} finally {
			try {
				ois.close();
			} catch (Exception localException2) {
			}
		}
		return new HashMap<String, String>();
	}

	public static void writeLastModMap(String wkdir, Map<String, String> lastModMap) {
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(new FileOutputStream(wkdir + "/lastModMap.d"));
			os.writeObject(lastModMap);
		} catch (FileNotFoundException localFileNotFoundException) {
			throw new BizException("Not Format!");
		} catch (IOException e) {
			throw new SRuntimeException(e);
		} finally {
			try {
				os.close();
			} catch (Exception localException1) {
			}
		}
	}

	public static List<FtpUploadConf> anyaCanUploadFiles(String workBaseDir, String configFilePath, FtpConf ftpconf,
			FtpUploadConf ftpUploadConf) throws Exception {
		String wkdir = workBaseDir + "/" + new File(configFilePath).getName();

		Map<?, ?> lastModMap = readLastModMap(wkdir);

		List<FtpUploadConf> expandFtpUploadConfList = expandFtpUploadConf(ftpconf, ftpUploadConf);
		List<FtpUploadConf> canFtpUploadConfList = new ArrayList<FtpUploadConf>();
		for (FtpUploadConf expandFtpUploadConf : (ArrayList<FtpUploadConf>) expandFtpUploadConfList) {
			String path = expandFtpUploadConf.getClientPath();
			if ((lastModMap.containsKey(path))
					&& (((String) lastModMap.get(path)).equals(expandFtpUploadConf.getFileMd5())))
				continue;
			canFtpUploadConfList.add(expandFtpUploadConf);
		}

		return canFtpUploadConfList;
	}

	public static void executeUpload(FtpConf conf, List<FtpUploadConf> expandFtpUploadConfList,
			IFtpStreamMonitor monitor, AbstractSexftpView srcView) {
		FtpPools ftpPools = new FtpPools(conf, new Consoleable() {
			public void console(String str) {
				System.out.println(str);
			}
		});
		XFtp ftp = ftpPools.getFtp();
		synchronized (ftp) {
			ftpPools.getConnectedFtp();
			final ExistFtpFile existFtpFile = new ExistFtpFile(ftp);
			try {
				String servPath = null;
				int uploadCount = expandFtpUploadConfList.size();
				Integer remembert = null;
				for (FtpUploadConf expandFtpUploadConf : expandFtpUploadConfList) {
					if (!expandFtpUploadConf.getServerPath().equals(servPath)) {
						monitor.printSimple("upload in " + expandFtpUploadConf.getServerPath());
						ftp.cdOrMakeIfNotExists(expandFtpUploadConf.getServerPath());
					}
					servPath = expandFtpUploadConf.getServerPath();
					final FtpFile existsFtpFile = existFtpFile
							.existsFtpFile(servPath + new File(expandFtpUploadConf.getClientPath()).getName());
					
					int returncode = 0;
					if ((existsFtpFile != null) && (PluginUtil.overwriteTips().booleanValue())) {
						if (remembert == null) {
							MessageDialogWithToggle t = new Question().question(srcView,
									"File [" + existsFtpFile.getName() + "] Exists,Overwrite?",
									"Remember Me In This Operation!");
							if (t.getToggleState()) {
								remembert = Integer.valueOf(t.getReturnCode());
							}
							returncode = t.getReturnCode();
						} else {
							returncode = remembert.intValue();
						}
						if (returncode != 2) {
							if (returncode == 1) {
								throw new AbortException();
							}
							if (returncode == 3) {
								--uploadCount;
								continue;
							}

							--uploadCount;
						}
					}
					// 无数据或者需要覆盖的情况
					if ( existsFtpFile == null || returncode == 2 ){
						monitor.printStreamString(expandFtpUploadConf, 0L, 0L, "");
						ftp.upload(expandFtpUploadConf.getClientPath(), new IFtpStreamMonitor() {
							public void printStreamString(FtpUploadConf ftpUploadConf, long uploadedSize,
									long totalSize, String info) {
								System.out.println(ftpUploadConf + " " + uploadedSize + " " + totalSize + " " + info);
							}

							public void printSimple(String info) {
								System.out.println(info + " " + ((existsFtpFile != null)
										? "(overwrites:" + existsFtpFile.getName() + ")" : ""));
							}
						});
					}
				}
				monitor.printSimple("Uploaded [" + uploadCount + "] Files!");
			} catch (AbortException localAbortException) {
			} finally {
			}
		}
	}

	private static List<FtpUploadConf> expandFtpUploadConf(FtpUploadConf ftpUploadConf) {
		String clientPath = ftpUploadConf.getClientPath();
		File file = new File(clientPath);
		List<FtpUploadConf> expandFtpUploadConfList = new ArrayList<FtpUploadConf>();
		expandFtpUploadConf(file, ftpUploadConf, expandFtpUploadConfList);
		return expandFtpUploadConfList;
	}

	private static void expandFtpUploadConf(File file, FtpUploadConf ftpUploadConf,
			List<FtpUploadConf> expandFtpUploadConfList) {
		if (file.isHidden())
			return;

		if (file.isDirectory()) {
			File[] subFiles = file.listFiles();
			for (File subFile : subFiles) {
				expandFtpUploadConf(subFile, ftpUploadConf, expandFtpUploadConfList);
			}
		} else {
			String clientFileFolderPath = file.getParentFile().getAbsolutePath();

			String srClientPath = ftpUploadConf.getClientPath();
			String srServerPath = ftpUploadConf.getServerPath();
			if (!srServerPath.endsWith("/"))
				throw new RuntimeException(srServerPath + " not end with /");
			String abPath = "";
			if (clientFileFolderPath.length() >= srClientPath.length()) {
				abPath = clientFileFolderPath.substring(srClientPath.length()).replace('\\', '/');
			}
			if (!abPath.endsWith("/")) {
				abPath = abPath + "/";
			}
			FtpUploadConf expandFtpUploadConf = new FtpUploadConf();
			expandFtpUploadConf.setClientPath(file.getAbsolutePath());
			expandFtpUploadConf.setServerPath((srServerPath + abPath).replaceAll("//", "/"));
			expandFtpUploadConf.setFileMd5(FileMd5.getMD5(file));
			expandFtpUploadConfList.add(expandFtpUploadConf);
		}
	}

	public static class Question {
		private MessageDialogWithToggle tg = null;
		public MessageDialogWithToggle question(final AbstractSexftpView srcView, final String msg, final String toggle) {
			Display.getDefault().syncExec(new SexftpRun(srcView) {
				public void srun() throws Exception {
					Question.this.tg = srcView.showQuestion(msg, toggle);
				}
			});
			return this.tg;
		}
	}
}
