package com.frankhon.fantasymusic;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by Frank_Hon on 1/6/2020.
 * E-mail: v-shhong@microsoft.com
 */
public class PanelLayout extends ViewGroup {

    private int mPanelHeight = Util.dp2px(68);
    private int mShadowHeight = Util.dp2px(5);

    public PanelLayout(Context context) {
        this(context, null);
    }

    public PanelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    protected void onDraw(Canvas canvas) {

    }
}
