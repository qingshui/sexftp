package org.sexftp.core.bean;

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
}
