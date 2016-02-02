package com.plantandfood.aspirelite;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class Log {
    /* Class for logging events/information */
    ArrayList<Event> events;
    LinearLayout area;
    Context context;

    public static final int DEBUG = 0;
    public static final int MESSAGE = 0;
    public static final int WARN = 0;
    public static final int ERROR = 0;

    public Log(Context context, LinearLayout area) {
        /* Create a new logger */
        events = new ArrayList<>();
        this.area = area;
        this.context = context;
    }

    public void log(int level, CharSequence message) {
        /* Log a message */
        events.add(new Event(level, message));
        android.util.Log.e("t", message.toString());
        if (level == DEBUG) {
            /* Do nothing */
            return;
        }
        TextView view = new TextView(context);
        view.setText(message);
        // TODO: Add other formatting.
        area.addView(view);
    }

    public void clear() {
        events.clear();
        area.removeAllViews();
    }
}
