package com.example.path_tracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FileListActivity extends AppCompatActivity {

    private ListView listViewFiles;
    private ArrayList<String> txtfileNamesList;
    private ArrayList<String> selectedFiles;
    private ArrayAdapter<String> adapter;
    private Button btnSelect;
    private Button btnDelete;

    private boolean isSelectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listfiles);

        listViewFiles = findViewById(R.id.listViewFiles);
        btnSelect = findViewById(R.id.btnSelect);
        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setVisibility(View.GONE);
        selectedFiles = new ArrayList<>();

        // Get the directory
        File filesDirectory = getFilesDir();

        // Get all the files that are in text form
        String[] fileNamesArray = filesDirectory.list();
        ArrayList<String> fileNames = new ArrayList<>(Arrays.asList(fileNamesArray));
        txtfileNamesList = new ArrayList<>();
        for (String s : fileNames) {
            if (s.contains(".txt")) {
                txtfileNamesList.add(s);
            }
        }

        // Display the list of file names
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, txtfileNamesList);
        listViewFiles.setAdapter(adapter);

        // listViewFiles.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listViewFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // add or remove this file from the selected files
                if (isSelectMode) {
                    String fileName = txtfileNamesList.get(position);
                    if (selectedFiles.contains(fileName)) {
                        selectedFiles.remove(fileName);
                    } else {
                        selectedFiles.add(fileName);
                    }
                    adapter.notifyDataSetChanged();
                } else { // opens the file
                    String fileName = (String) parent.getItemAtPosition(position);
                    Intent intent = new Intent(FileListActivity.this, DisplayTextFileActivity.class);
                    intent.putExtra("FILE_NAME", fileName);
                    startActivity(intent);
                }
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSelectMode();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });
    }

    private void changeSelectMode() {
        if (isSelectMode) { // when select mode is on, turn it off
            isSelectMode = false;
            btnSelect.setText("Select");
            btnDelete.setVisibility(View.GONE);
            selectedFiles.clear();
            listViewFiles.setChoiceMode(ListView.CHOICE_MODE_NONE);
        } else { // when select mode is off, turn it on
            isSelectMode = true;
            btnSelect.setText("Done");
            btnDelete.setVisibility(View.VISIBLE);
            selectedFiles.clear();
            listViewFiles.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        }
        adapter.notifyDataSetChanged();
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Files");
        builder.setMessage("Are you sure you want to delete the selected files?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (String fileName : selectedFiles) {
                    File file = new File(getFilesDir(), fileName);
                    file.delete();
                }
                changeSelectMode();
                updateFileList();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }


    private void updateFileList() {
        File filesDirectory = getFilesDir();
        String[] fileNamesArray = filesDirectory.list();
        ArrayList<String> fileNamesArrayList = new ArrayList<>(Arrays.asList(fileNamesArray));
        txtfileNamesList.clear();
        for (String s : fileNamesArrayList) {
            if (s.contains(".txt")) {
                txtfileNamesList.add(s);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
