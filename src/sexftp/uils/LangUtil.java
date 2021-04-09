package sexftp.uils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.core.runtime.Platform;
import org.sexftp.core.bean.LanguageConf;
import org.sexftp.core.bean.LanguageItem;
import org.sexftp.core.exceptions.SRuntimeException;
import org.sexftp.core.utils.FileUtil;

public class LangUtil {
	private static LanguageConf langconf = null;
	private static String langfile = null;
	private static String plugPath = null;

	@SuppressWarnings("deprecation")
	public static String langText(String text) {
		if (text == null)
			return "";
		if (langconf == null) {
			try {
				plugPath = Platform.asLocalURL(Platform.getBundle("sexftp").getEntry("")).getFile();
				langfile = plugPath + "/languages/lang.xml";
				if (new File(langfile).exists()) {
					langconf = LanguageConf.fromXML(langfile);
				} else {
					langconf = new LanguageConf();
				}
			} catch (IOException e) {
				e.printStackTrace();
				langconf = new LanguageConf();
			}
		}
		if (langconf.getLangList() == null) {
			langconf.setLangList(new ArrayList<LanguageItem>());
		}
		int lanSize = langconf.getLangList().size();
		String langText = langconf.getLangText(text);
		if ((langconf.getLangList().size() > lanSize) && (langfile != null)) {
			Map<String, LanguageItem> langMap = langconf.getLangMap();
			langconf.setLangMap(null);

			Map<String, LanguageItem> lMap = new LinkedHashMap<String, LanguageItem>();
			for (LanguageItem li : langconf.getLangList()) {
				lMap.put(li.getEnus(), li);
			}
			langconf.setLangList(new ArrayList<LanguageItem>(lMap.values()));

			String newXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + langconf.toXML();
			langconf.setLangMap(langMap);
			try {
				FileUtil.writeByte2File(langfile, newXml.getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				throw new SRuntimeException(e);
			}
		}
		return langText;
	}

	public static void main(String[] args) {
	}
}
