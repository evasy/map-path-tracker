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

//    public double distanceTo(Point otherPoint) {
//        double earthRadius = 6371000; // Radius of the Earth in meters
//        double dLat = Math.toRadians(otherPoint.latitude - this.latitude);
//        double dLon = Math.toRadians(otherPoint.longitude - this.longitude);
//        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//                Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(otherPoint.latitude)) *
//                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        double distance = earthRadius * c;
//        return distance;
//    }

}
