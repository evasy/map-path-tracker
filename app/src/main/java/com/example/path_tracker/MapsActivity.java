package com.example.path_tracker;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.provider.FontsContractCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

    private boolean isUpdatingLocation = false; // state of whether the location is updating



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
        }


    }

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
                                longitude = currentLocation.getLongitude();
                                latitude = currentLocation.getLatitude();
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

    private boolean isLocationPermissionGranted(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            return false;
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
    }

    private void startLocationUpdates() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                getLocation();
                String newLocation;
                newLocation = String.valueOf(latitude) + "," + String.valueOf(longitude) + "," ; // time stamp
                coordinates.add(newLocation);
                mHandler.postDelayed(this, 3000); // Update every 3 seconds
            }
        };
        mHandler.postDelayed(mRunnable, 0); // Start immediately
    }

    private void stopLocationUpdates() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
            mRunnable = null;
        }
        System.out.println(coordinates);
        writeToFile();
        coordinates.clear();
    }

        private void writeToFile() {
        String filename = "file.txt"; // TODO: make it show in date and time
        String filepath = "/Users/yitongshan/AndroidStudioProjects/pathtracker/app/RecordedPaths/" + filename;
        try {
            FileWriter fileWriter = new FileWriter(filepath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (String location : coordinates) {
                bufferedWriter.write(location);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            System.out.println("Data written to the file successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}