import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import data.ResponseWrapper;

public class ReceivingCarController {
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

	public ReceivingCarController() {
		this.getDBConnection();
	}

	public int getParkingCodeForSubscriber(ResponseWrapper response) {
		int subscriberCode = Integer.parseInt(response.getData().toString());
		int parkingCode = -1;

		try (PreparedStatement ps = conn.prepareStatement(
				"SELECT confirmation_code FROM `subscriberparking` WHERE subscriberCode = ? AND status = 'ACTIVE'")) {
			ps.setInt(1, subscriberCode);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				parkingCode = rs.getInt("confirmation_code");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return parkingCode;
	}

	public String extendParkingTime(ResponseWrapper response) {
		int subscriberCode = Integer.parseInt(response.getData().toString());
		int selectedTime = Integer.parseInt(response.getExtra().toString());
		int parkingCode = getParkingCodeForSubscriber(response);

		return handleExtensionRequest(subscriberCode, parkingCode, selectedTime);
	}

	public String handleExtensionRequest(int subscriberCode, int parkingCode, int requestedExtensionHours) {
		String selectQuery = "SELECT time, date FROM subscriberparking WHERE subscriberCode = ? AND parkingCode = ? AND status = 'ACTIVE'";
		String conflictQuery = """
				    SELECT COUNT(*) FROM subscriberparking
				    WHERE subscriberCode != ? AND parkingCode = ? AND status = 'ACTIVE'
				    AND (time < ? AND ADDTIME(time, SEC_TO_TIME(4 * 3600)) > ?)
				""";
		String updateQuery = "UPDATE subscriberparking SET numberOfExtends = numberOfExtends + ? WHERE subscriberCode = ? AND parkingCode = ?";

		try (Connection conn = mysqlConnection.getInstance().getConnection();
				PreparedStatement ps1 = conn.prepareStatement(selectQuery)) {

			ps1.setInt(1, subscriberCode);
			ps1.setInt(2, parkingCode);
			ResultSet rs = ps1.executeQuery();

			if (!rs.next())
				return "No active parking found.";

			LocalDate date = rs.getDate("date").toLocalDate();
			LocalTime entryTime = rs.getTime("time").toLocalTime();
			LocalDateTime entry = LocalDateTime.of(date, entryTime);
			LocalDateTime defaultEnd = entry.plusHours(4);

			// Try the maximum extension possible, decreasing
			for (int tryHours = requestedExtensionHours; tryHours >= 1; tryHours--) {
				LocalDateTime proposedEnd = defaultEnd.plusHours(tryHours);

				try (PreparedStatement ps2 = conn.prepareStatement(conflictQuery)) {
					ps2.setInt(1, subscriberCode);
					ps2.setInt(2, parkingCode);
					ps2.setTime(3, Time.valueOf(proposedEnd.toLocalTime()));
					ps2.setTime(4, Time.valueOf(defaultEnd.toLocalTime()));

					ResultSet check = ps2.executeQuery();
					if (check.next() && check.getInt(1) == 0) {
						// no conflict → update and return message
						try (PreparedStatement ps3 = conn.prepareStatement(updateQuery)) {
							ps3.setInt(1, tryHours);
							ps3.setInt(2, subscriberCode);
							ps3.setInt(3, parkingCode);
							ps3.executeUpdate();
						}
						return "Extension approved for " + tryHours + " hour(s).";
					}
				}
			}

			// if loop completes, no extension possible
			return "Extension denied: no available time slot.";

		} catch (SQLException e) {
			e.printStackTrace();
			return "Error processing extension request.";
		}
	}

}
