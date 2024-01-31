package com.example.path_tracker.NavigatingMode;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import com.example.path_tracker.R;

public class CompassActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];
    private TextView textView;

    private final float PATH_DIRECTION = 90; // can be dynamic, Example: (East)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        textView = findViewById(R.id.textView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL); // register listener
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this); // unregister
    }

    /**
     * This method is called whenever there is a new sensor event.
     * It processes the sensor data, calculates the device orientation, and displays the information.
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // sensor type check
        if (event.sensor == accelerometer) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor == magnetometer) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix, orientation);
            float azimuthInRadians = orientation[0];
            float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

            float angleDifference = calculateAngleDifference(azimuthInDegrees, PATH_DIRECTION);

            textView.setText("Direction: " + azimuthInDegrees + "°" + "\nAngle Difference: " + angleDifference + "°");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Implementing changes in sensor accuracy if necessary.
    }

    private float calculateAngleDifference(float currentDirection, float pathDirection) {
        float angleDifference = pathDirection - currentDirection;

        while (angleDifference < 0) {
            angleDifference += 360;
        }

        while (angleDifference >= 360) {
            angleDifference -= 360;
        }

        if (angleDifference > 180) {
            angleDifference = 360 - angleDifference;
        }

        return angleDifference;
    }
}
