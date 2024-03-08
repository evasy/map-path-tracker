package com.example.path_tracker.NavigatingMode;
// method 1: this way is to directly based on the points, stay on the path
// method 2: stay the right direction. consider the obstacles.
import android.location.Location;
import android.os.Handler;

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

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NavigateActivity extends AppCompatActivity implements LocationListener {

    private Handler handler = new Handler();
    static ArrayList<Point> points = new ArrayList<>();
    static ArrayList<Point> RDPpoints = new ArrayList<>();
    private LocationManager locationManager;
    private Point currentLocation;
    static String fileName; // retrieve the recorded path file
    private boolean isNavigating = false; // to track navigation status
    private double thresholdDistance; // threshold of the user's location and how far from the next waypoint

    private float minUpdateDistance = 0.0F; // min distance change for updates in meters

    private long minUpdateTime = 1000; // min time between updates in milliseconds (1 s = 1000 ms)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            startLocationUpdates();
        }

        // Retrieve the intent that started this activity
        Intent intent = getIntent();
        if (intent != null) {
            fileName = intent.getStringExtra("FILE_NAME");
        }

        double epsilon = 0.0001; // might need to be changed

        // convert the txt file content to points
        points = stringToPoint(fileName);
        // using the RDP algorithm to simplify our points
        RDPpoints = RDPAlgo.simplify(points, epsilon);

        Button startPauseButton = findViewById(R.id.btnStartPause);
        Button btnEnd = findViewById(R.id.btnEnd);

        btnEnd.setVisibility(View.INVISIBLE);

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopNavigation();
                stopLocationUpdates();
            }
        });

        // Set click listener for the start/pause button
        startPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNavigating) {
                    // Start navigation
                    isNavigating = true;
                    startPauseButton.setText("Pause");
                    btnEnd.setVisibility(View.VISIBLE);
                    // Start navigation with simplified points
                    startNavigation(RDPpoints);
                } else {
                    // Pause navigation
                    isNavigating = false;
                    startPauseButton.setText("Start");
                    btnEnd.setVisibility(View.GONE);
                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            // Handle permission denied
        }
    }

    private void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minUpdateTime, minUpdateDistance, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = new Point(location.getLatitude(), location.getLongitude());
    }

    /**
     * Transfer the content in the file into Points type.
     * Note: right now we don't need timestamp.
     * @param fileName The name of the file containing the coordinates
     * @return ArrayList<Point> containing the parsed points from the file
     */
    private ArrayList<Point> stringToPoint(String fileName) {
        ArrayList<Point> points = new ArrayList<>();
        try {
            // Open the file
            InputStream inputStream = openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] coordinates = line.split(",");
                double latitude = Double.parseDouble(coordinates[0]);
                double longitude = Double.parseDouble(coordinates[1]);
                Point point = new Point(latitude, longitude);
                points.add(point);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return points;
    }



    /**
     * Start Navigation:
     *  - button turned to 'Pause' and 'End' button shows up
     *  - change isNavigating to true
     *  - start looping through the points, guide the user to the next point from the previous point
     *  - if the user reaches the waypoint, update to the next point
     * @param simplifiedPoints has the global variable input RDPpoints
     */
    private void startNavigation(ArrayList<Point> simplifiedPoints) {
        isNavigating = true;

        currentLocation = getCurrentLocation();

        for (int i = 0; i < simplifiedPoints.size(); i++) {
            Point nextPoint = simplifiedPoints.get(i);

            // provides direction to the next waypoint
            provideDirections(nextPoint);

            // Check if the user has reached the next waypoint
            if (hasReachedNextWaypoint(currentLocation, nextPoint)) {
                continue;
            }

            if (!isNavigating) {
                break;
            }

            // Check if the user has reached the final point
            if (i == simplifiedPoints.size() - 1) {
                stopNavigation(); // isNavigating = false;
            }
        }
    }


    /**
     * retrieving the current GPS location of the user
     * @return the GPS location in Point
     */
    private Point getCurrentLocation() {
        if (currentLocation != null) {
            return new Point(currentLocation.getLatitude(), currentLocation.getLongitude());
        }
        return null;
    }

    /**
     * check if the current location is around the next waypoint
     * if it reaches, return true
     */
    private boolean hasReachedNextWaypoint(Point currentLocation, Point nextWaypoint) {
        thresholdDistance = 0.5;

        // Calculate the distance between the current location and the next waypoint
        double distance = currentLocation.distanceTo(nextWaypoint);

        // Check if the distance is below the threshold
        if (distance <= thresholdDistance) {
            return true;
        } else {
            return false;
        }
    }


    private void provideDirections(Point nextPoint) {
        Point currentLocation = getCurrentLocation();

//        // Handle null cases, maybe show an error message or a default message
//        if (currentLocation == null || nextPoint == null) {
//            return;
//        }

        double bearing = calculateBearing(currentLocation, nextPoint);
        double distance = currentLocation.distanceTo(nextPoint);

        // Determine the direction to advise based on the bearing
        String instruction = getInstruction(bearing);

        // Update the UI
        TextView directionText = findViewById(R.id.directionText);
        directionText.setText(instruction);

        // update every 5 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                provideDirections(nextPoint);
            }
        }, 5000);
    }

    /**
     calculates the bearing from the current location to the next point.
     TODO: how to inform the user the direction they should be !!
     TODO: compass
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

    private String getInstruction(double bearing) {
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



    /**
     End Navigation. Either in the case when the user presses the end button,
     or the user has arrived.
     */
    private void stopNavigation() {
        isNavigating = false;
    }


}
