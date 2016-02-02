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
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    /* Class-local EntryAdapter instance */
    EntryAdapter entryAdapter;
    /* Class-local Log instance */
    Log log;
    /* Spinner position */
    int spinnerValue;
    /* Current (valid) results */
    ArrayList<Float> results;

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

    public void updateEntries() {
        /* Persist the entries, then refresh */
        // TODO: Implement persistence...
        refresh();
    }

    public void refresh() {
        /* Recalculate... something has changed */
        log.clear();
        log.log(Log.DEBUG, "Refreshing...");
        updateResults();
    }

    public boolean updateResults() {
        /* Find the Brix% readings, sanitizing/updating as we go */

        /* Run an initial check */
        results = new ArrayList<>();
        boolean error = false;
        for (int i = 0; i < entryAdapter.getCount(); i ++) {
            /* Check that item */
            String value = entryAdapter.getItem(i);
            if (value.length() != 0) {
                try {
                    /* Add the value (if it converts and is valid) */
                    Float brix = Float.parseFloat(value);
                    if (brix < 0 || brix > 32) {
                        /* Out of range! */
                        error = true;
                        log.log(Log.ERROR, "Invalid Brix% reading ('" + brix +
                                "' should be between 0 and 32)!");
                    } else {
                        /* A valid result!
                        * Clear any formatting and add the result.
                        */
                        results.add(brix);
                    }
                } catch (NumberFormatException e) {
                    /* Not a number */
                    error = true;
                    log.log(Log.ERROR, "Invalid number '" + value + "'!");
                }
            }
        }

        /* Check the value count */
        int min_entries = getResources().getInteger(R.integer.MIN_BRIX_READINGS);
        if (min_entries > results.size()) {
            /* Bail out, the count is too small */
            log.log(Log.ERROR, "Insufficient Brix% readings entered (" + results.size() + "/" +
                min_entries + ")!");
            error = true;
        }

        /* Check that the values are sane, and sanitize the results */
        ArrayList<Float> sanitized = new ArrayList<>();
        float stddev = stddevResult();
        float mean = meanResult();
        log.log(Log.ERROR, "Mean:" + mean + ", Stddev:" + stddev);
        for (int i = 0; i < results.size(); i ++) {
            if (results.get(i) < mean - (stddev * 2) ||
                    results.get(i) > mean + (stddev * 2)) {
                log.log(Log.ERROR, "Discarding out of range Brix% reading " + results.get(i));
                error = true;
            } else {
                sanitized.add(results.get(i));
            }
        }
        /* Save the sanitized results */
        results = sanitized;

        return error;
    }

    public float meanResult() {
        /* Calculate the mean result */
        float total = 0;
        for (int i = 0; i < results.size(); i ++) {
            total += results.get(i);
        }
        return total / results.size();
    }

    public float stddevResult() {
        /* Find the standard deviation of the results */
        float sum = 0;
        float avg = meanResult();
        for (int i = 0; i < results.size(); i ++) {
            sum += Math.pow(avg - results.get(i), 2f);
        }
        return (float)Math.sqrt(sum / results.size());
    }
}
