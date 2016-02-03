/* Main "activity"
 * Basically the landing stage for anyone opening the app...
 * This includes most of the UI and calculation code.
 */

package com.plantandfood.aspirelite;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    /* Constants */
    /* CHO calculation constants */
    float CHO_INTERCEPT = 66.8f;
    float CHO_SLOPE = 18f;
    /* Crop age constants */
    int AGE_OF_CROP_MATURITY = 4; // In years.
    int AGE_YOUNG = 1;
    int AGE_MATURE = 2;
    /* Data verification constants */
    int OUTLIER_RANGE = 2;

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
        /* Load the brix% values */
        try {
            FileInputStream file = openFileInput("brix-readings");
            int character;
            StringBuilder current = new StringBuilder();
            while ((character = file.read()) != -1) {
                if (character == '\0') {
                    /* Save the current string */
                    entryAdapter.add(current.toString());
                    log.log(Log.ERROR, "Loaded a brix% reading of " + current.toString());
                    current = new StringBuilder();
                } else {
                    current.append((char) character);
                }
            }
            file.close();
        } catch (Exception e) {
            log.log(Log.DEBUG, "Error loading brix% readings; got " + e.toString());
        }
        /* Add any missing required boxes */
        int brix_count = getResources().getInteger(R.integer.MIN_BRIX_READINGS);
        for (int i = entryAdapter.getCount(); i < brix_count; i++) {
            entryAdapter.add(null);
        }
        updateBrixMinus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Handle the menu creation event */
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Handle menu item selection */
        Intent intent;
        switch (item.getItemId()) {
            case R.id.help:
                intent = new Intent(this, HelpActivity.class);
                break;
            case R.id.feedback:
                intent = new Intent(this, FeedbackActivity.class);
                break;
            case R.id.legal:
                intent = new Intent(this, LegalActivity.class);
                break;
            case R.id.about:
                intent = new Intent(this, AboutActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        /* Launch the new activity and return */
        startActivity(intent);
        return true;
    }

    public void addEntry(View view) {
        /* Add the button */
        entryAdapter.add(null);
        updateBrixMinus();
        persistEntries();
    }

    public void updateBrixMinus() {
        /* Update the state of the BrixMinus button */

        /* Re-enable the button if the count is high enough */
        if (entryAdapter.getCount() > getResources().getInteger(R.integer.MIN_BRIX_READINGS)) {
            findViewById(R.id.BrixMinus).setEnabled(true);
        } else {
            /* Otherwise, ensure that the button is disabled... */
            findViewById(R.id.BrixMinus).setEnabled(false);
        }
    }

    public void rmEntry(View view) {
        /* Remove an entry if there is enough */
        if (entryAdapter.getCount() > getResources().getInteger(R.integer.MIN_BRIX_READINGS)) {
            entryAdapter.rm();
        }
        updateBrixMinus();
        updateEntries();
    }

    public void resetResults(View view) {
        /* Reset the results - the reset button has been pressed
        * First, though, create a new dialog and check that the user *really* wants to reset all
        * fields. Realistically, it might also be wise to add some other precautions.
        */
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.Reset))
                .setMessage(getResources().getString(R.string.ResetAll))
                .setPositiveButton(getResources().getString(R.string.Reset),
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /* Continue with the delete... */
                        entryAdapter.reset(getResources().getInteger(R.integer.MIN_BRIX_READINGS));
                        updateBrixMinus();
                        updateEntries();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /* Do nothing; this is required so that there *is* a cancel button  */
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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
            log.log(Log.WARN, "Error saving values; got " + e.toString());
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        /* Spinner is deselected */
        log.log(Log.DEBUG, "Spinner is deselected");
    }

    public void updateEntries() {
        /* Persist the entries, then refresh */
        persistEntries();
        refresh();
    }

    private void persistEntries() {
        /* Persist the entries */
        try {
            FileOutputStream file = openFileOutput("brix-readings", Context.MODE_PRIVATE);
            for (int i = 0; i < entryAdapter.getCount(); i ++) {
                /* Persist this item */
                String value = entryAdapter.getItem(i);
                for (int c = 0; c < value.length(); c++) {
                    int character = value.charAt(c);
                    if (character != '\0') {
                        /* We use \0 (a null byte) as a delimeter */
                        file.write(character);
                    }
                }
                file.write('\0');
                log.log(Log.DEBUG, "Saved a Brix% reading of " + value);
            }
            file.close();
        } catch (Exception e) {
            log.log(Log.WARN, "Error saving values; got " + e.toString());
        }
    }

    public void refresh() {
        /* Recalculate... something has changed */
        log.clear();
        log.log(Log.DEBUG, "Refreshing...");
        if (!updateResults()) {
            /* Updating the results worked; print the CHO reading */
            log.log(Log.MESSAGE, "Estimated CHO: " + cho());
            comment();
        }
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
                        entryAdapter.markInvalid(i);
                    } else {
                        /* A valid result!
                        * Clear any formatting and add the result.
                        */
                        results.add(brix);
                        entryAdapter.markValid(i);
                    }
                } catch (NumberFormatException e) {
                    /* Not a number */
                    error = true;
                    log.log(Log.ERROR, "Invalid number '" + value + "'!");
                    entryAdapter.markInvalid(i);
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
        for (int i = 0; i < results.size(); i ++) {
            if (results.get(i) < mean - (stddev * OUTLIER_RANGE) ||
                    results.get(i) > mean + (stddev * OUTLIER_RANGE)) {
                log.log(Log.ERROR, "Discarding out of range Brix% reading " + results.get(i));
                error = true;
                entryAdapter.markInvalid(i);
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

    public float cho() {
        /* Return the estimated CHO reading */
        return CHO_INTERCEPT + (CHO_SLOPE * meanResult());
    }

    public void comment() {
        /* Print a comment on the currently estimated CHO reading and plant stage */

        String comment;
        boolean error = false;
        int age_category = AGE_MATURE;
        float cho = cho();

        // TODO: Implement young crop support.
        // TODO: Load the data from a database?

        if (age_category == AGE_MATURE) {
            if (spinnerValue == 0) {
                if (cho >= 0 && cho < 150) {
                    comment = "Root CHO content is very low for this stage of the annual growth cycle, probably due to inadequate replenishment of root reserves during the previous fern growth season.  This could have resulted from poor fern establishment, additional flushes during fern growth or premature fern loss or needle drop. These may have been caused by too much water, a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.  If a spear harvest is attempted, yield will probably be very low and subsequent fern growth will be poor.  No spear harvest will allow a long fern growth period and, therefore, plenty of opportunity for root recovery for the following season.";
                } else if (cho >= 150 && cho < 250) {
                    comment = "Root CHO content is low for this stage of the annual growth cycle, probably due to inadequate replenishment of root reserves during the previous fern growth season.  This could have resulted from poor fern establishment, additional flushes during fern growth or premature fern loss or needle drop.  These may have been caused by too much water, a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.  Spear yield will probably be low.  A short harvest would allow a longer fern growth period and, therefore, more opportunity for root recovery after close-up.";
                } else if (cho >= 250 && cho < 350) {
                    comment = "Root CHO content is below normal for this stage of the annual growth cycle, probably due to inadequate replenishment of root reserves during the previous fern growth season. This could have resulted from poor fern establishment, additional flushes during fern growth or premature fern loss or needle drop. These may have been caused by too much water, a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.  Spear yield will probably be lower than normal.";
                } else if (cho >= 350 && cho < 450) {
                    comment = "Root CHO content is good but not as high as it could be at this stage of the annual growth cycle,  probably due to incomplete replenishment of root reserves during the previous fern growth season.  This could have resulted from additional flushes during fern growth or premature fern loss or needle drop.  These may have been caused by too much water, a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.  As a result, spear yield could be reduced below optimum, especially if weather conditions are unfavourable for spear growth during harvest. A shorter harvest than usual would allow a longer fern growth period and, therefore, more opportunity for root recovery after close-up.";
                } else if (cho >= 450) {
                    comment = "The root system is full of CHO, as it should be at this stage of the annual growth cycle.  This is due to full replenishment of root CHO reserves during the previous fern growth season.  As a result, CHO availability will not restrict spear yield, and subsequent fern growth should be good.";
                } else {
                    comment = "Out-of-bounds cho measure of " + cho + "!";
                    error = true;
                }
            } else if (spinnerValue == 1) {
                if (cho >= 0 && cho < 150) {
                    comment = "Root CHO content is very low for this stage of the annual growth cycle. This could be because the harvest was too long, but it is more likely a consequence of inadequate replenishment of root reserves during the previous fern growth season. The latter could have resulted from poor fern establishment, additional flushes during fern growth or premature fern loss or needle drop.  These may have been caused by too much water, a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.  Low CHO availability will restrict initial growth of the ferns after close-up.";
                } else if (cho >= 150 && cho < 250) {
                    comment = "Root CHO content is lower than normal for this stage of the annual growth cycle.  This could be because the harvest was too long, but it is more likely a consequence of inadequate replenishment of root reserves during the previous season.  The latter could have resulted from poor fern establishment, additional flushes during fern growth or premature fern loss or needle drop.  These may have been caused by too much water, a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.  Low CHO availability may restrict initial growth of the ferns after close-up.";
                } else if (cho >= 250 && cho < 350) {
                    comment = "Root CHO content is about normal for this stage of the annual growth cycle.  Root reserves must have been replenished fully during the previous fern growth season, and have not been depleted too much during harvest.  At the higher end of the range, spear harvest could be continued without harming the crop.  CHO availability is unlikely to restrict fern establishment after close-up.";
                } else if (cho >= 350) {
                    comment = "Root CHO content is above normal for this stage of the annual growth cycle.  Root reserves must have been replenished fully during the previous fern growth season, and have not been depleted during harvest.  Spear harvest could be continued without harming the crop.  CHO availability will not restrict fern establishment after close-up.";
                } else {
                    comment = "Out-of-bounds cho measure of " + cho + "!";
                    error = true;
                }
            } else if (spinnerValue == 2) {
                if (cho >= 0 && cho < 200) {
                    comment = "Root CHO content is lower than normal for this stage of the annual growth cycle. This is likely to be associated with either excessive or poor fern establishment.  Excessive initial fern growth, which could result from too much water or fertiliser application, would cause a large CHO depletion.  Poor establishment would result from a root CHO content that was already low at close-up.  This could be because the harvest was too long, but it is more likely a consequence of inadequate replenishment of root reserves during the previous season.";
                } else if (cho >= 200 && cho < 300) {
                    comment = "Root CHO content is about normal for this stage of the annual growth cycle.  Root reserves must have been replenished fully during the previous fern growth season, and have not been depleted too much during harvest or initial fern growth.";
                } else if (cho >= 300 && cho < 400) {
                    comment = "Root CHO content is higher than normal for this stage of the annual growth cycle.  This condition is unusual because fern establishment is a heavy drain on root reserves, and much of the available CHO left after spear harvest is usually utilised during this phase. The high level suggests that spear harvest was too short, and could have been extended without harming the crop.  Alternatively, it could result from poor fern growth during establishment.";
                } else if (cho >= 400) {
                    comment = "Root CHO content is much higher than normal for this stage of the annual growth cycle. This condition is very unusual because fern establishment is a heavy drain on root reserves, and much of the available CHO left after spear harvest is usually utilised during this phase.  The high level suggests that spear harvest was too short, and could have been extended substantially without harming the crop.  Alternatively, it could result from poor fern growth during establishment.";
                } else {
                    comment = "Out-of-bounds cho measure of " + cho + "!";
                    error = true;
                }
            } else if (spinnerValue == 3) {
                if (cho >= 0 && cho < 300) {
                    comment = "Root CHO content is lower than normal for this stage of the crops annual growth cycle.  It should be recharging rapidly due to the activity of the established fern. The low value could result from poor fern establishment, fern still in the establishment phase, a recent new flush of fern growth or premature fern loss or needle drop.  The latter could be caused by a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.";
                } else if (cho >= 300 && cho < 400) {
                    comment = "Root CHO content is not yet fully recharged.  It is about normal for this stage of the crops annual growth cycle, and should be increasing rapidly due to the activity of the established fern.";
                } else if (cho >= 400 && cho < 500) {
                    comment = "Root CHO content is higher than normal for this stage of the crops annual growth cycle.  It is almost fully recharged, but should still be increasing due to the activity of the established fern.";
                } else if (cho >= 500) {
                    comment = "Root CHO content is fully recharged. This has occurred earlier than usual, perhaps because the CHO content was not fully depleted earlier in the season.";
                } else {
                    comment = "Out-of-bounds cho measure of " + cho + "!";
                    error = true;
                }
            } else if (spinnerValue == 4) {
                if (cho >= 0 && cho < 300) {
                    comment = "Root CHO content is much lower than normal for this stage of the crops annual growth cycle.  The CHO content should be almost fully recharged by this stage.  The low value could result from poor fern establishment, a recent new flush of fern growth or premature fern loss or needle drop.  The latter could be caused by a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.";
                } else if (cho >= 300 && cho < 400) {
                    comment = "Root CHO content is lower than normal for this stage of the crops annual growth cycle.  The CHO content should be almost fully recharged by this stage. The low value could result from poor fern establishment, a recent new flush of fern growth or premature fern loss or needle drop.  The latter could be caused by a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.";
                } else if (cho >= 400 && cho < 500) {
                    comment = "Root CHO content is about normal for this stage of the crops annual growth cycle.  It is almost fully recharged, but should still be increasing due to the activity of the established fern.";
                } else if (cho >= 500) {
                    comment = "Root CHO content is fully recharged. This has occurred slightly earlier than usual, perhaps because the CHO content was not fully depleted earlier in the season, but ensures that the crop is well set up for good performance next season.";
                } else {
                    comment = "Out-of-bounds cho measure of " + cho + "!";
                    error = true;
                }
            } else if (spinnerValue == 5) {
                if (cho >= 0 && cho < 150) {
                    comment = "Root CHO content is very low for this stage of the crops annual growth cycle due to inadequate replenishment of root reserves during fern growth.  This could have resulted from poor fern establishment, additional flushes during fern growth or premature fern loss or needle drop.  These may have been caused by too much water, a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.  Next season, spear yield will probably be very low if the crop is harvested and subsequent fern growth will be poor.";
                } else if (cho >= 150 && cho < 250) {
                    comment = "Root CHO content is low for this stage of the crops annual growth cycle due to inadequate replenishment of root reserves during fern growth.  This could have resulted from poor fern establishment, additional flushes during fern growth or premature fern loss or needle drop.  These may have been caused by too much water, a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.  Next season, spear yield will probably be low if the crop is harvested and subsequent fern growth will be poor.";
                } else if (cho >= 250 && cho < 350) {
                    comment = "Root CHO content is below normal for this stage of the crops annual growth cycle due to inadequate replenishment of root reserves during fern growth.  This could have resulted from poor fern establishment, additional flushes during fern growth or premature fern loss or needle drop.  These may have been caused by too much water, a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.  Next season, spear yield will probably be lower than normal if the crop is harvested and subsequent fern growth will be poor.";
                } else if (cho >= 350 && cho < 450) {
                    comment = "The root system CHO content is good but not as high as it could be at this stage of the crops annual growth cycle due to incomplete replenishment of root reserves during fern growth.  This could have resulted from additional flushes during fern growth or premature fern loss or needle drop.  These may have been caused by too much water, a foliar disease such as Stemphylium or physical damage such as that caused by wind or hail.  Next season, spear yield could be reduced below optimum, especially if weather conditions are unfavourable for spear growth during harvest.";
                } else if (cho >= 450) {
                    comment = "The root system is full of CHO, as it should be at this stage of the crops annual growth cycle.  The high CHO content is the result of good fern growth and replenishment of root CHO reserves during the season. This ensures that the crop is well set up for good performance next season. CHO availability will not restrict spear yield and subsequent fern growth.";
                } else {
                    comment = "Out-of-bounds cho measure of " + cho + "!";
                    error = true;
                }
            } else {
                comment = "Unknown stage " + spinnerValue + "!";
                error = true;
            }
        } else {
            comment = "Unknown age category " + age_category + "!";
            error = true;
        }

        if (error) {
            log.log(Log.ERROR, comment);
        } else {
            log.log(Log.MESSAGE, comment);
        }
    }
}
