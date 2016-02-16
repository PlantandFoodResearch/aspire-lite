/* PlantStageSpinner abstraction - spinner with persist/resume code builtin */

package com.plantandfood.aspirelite;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class PlantStageSpinner extends Spinner {

    /* Local LogTag */
    private String LogTag = "PlantStageSpinner";

    /* Local context */
    private Context context;
    /* Current value */
    int value;

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

        /* Load the current state */
        load();
    }

    /* Persist/resume code */
    public void persist() {
        /* Save the current value */
        // TODO: Make this private.
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

}
