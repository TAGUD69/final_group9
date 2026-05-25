package com.busreservation.models;

public class Bus {
    private int busId;
    private String busNumber;
    private String busName;
    private int capacity;
    private String busType;
    
    public Bus(int busId, String busNumber, String busName, int capacity, String busType) {
        this.busId = busId;
        this.busNumber = busNumber;
        this.busName = busName;
        this.capacity = capacity;
        this.busType = busType;
    }
    
    public int getBusId() { return busId; }
    public void setBusId(int busId) { this.busId = busId; }
    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }
    public String getBusName() { return busName; }
    public void setBusName(String busName) { this.busName = busName; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getBusType() { return busType; }
    public void setBusType(String busType) { this.busType = busType; }
    
    @Override
    public String toString() {
        return busNumber + " - " + busName + " (" + busType + ")";
    }
}