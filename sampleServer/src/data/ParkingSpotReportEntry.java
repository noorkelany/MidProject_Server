package data;

import java.io.Serializable;

/**
 * Represents a monthly parking report entry for a single parking spot. This
 * data is used in charts to visualize usage and performance of each spot.
 */
public class ParkingSpotReportEntry implements Serializable {

	/** The unique identifier of the parking spot */
	private int parkingCode;

	/** Total parking duration without any extensions (in minutes) */
	private int regularDuration;

	/** Total duration that came from 1-hour extensions (in minutes) */
	private int extendedDuration;

	/** Number of times a car was returned late in this parking spot */
	private int lateReturns;

	/**
	 * Constructs a new report entry for a parking spot.
	 *
	 * @param parkingCode      the parking spot ID
	 * @param regularDuration  total regular parking time in minutes
	 * @param extendedDuration total extended parking time in minutes
	 * @param lateReturns      count of late returns
	 */
	public ParkingSpotReportEntry(int parkingCode, int regularDuration, int extendedDuration, int lateReturns) {
		this.parkingCode = parkingCode;
		this.regularDuration = regularDuration;
		this.extendedDuration = extendedDuration;
		this.lateReturns = lateReturns;
	}

	/** @return the parking spot ID */
	public int getParkingCode() {
		return parkingCode;
	}

	/** @return total regular parking duration in minutes */
	public int getRegularDuration() {
		return regularDuration;
	}

	/** @return total extended duration (from extensions) in minutes */
	public int getExtendedDuration() {
		return extendedDuration;
	}

	/** @return number of times cars were returned late */
	public int getLateReturns() {
		return lateReturns;
	}

	@Override
	public String toString() {
		return "ParkingSpotReportEntry{" + "parkingCode=" + parkingCode + ", regularDuration=" + regularDuration
				+ ", extendedDuration=" + extendedDuration + ", lateReturns=" + lateReturns + '}';
	}
}
