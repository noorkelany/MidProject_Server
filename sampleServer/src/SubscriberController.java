import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import data.Subscriber;

public class SubscriberController {
    /** JDBC connection object */
	private mysqlConnection instance = null;
	Connection conn = null;
	
    /**
     * Initializes the connection to the database.
     * Should be called before performing any queries.
     */
    public void getDBConnection() {
        try {
            instance = mysqlConnection.getInstance();
            conn = instance.getConnection();
        } catch (Exception e) {
            System.out.println("Error connecting to DB: " + e.getMessage());
        }
    }
    /**
     * this function for return all the subscribers
     * @return
     */
	public ArrayList<Subscriber> getAllSubscribers() {
	    ArrayList<Subscriber> allSubscribers = null;
        instance = mysqlConnection.getInstance();
	    try {
	    	conn = instance.getConnection();
	        allSubscribers = mysqlConnection.printSubscribers(conn);
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    return allSubscribers;
	}
}
