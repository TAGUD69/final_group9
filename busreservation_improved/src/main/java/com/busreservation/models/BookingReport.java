package com.busreservation.models;

public class BookingReport {
    private String date;
    private int bookings;
    private double revenue;
    private String origin;
    private String destination;

    public BookingReport(String date, int bookings, double revenue, String origin, String destination) {
        this.date = date; this.bookings = bookings; this.revenue = revenue;
        this.origin = origin; this.destination = destination;
    }

    public String getDate() { return date; }
    public int getBookings() { return bookings; }
    public double getRevenue() { return revenue; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getFormattedRevenue() { return String.format("₱%.2f", revenue); }
}
