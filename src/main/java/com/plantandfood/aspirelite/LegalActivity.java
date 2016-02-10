/* Activity for the Legal screen */

package com.plantandfood.aspirelite;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class LegalActivity extends ChildActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);

        /* Set the link method */
        TextView aboutText = (TextView) findViewById(R.id.LegalText);
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
