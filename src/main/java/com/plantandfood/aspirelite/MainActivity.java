package com.plantandfood.aspirelite;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    /* Class-local EntryAdapter instance */
    EntryAdapter entryAdapter;
    /* Class-local Log instance */
    Log log;
    /* Spinner position */
    int spinnerValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Restore the UI */
        super.onCreate(savedInstanceState);

        /* Set the current view */
        setContentView(R.layout.activity_main);

        /* Initialise the logger */
        this.log = new Log(this, (LinearLayout)findViewById(R.id.MessageList));

        /* Create the entryAdapter and populate the grid */
        GridView entryLayout = (GridView) findViewById(R.id.BrixEntryLayout);
        entryAdapter = new EntryAdapter(this);
        entryLayout.setAdapter(entryAdapter);

        /* Create the initial text entry elements */
        Resources res = getResources();
        int brix_count = res.getInteger(R.integer.MIN_BRIX_READINGS);
        for (int i = 0; i < brix_count; i++) {
            addEntry(null);
        }

        /* Initialise the spinner */
        Spinner spinner = (Spinner) findViewById(R.id.PlantStageSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.plant_stage_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        /* Try to resume from the file */
        /* Find the current spinner value */
        try {
            FileInputStream file = openFileInput("plant-stage");
            int spinnerValue = file.read();
            spinner.setSelection(spinnerValue);
            log.log(Log.DEBUG, "Loaded the plant stage of " + spinnerValue);
            file.close();
        } catch (Exception e) {
            log.log(Log.DEBUG, "Error loading plant stage; got " + e.toString());
        }
    }

    @Override
    protected void onPause() {
        /* Save the current state... this is always supposed to be called */

        super.onPause();
    }

    public void addEntry(View view) {
        /* Add the button */
        entryAdapter.add();
        /* Re-enable the button if the count is high enough */
        if (entryAdapter.getCount() > getResources().getInteger(R.integer.MIN_BRIX_READINGS)) {
            findViewById(R.id.BrixMinus).setEnabled(true);
        } else {
            /* Otherwise, ensure that the button is disabled... */
            findViewById(R.id.BrixMinus).setEnabled(false);
        }
    }

    public void rmEntry(View view) {
        /* Find the minimum number of entries */
        int brix_count = getResources().getInteger(R.integer.MIN_BRIX_READINGS);
        /* Remove an entry if there is enough */
        if (entryAdapter.getCount() > brix_count) {
            entryAdapter.rm();
        }
        /* Disable the button if the count is now too low */
        if (entryAdapter.getCount() <= brix_count) {
            view.setEnabled(false);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        /* Record the value */
        spinnerValue = position;
        /* Refresh the display */
        refresh();
        /* Spinner has something selected */
        log.log(Log.DEBUG, "Spinner at position " + position + " has been selected");
        /* Save the value */
        try {
            FileOutputStream file = openFileOutput("plant-stage", Context.MODE_PRIVATE);
            file.write(spinnerValue);
            file.close();
            log.log(Log.DEBUG, "Saved the plant stage of " + spinnerValue);
        } catch (Exception e) {
            log.log(Log.DEBUG, "File not found; got " + e.toString());
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        /* Spinner is deselected */
        log.log(Log.DEBUG, "Spinner is deselected");
    }

    public void refresh() {
        /* Recalculate... something has changed */
        log.clear();
        log.log(Log.DEBUG, "Refreshing...");
    }
}

