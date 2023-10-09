package com.example.path_tracker.RecordingMode;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.provider.FontsContractCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.path_tracker.R;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.path_tracker.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient mFusedLocationClient;
    // List<Address> listGeoCoder;
    private static final int LOCATION_PERMISSION_CODE = 1028;
    private static final String TAG = "MapsActivity";

    // record the coordinates, and a state of 'obstacled' or not
    private ArrayList<String> coordinates = new ArrayList<>();

    private double longitude;
    private double latitude;

    // for executing periodically (every 3 seconds)
    private Handler mHandler;
    private Runnable mRunnable;

    private Button btnStartPause;
    private Button btnSaveFile;

    private boolean isUpdatingLocation = false; // state of whether the location is updating
    private String fileName;
    private String filepath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnStartPause = findViewById(R.id.btnStartPause);
        btnStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isUpdatingLocation) {
                    mMap.clear();
                    startLocationUpdates();
                    btnStartPause.setText("Pause");
                } else {
                    stopLocationUpdates();
                    btnStartPause.setText("Start");
                }
                isUpdatingLocation = !isUpdatingLocation;
            }
        });
        if (isLocationPermissionGranted()) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            getLocation();
        }
        else {
            requestLocationPermission();
            getLocation();
        }
    }

    /**
      * retrieves the device's current location.
      * checks for location permission before requesting the location.
      */
    private void getLocation() {
        Log.d(TAG, "Get the device's current location");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (isLocationPermissionGranted()) {
                Task location = mFusedLocationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                latitude = currentLocation.getLatitude();
                                longitude = currentLocation.getLongitude();

                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(latitude, longitude), 16)); // zoom to center

                                Log.i("GOOGLE_MAP_TAG", "Current location coordinate (latitude, longitude): " +
                                        String.valueOf(latitude) + ", " + String.valueOf(longitude));
                            } else {
                                Log.d(TAG, "Location is null");
                            }
                        } else {
                            Log.d(TAG, "Error getting location: " + task.getException().getMessage());
                        }
                    }

                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Check if the map is ready.
     * It checks for location permission and enables the "My Location" button on the map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getLocation(); // Retrieve current location
        } else {
            requestLocationPermission();
        }
    }

    /**
     * checks if the location permission is granted.
     */
    private boolean isLocationPermissionGranted(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * requests the location permission from the user.
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
    }


    /**
     * retrieves location every 3 seconds and
     * store the coordinates as a string into 'coordinates'
     */
    private void startLocationUpdates() {
        mMap.clear(); // if there are markers, clear them before start

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                getLocation();
                if (latitude != 0 && longitude != 0) {
                    markAndLocate(latitude, longitude);
                }

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String timestamp = now.format(formatter);

                String newLocation;
                newLocation = String.valueOf(latitude) + "," + String.valueOf(longitude) + ",";
//                newLocation = String.valueOf(latitude) + "," + String.valueOf(longitude) + "," + timestamp;
                coordinates.add(newLocation);

                Toast.makeText(getApplicationContext(), "Your Location: " + latitude + ", " + longitude,
                        Toast.LENGTH_SHORT).show(); // output location for testing
                mHandler.postDelayed(this, 3000); // Update every 3 seconds
            }
        };
        mHandler.postDelayed(mRunnable, 0); // Start immediately
    }

    /**
     * writes the coordinates into a txt file and clears the 'coordinates' arraylist
     */
    private void stopLocationUpdates() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
            mRunnable = null;
        }
        System.out.println(coordinates);
        editFileName();
    }

    /**
     * add a marker on the map.
     */
    private void markAndLocate(double latitude, double longitude) {
        MarkerOptions mp = new MarkerOptions();
        mp.position(new LatLng(latitude, longitude));
        mMap.addMarker(mp);
    }

    /**
     * edit the name of the file based on the input
     * and save the file with the entered name in text format.
     */
    private void editFileName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Path Name:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // save button
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fileName = input.getText().toString();
                saveFile(fileName);
            }

        });

        // cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * save the arraylist of coordinates
     * @param fileName is the input filename
     */
    private void saveFile(String fileName) {
//        LocalDateTime now = LocalDateTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String timestamp = now.format(formatter);
        fileName = fileName + ".txt";
        filepath = getApplicationContext().getFilesDir() + "/" + fileName; // /data/user/0/com.example.path_tracker/files/filename.txt
        try {
            FileWriter fileWriter = new FileWriter(filepath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (String location : coordinates) {
                bufferedWriter.write(location);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            System.out.println("Data written to the file at " + filepath + " successfully.");
            Toast.makeText(getApplicationContext(), "File Path: " + filepath + ", saved.", Toast.LENGTH_SHORT).show();

            filepath = ""; // reset
            coordinates.clear(); // reset

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}