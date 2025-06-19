package data;

import java.io.Serializable;

public class Worker implements Serializable{

	private static final long serialVersionUID = 1L;
	private String username,password;
	
	public Worker(String username,String password) {
		this.setUsername(username);
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
