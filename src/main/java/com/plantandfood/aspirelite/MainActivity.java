/* Main "activity"
 * Basically the landing stage for anyone opening the app...
 * This mostly consists of the callbacks/init code.
 */

package com.plantandfood.aspirelite;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements SomethingChangedListener, ScrollToListener {

    /* Class-local Toast for recalculated events */
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Restore the UI */
        super.onCreate(savedInstanceState);

        /* Set the current view */
        setContentView(R.layout.activity_main);

        /* Set the callback listeners... */
        EntryGrid grid = (EntryGrid) findViewById(R.id.BrixEntryLayout);
        grid.somethingChangedListener = this;
        grid.scrollToListener = this;

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
        if (updateMessages()) {
            toast.show();
        }
    }

    private boolean updateMessages() {
        /* Update the displayed messages, and return the result */
        return (new BrixCalc(getResources(),
                (EntryGrid) findViewById(R.id.BrixEntryLayout))).calculate(
                    (MessageList) findViewById(R.id.MessageList),
                    (PlantStageSpinner) findViewById(R.id.PlantStageSpinner));
    }
}
