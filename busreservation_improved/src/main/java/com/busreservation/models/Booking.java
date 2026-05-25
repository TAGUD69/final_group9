package com.busreservation.models;

public class Booking {
    private int bookingId;
    private String bookingDate;
    private String seatNumbers;
    private String passengerName;
    private double totalFare;
    private String status;
    private String departureTime;
    private String origin;
    private String destination;
    private String busName;
    
    public Booking(int bookingId, String bookingDate, String seatNumbers, String passengerName, double totalFare, String status, String departureTime, String origin, String destination, String busName) {
        this.bookingId = bookingId;
        this.bookingDate = bookingDate;
        this.seatNumbers = seatNumbers;
        this.passengerName = passengerName;
        this.totalFare = totalFare;
        this.status = status;
        this.departureTime = departureTime;
        this.origin = origin;
        this.destination = destination;
        this.busName = busName;
    }
    
    public int getBookingId() { return bookingId; }
    public String getBookingDate() { return bookingDate; }
    public String getSeatNumbers() { return seatNumbers; }
    public String getPassengerName() { return passengerName; }
    public double getTotalFare() { return totalFare; }
    public String getStatus() { return status; }
    public String getDepartureTime() { return departureTime; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getBusName() { return busName; }
}