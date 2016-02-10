package com.plantandfood.aspirelite;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ElementGrid extends ViewGroup {

    /* Column and row sizes */
    int columnWidth = 0;
    int rowHeight = 0;

    /* Saved context */
    Context context;

    public ElementGrid(Context context) {
        super(context);
        this.context = context;
    }

    public ElementGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initAttrs(attrs);
    }

    public ElementGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initAttrs(attrs);
    }

    private void initAttrs(AttributeSet attrs) {
        /* Initialise the attributes */

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.ElementGrid, 0, 0);
        try {
            columnWidth = array.getInteger(R.styleable.ElementGrid_columnWidth, 100);
        } finally {
            array.recycle();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        /* Recalculate the positions of all of the children */

        /* Find the buffering on either side */
        int buffer = ((right - left) % columnWidth) / 2;

        int cur_left = buffer;
        int cur_top = 0;
        for (int i = 0; i < this.getChildCount(); i ++) {
            View view = getChildAt(i);
            view.layout(cur_left,
                    cur_top,
                    cur_left + columnWidth,
                    cur_top + rowHeight);
            cur_left += columnWidth;
            if (left + cur_left + columnWidth > right) {
                cur_left = buffer;
                cur_top += rowHeight;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /* Find the width and height of this element */

        /* Find the required row height */
        rowHeight = 0;
        for (int i = 0; i < this.getChildCount(); i ++) {
            View view = getChildAt(i);
            view.measure(MeasureSpec.makeMeasureSpec(columnWidth, MeasureSpec.AT_MOST),
                    heightMeasureSpec);
            if (rowHeight < view.getMeasuredHeight()) {
                rowHeight = view.getMeasuredHeight();
            }
        }

        /* Use the current width */
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        /* Set the height to be the required height */
        int childrenPerRow = widthSize / columnWidth;
        int rowCount = this.getChildCount() / childrenPerRow;
        if (this.getChildCount() % childrenPerRow > 0) {
            rowCount += 1;
        }

        /* Save the dimensions */
        setMeasuredDimension(widthSize, rowCount * rowHeight);
    }
}
