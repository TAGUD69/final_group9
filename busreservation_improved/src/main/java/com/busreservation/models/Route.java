package com.busreservation.models;

public class Route {
    private int routeId;
    private String origin;
    private String destination;
    private double distanceKm;
    private double baseFare;
    
    public Route(int routeId, String origin, String destination, double distanceKm, double baseFare) {
        this.routeId = routeId;
        this.origin = origin;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.baseFare = baseFare;
    }
    
    public int getRouteId() { return routeId; }
    public void setRouteId(int routeId) { this.routeId = routeId; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }
    
    @Override
    public String toString() {
        return origin + " → " + destination + " (₱" + String.format("%.2f", baseFare) + ")";
    }
}