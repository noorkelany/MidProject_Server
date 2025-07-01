import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import data.Subscriber;

public class RegisterController {
	private mysqlConnection instance = null;
	private Connection conn;

	public RegisterController() {
		getDBConnection();
	}

	private void getDBConnection() {
		try {
			instance = mysqlConnection.getInstance();
			conn = instance.getConnection();
		} catch (Exception e) {
			System.out.println("Error connecting to DB");
		}
	}

	
	private boolean isEmailExists(String email) {
		try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM subscribers WHERE email = ?")) {
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String registerNewSubscriber(Subscriber subscriber) {
		try {
			if (isEmailExists(subscriber.getEmail())) {
				return "Registration failed: Email already exists.";
			}

			int code = mysqlConnection.generateUniqueCode();
			subscriber.setCode(code);

			boolean success = mysqlConnection.saveSubscriber(subscriber);
			return success
					? "Registration successful. Your code is: " + code
					: "Registration failed: Could not insert subscriber.";
		} catch (Exception e) {
			e.printStackTrace(); // For debugging purposes
			return "Registration failed: Server error.";
		}
	}
}
