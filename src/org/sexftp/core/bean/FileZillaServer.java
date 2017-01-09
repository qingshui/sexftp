package org.sexftp.core.bean;

public class FileZillaServer {
	private String Host;
	private String Port;
	private String Protocol;
	private String User;
	private String Pass;

	public String getHost() {
		return this.Host;
	}

	public void setHost(String host) {
		this.Host = host;
	}

	public String getPort() {
		return this.Port;
	}

	public void setPort(String port) {
		this.Port = port;
	}

	public String getProtocol() {
		return this.Protocol;
	}

	public void setProtocol(String protocol) {
		this.Protocol = protocol;
	}

	public String getUser() {
		return this.User;
	}

	public void setUser(String user) {
		this.User = user;
	}

	public String getPass() {
		return this.Pass;
	}

	public void setPass(String pass) {
		this.Pass = pass;
	}

	public int hashCode() {
		int result = 1;
		result = 31 * result + ((this.Host == null) ? 0 : this.Host.hashCode());
		result = 31 * result + ((this.Pass == null) ? 0 : this.Pass.hashCode());
		result = 31 * result + ((this.Port == null) ? 0 : this.Port.hashCode());
		result = 31 * result + ((this.Protocol == null) ? 0 : this.Protocol.hashCode());
		result = 31 * result + ((this.User == null) ? 0 : this.User.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (super.getClass() != obj.getClass())
			return false;
		FileZillaServer other = (FileZillaServer) obj;
		if (this.Host == null)
			if (other.Host != null)
				return false;
			else if (!this.Host.equals(other.Host))
				return false;
		if (this.Pass == null)
			if (other.Pass != null)
				return false;
			else if (!this.Pass.equals(other.Pass))
				return false;
		if (this.Port == null)
			if (other.Port != null)
				return false;
			else if (!this.Port.equals(other.Port))
				return false;
		if (this.Protocol == null)
			if (other.Protocol != null)
				return false;
			else if (!this.Protocol.equals(other.Protocol))
				return false;
		if (this.User == null)
			if (other.User != null)
				return false;
			else if (!this.User.equals(other.User))
				return false;
		return true;
	}
}
