/* The ExpandedGrid class is a GridView that expands to fill it's contents.
 * I found a hacky workaround on the net somewhere; eventually, this should be replaced.
 */

package com.plantandfood.aspirelite;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

public class ExpandedGrid extends GridView {
    /* Overridden expanded grid - a GridView that expands to fill it's contents */

    public ExpandedGrid(Context context) {
        super(context);
        stopScroll();
    }
    public ExpandedGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        stopScroll();
    }
    public ExpandedGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        stopScroll();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

        getLayoutParams().height = getMeasuredHeight();
    }

    public void stopScroll() {
        /* Disable scroll... */
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
    }
}
