package sexftp.install;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.sexftp.core.utils.ByteUtils;
import org.sexftp.core.utils.FileUtil;

public class Install {
	private static String lang = "en";
	private static String curDir = ".";

	private static Set<String> findedPathSet = new HashSet<String>();
	private static long startTime = 0L;

	public static void main(String[] args) throws Exception {
		URL resource = Install.class.getClassLoader().getResource("sexftp/install/Install.class");

		String url = URLDecoder.decode(resource.getFile(), "utf-8");
		if (url.indexOf(".jar") > 0) {
			url = url.replace("file:/", "");
			url = url.substring(0, url.indexOf(".jar") + 4);
			if (new File(url).exists()) {
				curDir = new File(url).getParent();
				System.out.println("Install Dir Changed:" + curDir);
			}
		}

		System.out.println("[sexftp]  Chose Language:");
		String lan = readStringFromSystemin("2", new String[] { "1 - English", "2 - 简体中文" });
		if (lan.equals("2"))
			lang = "zh";

		note();

		println("[sexftp]  Are You Sure To Go Ahead Install?", "[sexftp]  准备好了安装Sexftp吗？");
		String okInstallBegin = readStringFromSystemin("1", new String[] { "1 - Ok", "2 - Cancel" });
		if (okInstallBegin.equals("2")) {
			System.exit(-1);
		}

		File[] findFile = findFile();
		String[] inItem = new String[findFile.length + 1];
		println("[sexftp]  Chose Eclispe Folder", "[sexftp]  选择eclipse安装目录:");
		for (int i = 0; i < findFile.length; ++i) {
			File file = findFile[i];

			inItem[i] = (i + 1 + " - " + file.getParentFile().getParentFile().getParentFile());
		}
		inItem[findFile.length] = (findFile.length + 1 + " - Not above?以上都不是?");
		String input = readStringFromSystemin(null, inItem);

		if (Integer.parseInt(input) == findFile.length + 1) {
			note();

			println("[sexftp]  Make Sure And Run Me Again!", "[sexftp]  请确认好后再运行本安装程序！");
			System.exit(-1);
		}

		File okfile = findFile[(Integer.parseInt(input) - 1)];

		System.out.println("[sexftp]  将安装到:" + okfile);
		while (true) {
			byte[] configdatas = FileUtil.readBytesFromInStream(new FileInputStream(okfile));
			String configHex = ByteUtils.getHexString(configdatas);
			String sexftpstr = ByteUtils.getHexString("sexftp,".getBytes("utf-8"));
			if (configHex.indexOf(sexftpstr) < 0) {
				break;
			}
			String sexftpendstr = ByteUtils.getHexString(",4,false".getBytes("utf-8"));

			String enterHex = ByteUtils.getHexString("\n".getBytes("utf-8"));
			String newLineHex = ByteUtils.getHexString("\r".getBytes("utf-8"));

			String[] sfhsps = configHex.split(sexftpstr)[1].split(sexftpendstr);
			String sexftpItemHex = sexftpstr + sfhsps[0] + sexftpendstr;

			configHex = configHex.replace(enterHex + newLineHex + sexftpItemHex, "").trim();
			configHex = configHex.replace(newLineHex + enterHex + sexftpItemHex, "").trim();
			configHex = configHex.replace(enterHex + sexftpItemHex, "").trim();
			configHex = configHex.replace(sexftpItemHex, "").trim();

			println("[sexftp]  Sexftp Intalled,Will Unistall!", "[sexftp]  发现已经装了Sexftp，将先卸载她！");
			println(new String(ByteUtils.getByteArray(sexftpItemHex), "utf-8"));

			String isokUnistall = readStringFromSystemin("1", new String[] { "1 - Ok", "2 - Cancel" });
			if (isokUnistall.equals("2")) {
				System.exit(-1);
			}

			ByteUtils.writeByte2Stream(ByteUtils.getByteArray(configHex), new FileOutputStream(okfile));
		}

		println("[Sexftp]  Installing...", "[Sexftp]  安装中...");
		install(okfile, new File(new File(curDir).getAbsolutePath()));
		println("[Sexftp]  Installed Success! Restart Eclipse/Myeclipse to Start!",
				"[Sexftp]  安装成功，重启 Eclipse/Myeclise即可生效。");
		println("[Sexftp]  If Not Success By Thes Install Program,Contact Us!", "[Sexftp]  如果本安装程序没能成功，请联系我们解决。");
		input = readStringFromSystemin("1", new String[] { "1 - Exit." });
	}

	public static void note() {
		println("[Sexftp]  Make Sure You Done One of these as follows:\r\n   1、move install files to Eclipse/Myeclipse install directory.\r\n   2、Run The Eclipse/Myeclise.\r\nIf you Do One Of Them,I Can find Eclipse/Myeclipse to Install Sexftp.",
				"[Sexftp]  确定你做了如下两件事之一：\r\n   1、将本安装程序解压在 Eclipse/Myeclipse安装目录再运行。\r\n   2、运行 Eclipse/Myeclipse。\r\n以上两种方法只要选择一种即可， 本安装程序就能找到 Eclipse/Myeclipse以便安装 Sexftp 插件.");
	}

