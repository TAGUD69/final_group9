package com.busreservation.models;

public class Schedule {
    private int scheduleId;
    private String busNumber;
    private String busName;
    private String busType;
    private String origin;
    private String destination;
    private String departureTime;
    private String arrivalTime;
    private int availableSeats;
    private double fare;
    private int capacity;
    
    public Schedule(int scheduleId, String busNumber, String busName, String busType, String origin, String destination, String departureTime, String arrivalTime, int availableSeats, double fare) {
        this.scheduleId = scheduleId;
        this.busNumber = busNumber;
        this.busName = busName;
        this.busType = busType;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = formatDate(departureTime);
        this.arrivalTime = formatDate(arrivalTime);
        this.availableSeats = availableSeats;
        this.fare = fare;
    }
    
    public Schedule(int scheduleId, String busNumber, String busName, String origin, String destination, String departureTime, String arrivalTime, int availableSeats, int capacity) {
        this.scheduleId = scheduleId;
        this.busNumber = busNumber;
        this.busName = busName;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = formatDate(departureTime);
        this.arrivalTime = formatDate(arrivalTime);
        this.availableSeats = availableSeats;
        this.capacity = capacity;
    }
    
    private String formatDate(String dateTime) {
        if (dateTime == null) return "";
        if (dateTime.length() >= 16) {
            return dateTime.substring(0, 16).replace("T", " ");
        }
        return dateTime;
    }
    
    public String getDuration() {
        try {
            String dep = departureTime;
            String arr = arrivalTime;
            if (dep != null && arr != null && dep.contains(" ") && arr.contains(" ")) {
                String depTime = dep.split(" ")[1];
                String arrTime = arr.split(" ")[1];
                int depHour = Integer.parseInt(depTime.split(":")[0]);
                int depMin = Integer.parseInt(depTime.split(":")[1]);
                int arrHour = Integer.parseInt(arrTime.split(":")[0]);
                int arrMin = Integer.parseInt(arrTime.split(":")[1]);
                
                int totalDepMinutes = depHour * 60 + depMin;
                int totalArrMinutes = arrHour * 60 + arrMin;
                int durationMinutes = totalArrMinutes - totalDepMinutes;
                
                if (durationMinutes < 0) durationMinutes += 24 * 60;
                
                int hours = durationMinutes / 60;
                int minutes = durationMinutes % 60;
                
                return hours + "h " + minutes + "m";
            }
        } catch (Exception e) {}
        return "N/A";
    }
    
    public int getScheduleId() { return scheduleId; }
    public String getBusNumber() { return busNumber; }
    public String getBusName() { return busName; }
    public String getBusType() { return busType; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public int getAvailableSeats() { return availableSeats; }
    public double getFare() { return fare; }
    public int getCapacity() { return capacity; }
}