package com.plantandfood.aspirelite;

import java.util.ArrayList;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

public class EntryAdapter extends BaseAdapter {
    private MainActivity context;
    private ArrayList<EditText> entries = new ArrayList<>();

    public EntryAdapter(MainActivity c) {
        context = c;
    }

    public int getCount() {
        /* Return the number of elements */
        return entries.size();
    }

    public String getItem(int position) {
        /* Get the "item" at the corresponding position */
        return entries.get(position).getText().toString();
    }

    public long getItemId(int position) {
        /* I'm not completely clear on what this actually does... */
        return entries.get(position).getId();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        /* Create an EditText */
        return entries.get(position);
    }

    public void add() {
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
                context.refresh();
            }
        });

        /* Add the item */
        entries.add(brix);

        this.notifyDataSetChanged();
    }

    public void rm() {
        /* Remove an existing entry */
        entries.remove(entries.size() - 1);
        this.notifyDataSetChanged();
    }
}
