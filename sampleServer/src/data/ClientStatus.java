package data;



public class ClientStatus {
	private final String ip;
	private final String hostName;
	private String status;

	public ClientStatus(String ip, String hostName, String status) {
		this.ip = ip;
		this.hostName = hostName;
		this.status = status;
	}

	public String getIp() {
		return ip;
	}

	public String getHostName() {
		return hostName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}