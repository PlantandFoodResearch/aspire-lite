/* An adaptor for the Brix% reading entry widgets!
 * This basically maintains an ArrayList of EditTexts, which is hooked up to a GridView in the main
 * activity.
 */

package com.plantandfood.aspirelite;

import java.util.ArrayList;

import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.inputmethod.EditorInfo;

public class EntryAdapter extends BaseAdapter {
    private MainActivity context;
    private ArrayList<EditText> entries = new ArrayList<>();
    private ImageButton plusButton;

    public EntryAdapter(MainActivity c) {
        /* Save the context */
        context = c;

        /* Create the plus button */
        plusButton = new ImageButton(context);
        /* Add the listener, and set the id */
        plusButton.setOnClickListener(context);
        plusButton.setId(R.id.BrixPlus);
        /* Define a content description */
        plusButton.setContentDescription(context.getResources().getString(R.string.Plus));
        /* Set the background image */
        plusButton.setImageResource(R.drawable.ic_add_circle_outline_24dp);
        /* Set the dimensions in dp */
        int size = ((Float)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                48, context.getResources().getDisplayMetrics())).intValue();
        plusButton.setMinimumHeight(size);
        plusButton.setMaxHeight(size);
        plusButton.setMinimumWidth(size);
        plusButton.setMaxWidth(size);
    }

    public int getCount() {
        /* Return the number of elements, plus one for the plus button... */
        return entries.size() + 1;
    }

    public int size() {
        /* Return the number of EditTexts */
        return entries.size();
    }

    public String getItem(int position) {
        /* Get the "item" at the corresponding position */
        if (position == entries.size()) {
            return "";
        }
        return entries.get(position).getText().toString();
    }

    public long getItemId(int position) {
        /* I'm not completely clear on what this actually does... */
        if (position == entries.size()) {
            return plusButton.getId();
        }
        return entries.get(position).getId();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        /* Return the view at the given position */
        if (position == entries.size()) {
            return plusButton;
        }
        return entries.get(position);
    }

    public void add(String value) {
        /* Add a new entry to the list */

        /* Create a new entry widget */
        EditText brix = new EditText(context);
        /* Set the type */
        brix.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        /* Set the width of the element */
        brix.setWidth(80); /* TODO: Fix this to use a constant somewhere */
        /* Force the element to be single line */
        brix.setSingleLine(true);
        /* Center the text */
        brix.setGravity(Gravity.CENTER);
        /* Stop using the IME "extracted text" UI */
        brix.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        /* Set the starting value, if one is given */
        if (value != null) {
            brix.setText(value, TextView.BufferType.EDITABLE);
        }

        /* Add the watcher for this item
        * This just calls MainActivity.refresh; within my limited knowledge of Java, this seems to
        * be the best that I can do.
        */
        brix.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                context.updateEntries();
            }
        });

        /* Add a handler so that the screens scrolls to this element once focused */
        brix.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    context.scrollTo(view);
                }
            }
        });

        /* Add the item */
        entries.add(brix);

        this.notifyDataSetChanged();

        /* Request the focus for the newly added element */
        brix.requestFocus();
    }

    public void reset(int count) {
        /* Remove all of the existing entries, and repopulate with count fresh boxes */

        entries.clear();
        for (int i = 0; i < count; i ++) {
            add(null);
        }
    }

    public void markInvalid(int position) {
        /* Mark the item at the given position as invalid */
        ((EditText)this.getView(position, null, null)).setTextColor(Color.parseColor("red"));
    }

    public void markValid(int position) {
        /* Mark the item at the given position as valid */
        ((EditText)this.getView(position, null, null)).setTextColor(Color.parseColor("black"));
    }
}
