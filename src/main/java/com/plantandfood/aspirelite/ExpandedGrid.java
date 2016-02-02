package com.plantandfood.aspirelite;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class ExpandedGrid extends GridView {

    public ExpandedGrid(Context context) {
        super(context);
    }
    public ExpandedGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ExpandedGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
