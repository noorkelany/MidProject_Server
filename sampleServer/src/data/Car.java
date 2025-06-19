package data;

import java.io.Serializable;
import java.time.Year;

public class Car implements Serializable {
	private static final long serialVersionUID = 1L;
	private String carNumber,model;
	private int year;
	private Subscriber subscriber;
	
	public Car(String carNumber, String model, int year) {
		this.carNumber = carNumber;
		this.model = model;
		this.year = year;
	}

	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * Validates and formats an Israeli car number.
	 * Accepts both hyphenated and non-hyphenated input (e.g., "1234567", "12-345-67").
	 *
	 * @param carNumber The input car number (with or without hyphens).
	 * @return The formatted car number (e.g., "12-345-67" or "123-45-678") if valid, or null if invalid.
	 */
	public static String formatIsraeliCarNumber(String carNumber) {
	    if (carNumber == null) return null;

	    String cleaned = carNumber.replaceAll("-", "");

	    // Old format: 7 digits → XX-XXX-XX
	    if (cleaned.matches("\\d{7}")) {
	        return cleaned.substring(0, 2) + "-" + cleaned.substring(2, 5) + "-" + cleaned.substring(5);
	    }

	    // New format: 8 digits → XXX-XX-XXX
	    else if (cleaned.matches("\\d{8}")) {
	        return cleaned.substring(0, 3) + "-" + cleaned.substring(3, 5) + "-" + cleaned.substring(5);
	    }
	    return null;
	}
	/**
	 * Validates that the car model contains only letters (A–Z or a–z) and spaces.
	 *
	 * @param model The car model string.
	 * @return true if valid, false otherwise.
	 */
	public static boolean isValidCarModel(String model) {
	    return model != null && model.matches("[a-zA-Z ]+");
	}
	/**
	 * Validates that the car year is a 4-digit number within a reasonable range (e.g., 1900 to current year + 1).
	 *
	 * @param year The year to validate.
	 * @return true if valid, false otherwise.
	 */
	public static boolean isValidCarYear(int year) {
	    int currentYear = Year.now().getValue();
	    return year >= 1900 && year <= currentYear + 1;
	}

	public Subscriber getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}
	
	public String getCarNumber() {
		return carNumber;
	}

	public String getModel() {
		return model;
	}

	public int getYear() {
		return year;
	}

	@Override
	public String toString() {
	    return carNumber + " " + model + " " + year;
	}
}
