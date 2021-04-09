package org.sexftp.core.bean;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LanguageItem {
	private String key = "";
	private String enus = "";
	private String zhcn = "";

	public LanguageItem(String key) {
		this.enus = key;
		this.key = key;
	}

	public LanguageItem() {
	}

	public String getEnus() {
		return this.enus;
	}

	public void setEnus(String enus) {
		this.enus = enus;
	}

	public String getZhcn() {
		return this.zhcn;
	}

	public void setZhcn(String zhcn) {
		this.zhcn = zhcn;
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	public static LanguageItem fromXML(NodeList nodeLst) {
		LanguageItem item = new LanguageItem();
		for (int i = 0; i < nodeLst.getLength(); ++i) {
        	Node node = nodeLst.item(i);
        	if (node.getNodeType() != Node.ELEMENT_NODE) {
        		continue;
        	}
        	String name = node.getNodeName();
        	String value = node.getTextContent();
        	if (name == "key") {
        		item.key = value;
        	} else if (name == "enus") {
        		item.enus = value;
        	} else if (name == "zhcn") {
        		item.zhcn = value;
        	} 
        }
		return item;
	}
	public String toXML() {
		String xml = "\t<languageItem>\r\n";
	    xml += "\t\t<key>" + this.key + "</key>\r\n";
	    xml += "\t\t<enus>" + this.enus + "</enus>\r\n";
	    xml += "\t\t<zhcn>" + this.zhcn + "</zhcn>\r\n";
	    xml += "\t</languageItem>\r\n";
	    return xml;
	}
}
