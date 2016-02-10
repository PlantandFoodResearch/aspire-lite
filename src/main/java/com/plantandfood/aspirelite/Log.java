/* Logging framework!
 * This handles logging for the app, which includes the in-app messages.
 */

package com.plantandfood.aspirelite;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class Log {
    /* Class for logging events/information */
    ArrayList<Event> events;
    LinearLayout area;
    Context context;

    public static final int DEBUG = 0;
    public static final int MESSAGE = 1;
    public static final int WARN = 2;
    public static final int ERROR = 3;

    public Log(Context context, LinearLayout area) {
        /* Create a new logger */
        events = new ArrayList<>();
        this.area = area;
        this.context = context;
    }

    public void log(int level, CharSequence message) {
        /* Log a message */
        events.add(new Event(level, message));
        // Use Log.e to avoid the debug logs being lost in the maze of other log messages...
        android.util.Log.e("Aspire Lite", message.toString());
        if (level == DEBUG) {
            /* Do nothing */
            return;
        }
        TextView view = new TextView(context);
        view.setText(message);
        if (level == WARN) {
            view.setTextColor(Color.parseColor("purple"));
            view.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (level == ERROR) {
            view.setTextColor(Color.parseColor("red"));
            view.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            view.setTextColor(Color.parseColor("black"));
        }
        area.addView(view);
    }

    public void clear() {
        events.clear();
        area.removeAllViews();
    }
}
