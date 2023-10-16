package com.example.path_tracker.NavigatingMode;

import java.util.ArrayList;
import java.util.List;

public class RDPAlgo {

    static int min_points = 5;

    public static List<Point> simplify(List<Point> points, double epsilon) {
        if (points.size() < min_points) {
            return points;
        }

        int index = 0;
        double maxDistance = 0.0;

        for (int i = 1; i < points.size() - 1; i++) {
            double distance = LineDistance(points.get(i), points.get(0), points.get(points.size() - 1));

            if (distance > maxDistance) {
                maxDistance = distance;
                index = i;
            }
        }

        List<Point> simplifiedpoints = new ArrayList<>();

        if (maxDistance > epsilon) {
            List<Point> firstPart = simplify(points.subList(0, index + 1), epsilon);
            List<Point> secondPart = simplify(points.subList(index, points.size()), epsilon);

            // Remove the last point of the firstPart to avoid duplication
            firstPart.remove(firstPart.size() - 1);

            simplifiedpoints.addAll(firstPart);
            simplifiedpoints.addAll(secondPart);
        } else {
            simplifiedpoints.add(points.get(0));
            simplifiedpoints.add(points.get(points.size() - 1));
        }

        return simplifiedpoints;
    }

    private static double LineDistance(Point point, Point lineStart, Point lineEnd) {
        double lineLength = lineStart.distanceTo(lineEnd);

        if (lineLength == 0.0) {
            return point.distanceTo(lineStart);
        }

        double t = ((point.getLongitude() - lineStart.getLongitude()) * (lineEnd.getLongitude() - lineStart.getLongitude()) +
                (point.getLatitude() - lineStart.getLatitude()) * (lineEnd.getLatitude() - lineStart.getLatitude())) / (lineLength * lineLength);

        t = Math.max(0, Math.min(1, t));

        double projectedX = lineStart.getLongitude() + t * (lineEnd.getLongitude() - lineStart.getLongitude());
        double projectedY = lineStart.getLatitude() + t * (lineEnd.getLatitude() - lineStart.getLatitude());

        return point.distanceTo(new Point(projectedY, projectedX));
    }
}
