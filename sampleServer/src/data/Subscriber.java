package data;

import java.io.Serializable;

public class Subscriber implements Serializable {

	private String username;
	private String password;
	private String phoneNumber;
	private String email;
	private int code;

	/**
	 * @param id          id of the subscriber (unique id)
	 * @param usrname     username of the subscriber
	 * @param password    subscriber's password
	 * @param phoneNumber subscriber's phoneNumber
	 * @param email       subscriber's email
	 * @param code        subscriber's code (unique code too)
	 */

	public Subscriber(String usrname, String password, String phoneNumber, String email, int code) {
		this.username = usrname;
		this.password = password;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.code = code;
	}

	
	public Subscriber(int code) {
		this.code = code;
	}


	/**
	 * get username of the subscriber
	 * 
	 * @return
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * set new value for username
	 * 
	 * @param usrname
	 */
	public void setUsername(String usrname) {
		this.username = usrname;
	}

	/**
	 * method to get password
	 * 
	 * @return subscriber's password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * method to set new value for password
	 * 
	 * @param password new value
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * method to get phoneNumber
	 * 
	 * @return subscriber's phoneNumber
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * method to set new value for phoneNumber
	 * 
	 * @param phoneNumber new value
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * method to get email
	 * 
	 * @return sbscriber's email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * method to set new value for email
	 * 
	 * @param email new value
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * method to get code
	 * 
	 * @return subscriber's code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * set new value for code
	 * 
	 * @param code
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * method to display the details - username & password
	 */
	@Override
	public String toString() {
		return "username " + username + " password " + password + " code " + code;
	}
}
