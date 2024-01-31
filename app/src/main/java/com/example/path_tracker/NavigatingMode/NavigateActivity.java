package com.example.path_tracker.NavigatingMode;
// method 1: this way is to directly based on the points, stay on the path
// method 2: stay the right direction. consider the obstacles.
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.path_tracker.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.widget.Button;
import android.widget.TextView;

public class NavigateActivity extends AppCompatActivity {

    static ArrayList<Point> points = new ArrayList<>();
    static ArrayList<Point> RDPpoints = new ArrayList<>();
    private LocationManager locationManager;
    private Point currentLocation;
    static String fileName; // retrieve the recorded path file
    private boolean isNavigating = false; // to track navigation status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Request location updates
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, (float) 0, (LocationListener) this);
        }

        Intent intent = getIntent();
        if (intent != null) {
            fileName = intent.getStringExtra("FILE_NAME");
        }
        points = stringToPoint(fileName);

        double epsilon = 0.0001; // might need to be changed
        RDPpoints = RDPAlgo.simplify(points, epsilon);

        // Initialize navigation buttons and set their listeners
        Button startButton = findViewById(R.id.start_button);

        startButton.setOnClickListener(v -> startNavigation(RDPpoints));
//        pauseButton.setOnClickListener(v -> pauseNavigation());
    }

    /**
     * transfer the content in the file into coordinates type.
     * Note: right now we don't need timestamp.
     * @param fileName
     */
    private ArrayList<Point> stringToPoint(String fileName) {
        try {
            // Open the file using InputStream
            InputStream inputStream = openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                Point c = new Point(Double.parseDouble(line.split(",")[0]),Double.parseDouble(line.split(",")[1]));
                points.add(c);
            }
            reader.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return points;
    }


    // RDPpoints is the global variable of the simplifiedPoints
    private void startNavigation(ArrayList<Point> simplifiedPoints) {
        isNavigating = true;

        currentLocation = getCurrentLocation();

        for (int i = 0; i < simplifiedPoints.size(); i++) {
            Point nextPoint = simplifiedPoints.get(i);

            provideDirections(nextPoint);

            while (isNavigating && !hasReached(nextPoint, currentLocation)) {
                currentLocation = getCurrentLocation();
                updateDirections(currentLocation, nextPoint);
            }

            if (!isNavigating) {
                break;
            }

            // Check if the user has reached the final point
            if (i == simplifiedPoints.size() - 1) {
                onArrival();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, (float) 0, (LocationListener) this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Point location) {
        currentLocation = location;
    }

    private Point getCurrentLocation() {
        if (currentLocation != null) {
            return new Point(currentLocation.getLatitude(), currentLocation.getLongitude());
        }
        return null;
    }

    private boolean hasReached(Point destination, Point currentLocation) {
        return false; // Placeholder return
    }

    private void provideDirections(Point nextPoint) {
        Point currentLocation = getCurrentLocation();
        if (currentLocation == null || nextPoint == null) {
            // Handle null cases, maybe show an error message or a default message
            return;
        }

        double bearing = calculateBearing(currentLocation, nextPoint);
        double distance = currentLocation.distanceTo(nextPoint);

        // Determine the direction to advise based on the bearing
        String directionAdvice = getDirectionAdvice(bearing);

        // Update the UI with the direction and distance
        TextView directionText = findViewById(R.id.directionText);
        directionText.setText(directionAdvice + " for " + distance + " meters");
    }

    /**
     calculates the bearing from the current location to the next point.
     */
    private double calculateBearing(Point start, Point end) {
        double longitude1 = Math.toRadians(start.getLongitude());
        double longitude2 = Math.toRadians(end.getLongitude());
        double latitude1 = Math.toRadians(start.getLatitude());
        double latitude2 = Math.toRadians(end.getLatitude());

        double longDiff = longitude2 - longitude1;
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    private String getDirectionAdvice(double bearing) {
        if(bearing >= 45 && bearing < 135){
            return "Turn right";
        } else if(bearing >= 135 && bearing < 225){
            return "Go straight";
        } else if(bearing >= 225 && bearing < 315){
            return "Turn left";
        } else {
            return "Continue";
        }
    }


    private void updateDirections(Point currentLocation, Point destination) {
    }

    // notify the user they have arrived
    private void onArrival() {
        isNavigating = false;
    }


}
