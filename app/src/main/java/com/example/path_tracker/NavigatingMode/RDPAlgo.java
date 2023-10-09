package com.example.path_tracker.NavigatingMode;

import java.util.ArrayList;
import java.util.List;

public class RDPAlgo {

    static int min_coordinates = 5;

    public static List<Coordinate> simplify(List<Coordinate> coordinates, double epsilon) {
        if (coordinates.size() < min_coordinates) {
            return coordinates;
        }

        int index = 0;
        double maxDistance = 0.0;

        for (int i = 1; i < coordinates.size() - 1; i++) {
            double distance = perpendicularDistance(coordinates.get(i), coordinates.get(0), coordinates.get(coordinates.size() - 1));

            if (distance > maxDistance) {
                maxDistance = distance;
                index = i;
            }
        }

        List<Coordinate> simplifiedCoordinates = new ArrayList<>();

        if (maxDistance > epsilon) {
            List<Coordinate> firstPart = simplify(coordinates.subList(0, index + 1), epsilon);
            List<Coordinate> secondPart = simplify(coordinates.subList(index, coordinates.size()), epsilon);

            // Remove the last point of the firstPart to avoid duplication
            firstPart.remove(firstPart.size() - 1);

            simplifiedCoordinates.addAll(firstPart);
            simplifiedCoordinates.addAll(secondPart);
        } else {
            simplifiedCoordinates.add(coordinates.get(0));
            simplifiedCoordinates.add(coordinates.get(coordinates.size() - 1));
        }

        return simplifiedCoordinates;
    }

    private static double perpendicularDistance(Coordinate point, Coordinate lineStart, Coordinate lineEnd) {
        double lineLength = lineStart.distanceTo(lineEnd);

        if (lineLength == 0.0) {
            return point.distanceTo(lineStart);
        }

        double t = ((point.getLongitude() - lineStart.getLongitude()) * (lineEnd.getLongitude() - lineStart.getLongitude()) +
                (point.getLatitude() - lineStart.getLatitude()) * (lineEnd.getLatitude() - lineStart.getLatitude())) / (lineLength * lineLength);

        t = Math.max(0, Math.min(1, t));

        double projectedX = lineStart.getLongitude() + t * (lineEnd.getLongitude() - lineStart.getLongitude());
        double projectedY = lineStart.getLatitude() + t * (lineEnd.getLatitude() - lineStart.getLatitude());

        return point.distanceTo(new Coordinate(projectedY, projectedX));
    }
}
