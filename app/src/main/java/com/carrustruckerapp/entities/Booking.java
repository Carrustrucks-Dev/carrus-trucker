package com.carrustruckerapp.entities;

/**
 * Created by Saurbhv on 10/29/15.
 */
public class Booking implements Comparable<Booking> {
    public String booking_id;
    public String bookingTime;
    public String name;
    public String truckName;
    public String shipingJourney;
    public String timeSlot;
    public String status;

    public String getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(String booking_id) {
        this.booking_id = booking_id;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTruckName() {
        return truckName;
    }

    public void setTruckName(String truckName) {
        this.truckName = truckName;
    }

    public String getShipingJourney() {
        return shipingJourney;
    }

    public void setShipingJourney(String shipingJourney) {
        this.shipingJourney = shipingJourney;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int compareTo(Booking o) {
        return getBookingTime().compareTo(o.getBookingTime());
    }
}
