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

        /* Format the text, and set the link method */
        TextView aboutText = (TextView) findViewById(R.id.AboutText);
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());
        aboutText.setText(Html.fromHtml(
                getResources().getString(R.string.AboutBlurb,
                    getResources().getString(R.string.app_name),
                    BuildConfig.VERSION_NAME)));
    }
}
