package data;

import java.io.Serializable;

public class Login implements Serializable {
	private static final long serialVersionUID = 1L;
	private String username, passwrod;

	public Login(String username, String passwrod) {
		this.username = username;
		this.passwrod = passwrod;
	}

	public String getUsername() {
		return username;
	}

	public String getPasswrod() {
		return passwrod;
	}

}
