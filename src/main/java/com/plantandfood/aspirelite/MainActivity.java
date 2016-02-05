/* Main "activity"
 * Basically the landing stage for anyone opening the app...
 * This includes most of the UI and calculation code.
 */

package com.plantandfood.aspirelite;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        View.OnClickListener {

    /* Class-local EntryAdapter instance */
    EntryAdapter entryAdapter;
    /* Class-local Log instance */
    Log log;
    /* Class-local Toast for recalculated events */
    Toast toast;
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

        /* Initialise the toast */
        toast = Toast.makeText(this, getResources().getString(R.string.CommentUpdated),
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);

        /* Try to resume from the file */
        /* Find the current spinner value */
        try {
            FileInputStream file = openFileInput("plant-stage");
            spinnerValue = file.read();
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
                    entryAdapter.create(current.toString());
                    log.log(Log.DEBUG, "Loaded a brix% reading of " + current.toString());
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
        for (int i = entryAdapter.size(); i < brix_count; i++) {
            entryAdapter.create(null);
        }

        /* Update the display messages */
        updateMessages();
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

    public void onClick(View view) {
        /* Handle a click */
        switch (view.getId()) {
            case R.id.BrixPlus:
                /* Add the entries */
                entryAdapter.add();
                persistEntries();
                break;
            case R.id.ResetButton:
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
                                updateEntries();
                            }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            /* Do nothing; this is required so that there *is* a cancel button  */
                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
                break;
        }
    }

    public void scrollTo(final View view) {
        /* Scroll to the given view */
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.MainView).scrollTo(0, view.getBottom());
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        /* Handle the spinner being selected.
         * We first confirm that the spinnerValue has actually changed; if it has,
         * save the value and refresh the display
         */
        if (spinnerValue != position) {
            /* Note the value */
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
            for (int i = 0; i < entryAdapter.size(); i ++) {
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
        if (!updateMessages()) {
            toast.show();
        }
    }

    public boolean updateMessages() {
        /* Update the displayed messages */
        log.clear();
        boolean error = updateResults();
        if (!error) {
            /* Updating the results worked; print the CHO reading */
            log.log(Log.MESSAGE, getResources().getString(R.string.BrixCHO, cho()));
            comment();
        }
        return error;
    }

    public boolean updateResults() {
        /* Find the Brix% readings, sanitizing/updating as we go */

        /* Gather the initial dataset */
        results = new ArrayList<>();
        boolean error = false;
        for (int i = 0; i < entryAdapter.size(); i ++) {
            /* Check that item */
            String value = entryAdapter.getItem(i);
            if (value.length() != 0) {
                try {
                    /* Add the value (if it converts and is valid) */
                    Float brix = Float.parseFloat(value);
                    if (brix < 0 || brix > 32) {
                        /* Out of range! */
                        error = true;
                        log.log(Log.ERROR, getResources().getString(R.string.BrixErrorOutOfRange,
                                brix, 0, 32));
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
                    log.log(Log.ERROR, getResources().getString(R.string.BrixErrorNotANumber, value));
                    entryAdapter.markInvalid(i);
                }
            }
        }

        /* Check if there were no values.
         * If so, print a different message and return, to avoid "frightening" first time users
         * with a bold red error...
         * TODO: Is this really valuable? Perhaps just change the "Insufficient readings" text to
         *       be a "MESSAGE"?
         */
        if (!error && (results.size() == 0)) {
            log.log(Log.MESSAGE, getResources().getString(R.string.Introduction));
            return true;
        }

        /* Check the value count */
        int min_entries = getResources().getInteger(R.integer.MIN_BRIX_READINGS);
        if (min_entries > results.size()) {
            /* Bail out, the count is too small */
            log.log(Log.ERROR, getResources().getString(R.string.BrixErrorInsufficientValues,
                    results.size(), min_entries));
            error = true;
        }

        /* If we have not yet encountered an error, check that the values are sane, and sanitize
         * the results. This prevents an error showing for possibly valid results while the values
         * are still being entered.
         */
        if (!error) {
            ArrayList<Float> sanitized = new ArrayList<>();
            float stddev = stddevResult();
            float mean = meanResult();
            for (int i = 0; i < results.size(); i++) {
                if (results.get(i) < mean - (stddev * OUTLIER_RANGE) ||
                        results.get(i) > mean + (stddev * OUTLIER_RANGE)) {
                    log.log(Log.ERROR, getResources().getString(R.string.BrixErrorTooVariable, results.get(i)));
                    error = true;
                    /* TODO: This is buggy (will not work if there is an empty entry) */
                    entryAdapter.markInvalid(i);
                } else {
                    sanitized.add(results.get(i));
                }
            }
            /* Save the sanitized results */
            results = sanitized;
        }

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
                    comment = getResources().getString(R.string.comment_d1_0);
                } else if (cho >= 150 && cho < 250) {
                    comment = getResources().getString(R.string.comment_d1_150);
                } else if (cho >= 250 && cho < 350) {
                    comment = getResources().getString(R.string.comment_d1_250);
                } else if (cho >= 350 && cho < 450) {
                    comment = getResources().getString(R.string.comment_d1_350);
                } else if (cho >= 450) {
                    comment = getResources().getString(R.string.comment_d1_450);
                } else {
                    comment = getResources().getString(R.string.BrixErrorOutOfRangeCHO);
                    error = true;
                }
            } else if (spinnerValue == 1) {
                if (cho >= 0 && cho < 150) {
                    comment = getResources().getString(R.string.comment_cu_0);
                } else if (cho >= 150 && cho < 250) {
                    comment = getResources().getString(R.string.comment_cu_150);
                } else if (cho >= 250 && cho < 350) {
                    comment = getResources().getString(R.string.comment_cu_250);
                } else if (cho >= 350) {
                    comment = getResources().getString(R.string.comment_cu_350);
                } else {
                    comment = getResources().getString(R.string.BrixErrorOutOfRangeCHO);
                    error = true;
                }
            } else if (spinnerValue == 2) {
                if (cho >= 0 && cho < 200) {
                    comment = getResources().getString(R.string.comment_fe_0);
                } else if (cho >= 200 && cho < 300) {
                    comment = getResources().getString(R.string.comment_fe_200);
                } else if (cho >= 300 && cho < 400) {
                    comment = getResources().getString(R.string.comment_fe_300);
                } else if (cho >= 400) {
                    comment = getResources().getString(R.string.comment_fe_400);
                } else {
                    comment = getResources().getString(R.string.BrixErrorOutOfRangeCHO);
                    error = true;
                }
            } else if (spinnerValue == 3) {
                if (cho >= 0 && cho < 300) {
                    comment = getResources().getString(R.string.comment_pfg1_0);
                } else if (cho >= 300 && cho < 400) {
                    comment = getResources().getString(R.string.comment_pfg1_300);
                } else if (cho >= 400 && cho < 500) {
                    comment = getResources().getString(R.string.comment_pfg1_400);
                } else if (cho >= 500) {
                    comment = getResources().getString(R.string.comment_pfg1_500);
                } else {
                    comment = getResources().getString(R.string.BrixErrorOutOfRangeCHO);
                    error = true;
                }
            } else if (spinnerValue == 4) {
                if (cho >= 0 && cho < 300) {
                    comment = getResources().getString(R.string.comment_pfg2_0);
                } else if (cho >= 300 && cho < 400) {
                    comment = getResources().getString(R.string.comment_pfg2_300);
                } else if (cho >= 400 && cho < 500) {
                    comment = getResources().getString(R.string.comment_pfg2_400);
                } else if (cho >= 500) {
                    comment = getResources().getString(R.string.comment_pfg2_500);
                } else {
                    comment = getResources().getString(R.string.BrixErrorOutOfRangeCHO);
                    error = true;
                }
            } else if (spinnerValue == 5) {
                if (cho >= 0 && cho < 150) {
                    comment = getResources().getString(R.string.comment_d2_0);
                } else if (cho >= 150 && cho < 250) {
                    comment = getResources().getString(R.string.comment_d2_150);
                } else if (cho >= 250 && cho < 350) {
                    comment = getResources().getString(R.string.comment_d2_250);
                } else if (cho >= 350 && cho < 450) {
                    comment = getResources().getString(R.string.comment_d2_350);
                } else if (cho >= 450) {
                    comment = getResources().getString(R.string.comment_d2_450);
                } else {
                    comment = getResources().getString(R.string.BrixErrorOutOfRangeCHO);
                    error = true;
                }
            } else {
                /* TODO: Translate this into an actual string */
                comment = getResources().getString(R.string.BrixErrorUnknownStage, spinnerValue);
                error = true;
            }
        } else {
            /* TODO: Translate this into an actual string */
            comment = getResources().getString(R.string.BrixErrorUnknownCategory, age_category);
            error = true;
        }

        /* TODO: This is more of a workaround... */
        if (error) {
            log.log(Log.ERROR, comment);
        } else {
            log.log(Log.MESSAGE, comment);
        }
    }
}
