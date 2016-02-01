package com.plantandfood.aspirelite;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    /* Class-local EntryAdapter instance */
    EntryAdapter entryAdapter;
    /* Class-local Log instance */
    Log log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        log.log(Log.DEBUG, "Adding an item");
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
}

