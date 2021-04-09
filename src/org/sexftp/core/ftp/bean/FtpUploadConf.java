package org.sexftp.core.ftp.bean;

import org.sexftp.core.Tosimpleable;
import org.sexftp.core.utils.StringUtil;
import org.w3c.dom.*;
import java.util.ArrayList;

public class FtpUploadConf implements Tosimpleable {
	private String clientPath;
	private String serverPath;
	private String fileMd5;
	private String[] excludes;
	private String[] includes;

	public String getClientPath() {
		return this.clientPath;
	}

	public void setClientPath(String clientPath) {
		this.clientPath = clientPath;
	}

	public String getServerPath() {
		return this.serverPath;
	}

	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}

	public String getFileMd5() {
		return this.fileMd5;
	}

	public void setFileMd5(String fileMd5) {
		this.fileMd5 = fileMd5;
	}

	public String[] getExcludes() {
		return this.excludes;
	}

	public void setExcludes(String[] excludes) {
		this.excludes = excludes;
	}

	public String toString() {
		return String.format("%s <-> %s", new Object[] { this.clientPath, this.serverPath });
	}

	public String[] getIncludes() {
		return this.includes;
	}

	public void setIncludes(String[] includes) {
		this.includes = includes;
	}

	public String toSimpleString() {
		return toSimpleString(60);
	}

	public String toSimpleString(int maxlen) {
		return String.format("%s <-> %s", new Object[] { StringUtil.simpString(this.clientPath, maxlen),
				StringUtil.simpString(this.serverPath, maxlen) });
	}
	public String toXML() {
		String xml = "\t<ftpUploadConfList class=\"org.sexftp.core.ftp.bean.FtpUploadConf\">\r\n";
		xml += "\t\t<clientPath>" + this.clientPath + "</clientPath>\r\n";
        xml += "\t\t<serverPath>" + this.serverPath + "</serverPath>\r\n";
        for (String s : this.excludes) {
        	xml += "\t\t<excludes>" + s + "</excludes>\r\n";
        }
        for (String s: this.includes) {
        	xml += "\t\t<includes>" + s + "</includes>\r\n";
        }
    	xml += "\t</ftpUploadConfList>\r\n";
    	return xml;
	}
	public void parseXML(NodeList nodeLst) {
		ArrayList<String> excludesLst = new ArrayList<String>();
		ArrayList<String> includeLst = new ArrayList<String>();
		for (int i = 0; i < nodeLst.getLength(); ++i) {
        	Node node = nodeLst.item(i);
        	if (node.getNodeType() != Node.ELEMENT_NODE) {
        		continue;
        	}
        	String name = node.getNodeName();
        	String value = node.getTextContent();
        	if (name == "clientPath") {
        		this.clientPath = value;
        	} else if (name == "serverPath") {
        		this.serverPath = value;
        	} else if (name == "excludes") {
        		excludesLst.add(value);
        	} else if (name == "includes") {
        		includeLst.add(value);
        	} 
//        	System.out.println("name:" + name + ",value:" + value);
        }
		
		if (excludesLst.size() > 0) {
			this.excludes = new String[excludesLst.size()];
			for (int i = 0; i < excludesLst.size(); ++i) {
				this.excludes[i] = excludesLst.get(i);
			}
		} else {
			this.excludes = null;
		}
		if (includeLst.size() > 0) {
			this.includes = new String[includeLst.size()];
			for (int i = 0; i < includeLst.size(); ++i) {
				this.includes[i] = includeLst.get(i);
			}
		} else {
			this.includes = null;
		}
	}
}
