

import java.sql.*;
import java.time.LocalDate;

/**
 * This class is responsible for generating and saving the monthly parking spot report.
 * It summarizes data from the `subscriberparking` table and inserts it into `parkingspotsreport`.
 * Each report entry includes:
 * - Total regular parking time (excluding extensions)
 * - Total extended time (based on numberOfExtends * 60)
 * - Total number of late returns (based on end time being after planned)
 */
public class ParkingSpotReportController {

    private final Connection conn;

    /**
     * Initializes the controller and establishes a database connection.
     */
    public ParkingSpotReportController() {
        try {
            this.conn = mysqlConnection.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to DB", e);
        }
    }

    /**
     * Generates and saves the parking spot report for the current month.
     * Should be called only on the last day of the month.
     */
    public void generateMonthlyReport() {
        String currentMonth = LocalDate.now().withDayOfMonth(1).toString().substring(0, 7); // e.g., "2025-06"

        try {
            // 1. Get all parking spots (even those without data this month)
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT parkingCode FROM parkingSpot");

            while (rs.next()) {
                int parkingCode = rs.getInt("parkingCode");

                // 2. Get all usage records for this spot and month
                String query = """
                    SELECT time, receivingCarTime, numberOfExtends 
                    FROM subscriberparking 
                    WHERE parkingCode = ? 
                      AND date LIKE ?
                      AND status = 'NOT ACTIVE'
                    """;
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setInt(1, parkingCode);
                ps.setString(2, currentMonth + "%");

                ResultSet usage = ps.executeQuery();

                int totalDurationMin = 0;
                int totalExtensions = 0;
                int lateReturns = 0;

                while (usage.next()) {
                    Time start = usage.getTime("time");
                    Time end = usage.getTime("receivingCarTime");
                    int extendsCount = usage.getInt("numberOfExtends");

                    if (start != null && end != null) {
                        int diffMin = (int) ((end.getTime() - start.getTime()) / (60 * 1000)); // minutes
                        totalDurationMin += diffMin;

                        // Assume all end times after start are late (based on receiving car logic)
                        if (end.after(start)) {
                            lateReturns++;
                        }
                    }

                    totalExtensions += extendsCount;
                }

                int extendedMin = totalExtensions * 60;
                int regularMin = Math.max(0, totalDurationMin - extendedMin);

                // 3. Save this spot's report
                String insert = """
                    INSERT INTO parkingspotsreport 
                    (parkingCode, month, regularDuration, extendedDuration, lateReturns)
                    VALUES (?, ?, ?, ?, ?)
                    """;
                PreparedStatement insertStmt = conn.prepareStatement(insert);
                insertStmt.setInt(1, parkingCode);
                insertStmt.setString(2, currentMonth);
                insertStmt.setInt(3, regularMin);
                insertStmt.setInt(4, extendedMin);
                insertStmt.setInt(5, lateReturns);
                insertStmt.executeUpdate();
            }

            System.out.println("✅ Parking spot report saved for month " + currentMonth);

        } catch (SQLException e) {
            System.err.println("❌ Error generating parking spot report: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
