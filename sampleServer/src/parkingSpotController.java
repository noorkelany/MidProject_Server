import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;

import data.Login;
import data.ResponseWrapper;

public class parkingSpotController {
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

	public parkingSpotController() {
		this.getDBConnection();
	}

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

	public boolean findParkingCode(ResponseWrapper response) {
		// "Parking Code", parkingCode, subscriberCode
		int confirmationCode = Integer.parseInt(response.getData().toString());
		int subscriberCode = Integer.parseInt(response.getExtra().toString());
		Statement stmt;
		boolean exists = false, result = false;
		try {
			LocalDate today = LocalDate.now();
			LocalTime now = LocalTime.now();

			PreparedStatement ps = conn.prepareStatement("SELECT * FROM subscriberparking "
					+ "WHERE confirmation_code = ? AND subscriberCode = ? " + "AND status = 'ACTIVE' "
					+ "AND DATE(time) = CURRENT_DATE "
					+ "AND TIME(time) BETWEEN (CURRENT_TIME - INTERVAL 15 MINUTE) AND (CURRENT_TIME + INTERVAL 15 MINUTE)");

			ps.setInt(1, confirmationCode);
			ps.setInt(2, subscriberCode);

			ResultSet rs = ps.executeQuery();
			exists = rs.next();
			
			System.out.println("before yes1 -> "+confirmationCode+" "+subscriberCode);

			if (exists) {
				System.out.println("yes1");
				int parkingCode = rs.getInt("parkingCode");
				result = emptyRelevantParkingSpot(confirmationCode, subscriberCode, parkingCode);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return exists && result;
	}

	public boolean emptyRelevantParkingSpot(int confirmationCode, int subscriberCode, int parkingCode) {
		boolean success1 = false, success2 = false;
		System.out.println("yes2");
		try {
			LocalDate today = LocalDate.now();
			LocalTime now = LocalTime.now().withSecond(0).withNano(0); // Normalize for precision

			// 1. Update subscriberparking
			PreparedStatement ps1 = conn
					.prepareStatement("UPDATE subscriberparking SET status = 'NOT ACTIVE', receivingCarTime = ? "
							+ "WHERE confirmation_code = ? AND subscriberCode = ? AND status = 'ACTIVE' "
							+ "AND DATE(time) = CURRENT_DATE "
							+ "AND TIME(time) BETWEEN (CURRENT_TIME - INTERVAL 15 MINUTE) AND (CURRENT_TIME + INTERVAL 15 MINUTE)");
			ps1.setTime(1, java.sql.Time.valueOf(now));
			ps1.setInt(2, confirmationCode);
			ps1.setInt(3, subscriberCode);
			int updated1 = ps1.executeUpdate();
			success1 = updated1 > 0;
			ps1.close();

			// 2. Update parkingspot
			PreparedStatement ps2 = conn.prepareStatement(
					"UPDATE parkingspot SET status = 'empty', subscriber_code = NULL WHERE subscriber_code = ? AND parkingCode = ?");
			ps2.setInt(1, subscriberCode);
			ps2.setInt(2, parkingCode);
			int updated2 = ps2.executeUpdate();
			success2 = updated2 > 0;
			ps2.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return success1 && success2;
	}
}