	public static void install(File okfile, File meFolder) {
		File sexftpJar = null;
		File[] listFiles = meFolder.listFiles();
		if (listFiles != null) {
			Arrays.sort(listFiles);
			for (int i = listFiles.length - 1; i >= 0; --i) {
				if (!listFiles[i].getName().endsWith(".jar"))
					continue;
				sexftpJar = listFiles[i];
				break;
			}
		}

		if (sexftpJar == null) {
			println("[Sexftp]  Plugin Jar Losted!", "[Sexftp]  插件Jar丢失！");
			println("In The Folder:", "在这个目录下：");
			System.out.println(meFolder);
			System.exit(-1);
		}

		File newPluginFolder = new File(
				okfile.getParentFile().getParentFile().getParentFile().getAbsolutePath() + "/sexftp/");
		if (!newPluginFolder.exists()) {
			newPluginFolder.mkdirs();
		}
		FileUtil.copyFile(sexftpJar.getAbsolutePath(), newPluginFolder.getAbsolutePath() + "/" + sexftpJar.getName());
		String mefolderpath = newPluginFolder.getAbsolutePath().replace('\\', '/');
		if (!mefolderpath.endsWith("/"))
			mefolderpath = mefolderpath + "/";
		mefolderpath = mefolderpath + sexftpJar.getName();

		String addconfigStr = sexftpJar.getName().replace('_', ',').replace(".jar", "") + ",file:/" + mefolderpath
				+ ",4,false";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(okfile, true));
			bw.write("\r\n" + addconfigStr);
			bw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static File[] findFile() throws Exception {
		File f = new File(curDir);
		println("[sexftp]  Find eclipse From:", "[sexftp]  从这里开始寻找eclipse:");
		println(f.getAbsolutePath());
		println("[sexftp]  It may taken 10 Seconds.", "[sexftp]  可能需要10秒钟的时间.");

		f = findEclpseFile(new File(f.getAbsolutePath()));
		List<File> files = new ArrayList<File>();
		if (f != null) {
			files.add(f);
		}

		try {
			Process process = Runtime.getRuntime().exec("wmic process get ExecutablePath");
			String in = new String(FileUtil.readBytesFromInStream(process.getInputStream()));
			String[] its = in.split("\n");

			for (String it : its) {
				if (!it.trim().endsWith("eclipse.exe"))
					continue;
				startTime = System.currentTimeMillis();
				File okf = findEclipseFile(new File(it.trim()).getParentFile());
				if (okf != null) {
					files.add(okf);
				}
			}
		} catch (Exception localException) {
			if (files.size() == 0) {
				println("[sexftp]  Try Find Windows edclipse Process Failed,You Can Do[WINDOWS KEY->RUN] Input WMIC Then Enter!After That,Rerun Me!",
						"[sexftp]  搜索eclipse进程失败，你可以先作这个操作:windows键->运行 输入WMIC回车,然后重新运行本安装程序。 ");
			}
		}
		return (File[]) files.toArray(new File[0]);
	}

	public static File findEclpseFile(File from) {
		startTime = System.currentTimeMillis();
		File st = from;
		for (int i = 0; (i < 20) && (st != null); ++i) {
			File findOk = findEclipseFile(st);
			findedPathSet.add(st.getAbsolutePath());
			if (findOk != null) {
				return findOk;
			}
			st = st.getParentFile();
		}
		return null;
	}

	public static File findEclipseFile(File f) {
		long findTime = System.currentTimeMillis() - startTime;
		if (findTime > 10000L) {
			return null;
		}
		if (findedPathSet.contains(f.getAbsolutePath())) {
			return null;
		}
		File maybefile = null;
		if (f.isDirectory()) {
			String maybePath = f.getAbsolutePath()
					+ "/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info";
			maybefile = new File(maybePath);
			if (maybefile.exists()) {
				return maybefile;
			}
			maybefile = null;
			if (f.listFiles() != null) {
				for (File subf : f.listFiles()) {
					maybefile = findEclipseFile(subf);
					if (maybefile != null) {
						break;
					}
				}
			}
		}
		return maybefile;
	}

	public static String readStringFromSystemin(String defaultVal, String[] item) throws Exception {
		Set<String> set = new LinkedHashSet<String>();
		for (String it : item) {
			String[] os = it.split("-");
			if (os.length < 2)
				throw new RuntimeException(it + " use error '-' flag!");
			set.add(os[0].trim());
			System.out.println("        " + it);
		}
		String r;
		do {
			System.out.print("[Input] Please Chose " + Arrays.toString(set.toArray()) + " "
					+ ((defaultVal == null) ? "" : defaultVal) + ":");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			r = br.readLine();
			if ((r.trim().length() == 0) && (defaultVal != null)) {
				return defaultVal;
			}
		} while (!set.contains(r));

		return r;
	}

	public static void print(String en, String zh) {
		if (lang.equals("zh")) {
			System.out.print(zh);
		} else {
			System.out.print(en);
		}
	}

	public static void println(String en, String zh) {
		if (lang.equals("zh")) {
			System.out.println(zh);
		} else {
			System.out.println(en);
		}
	}

	public static void println(String str) {
		System.out.println(str);
	}
}