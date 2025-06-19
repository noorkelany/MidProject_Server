import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import data.Order;
import data.SystemStatus;

/**
 * Handles database operations related to car delivery, including validating
 * subscription codes and finding empty parking spots.
 */
public class parkingSubscriberController {

	/** Singleton instance of the MySQL connection manager */
	private mysqlConnection instance = null;

	/** JDBC connection object */
	private Connection conn = null;

	/**
	 * Initializes the connection to the database. Should be called before
	 * performing any queries.
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
	 * Checks if a subscription with the given code exists in the database.
	 *
	 * @param code the subscription code to check
	 * @return true if the subscription exists, false otherwise
	 */
	public boolean checkSubscriptionExists(String code) {
		// Ensure connection is initialized before query
		if (conn == null) {
			getDBConnection();
		}

		String query = "SELECT * FROM subscribers WHERE code = ?";

		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, code);
			ResultSet rs = ps.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			System.out.println("Database error: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Searches for an available (empty) parking spot.
	 *
	 * @return the code of the first empty parking spot found, or an appropriate
	 *         message if none is available or an error occurred
	 */
	public int checkEmptyParkingSpots() {
		if (conn == null) {
			getDBConnection();
		}

		String query = "SELECT parkingCode FROM parkingspot WHERE status = 'empty' LIMIT 1";

		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt("parkingCode");
			} else {
				// No spot found
				return -1;
			}
		} catch (SQLException e) {
			System.out.println("Database error: " + e.getMessage());
			return -1;
		}
	}

	/**
	 * this function insert the car to delivery list
	 * 
	 * @param order
	 * @return
	 */
	public static SystemStatus handleCarDelivery(Order order) {
		// ❗ Check if subscriber already has an active delivery
		if (mysqlConnection.isAlreadyDeliveredForCar(order.getCar().getCarNumber()) == SystemStatus.ALREADY_DELIVERED) {
			return SystemStatus.ALREADY_DELIVERED;
		}

		// Insert car if needed
		if (mysqlConnection.carExists(order.getCar()) == SystemStatus.CAR_NOT_FOUND) {
			mysqlConnection.insertCarToDatabase(order.getCar());
		}

		// Occupy parking spot
		SystemStatus parkingStatus = mysqlConnection.occupyFirstAvailableSpot(order);
		if (parkingStatus == SystemStatus.NO_PARKING_SPOT) {
			return SystemStatus.NO_PARKING_SPOT;
		}

		// Insert delivery
		mysqlConnection.insertCarToDeliver(order);
		return SystemStatus.SUCCESS_DELIVERY;
	}

	/**
	 * This function returns all active subscriber parking records
	 * 
	 * @return List of SubscriberParking objects with status = 'ACTIVE'
	 */
	public ArrayList<Order> getActiveSubscriberParking() {
		ArrayList<Order> activeList = null;
		instance = mysqlConnection.getInstance();

		try {
			conn = instance.getConnection();
			activeList = mysqlConnection.printActiveParking(conn);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return activeList;
	}

}
