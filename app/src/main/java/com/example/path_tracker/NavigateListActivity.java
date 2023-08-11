package com.example.path_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class NavigateListActivity extends AppCompatActivity {

    private ListView listViewFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        listViewFiles = findViewById(R.id.listViewFiles);

        // Get the directory
        File filesDirectory = getFilesDir();

        // Get all the files that are in text form
        String[] fileNamesArray = filesDirectory.list();
        ArrayList<String> fileNamesArrayList = new ArrayList<>(Arrays.asList(fileNamesArray));
        ArrayList<String> fileNamesList = new ArrayList<>();
        for(String s : fileNamesArrayList) {
            if (s.contains(".txt")) {
                fileNamesList.add(s.substring(0,s.length()-4)); // only displays the path name
            }
        }

        // Display the list of file names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNamesList);
        listViewFiles.setAdapter(adapter);


        listViewFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = (String) parent.getItemAtPosition(position);

                Intent intent = new Intent(NavigateListActivity.this, NavigateActivity.class);
                intent.putExtra("FILE_NAME", fileName);
                startActivity(intent);
            }
        });



    }
}