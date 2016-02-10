package com.plantandfood.aspirelite;

import android.graphics.Color;
import android.widget.EditText;

public class EntryItem {

    /* EditText for this entry */
    EditText edit;

    public EntryItem(EditText edit) {
        this.edit = edit;
    }

    public String toString() {
        return edit.getText().toString();
    }

    public Float getBrix() throws NumberFormatException {
        /* Return the current value, as a float */
        return Float.parseFloat(this.toString());
    }

    public void set_valid(boolean is_valid) {
        /* Sets the validity of the current value */
        if (is_valid) {
            edit.setTextColor(Color.parseColor("black"));
        } else {
            edit.setTextColor(Color.parseColor("red"));
        }
    }
}
