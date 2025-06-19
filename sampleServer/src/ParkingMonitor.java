import java.sql.*;
import java.time.*;
import java.util.concurrent.*;

import data.EmailSender;

public class ParkingMonitor {

	// Schedule task every 10 minutes
	public static void startMonitoring() {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		scheduler.scheduleAtFixedRate(() -> {
			try {
				checkParkingDurations();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 0, 10, TimeUnit.MINUTES);
	}

//Schedule late every 1 minute
	public static void startMonitoringLateOrders() {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(() -> {
			checkLateOrders();
		}, 0, 1, TimeUnit.MINUTES); // run every minute

	}

	// Checks duration and sends emails
	public static void checkParkingDurations() {
		String query = """
				    SELECT sp.subscriberCode, sp.time, sp.receivingCarTime, sp.numberOfExtends, sp.status,
				           s.email, s.username, sp.parkingCode
				    FROM subscriberparking sp
				    JOIN subscribers s ON sp.subscriberCode = s.code
				    WHERE sp.status = 'ACTIVE'
				""";

		try (Connection conn = mysqlConnection.getInstance().getConnection();
				PreparedStatement stmt = conn.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {

			LocalTime now = LocalTime.now();

			while (rs.next()) {
				int subscriberCode = rs.getInt("subscriberCode");
				int parkingCode = rs.getInt("parkingCode");
				LocalTime entryTime = rs.getTime("time").toLocalTime();
				Time receivedTimeObj = rs.getTime("receivingCarTime");
				int extensions = rs.getInt("numberOfExtends");
				String email = rs.getString("email");
				String name = rs.getString("username");

				// Skip if car already picked up
				if (receivedTimeObj != null)
					continue;

				LocalTime allowedUntil = entryTime.plusHours(4 + extensions);
				Duration remaining = Duration.between(now, allowedUntil);

				if (remaining.isNegative()) {
					registerLate(parkingCode, subscriberCode);
					ParkingMonitor.sendEmail(email, name, "Your parking time has expired.");
				} else if (remaining.toMinutes() <= 10) {
					ParkingMonitor.sendEmail(email, name, "Only 10 minutes left in your parking time.");
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method for checking lateness for order
	 */
	public static void checkLateOrders() {
		String query = """
				SELECT order_number, order_date, startTime, subscriber_id
				FROM `order` WHERE order_date = CURDATE()
				""";

		try (PreparedStatement ps = mysqlConnection.getInstance().getConnection().prepareStatement(query);
				ResultSet rs = ps.executeQuery()) {

			LocalTime now = LocalTime.now();
			LocalDate today = LocalDate.now();

			while (rs.next()) {
				Time start = rs.getTime("startTime");
				int subscriberCode = rs.getInt("subscriber_id");
				LocalTime startTime = start.toLocalTime();
				LocalTime lateThreshold = startTime.plusMinutes(15);
				int order_number = rs.getInt("order_number");
				if (now.isAfter(lateThreshold)) {
					int orderNumber = rs.getInt("order_number");
					System.out.println("Order " + orderNumber + " is LATE!");

					// Send email
					String getSubscriber = "SELECT email,username from `subscribers` WHERE code = ?";
					PreparedStatement ps2 = mysqlConnection.getInstance().getConnection()
							.prepareStatement(getSubscriber);
					ps2.setInt(1, subscriberCode);
					ResultSet rs2 = ps2.executeQuery();
					rs2.next();
					String email = rs2.getString("email");
					String name = rs2.getString("username");
					System.out.println("Email :" + email + " Subscriber: " + subscriberCode);
					ParkingMonitor.sendEmail(email, name,
							"Your late for the order, the place has been freed and your order is cancalled");
					// delete from dataBase

					String deleteRow = "DELETE FROM `order` WHERE order_number = ?";
					// order_number
					PreparedStatement ps3 = mysqlConnection.getInstance().getConnection().prepareStatement(deleteRow);
					ps3.setInt(1, order_number);
					ps3.executeUpdate();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Sends an HTML email
	public static void sendEmail(String to, String name, String message) {
		/*
		 * String htmlContent = String.format(""" <html> <body> <h2 style='color:
		 * red;'>Dear %s,</h2> <p style='font-size: 16px;'>%s</p> </body> </html> """,
		 * name, message);
		 */
		String htmlContent = String.format(
				"""
						<html>
						<body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;">
						    <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); padding: 30px;">
						        <h2 style="color: #004080; text-align: center; margin-bottom: 20px;">Dear %s,</h2>
						        <p style="font-size: 20px; text-align: center;">
						            <span style="color: red; font-weight: bold;">%s</span>
						        </p>
						        <div style="text-align: center; margin-top: 30px;">
						            <img src="https://www.keflatwork.com/wp-content/uploads/2019/01/parking-lot-with-trees.jpg" alt="Parking Lot" style="width: 100%%; max-width: 550px; border-radius: 6px;" />
						        </div>
						         <p style="font-size: 16px; text-align: center; margin-top: 40px;">
						         Best regards,<br/>
						         <strong>Auto BPark</strong>
						     </p>
						    </div>
						</body>
						</html>
								""",
				name, message);
		EmailSender.sendEmailDesigned(to, htmlContent);
	}

	public static int getNumberOfWarnings(int subscriberCode, int parkingCode) {
		try {
			String query = """
					    SELECT numberOfWarnings FROM latemessage
					     WHERE subscriberCode=(?) AND parkingCode=(?)
					""";
			Connection conn = mysqlConnection.getInstance().getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, subscriberCode);
			pstmt.setInt(2, parkingCode);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("numberOfWarnings");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void registerLate(int parkingCode, int subscriberCode) {
		try {
			Connection conn = mysqlConnection.getInstance().getConnection();
			int numberOfWarnings = getNumberOfWarnings(subscriberCode, parkingCode);
			String query;
			if (numberOfWarnings == 0) {
				query = """
						    INSERT INTO latemessage (text,date,numberOfwarnings,subscriberCode,parkingCode)
						    VALUES (?,?,?,?,?)
						""";
				PreparedStatement pstmt2 = conn.prepareStatement(query);
				pstmt2.setString(1, "Late for receiving the car");
				pstmt2.setDate(2, Date.valueOf(LocalDate.now()));
				pstmt2.setInt(3, 1);
				pstmt2.setInt(4, subscriberCode);
				pstmt2.setInt(5, parkingCode);
				pstmt2.executeUpdate();
			} else {
				// UPDATE table_name
				// SET column1 = value1, column2 = value2, ...
				// WHERE condition;
				query = """
						    UPDATE latemessage SET numberOfWarnings = (?)
						    WHERE subscriberCode=(?) AND parkingCode =(?)
						""";
				PreparedStatement pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, numberOfWarnings + 1);
				pstmt.setInt(2, subscriberCode);
				pstmt.setInt(3, parkingCode);
				pstmt.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void startMonthlyReportScheduler(MonthlyReportController monthlyReportController) {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		Runnable task = () -> {
			LocalDate today = LocalDate.now();
			monthlyReportController.generateMonthlyParkingReport();
			if (today.getDayOfMonth() == today.lengthOfMonth()) {
				System.out.println("📅 Last day of the month — generating report...");
			} else {
				System.out.println("📅 Not the last day of the month — skipping report.");
			}
		};

		// Schedule to run once every 24 hours
		scheduler.scheduleAtFixedRate(task, 0, 24, TimeUnit.HOURS);
	}

	public static void scheduleParkingSpotReport(ParkingSpotReportController controller) {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		Runnable task = () -> {
			LocalDate today = LocalDate.now();
			controller.generateMonthlyReport();
			if (today.getDayOfMonth() == today.lengthOfMonth()) {
				System.out.println("📅 Last day of the month — generating parking spot report...");
			} else {
				System.out.println("📅 Not the last day — skipping parking spot report.");
			}
		};

		// Schedule to run once every 24 hours (or 5 seconds for testing)
		scheduler.scheduleAtFixedRate(task, 0, 24, TimeUnit.HOURS);
		// 🔁 Replace 24 with 5 and TimeUnit.SECONDS if testing
	}

}
