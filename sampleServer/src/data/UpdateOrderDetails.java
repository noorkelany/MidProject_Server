package data;
import java.io.Serializable;
import java.sql.Date;

/**
 * Class for saving details for updating an order
 */
public class UpdateOrderDetails implements Serializable {
	private static final long serialVersionUID = 1L;
	private int order_number;
	private int parking_space;
	// LocalDate today = LocalDate.of(2024, Month.APRIL, 12);
	private Date order_date;

	/**
	 * constructor
	 * @param order_number value of order number
	 * @param parking_space value of parking space
	 * @param order_date value of order date
	 */
	public UpdateOrderDetails(int order_number, int parking_space, Date order_date) {
		this.order_number = order_number;
		this.parking_space = parking_space;
		this.order_date = order_date;
	}

	/**
	 * @return order number
	 */
	public int getOrder_number() {
		return order_number;
	}

	/**
	 * set new value for param
	 * @param order_number
	 */
	public void setOrdחer_number(int order_number) {
		this.order_number = order_number;
	}

	/**
	 * @return parking space
	 */
	public int getParking_space() {
		return parking_space;
	}

	/**
	 * set new value for param
	 * @param parking_space
	 */
	public void setParking_space(int parking_space) {
		this.parking_space = parking_space;
	}

	/**
	 * @return order date
	 */
	public Date getOrder_date() {
		return order_date;
	}

	/**
     * set new value for param
	 * @param order_date
	 */
	public void setOrder_date(Date order_date) {
		this.order_date = order_date;
	}

	/**
	 *returns string that contains all information about the updateOrder
	 */
	@Override
	public String toString() {
		return "order_number=" + order_number + ", parking_space=" + parking_space + ", order_date=" + order_date;
	}

}
