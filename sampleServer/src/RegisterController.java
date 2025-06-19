import java.sql.Connection;
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
	
	//register a new subscriber with a generated code and save them to the database
	public String registerNewSubscriber(Subscriber subscriber) {
		try {
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
