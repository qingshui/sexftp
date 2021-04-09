package org.sexftp.core.ftp.bean;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class FtpConf {
	String host = "localhost";
	Integer port = Integer.valueOf(21);
	String username = "root";
	String password;
	String serverType = "ftp";
	String name;
	private List<FtpUploadConf> ftpUploadConfList;

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServerType() {
		return this.serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public List<FtpUploadConf> getFtpUploadConfList() {
		return this.ftpUploadConfList;
	}

	public void setFtpUploadConfList(List<FtpUploadConf> ftpUploadConfList) {
		this.ftpUploadConfList = ftpUploadConfList;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return String.format("%s[%s - %s:%s@%s]",
				new Object[] { this.name, this.serverType, this.host, this.port, this.username });
	}
	public String toXML() {
		String xml = "<FtpConf class=\"org.sexftp.core.ftp.bean.FtpConf\">\r\n";
		xml += "\t<host>" + this.host + "</host>\r\n";
		xml += "\t<port>" + String.valueOf(this.port) + "</port>\r\n";
		xml += "\t<username>" + this.username + "</username>\r\n";
		xml += "\t<password>" + this.password + "</password>\r\n";
		xml += "\t<serverType>" + this.serverType + "</serverType>\r\n";
		xml += "\t<name>" + this.name + "</name>\r\n";
		for (FtpUploadConf conf : this.ftpUploadConfList) {
			xml += conf.toXML();
		}
		xml += "</FtpConf>\r\n";
		return xml;
	}
	private void parseConf(NodeList childList) {
		this.ftpUploadConfList = new ArrayList<FtpUploadConf>();
        for (int i = 0; i < childList.getLength(); ++i) {
        	Node node = childList.item(i);
        	if (node.getNodeType() != Node.ELEMENT_NODE) {
        		continue;
        	}
        	String name = node.getNodeName();
        	String value = node.getTextContent();
        	if (name == "host") {
        		this.host = value;
        	} else if (name == "port") {
        		this.port = Integer.valueOf(value);
        	} else if (name == "username") {
        		this.username = value;
        	} else if (name == "password") {
        		this.password = value;
        	} else if (name == "serverType") {
        		this.serverType = value;
        	} else if (name == "name") {
        		this.name = value;
        	} else if (name == "ftpUploadConfList") {
        		FtpUploadConf ftpUpload = new FtpUploadConf();
        		ftpUpload.parseXML(node.getChildNodes());
        		this.ftpUploadConfList.add(ftpUpload);
        	}
//        	System.out.println("name:" + name + ",value:" + value);
        }
	}
	public void parseXML(String xmlfile) {
		//1.创建DocumentBuilderFactory对象
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //2.创建DocumentBuilder对象
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
//            System.out.println(xmlfile);
            Document d = builder.parse(xmlfile);
            parseConf(d.getElementsByTagName("FtpConf").item(0).getChildNodes());
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	public static FtpConf xml2Bean(String xmlfile) {
		FtpConf conf = new FtpConf();
		conf.parseXML(xmlfile);
		return conf;
	}
}
