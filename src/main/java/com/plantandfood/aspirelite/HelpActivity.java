package com.plantandfood.aspirelite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        /* Set the link method */
        TextView aboutText = (TextView) findViewById(R.id.HelpLimitationsText);
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
