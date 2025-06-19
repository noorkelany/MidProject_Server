
// Required imports
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import data.ResponseWrapper;

public class OrderValidator {

	public static boolean isParkingSpotAvailable(int parkingCode, LocalDate date, LocalTime requestedStart,
			LocalTime requestedEnd) {
		String subscriberParkingSql = """
				    SELECT * FROM subscriberparking
				    WHERE parkingCode = ?
				      AND date = ?
				      AND status = 'ACTIVE'
				      AND (
				            time < ? AND
				            ADDTIME(time, SEC_TO_TIME((4 + numberOfExtends) * 3600)) > ?
				          )
				""";

		String orderSql = """
				    SELECT * FROM `order`
				    WHERE parking_space = ?
				      AND order_date = ?
				      AND (
				            startTime < ? AND
				            endTime > ?
				          )
				""";

		try (Connection conn = mysqlConnection.getInstance().getConnection()) {
			// Check subscriberparking conflicts
			try (PreparedStatement stmt1 = conn.prepareStatement(subscriberParkingSql)) {
				stmt1.setInt(1, parkingCode);
				stmt1.setDate(2, Date.valueOf(date));
				stmt1.setTime(3, Time.valueOf(requestedEnd));
				stmt1.setTime(4, Time.valueOf(requestedStart));

				if (stmt1.executeQuery().next()) {
					return false;
				}
			}

			// Check order conflicts
			try (PreparedStatement stmt2 = conn.prepareStatement(orderSql)) {
				stmt2.setInt(1, parkingCode);
				stmt2.setDate(2, Date.valueOf(date));
				stmt2.setTime(3, Time.valueOf(requestedEnd));
				stmt2.setTime(4, Time.valueOf(requestedStart));

				if (stmt2.executeQuery().next()) {
					return false;
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return true; // No conflicts
	}

	public static boolean isOverallAvailabilitySufficient(LocalDate date, LocalTime start, LocalTime end) {
		String totalSpotsQuery = "SELECT COUNT(*) FROM parkingspot";
		String overlappingOrdersQuery = """
				    SELECT COUNT(*) FROM `order`
				    WHERE order_date = ?
				      AND (startTime < ? AND endTime > ?)
				""";
		String overlappingSubscribersSpotsQuery = """
				    SELECT COUNT(*) FROM subscriberparking
				    WHERE date = ?
				      AND status = 'ACTIVE'
				      AND (
				            time < ? AND
				            ADDTIME(time, SEC_TO_TIME((4 + numberOfExtends) * 3600)) > ?
				          )
				""";
		try (Connection conn = mysqlConnection.getInstance().getConnection();
				Statement totalStmt = conn.createStatement();
				ResultSet totalRS = totalStmt.executeQuery(totalSpotsQuery)) {

			if (totalRS.next()) {
				int totalSpots = totalRS.getInt(1);

				try (PreparedStatement overlapStmt = conn.prepareStatement(overlappingOrdersQuery)) {
					overlapStmt.setDate(1, Date.valueOf(date));
					overlapStmt.setTime(2, Time.valueOf(end));
					overlapStmt.setTime(3, Time.valueOf(start));

					ResultSet overlapRS = overlapStmt.executeQuery();
					if (overlapRS.next()) {
						int overlappingOrders = overlapRS.getInt(1);
						// subscribers spot
						PreparedStatement overlapsubscriberSpotsStmt = conn
								.prepareStatement(overlappingSubscribersSpotsQuery);
						overlapsubscriberSpotsStmt.setDate(1, Date.valueOf(date));
						overlapsubscriberSpotsStmt.setTime(2, Time.valueOf(end));
						overlapsubscriberSpotsStmt.setTime(3, Time.valueOf(start));
						ResultSet overlapSP = overlapsubscriberSpotsStmt.executeQuery();
						if (overlapSP.next()) {
							int overlappingSPS = overlapSP.getInt(1);
							double freeRatio = (totalSpots - overlappingOrders - overlappingSPS) / (double) totalSpots;
							return freeRatio >= 0.4;
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static Map<Integer, Boolean> getAllSpotsStatus(LocalDate date, LocalTime start, LocalTime end) {
		Map<Integer, Boolean> statusMap = new LinkedHashMap<>();
		String queryAllSpots = "SELECT parkingCode FROM parkingspot";

		try (Connection conn = mysqlConnection.getInstance().getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(queryAllSpots)) {

			while (rs.next()) {
				int code = rs.getInt("parkingCode");
				boolean isAvailable = isParkingSpotAvailable(code, date, start, end);
				statusMap.put(code, isAvailable);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return statusMap;
	}

	public static ResponseWrapper insertNewOrder(ResponseWrapper rsp) {
		/*
		 * ResponseWrapper timeRequest = new ResponseWrapper("TIME", start, end);
		 * ResponseWrapper details = new ResponseWrapper("REQUEST_ORDER", date,
		 * timeRequest); ResponseWrapper rsp = new ResponseWrapper("ORDER_DETAILS",
		 * selectedSpotId, details);
		 */
		ResponseWrapper answer = null;
		try {
			Connection conn = mysqlConnection.getInstance().getConnection();
			int spotId = Integer.parseInt(rsp.getData().toString());
			ResponseWrapper details = (ResponseWrapper) rsp.getExtra();
			LocalDate date = (LocalDate) details.getData();
			ResponseWrapper time = (ResponseWrapper) details.getExtra();
			LocalTime startTime = (LocalTime) time.getData();
			LocalTime endTime = (LocalTime) time.getExtra();
			int confirmationCode = generateUniqueConfirmationCode();
			int subscriberCode = 17;
			String query = """
					INSERT INTO `order`
					(parking_space,order_date,confirmation_code,subscriber_id,
					date_of_placing_an_order,startTime,endTime) VALUES
					(?,?,?,?,?,?,?)
					""";
			PreparedStatement insertStmt = conn.prepareStatement(query);
			insertStmt.setInt(1, spotId);
			insertStmt.setDate(2, Date.valueOf(date));
			insertStmt.setInt(3, confirmationCode);
			insertStmt.setInt(4, subscriberCode);
			insertStmt.setDate(5, Date.valueOf(LocalDate.now()));
			insertStmt.setTime(6, Time.valueOf(startTime));
			insertStmt.setTime(7, Time.valueOf(endTime));

			int rowsAffected = insertStmt.executeUpdate();
			answer = new ResponseWrapper("CONFIRMATION_CODE", null);
			if (rowsAffected > 0) {
				answer.setData(confirmationCode);
				System.out.println("Insert successed");
			} else {
				System.out.println("Insert failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}

	public static int generateUniqueConfirmationCode() throws SQLException {
		Connection conn = mysqlConnection.getInstance().getConnection();
		Random random = new Random();
		int code;
		do {
			code = 1000 + random.nextInt(9000); // 1000–9999
		} while (!isCodeUnique(conn, code));
		return code;
	}

	public static boolean isCodeUnique(Connection conn, int code) throws SQLException {
		String sql = "SELECT 1 FROM `order` WHERE confirmation_code = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, code);
			try (ResultSet rs = stmt.executeQuery()) {
				return !rs.next(); // unique if not found
			}
		}
	}

	// Example usage
	public static void main(String[] args) {
		int parkingCode = 12;
		LocalDate requestDate = LocalDate.of(2025, 5, 28);
		LocalTime start = LocalTime.of(13, 0);
		LocalTime end = LocalTime.of(15, 0);

		boolean available = isParkingSpotAvailable(parkingCode, requestDate, start, end);
		boolean hasCapacity = isOverallAvailabilitySufficient(requestDate, start, end);
		Map<Integer, Boolean> spotStatuses = getAllSpotsStatus(requestDate, start, end);

		System.out.println("Spot free? " + available);
		System.out.println("At least 40% spots free? " + hasCapacity);
		System.out.println("Spot statuses: " + spotStatuses);
	}
}
