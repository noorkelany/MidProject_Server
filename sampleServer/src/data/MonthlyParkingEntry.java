package data;

import java.io.Serializable;

/**
 * Represents a single parking session entry in the monthly report.
 * 
 * This model is designed to be serializable and used both on the server (for data generation)
 * and the client (for table and chart display). It contains parking session details
 * such as start/end times, duration, delay warnings, and number of extensions.
 */
public class MonthlyParkingEntry implements Serializable {

    /** Unique identifier for the subscriber */
    private final Integer subscriberCode;

    /** Car number associated with the parking session */
    private final String carNumber;

    /** Date of the parking session (formatted as YYYY-MM-DD) */
    private final String parkingDate;

    /** Start time of the parking session (formatted as HH:mm:ss) */
    private final String startTime;

    /** End time of the parking session (formatted as HH:mm:ss) */
    private final String endTime;

    /** Total duration of the parking session in minutes */
    private final Integer durationMinutes;

    /** Number of times the parking session was extended */
    private final Integer numberOfExtends;

    /** Number of delay warnings issued during the session */
    private final Integer delayWarnings;

    /**
     * Constructs a new MonthlyParkingEntry.
     *
     * @param subCode        Subscriber code
     * @param car            Car number
     * @param date           Parking date (YYYY-MM-DD)
     * @param start          Start time (HH:mm:ss)
     * @param end            End time (HH:mm:ss)
     * @param duration       Total duration in minutes
     * @param extendsCount   Number of extensions
     * @param warnings       Delay warnings count
     */
    public MonthlyParkingEntry(int subCode, String car, String date, String start, String end, int duration,
                                int extendsCount, int warnings) {
        this.subscriberCode = subCode;
        this.carNumber = car;
        this.parkingDate = date;
        this.startTime = start;
        this.endTime = end;
        this.durationMinutes = duration;
        this.numberOfExtends = extendsCount;
        this.delayWarnings = warnings;
    }

    public Integer getSubscriberCode() {
        return subscriberCode;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public String getParkingDate() {
        return parkingDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Integer getNumberOfExtends() {
        return numberOfExtends;
    }

    public Integer getDelayWarnings() {
        return delayWarnings;
    }

    @Override
    public String toString() {
        return "MonthlyParkingEntry{" +
                "subscriberCode=" + subscriberCode +
                ", carNumber='" + carNumber + '\'' +
                ", parkingDate='" + parkingDate + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", durationMinutes=" + durationMinutes +
                ", numberOfExtends=" + numberOfExtends +
                ", delayWarnings=" + delayWarnings +
                '}';
    }
}
