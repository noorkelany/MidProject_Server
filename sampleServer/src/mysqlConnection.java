import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;

import data.Car;
import data.Manager;
import data.Order;
import data.Subscriber;
import data.SystemStatus;

public class mysqlConnection {
	private static mysqlConnection instance;

	// Singleton instance
	public static mysqlConnection getInstance() {
		if (instance == null) {
			instance = new mysqlConnection();
		}
		return instance;
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(
				"jdbc:mysql://localhost/parkingdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jerusalem",
				"root", "Jj69Jj692001");
	}

	private mysqlConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			System.out.println("Driver definition succeed");
		} catch (Exception ex) {
			System.out.println("Driver definition failed");
		}
	}

	public static Order returnOrderByID(int orderID) {
		Order ord = null;
		String sql = "SELECT * FROM `order` WHERE order_number = ?";

		try (Connection conn = getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, orderID);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					ord = new Order(rs.getInt(1), rs.getInt(2), rs.getDate(3), rs.getInt(4), rs.getInt(5),
							rs.getDate(6));
				}
			}
		} catch (SQLException e) {
			System.out.println("Error on returnOrderByID function");
			e.printStackTrace();
		}
		return ord;
	}

	public static boolean checkValidOrderNumber(int orderNumber) {
		boolean exists = false;
		String sql = "SELECT * FROM `order` WHERE order_number = ?";

		try (Connection conn = getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, orderNumber);
			try (ResultSet rs = ps.executeQuery()) {
				exists = rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return exists;
	}

	public static int updateParkingSpaceANDOrderDate(int orderNumber, int parkingSpace, Date orderDate) {
		String sql = "UPDATE `order` SET parking_space = ?, order_date = ? WHERE order_number = ?";

		try (Connection conn = getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			if (!checkValidOrderNumber(orderNumber)) {
				return -1;
			}

			ps.setInt(1, parkingSpace);
			ps.setDate(2, orderDate);
			ps.setInt(3, orderNumber);
			return ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static ArrayList<Order> printOrders() {
		ArrayList<Order> allOrders = new ArrayList<>();
		String sql = "SELECT * FROM `order`";

		try (Connection conn = getInstance().getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				Order ord = new Order(rs.getInt(1), rs.getInt(2), rs.getDate(3), rs.getInt(4), rs.getInt(5),
						rs.getDate(6));
				allOrders.add(ord);
			}
		} catch (SQLException e) {
			System.out.println("Error on printOrders function");
			e.printStackTrace();
		}
		return allOrders;
	}

	public static SystemStatus occupyFirstAvailableSpot(Order order) {
		String selectSql = "SELECT parkingCode FROM parkingspot WHERE status = 'empty' LIMIT 1";
		String updateSql = "UPDATE parkingspot SET status = 'occupied', subscriber_code = ? WHERE parkingCode = ?";

		try {
			Connection conn = getInstance().getConnection();
			PreparedStatement selectStmt = conn.prepareStatement(selectSql);
			ResultSet rs = selectStmt.executeQuery();
			if (rs.next()) {
				int parkingCode = rs.getInt("parkingCode");

				try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
					updateStmt.setInt(1, order.getSubscriber_id());
					updateStmt.setInt(2, parkingCode);
					updateStmt.executeUpdate();
					System.out.println("Parking spot " + parkingCode + " is now occupied.");
					return SystemStatus.PARKING_SPOT_AVAILABLE;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error on occupyFirstAvailableSpot function");
			e.printStackTrace();
		}
		return SystemStatus.NO_PARKING_SPOT;
	}

	public static void insertOrderToDatabase(Order order) {
		String sql = "INSERT INTO `Order` (parking_space, order_date, confirmation_code, subscriber_id, date_of_placing_an_order, car_number, startTime, endTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = getInstance().getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, order.getParking_space());
			pstmt.setDate(2, Date.valueOf(order.getStartTime().toLocalDate()));
			pstmt.setInt(3, order.getConfirmation_code());
			pstmt.setInt(4, order.getSubscriber_id());
			pstmt.setDate(5, Date.valueOf(order.getEndTime().toLocalDate()));
			pstmt.setString(6, order.getCar().getCarNumber());
			pstmt.setTimestamp(7, Timestamp.valueOf(order.getStartTime()));
			pstmt.setTimestamp(8, Timestamp.valueOf(order.getEndTime()));
			pstmt.executeUpdate();
			System.out.println("Order inserted successfully.");

		} catch (SQLException e) {
			System.out.println("Failed to insert order: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static boolean saveSubscriber(Subscriber subscriber) {
		String sql = "INSERT INTO subscribers (username, password, phoneNumber, email, code) VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, subscriber.getUsername());
			ps.setString(2, subscriber.getPassword());
			ps.setString(3, subscriber.getPhoneNumber());
			ps.setString(4, subscriber.getEmail());
			ps.setInt(5, subscriber.getCode());
			return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static int generateUniqueCode() {
		Random random = new Random();
		int code;
		do {
			code = 100000 + random.nextInt(900000);
		} while (!isCodeUnique(code));
		return code;
	}

	public static boolean isCodeUnique(int code) {
		String sql = "SELECT * FROM subscribers WHERE code = ?";

		try (Connection conn = getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, code);
			try (ResultSet rs = ps.executeQuery()) {
				return !rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * this function return subscribers list at the database
	 * 
	 * @param con
	 * @return
	 */
	public static ArrayList<Subscriber> printSubscribers(Connection con) {
		ArrayList<Subscriber> allSubscribers = new ArrayList<>();
		Statement stmt;
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM subscribers;");
			while (rs.next()) {
				Subscriber sub = new Subscriber(rs.getString("username"), rs.getString("password"),
						rs.getString("phoneNumber"), rs.getString("email"), rs.getInt("code"));
				allSubscribers.add(sub);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allSubscribers;
	}

	public static ArrayList<Order> printActiveParking(Connection con) {
		ArrayList<Order> activeList = new ArrayList<>();

		try {
			String query = "SELECT subscriberCode, confirmation_code, carNumber, parkingCode, time, receivingCarTime FROM subscriberparking WHERE status = 'ACTIVE'";
			PreparedStatement stmt = con.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Order sp = new Order(rs.getString("carNumber"), rs.getInt("confirmation_code"), rs.getTime("time"),
						rs.getTime("receivingCarTime"), rs.getInt("subscriberCode"), rs.getInt("parkingCode")

				);
				activeList.add(sp);
			}

			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return activeList;
	}

	public static SystemStatus isAlreadyDeliveredForCar(String carNumber) {
		String sql = "SELECT 1 FROM subscriberparking WHERE carNumber = ? AND status = 'ACTIVE'";

		try {
			Connection conn = getInstance().getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, carNumber);
			ResultSet rs = pstmt.executeQuery();

			boolean exists = rs.next();

			rs.close();
			pstmt.close();

			return exists ? SystemStatus.ALREADY_DELIVERED : SystemStatus.CAR_NOT_FOUND;
		} catch (SQLException e) {
			e.printStackTrace();
			return SystemStatus.CAR_NOT_FOUND;
		}
	}

	public static void insertCarToDeliver(Order order) {
		String sql = "INSERT INTO subscriberparking (subscriberCode, parkingCode, date, time, status, numberOfExtends, receivingCarTime,carNumber,confirmation_code) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?,?,?)";
		try {
			Connection conn = getInstance().getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, order.getSubscriber_id()); // subscriberCode
			pstmt.setInt(2, order.getParking_space()); // parkingCode
			pstmt.setDate(3, java.sql.Date.valueOf(order.getDelivery_time().toLocalDate()));
			System.out.print(order.getDelivery_time().toLocalTime());
			pstmt.setTime(4, java.sql.Time.valueOf(order.getDelivery_time().toLocalTime()));
			pstmt.setString(5, "ACTIVE"); // or pass it from the object if needed
			pstmt.setInt(6, order.getNumberofextend());
			pstmt.setTime(7, java.sql.Time.valueOf(order.getRecivingcartime().toLocalTime()));
			pstmt.setString(8, order.getCar().getCarNumber());
			pstmt.setInt(9, order.getConfirmation_code());
			pstmt.executeUpdate();
			System.out.println("Order inserted successfully.");

		} catch (SQLException e) {
			System.out.println("Failed to insert order: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void insertCarToDatabase(Car car) {
		if (carExists(car) == SystemStatus.CAR_EXISTS) {
			System.out.println("Car already exists in the database: " + car.getCarNumber());
			return;
		}

		String sql = "INSERT INTO Car (CarNumber, Model, year) VALUES (?, ?, ?)";

		try {
			Connection conn = getInstance().getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, car.getCarNumber());
			pstmt.setString(2, car.getModel());
			pstmt.setInt(3, car.getYear());
			pstmt.executeUpdate();
			System.out.println("Car inserted successfully: " + car.getCarNumber());

		} catch (SQLException e) {
			System.out.println("Failed to insert car: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static SystemStatus carExists(Car car) {
		String sql = "SELECT * FROM Car WHERE CarNumber = ?";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = getInstance().getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, car.getCarNumber());
			rs = pstmt.executeQuery();

			boolean exists = rs.next();

			rs.close();
			pstmt.close();

			return SystemStatus.CAR_EXISTS;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return SystemStatus.CAR_NOT_FOUND;
	}
	/**
	 * this function return if the order is exists with conformation code
	 * @param confirmationCode
	 * @return
	 * @throws SQLException
	 */
	public static Order getOrderByConfirmationCode(String confirmationCode) throws SQLException {
	    String query = "SELECT parking_space, order_number, order_date, confirmation_code, subscriber_id, date_of_placing_an_order "
	                 + "FROM `order` "
	                 + "WHERE confirmation_code = ?";
	    
		Connection conn = getInstance().getConnection();

	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setString(1, confirmationCode);
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            int parkingSpace = rs.getInt("parking_space");
	            int orderNumber = rs.getInt("order_number");
	            Date orderDate = rs.getDate("order_date");
	            int confirmation = Integer.parseInt(rs.getString("confirmation_code"));
	            int subscriberId = rs.getInt("subscriber_id");
	            Date placingDate = rs.getDate("date_of_placing_an_order");

	            return new Order(parkingSpace, orderNumber, orderDate, confirmation, subscriberId, placingDate);
	        } else {
	            return null; // not found
	        }
	    }
	}

	/**
	 * this function return the subscriber by his code
	 * @param subscriberCode
	 * @return
	 * @throws SQLException
	 */
	public static Subscriber getSubscriberByCode(int subscriberCode) throws SQLException {
	    String query = "SELECT username, password, phoneNumber, email, code FROM subscribers WHERE code = ?";
	    
	    Connection conn = getInstance().getConnection();
	    
	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setInt(1, subscriberCode);
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            String username = rs.getString("username");
	            String password = rs.getString("password");
	            String phone = rs.getString("phoneNumber");
	            String email = rs.getString("email");
	            int code = rs.getInt("code");

	            return new Subscriber(username, password, phone, email, code);
	        } else {
	            return null;
	        }
	    }
	}

}
