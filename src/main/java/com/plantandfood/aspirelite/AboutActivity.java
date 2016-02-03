package com.plantandfood.aspirelite;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends ChildActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        /* Set the link method, and format the help text */
        TextView aboutText = (TextView) findViewById(R.id.AboutText);
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());
        String text = String.format(getResources().getString(R.string.AboutBlurb),
                getResources().getString(R.string.app_name), BuildConfig.VERSION_NAME);
        aboutText.setText(Html.fromHtml(text));
    }
}
