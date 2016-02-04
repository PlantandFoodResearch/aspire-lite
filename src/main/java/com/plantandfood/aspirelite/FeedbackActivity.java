package com.plantandfood.aspirelite;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class FeedbackActivity extends ChildActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        /* Set the link method */
        TextView aboutText = (TextView) findViewById(R.id.FeedbackText);
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
