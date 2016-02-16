/* Extension to the LinearLayout class implementing Logger */

package com.plantandfood.aspirelite;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageList extends LinearLayout implements Logger {

    /* Initialisers */
    public MessageList(Context context) {
        super(context);
    }
    public MessageList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MessageList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void clear() {
        /* Clear all existing messages */
        removeAllViews();
    }

    public void message(String message) {
        /* Log a message */
        TextView text = new TextView(getContext());
        text.setText(message);
        text.setTextColor(Color.parseColor("black"));
        addView(text);
    }

    public void error(String error) {
        /* Log an error message */
        TextView text = new TextView(getContext());
        text.setText(error);
        text.setTextColor(Color.parseColor("red"));
        text.setTypeface(Typeface.DEFAULT_BOLD);
        addView(text);
    }
}
