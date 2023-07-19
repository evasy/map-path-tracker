package com.example.path_tracker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DisplayTextFileActivity extends AppCompatActivity {

    private TextView textViewFileContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_text_file);

        textViewFileContent = findViewById(R.id.textViewFileContent);

        String fileName = getIntent().getStringExtra("FILE_NAME");

        // Read and display the contents of the file
        displayFileContent(fileName);
    }

    private void displayFileContent(String fileName) {
        try {
            // Open the file using InputStream
            InputStream inputStream = openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            // Display the content in the TextView
            textViewFileContent.setText(stringBuilder.toString());
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
