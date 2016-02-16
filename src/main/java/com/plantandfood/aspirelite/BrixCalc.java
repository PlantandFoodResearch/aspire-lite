/* BrixCalc contains the input sanitizing and calculation code. */

package com.plantandfood.aspirelite;

import android.content.res.Resources;
import android.util.TypedValue;

import java.util.ArrayList;

class BrixCalc {
    
    /* Global resource handle */
    private Resources res;
    /* Saved readings */
    private ArrayList<EntryItem> readings;
    
    public BrixCalc(Resources res, ArrayList<EntryItem> readings) {
        /* Save the given values */
        
        this.res = res;
        this.readings = readings;
    }

    private float mean(ArrayList<Float> results) {
        /* Calculate the mean of the given results */
        float total = 0;
        for (int i = 0; i < results.size(); i++) {
            total += results.get(i);
        }
        return total / results.size();
    }

    private float stddev(float mean, ArrayList<Float> results) {
        /* Calculate the standard deviation of the given results */
        float sum = 0;
        for (int i = 0; i < results.size(); i++) {
            sum += Math.pow(mean - results.get(i), 2f);
        }
        return (float) Math.sqrt(sum / results.size());
    }
    
    private float sanitize(Logger logger, 
                                      ArrayList<EntryItem> readings) {
        /* Sanitize the given Brix readings */
        // TODO: Split this up into smaller functions.
        
        /* Find the constants */
        int brixMinimum = res.getInteger(R.integer.BRIX_MINIMUM);
        int brixMaximum = res.getInteger(R.integer.BRIX_MAXIMUM);
        
        /* Discard empty entries */
        ArrayList<EntryItem> nonEmpty = new ArrayList<>();
        for (int i = 0; i < readings.size(); i ++) {
            if (readings.get(i).toString().length() != 0) {
                nonEmpty.add(readings.get(i));
            }
        }

        /* Find invalid entries */
        ArrayList<EntryItem> validItems = new ArrayList<>();
        boolean error = false;
        for (int i = 0; i < nonEmpty.size(); i++) {
            /* Check that item */
            EntryItem item = nonEmpty.get(i);
            try {
                /* Add the value (if it converts and is valid) */
                Float brix = item.getBrix();
                if (brix < brixMinimum || brix > brixMaximum) {
                    /* Out of range! */
                    error = true;
                    logger.error(res.getString(R.string.BrixErrorOutOfRange,
                            item.toString(), brixMinimum, brixMaximum));
                    item.set_valid(false);
                } else {
                    /* A valid result!
                    * Clear any formatting and add the result.
                    */
                    validItems.add(item);
                    item.set_valid(true);
                }
            } catch (NumberFormatException e) {
                /* Not a number */
                error = true;
                logger.error(res.getString(R.string.BrixErrorNotANumber,
                        item.toString()));
                item.set_valid(false);
            }
        }

        /* Check if there were no values.
         * If so, print a different message and return, to avoid "frightening" first time users
         * with a bold red error...
         * TODO: Is this really valuable? Perhaps just change the "Insufficient readings" text?
         */
        if (!error && (validItems.size() == 0)) {
            logger.message(res.getString(R.string.Introduction));
            return -1f;
        }

        /* Check the value count */
        int min_entries = res.getInteger(R.integer.MIN_BRIX_READINGS);
        if (min_entries > validItems.size()) {
            /* Bail out, the count is too small */
            logger.message(res.getString(R.string.BrixErrorInsufficientValues,
                    validItems.size(), min_entries));
            return -1f;
        }
        
        /* Bail if we have encountered an error */
        if (error) {
            return -1f;
        }

        /* Now check that the values lie in a sane range */
        /* First, calculate the standard deviation and the mean */
        ArrayList <Float> results = new ArrayList<>();
        for (int i = 0; i < validItems.size(); i ++) {
            results.add(validItems.get(i).getBrix());
        }
        float mean = mean(results);
        float stddev = stddev(mean, results);
        /* Now check for outliers */
        int outlier_range = res.getInteger(R.integer.OUTLIER_RANGE);
        error = false;
        for (int i = 0; i < results.size(); i ++) {
            float brix = results.get(i);
            if (brix < mean - (stddev * outlier_range) ||
                    brix > mean + (stddev * outlier_range)) {
                logger.error(res.getString(R.string.BrixErrorTooVariable,
                        validItems.get(i).toString()));
                error = true;
                validItems.get(i).set_valid(false);
            }
        }
        if (error) {
            return -1f;
        }
        return mean;
    }
    
    private float cho(float mean) {
        /* Get the current estimated cho level */
        
        /* Load the CHO slope and intercept values */
        TypedValue value = new TypedValue();
        res.getValue(R.string.CHO_INTERCEPT, value, true);
        float cho_intercept = value.getFloat();
        res.getValue(R.string.CHO_SLOPE, value, true);
        float cho_slope = value.getFloat();

        /* Return the result */
        return cho_intercept + (cho_slope * mean);
    }

