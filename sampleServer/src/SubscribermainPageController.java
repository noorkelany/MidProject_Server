import javafx.fxml.FXML;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;

import data.Order;

public class SubscribermainPageController {

    public static ArrayList<Order> getOrdersBySubscriber(int subscriberId) {
        ArrayList<Order> list = new ArrayList<>();

        String sql = """
            SELECT order_number,
                   order_date,
                   date_of_placing_an_order,
                   parking_space,
                   car_number
              FROM `order`
             WHERE subscriber_id = ?
             ORDER BY order_date DESC
        """;

        try (Connection conn = mysqlConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, subscriberId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int orderNumber = rs.getInt("order_number");
                Date orderDate = rs.getDate("order_date");
                Date placingDate = rs.getDate("date_of_placing_an_order");
                int parkingSpace = rs.getInt("parking_space");
                String carNumber = rs.getString("car_number");

                list.add(new Order(
                        orderNumber,
                        orderDate,
                        placingDate,
                        parkingSpace,
                        carNumber
                ));
            }

            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    public static ArrayList<Order> getOrdersFromSubscriberParking(int subscriberId) {
        ArrayList<Order> list = new ArrayList<>();
        String sql = """
            SELECT parkingCode, date, time, status, numberOfExtends, receivingCarTime
            FROM subscriberparking
            WHERE subscriberCode = ?
        """;

        try (Connection conn = mysqlConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, subscriberId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int parkingSpace = rs.getInt("parkingCode");
                Date date = rs.getDate("date");
                Time time = rs.getTime("time");
                String status = rs.getString("status");
                int numberOfExtends = rs.getInt("numberOfExtends");
                Time receivingCarTime = rs.getTime("receivingCarTime");

                list.add(new Order(parkingSpace, date, time, status, numberOfExtends, receivingCarTime));
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

}


