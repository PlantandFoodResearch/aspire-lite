package com.plantandfood.aspirelite;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;

public class EntryGrid extends ElementGrid {

    /* The local EditTexts... */
    ArrayList<EditText> values;
    /* The local text changed listener */
    TextChangedListener textChangedListener;
    /* The scroll to listener */
    ScrollToListener scrollToListener;

    /* Initialisers */
    public EntryGrid(Context context) {
        super(context);
        onCreate();
    }
    public EntryGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        onCreate();
    }
    public EntryGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onCreate();
    }

    /* Bulk "on init" initialiser */
    private void onCreate() {
        /* Initialise self
        * Init the default variables, and create an "add" button.
        */
        values = new ArrayList<>();

        /* Add the button */
        /* Inflate the button */
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageButton plus = (ImageButton) inflater.inflate(R.layout.plus_button, this, false);
        /* Set some attributes */
        plus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                EditText entry = add("");
                entry.requestFocus();
            }
        });
        plus.setContentDescription(context.getResources().getString(R.string.Plus));
        /* Add it to this */
        this.addView(plus);
    }

    /* Listeners */
    public void setTextChangedListener(TextChangedListener listener) {
        /* Set the local listener for an entry changing */
        this.textChangedListener = listener;
    }

    public void setScrollToListener(ScrollToListener listener) {
        /* Set the local listener for an entry changing */
        this.scrollToListener = listener;
    }

    public void reset(int count) {
        /* Reset the fields */

        /* Remove the excess fields */
        while (values.size() > count) {
            EditText edit = values.remove(values.size() - 1);
            this.removeView(edit);
        }

        /* Clear the remaining */
        for (int i = 0; i < values.size(); i ++) {
            values.get(i).setText("");
        }
    }

    public int size() {
        /* Return the size - the number of entries */
        return values.size();
    }

    public EntryItem get(int index) {
        /* Return an item representing the item at the given index */
        return new EntryItem(values.get(index));
    }

    public EditText add(String string) {
        /* Add another item */

        /* Inflate the XML for the entry widget */
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        EditText entry = (EditText) inflater.inflate(R.layout.brix_entry,
                this, false);

        /* Set the string */
        if (string != null) {
            entry.setText(string);
        }

        /* Add the watcher for this item
        * This just calls MainActivity.refresh; within my limited knowledge of Java, this seems to
        * be the best that I can do.
        */
        entry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (textChangedListener != null) {
                    textChangedListener.textChangedCallback();
                }
            }
        });

        /* Add a handler so that the screens scrolls to this element once focused */
        entry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(final View view, boolean hasFocus) {
                if (hasFocus) {
                    if (scrollToListener != null) {
                        scrollToListener.scrollTo(view);
                    }
                }
            }
        });

        /* Add the value to the list */
        values.add(entry);
        this.addView(entry, this.getChildCount() - 1);

        /* Return the result */
        return entry;
    }
}
