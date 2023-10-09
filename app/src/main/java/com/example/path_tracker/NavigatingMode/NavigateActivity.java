package com.example.path_tracker.NavigatingMode;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.path_tracker.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.widget.TextView;

public class NavigateActivity extends AppCompatActivity {

    static ArrayList<Point> points = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        Intent intent = getIntent();
        if (intent != null) {
            String fileName = intent.getStringExtra("FILE_NAME");
        }
    }

    /**
     * transfer the content in the file into coordinates type.
     * Note: right now we don't need timestamp.
     * @param fileName
     */
    private void stringToCoordinate(String fileName) {
        try {
            // Open the file using InputStream
            InputStream inputStream = openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();
            inputStream.close();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
