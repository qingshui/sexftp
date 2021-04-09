package org.sexftp.core.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.desy.textparse.SmartText;
import org.desy.textparse.context.SmartTextContext;
import org.desy.textparse.exceptions.FunExecuteException;
import org.desy.textparse.exceptions.FunParseException;
import org.desy.textparse.funexecutors.AbstractFunParamActiveExeutor;
import org.desy.textparse.interfaces.FunParseable;
import org.sexftp.core.ftp.bean.FtpUploadConf;
import org.sexftp.core.utils.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sexftp.uils.PluginUtil;

public class LanguageConf {
	private String defaultLang = "";
	private List<LanguageItem> langList = new ArrayList<LanguageItem>();

	private Map<String, LanguageItem> langMap = null;

	private static final String FUN_K = "##FUN" + new Date().getTime() + "##";

	public List<LanguageItem> getLangList() {
		return this.langList;
	}

	public void setLangList(List<LanguageItem> langList) {
		this.langList = langList;
	}

	public String getDefaultLang() {
		return this.defaultLang;
	}

	public void setDefaultLang(String defaultLang) {
		this.defaultLang = defaultLang;
	}

	public Map<String, LanguageItem> getLangMap() {
		return this.langMap;
	}

	public void setLangMap(Map<String, LanguageItem> langMap) {
		this.langMap = langMap;
	}

	public String getLangText(String text) {
		String defaultlang = PluginUtil.getLanguage();
		if (defaultlang == null)
			defaultlang = "enus";
		List<String> params = new ArrayList<String>();
		if (text != null) {
			text = fixStr(text, params);
		}
		if (this.langMap == null) {
			Map<String, LanguageItem> langMapTmp = new HashMap<String, LanguageItem>();
			if (this.langList != null) {
				for (LanguageItem langitem : this.langList) {
					langMapTmp.put(langitem.getKey(), langitem);
				}
			}
			this.langMap = langMapTmp;
		}
		LanguageItem langItem = (LanguageItem) this.langMap.get(text);
		String ok = "";
		if (langItem != null) {
			if (("zhcn".equals(defaultlang)) && (langItem.getZhcn() != null) && (langItem.getZhcn().length() > 0)) {
				ok = langItem.getZhcn();
			} else if ((langItem.getEnus() != null) && (langItem.getEnus().length() > 0)) {
				ok = langItem.getEnus();
			} else {
				ok = text;
			}
		} else {
			synchronized (this.langList) {
				if (!this.langMap.containsKey(text))
					this.langList.add(new LanguageItem(text));
			}
			ok = text;
		}
		if (ok == null)
			ok = "";
		return (String) String.format(ok, params.toArray());
	}

	String fixStr(String str, List<String> params) {
		if (str.length() > 0) {
			str = StringUtil.replaceAll(str, "(", FUN_K + "Ykh(");
			str = StringUtil.replaceAll(str, "[", FUN_K + "Fkh(");
			str = StringUtil.replaceAll(str, "]", ")");
			SmartTextContext ctx = new SmartTextContext();
			Ykh ykh = new Ykh("yhk", params);
			Ykh fkh = new Ykh("fhk", params);
			ctx.getFunConfig().registerFun("Ykh", ykh);
			ctx.getFunConfig().registerFun("Fkh", fkh);
			ctx.getFunConfig().setFeatureStr(FUN_K);
			SmartText s = new SmartText(ctx);
			str = (String) s.parse(str);
			return str;
		}
		return null;
	}

	public class Ykh extends AbstractFunParamActiveExeutor {
		private List<String> params = new ArrayList<String>();
		private String pix = "yhk";

		public Ykh(String pix, List<String> params) {
			this.pix = pix;
			this.params = params;
		}

		@Override
		public Object exec(SmartTextContext arg0, Object... args) throws FunExecuteException {
			String ag = "";
			if (args != null) {
				for (int i = 0; i < args.length; ++i) {
					if (i > 0) {
						ag = ag + arg0.getFunConfig().getParamSeparater();
					}
					ag = ag + args[i];
				}
			}
			this.params.add(ag);
			if (this.pix.equals("yhk")) {
				return "(%s)";
			}
			if (this.pix.equals("fhk")) {
				return "[%s]";
			}
			return "%s";
		}

		public Object[] parseParams(List<FunParseable> arg0, SmartTextContext arg1) throws FunParseException {
			return super.parseParams(arg0, arg1);
		}

		public String[] getParams() {
			return (String[]) this.params.toArray(new String[0]);
		}
	}
	public void parseConf(NodeList childList) {
		this.langList = new ArrayList<LanguageItem>();
        for (int i = 0; i < childList.getLength(); ++i) {
        	Node node = childList.item(i);
        	if (node.getNodeType() != Node.ELEMENT_NODE) {
        		continue;
        	}
        	String name = node.getNodeName();
        	String value = node.getTextContent();
        	if (name == "defaultLang") {
        		this.defaultLang = value;
        	} else if (name == "languageItem") {
        		this.langList.add(LanguageItem.fromXML(node.getChildNodes()));
        	}
//        	System.out.println("name:" + name + ",value:" + value);
        }
	}
	public static LanguageConf fromXML(String xmlfile) {
		LanguageConf conf = null;
		//1.创建DocumentBuilderFactory对象
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //2.创建DocumentBuilder对象
        try {
        	conf = new LanguageConf();
            DocumentBuilder builder = factory.newDocumentBuilder();
//            System.out.println(xmlfile);
            Document d = builder.parse(xmlfile);
            conf.parseConf(d.getElementsByTagName("org.sexftp.core.bean.LanguageConf").item(0).getChildNodes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conf;
	}
	public String toXML() {
		String xml = "<org.sexftp.core.bean.LanguageConf>\r\n";
		xml += "\t<defaultLang>" + this.defaultLang + "</defaultLang>\r\n";
		for (LanguageItem item : this.langList) {
			xml += item.toXML();
		}
		xml += "</org.sexftp.core.bean.LanguageConf>\r\n";
		return xml;
	}
}