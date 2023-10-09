package com.example.path_tracker.NavigatingMode;

public class Point {
    double latitude;
    double longitude;

    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double distanceTo(Point c) {
        return Math.sqrt(Math.pow(((latitude-c.getLatitude())*111139),2) + Math.pow(((longitude - c.getLongitude())*111139),2));
    }
}
