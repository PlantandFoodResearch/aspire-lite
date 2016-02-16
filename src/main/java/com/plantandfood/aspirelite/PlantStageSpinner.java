/* PlantStageSpinner abstraction - spinner with persist/resume code builtin */

package com.plantandfood.aspirelite;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class PlantStageSpinner extends Spinner
        implements AdapterView.OnItemSelectedListener {

    /* Local LogTag */
    private String LogTag = "PlantStageSpinner";

    /* Local context */
    private Context context;
    /* Current value */
    private int value;

    /* Callback for changes */
    SomethingChangedListener listener;

    /* Initialisers */
    public PlantStageSpinner(Context context) {
        super(context);
        onCreate(context);
    }
    public PlantStageSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        onCreate(context);
    }
    public PlantStageSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onCreate(context);
    }

    private void onCreate(Context context) {
        /* Initialise this */
        this.context = context;

        /* Set the array adapter for this */
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.plant_stage_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.setAdapter(adapter);
        this.setOnItemSelectedListener(this);

        /* Load the current state */
        load();
    }

    /* Persist/resume code */
    private void persist() {
        /* Save the current value */
        try {
            FileOutputStream file = context.openFileOutput(
                    getResources().getString(R.string.persist_plant_stage),
                    Context.MODE_PRIVATE);
            file.write(value);
            file.close();
            Log.d(LogTag, "Saved the plant stage of " + value);
        } catch (Exception e) {
            Log.e(LogTag, "Error saving values; got " + e.toString());
        }
    }
    private void load() {
        /* Load the previous stage from a file */
        try {
            FileInputStream file = context.openFileInput(
                    getResources().getString(R.string.persist_plant_stage));
            value = file.read();
            this.setSelection(value);
            Log.d(LogTag, "Loaded the plant stage of " + value);
            file.close();
        } catch (Exception e) {
            Log.e(LogTag, "Error loading plant stage; got " + e.toString());
        }
    }

    /* Handlers for selection/deselection */
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        /* Handle the spinner being selected.
         * We first confirm that the spinner's value has actually changed; if
         * it has, save the value and refresh the display
         */
        if (value != position) {
            /* Save the value */
            value = position;
            /* Save the value */
            persist();
            /* Call the listener, if set */
            if (listener != null) {
                listener.textChangedCallback();
            }
        }
    }
    public void onNothingSelected(AdapterView<?> parent) {
        /* Spinner is deselected. We ignore this... */
    }
}
