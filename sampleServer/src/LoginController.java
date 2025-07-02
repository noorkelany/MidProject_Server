import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import data.Login;
import data.Manager;
import data.ResponseWrapper;
import data.Subscriber;
import data.Worker;

public class LoginController {
	private mysqlConnection instance = null;
	Connection conn = null;

	public void getDBConnection() {
		try {
			instance = mysqlConnection.getInstance();
			conn = instance.getConnection();
		} catch (Exception e) {
			System.out.println("Error connecting to DB");
		}
	}

	public LoginController() {
		this.getDBConnection();
	}

	/**
	 * check if the subscriber entered the right username and password
	 * 
	 * @param con
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean validSubscriber(ResponseWrapper response) {
		Login loginDetails = (Login) response.getData();
		Statement stmt;
		boolean exists = false;
		try {
			System.out.println("Server " + loginDetails.getUsername() + "  " + loginDetails.getPasswrod());
			stmt = conn.createStatement();
			PreparedStatement ps = conn
					.prepareStatement("SELECT * FROM `subscriber` WHERE BINARY username=(?) AND BINARY password=(?)");
			ps.setString(1, loginDetails.getUsername());
			ps.setString(2, loginDetails.getPasswrod());
			ResultSet rs = ps.executeQuery();
			exists = rs.next();
			System.out.println("Exists? " + exists);
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return exists;
	}

	/**
	 * method for checking if any user exists (correct username & password) if so
	 * retreive type of user( subscriber, parking attendant, manager)
	 * 
	 * @param username username to check
	 * @param password password to check
	 * @return type of user (subscriber, parking attendant, manager) if doesn't
	 *         exist - invalid
	 */
	public String validateLogin(String username, String password) {
	    String sqlWorker = "SELECT * FROM workers WHERE BINARY username=? AND BINARY password=?";
	    String sqlSubscriber = "SELECT * FROM subscribers WHERE BINARY username=? AND code=?";
	    String sqlManager = "SELECT * FROM managers WHERE BINARY username=? AND BINARY password=?";
	    
	    try {
	        // Check worker
	        PreparedStatement psWorker = conn.prepareStatement(sqlWorker);
	        psWorker.setString(1, username);
	        psWorker.setString(2, password);
	        ResultSet rsWorker = psWorker.executeQuery();
	        if (rsWorker.next()) {
	            return "worker";
	        }

	        // Check subscriber (only if password is a number)
	        try {
	            int code = Integer.parseInt(password);
	            PreparedStatement psSub = conn.prepareStatement(sqlSubscriber);
	            psSub.setString(1, username);
	            psSub.setInt(2, code);
	            ResultSet rsSub = psSub.executeQuery();
	            if (rsSub.next()) {
	                return "subscriber";
	            }
	        } catch (NumberFormatException e) {
	            // Not a valid code, skip subscriber check
	        }

	        // Check manager
	        PreparedStatement psMng = conn.prepareStatement(sqlManager);
	        psMng.setString(1, username);
	        psMng.setString(2, password);
	        ResultSet rsMng = psMng.executeQuery();
	        if (rsMng.next()) {
	            return "manager";
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return "invalid";
	}


	/**
	 * method to retreive subscriber by username
	 * 
	 * @param username username of the subscriber
	 * @return return the subscriber with all it's details
	 */
	public Subscriber getSubscriberByUsername(String username) {
		String sql = "SELECT * FROM `subscribers` WHERE username = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				// String usrname, String password, String phoneNumber, String email, int code
				Subscriber s = new Subscriber(rs.getString("username"), rs.getString("password"),
						rs.getString("phoneNumber"), rs.getString("email"), rs.getInt("code"));
				// Add other fields as needed
				return s;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * method to retreive subscriber by code
	 * 
	 * @param code subscriber's code
	 * @return return the subscriber with all it's details
	 */
	public Subscriber getSubscriberByCode(int code) {
		String sql = "SELECT * FROM `subscribers` WHERE code = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, code);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				// String usrname, String password, String phoneNumber, String email, int code
				Subscriber s = new Subscriber(rs.getString("username"), rs.getString("password"),
						rs.getString("phoneNumber"), rs.getString("email"), rs.getInt("code"));
				// Add other fields as needed
				return s;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	/**
	 * this function return the manager by user name
	 * @param username
	 * @return
	 */
	public Manager getManagerByUsername(String username) {
	    String sql = "SELECT * FROM `managers` WHERE username = ?";
	    try {
	        PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setString(1, username);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            // assuming Manager class has a constructor Manager(String username, String password)
	            return new Manager(rs.getString("username"), rs.getString("password"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	/**
	 *  this function return the worker by user name
	 * @param username
	 * @return
	 */
	public Worker getWorkerByUsername(String username) {
	    String sql = "SELECT * FROM `workers` WHERE username = ?";
	    try {
	        PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setString(1, username);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            return new Worker(rs.getString("username"), rs.getString("password"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

}
