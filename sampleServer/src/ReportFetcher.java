



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.cj.MysqlConnection;

import data.MonthlyParkingEntry;

public class ReportFetcher {

    private Connection conn;

    public ReportFetcher() {
        try {
			this.conn = mysqlConnection.getInstance().getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public ArrayList<MonthlyParkingEntry> getMonthlyReport(String monthYear) {
        ArrayList<MonthlyParkingEntry> report = new ArrayList<>();

        String query = """
            SELECT subscriberCode, carNumber, parkingDate, startTime, endTime, 
                   durationMinutes, numberOfExtends, delayWarnings
            FROM monthly_parking_summary
            WHERE month_year = ?
            ORDER BY parkingDate ASC;
        """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, monthYear);  // e.g. "2025-06"
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                MonthlyParkingEntry entry = new MonthlyParkingEntry(
                        rs.getInt("subscriberCode"),
                        rs.getString("carNumber"),
                        rs.getDate("parkingDate").toString(),
                        rs.getTime("startTime").toString(),
                        rs.getTime("endTime").toString(),
                        rs.getInt("durationMinutes"),
                        rs.getInt("numberOfExtends"),
                        rs.getInt("delayWarnings")
                );
                report.add(entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return report;
    }
}
