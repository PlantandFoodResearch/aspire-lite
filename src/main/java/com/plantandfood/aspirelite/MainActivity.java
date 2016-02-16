/* Main "activity"
 * Basically the landing stage for anyone opening the app...
 * This includes most of the UI and calculation code.
 */

package com.plantandfood.aspirelite;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements SomethingChangedListener, ScrollToListener {

    /* Class-local Log instance */
    private Log log;
    /* Class-local Toast for recalculated events */
    private Toast toast;
    /* Current (valid) results */
    private ArrayList<Float> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Restore the UI */
        super.onCreate(savedInstanceState);

        /* Set the current view */
        setContentView(R.layout.activity_main);

        /* Save the EntryGrid reference */
        EntryGrid grid = (EntryGrid) findViewById(R.id.BrixEntryLayout);
        /* Set the callback listeners... */
        grid.somethingChangedListener = this;
        grid.scrollToListener = this;

        /* Initialise the logger */
        this.log = new Log(this, (LinearLayout)findViewById(R.id.MessageList));

        /* Initialise the spinner */
        ((PlantStageSpinner) findViewById(R.id.PlantStageSpinner)).listener =
                this;

        /* Initialise the toast */
        toast = Toast.makeText(this,
                getResources().getString(R.string.CommentUpdated),
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);

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

    public void onReset(View view) {
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
                        ((EntryGrid) findViewById(R.id.BrixEntryLayout)).reset();
                    }
            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    /* Do nothing; this is required so that there *is* a cancel button  */
                }
            }).setIcon(android.R.drawable.ic_dialog_alert).show();
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

    public void refresh() {
        /* Recalculate... something has changed */
        log.clear();
        log.log(Log.DEBUG, "Refreshing...");
        if (!updateMessages()) {
            toast.show();
        }
    }

    private boolean updateMessages() {
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

    private boolean updateResults() {
        /* Find the Brix% readings, sanitizing/updating as we go */

        /* Gather the initial dataset */
        ArrayList<EntryItem> validItems = new ArrayList<>();
        results = new ArrayList<>();
        boolean error = false;
        EntryGrid grid = (EntryGrid) findViewById(R.id.BrixEntryLayout);
        for (int i = 0; i < grid.size(); i ++) {
            /* Check that item */
            EntryItem item = grid.get(i);
            if (item.toString().length() != 0) {
                try {
                    /* Add the value (if it converts and is valid) */
                    Float brix = item.getBrix();
                    if (brix < 0 || brix > 32) {
                        /* Out of range! */
                        error = true;
                        log.log(Log.ERROR,
                                getResources().getString(R.string.BrixErrorOutOfRange,
                                item.toString(), 0, 32));
                        item.set_valid(false);
                    } else {
                        /* A valid result!
                        * Clear any formatting and add the result.
                        */
                        validItems.add(item);
                        results.add(brix);
                        item.set_valid(true);
                    }
                } catch (NumberFormatException e) {
                    /* Not a number */
                    error = true;
                    log.log(Log.ERROR,
                            getResources().getString(R.string.BrixErrorNotANumber,
                                    item.toString()));
                    item.set_valid(false);
                }
            }
        }

        /* Check if there were no values.
         * If so, print a different message and return, to avoid "frightening" first time users
         * with a bold red error...
         * TODO: Is this really valuable? Perhaps just change the "Insufficient readings" text?
         */
        if (!error && (validItems.size() == 0)) {
            log.log(Log.MESSAGE, getResources().getString(R.string.Introduction));
            return true;
        }

        /* Check the value count */
        int min_entries = getResources().getInteger(R.integer.MIN_BRIX_READINGS);
        if (min_entries > validItems.size()) {
            /* Bail out, the count is too small */
            log.log(Log.MESSAGE, getResources().getString(R.string.BrixErrorInsufficientValues,
                    validItems.size(), min_entries));
            error = true;
        }

        /* If we have not yet encountered an error, check that the values are sane, and sanitize
         * the results. This prevents an error showing for possibly valid results while the values
         * are still being entered.
         */
        int outlier_range = getResources().getInteger(R.integer.OUTLIER_RANGE);
        if (!error) {
            float stddev = stddevResult();
            float mean = meanResult();
            results = new ArrayList<>();
            for (int i = 0; i < validItems.size(); i++) {
                float brix = validItems.get(i).getBrix();
                if (brix < mean - (stddev * outlier_range) ||
                        brix > mean + (stddev * outlier_range)) {
                    log.log(Log.ERROR,
                            getResources().getString(R.string.BrixErrorTooVariable,
                                    validItems.get(i).toString()));
                    error = true;
                    validItems.get(i).set_valid(false);
                } else {
                    results.add(brix);
                }
            }
        }

        return error;
    }

    private float meanResult() {
        /* Calculate the mean result */
        float total = 0;
        for (int i = 0; i < results.size(); i ++) {
            total += results.get(i);
        }
        return total / results.size();
    }

    private float stddevResult() {
        /* Find the standard deviation of the results */
        float sum = 0;
        float avg = meanResult();
        for (int i = 0; i < results.size(); i ++) {
            sum += Math.pow(avg - results.get(i), 2f);
        }
        return (float)Math.sqrt(sum / results.size());
    }

    private float cho() {
        /* Return the estimated CHO reading */

        /* Load the CHO slope and intercept values */
        Resources res = getResources();
        TypedValue value = new TypedValue();
        res.getValue(R.string.CHO_INTERCEPT, value, true);
        float cho_intercept = value.getFloat();
        res.getValue(R.string.CHO_SLOPE, value, true);
        float cho_slope = value.getFloat();

        /* Return the result */
        return cho_intercept + (cho_slope * meanResult());
    }

    private void comment() {
        /* Print a comment on the currently estimated CHO reading and plant stage */

        String comment;
        boolean error = false;
        float cho = cho();
        int spinnerValue = ((PlantStageSpinner)
                findViewById(R.id.PlantStageSpinner))
                .getSelectedItemPosition();

        // TODO: Load the data from a database?
        // TODO: Clean this up...

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
            comment = getResources().getString(R.string.BrixErrorUnknownStage,
                    ((PlantStageSpinner) findViewById(R.id.PlantStageSpinner))
                            .getSelectedItem().toString());
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