    private boolean comment(Logger logger, float cho, PlantStageSpinner spinner) {
        /* Log a comment on the currently estimated CHO reading and plant stage */

        String comment;
        boolean error = false;
        int spinnerValue = spinner.getSelectedItemPosition();
        
        // TODO: Load the data from a database?
        // TODO: Clean this up...

        if (spinnerValue == 0) {
            if (cho >= 0 && cho < 150) {
                comment = res.getString(R.string.comment_d1_0);
            } else if (cho >= 150 && cho < 250) {
                comment = res.getString(R.string.comment_d1_150);
            } else if (cho >= 250 && cho < 350) {
                comment = res.getString(R.string.comment_d1_250);
            } else if (cho >= 350 && cho < 450) {
                comment = res.getString(R.string.comment_d1_350);
            } else if (cho >= 450) {
                comment = res.getString(R.string.comment_d1_450);
            } else {
                comment = res.getString(R.string.BrixErrorOutOfRangeCHO);
                error = true;
            }
        } else if (spinnerValue == 1) {
            if (cho >= 0 && cho < 150) {
                comment = res.getString(R.string.comment_cu_0);
            } else if (cho >= 150 && cho < 250) {
                comment = res.getString(R.string.comment_cu_150);
            } else if (cho >= 250 && cho < 350) {
                comment = res.getString(R.string.comment_cu_250);
            } else if (cho >= 350) {
                comment = res.getString(R.string.comment_cu_350);
            } else {
                comment = res.getString(R.string.BrixErrorOutOfRangeCHO);
                error = true;
            }
        } else if (spinnerValue == 2) {
            if (cho >= 0 && cho < 200) {
                comment = res.getString(R.string.comment_fe_0);
            } else if (cho >= 200 && cho < 300) {
                comment = res.getString(R.string.comment_fe_200);
            } else if (cho >= 300 && cho < 400) {
                comment = res.getString(R.string.comment_fe_300);
            } else if (cho >= 400) {
                comment = res.getString(R.string.comment_fe_400);
            } else {
                comment = res.getString(R.string.BrixErrorOutOfRangeCHO);
                error = true;
            }
        } else if (spinnerValue == 3) {
            if (cho >= 0 && cho < 300) {
                comment = res.getString(R.string.comment_pfg1_0);
            } else if (cho >= 300 && cho < 400) {
                comment = res.getString(R.string.comment_pfg1_300);
            } else if (cho >= 400 && cho < 500) {
                comment = res.getString(R.string.comment_pfg1_400);
            } else if (cho >= 500) {
                comment = res.getString(R.string.comment_pfg1_500);
            } else {
                comment = res.getString(R.string.BrixErrorOutOfRangeCHO);
                error = true;
            }
        } else if (spinnerValue == 4) {
            if (cho >= 0 && cho < 300) {
                comment = res.getString(R.string.comment_pfg2_0);
            } else if (cho >= 300 && cho < 400) {
                comment = res.getString(R.string.comment_pfg2_300);
            } else if (cho >= 400 && cho < 500) {
                comment = res.getString(R.string.comment_pfg2_400);
            } else if (cho >= 500) {
                comment = res.getString(R.string.comment_pfg2_500);
            } else {
                comment = res.getString(R.string.BrixErrorOutOfRangeCHO);
                error = true;
            }
        } else if (spinnerValue == 5) {
            if (cho >= 0 && cho < 150) {
                comment = res.getString(R.string.comment_d2_0);
            } else if (cho >= 150 && cho < 250) {
                comment = res.getString(R.string.comment_d2_150);
            } else if (cho >= 250 && cho < 350) {
                comment = res.getString(R.string.comment_d2_250);
            } else if (cho >= 350 && cho < 450) {
                comment = res.getString(R.string.comment_d2_350);
            } else if (cho >= 450) {
                comment = res.getString(R.string.comment_d2_450);
            } else {
                comment = res.getString(R.string.BrixErrorOutOfRangeCHO);
                error = true;
            }
        } else {
            comment = res.getString(R.string.BrixErrorUnknownStage,
                    spinner.getSelectedItem().toString());
            error = true;
        }

        if (error) {
            /* Bail out! */
            logger.error(comment);
            return false;
        }
        
        /* Otherwise, print the CHO message, the main message, and finish */
        logger.message(res.getString(R.string.BrixCHO, cho));
        logger.message(comment);
        return true;
    }
    
    public boolean calculate(Logger logger, PlantStageSpinner spinner) {
        /* Calculate and display the results */

        /* Clear the logger */
        logger.clear();

        /* Sanitize the results, and bail on error */
        float mean = sanitize(logger, readings);
        if (mean == -1) {
            return false;
        }
        
        /* Print the comment */
        return comment(logger, cho(mean), spinner);
    }
}
