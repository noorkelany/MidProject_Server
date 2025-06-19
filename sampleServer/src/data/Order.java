package data;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;

/**
 * Class for saving details for new Order
 */
public class Order implements Serializable {
	private static final long serialVersionUID = 1L;
	private int parking_space; // parking space for the car
	private int order_number; // number of the order
	private Date order_date;
	private int confirmation_code; // parking confirmation code for the parking
	private int subscriber_id; // subscriber code
	private Date date_of_placing_an_order;
	private LocalDateTime startTime, endTime; // start and end time of the order
	private Car car; // place order for car
	LocalDateTime delivery_time, recivingcartime;
	private Time del_Time, rec_Time;
	private String carNumber;
	int numberofextend;

	/**
	 * constructor
	 * 
	 * @param parking_space            value of parking spot
	 * @param order_number             value of parking spot
	 * @param order_date               value of order date
	 * @param confirmation_code        value of confirmation code
	 * @param subscriber_id            value of subscriber id
	 * @param date_of_placing_an_order value of placing order date
	 */
	public Order(int parking_space, int order_number, Date order_date, int confirmation_code, int subscriber_id,
			Date date_of_placing_an_order) {
		super();
		this.parking_space = parking_space;
		this.order_number = order_number;
		this.order_date = order_date;
		this.confirmation_code = confirmation_code;
		this.subscriber_id = subscriber_id;
		this.date_of_placing_an_order = date_of_placing_an_order;
	}

	/**
	 * 
	 * @param confirmation_code confirmation for the parking
	 * @param subscriber_id     subscriber id
	 * @param startDate         start time of the order
	 * @param endTime           end time of the order
	 * @param car               place the order for the car
	 */
	public Order(int confirmation_code, int subscriber_id, LocalDateTime startDate, LocalDateTime endTime, Car car) {
		this.setCar(car);
		this.setStartTime(startDate);
		this.setEndTime(endTime);
		this.setConfirmation_code(confirmation_code);
		this.setSubscriber_id(subscriber_id);
	}

	public Order(int confirmation_code, int parking_space) {
		this.parking_space = parking_space;
		this.confirmation_code = confirmation_code;
	}

	public Order(int subscriber_id) {
		this.subscriber_id = subscriber_id;
		// this.parking_space=parking_space;
	}

	public Order(int subscriber_id, int parking_space, LocalDateTime order_date, int numberofextend,
			LocalDateTime recivingcartime, Car car, int confirmation_code) {
		this.subscriber_id = subscriber_id;
		this.parking_space = parking_space;
		this.delivery_time = order_date;
		this.numberofextend = numberofextend;
		this.recivingcartime = recivingcartime;
		this.car = car;
		this.confirmation_code = confirmation_code;
	}

	/**
	 * 
	 * @param carNumber
	 * @param confirmation_code
	 * @param del_time
	 * @param rec_time
	 * @param subscriber_id
	 * @param parking_space
	 */
	public Order(String carNumber, int confirmation_code, Time del_time, Time rec_time, int subscriber_id,
			int parking_space) {
		this.setCarNumber(carNumber);
		this.confirmation_code = confirmation_code;
		this.setDel_Time(del_time);
		this.setRec_Time(rec_time);
		this.subscriber_id = subscriber_id;
		this.parking_space = parking_space;
	}

	/**
	 * @return parking space
	 */
	public int getParking_space() {
		return parking_space;
	}

	/**
	 * set new value for param
	 * 
	 * @param parking_space
	 */
	public void setParking_space(int parking_space) {
		this.parking_space = parking_space;
	}

	/**
	 * @return order number
	 */
	public int getOrder_number() {
		return order_number;
	}

	/**
	 * set new value for param
	 * 
	 * @param order_number
	 */
	public void setOrder_number(int order_number) {
		this.order_number = order_number;
	}

	/**
	 * @return order date
	 */
	public Date getOrder_date() {
		return order_date;
	}

	/**
	 * set new value for param
	 * 
	 * @param order_date
	 */
	public void setOrder_date(Date order_date) {
		this.order_date = order_date;
	}

	/**
	 * @return confirmation code
	 */
	public int getConfirmation_code() {
		return confirmation_code;
	}

	/**
	 * set new value for param
	 * 
	 * @param confirmation_code
	 */
	public void setConfirmation_code(int confirmation_code) {
		this.confirmation_code = confirmation_code;
	}

	/**
	 * @return subscriber id
	 */
	public int getSubscriber_id() {
		return subscriber_id;
	}

	/**
	 * set new value for param
	 * 
	 * @param subscriber_id
	 */
	public void setSubscriber_id(int subscriber_id) {
		this.subscriber_id = subscriber_id;
	}

	/**
	 * @return date of placing an order
	 */
	public Date getDate_of_placing_an_order() {
		return date_of_placing_an_order;
	}

	/**
	 * set new value for param
	 * 
	 * @param date_of_placing_an_order
	 */
	public void setDate_of_placing_an_order(Date date_of_placing_an_order) {
		this.date_of_placing_an_order = date_of_placing_an_order;
	}

	/**
	 * returns string that contains all information about the order
	 */
	@Override
	public String toString() {
		return parking_space + " " + order_number + " " + confirmation_code + " " + subscriber_id + " " + startTime
				+ " " + endTime + " " + car;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	public void setDelivery_time(LocalDateTime delivery_time) {
		this.delivery_time = delivery_time;
	}

	public void setRecivingcartime(LocalDateTime recivingcartime) {
		this.recivingcartime = recivingcartime;
	}

	public void setDel_Time(Time del_Time) {
		this.del_Time = del_Time;
	}

	public void setRec_Time(Time rec_Time) {
		this.rec_Time = rec_Time;
	}

	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}

	public void setNumberofextend(int numberofextend) {
		this.numberofextend = numberofextend;
	}

	public LocalDateTime getDelivery_time() {
		return delivery_time;
	}

	public LocalDateTime getRecivingcartime() {
		return recivingcartime;
	}

	public Time getDelTime() {
		return del_Time;
	}

	public Time getRecTime() {
		return rec_Time;
	}

	public String getCarNumber() {
		return carNumber;
	}

	public int getNumberofextend() {
		return numberofextend;
	}

}
