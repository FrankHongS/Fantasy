package com.frankhon.fantasymusic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

/**
 * Created by Frank_Hon on 1/6/2020.
 * E-mail: v-shhong@microsoft.com
 */
public class PanelLayout extends ViewGroup {

    private static final String TAG = PanelLayout.class.getSimpleName();

    private int mPanelHeight = Util.dp2px(68);

    private View mMainView;
    private View mSlideView;

    private Rect mClipRect;
    private int mCoveredFadeColor = 0x44000000;
    private Paint mCoveredFadePaint = new Paint();

    private ViewDragHelper mViewDragHelper;
    private int mSlideRange;
    // range [0, 1]
    private float mSlideOffsetPercent = 0;
    private boolean isCollapsed = true;

    private float eventX, eventY;

    public PanelLayout(Context context) {
        this(context, null);
    }

    public PanelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mClipRect = new Rect();
        mViewDragHelper = ViewDragHelper.create(this, 0.5f, new PanelViewDragHelperCallback());

//        setWillNotDraw(false);//需要设置，或者ViewGroup不会调用onDraw()

        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PanelLayout);

            mPanelHeight = ta.getDimensionPixelSize(R.styleable.PanelLayout_panelHeight, mPanelHeight);

            ta.recycle();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //如果子View的大小是match_parent，它的测量模式和父布局一样
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Width must have an exact value");
        } else if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Height must have an exact value");
        }

        if (heightSize <= mPanelHeight) {
            throw new IllegalStateException("Height must be more than panel height");
        }

        int childCount = getChildCount();

        if (childCount != 2) {
            throw new IllegalStateException("panel layout must have exactly 2 children");
        }
        int layoutHeight = heightSize - getPaddingTop() - getPaddingBottom();

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams layoutParams = child.getLayoutParams();

            int childWidthSpec;
            if (layoutParams.width == LayoutParams.WRAP_CONTENT) {
                childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST);
            } else if (layoutParams.width == LayoutParams.MATCH_PARENT) {
                childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
            } else {
                childWidthSpec = MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY);
            }

            int childHeightSpec;
            if (i == 0) {
                if (layoutParams.height == LayoutParams.WRAP_CONTENT) {
                    childHeightSpec = MeasureSpec.makeMeasureSpec(layoutHeight - mPanelHeight, MeasureSpec.AT_MOST);
                } else if (layoutParams.height == LayoutParams.MATCH_PARENT) {
                    childHeightSpec = MeasureSpec.makeMeasureSpec(layoutHeight - mPanelHeight, MeasureSpec.EXACTLY);
                } else {
                    childHeightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
                }
            } else {
                if (layoutParams.height == LayoutParams.WRAP_CONTENT || layoutParams.height == LayoutParams.MATCH_PARENT) {
                    throw new IllegalStateException("slide view must have an exact value");
                } else {
                    childHeightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
                    mSlideRange = layoutParams.height - mPanelHeight;
                }
            }

            child.measure(childWidthSpec, childHeightSpec);
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        mMainView = getChildAt(0);
        mMainView.layout(
                paddingLeft,
                paddingTop,
                paddingLeft + mMainView.getMeasuredWidth(),
                paddingTop + mMainView.getMeasuredHeight()
        );

        mSlideView = getChildAt(1);
        mSlideView.layout(
                paddingLeft,
                paddingTop + mMainView.getMeasuredHeight(),
                paddingLeft + mSlideView.getMeasuredWidth(),
                paddingTop + mMainView.getMeasuredHeight() + mSlideView.getMeasuredHeight()
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {

    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result;
        int save = canvas.save();

        if (mMainView == child) {
            canvas.getClipBounds(mClipRect);
            mClipRect.bottom = mSlideView.getTop();
            canvas.clipRect(mClipRect);
        }

        result = super.drawChild(canvas, child, drawingTime);
        canvas.restoreToCount(save);

        if (mCoveredFadeColor != 0) {
            int baseAlpha = mCoveredFadeColor >>> 24;
            int imag = (int) (baseAlpha * mSlideOffsetPercent);
            int color = imag << 24 | (mCoveredFadeColor & 0xffffff);
            mCoveredFadePaint.setColor(color);
            canvas.drawRect(mClipRect, mCoveredFadePaint);
        }

        return result;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(TAG, "onInterceptTouchEvent: " + ev.getActionMasked());
        //当panel处于滑上的状态时，panel以上的阴影区域，所有事件屏蔽，不能进行交互
        if (ev.getY() <= mSlideView.getTop() && !isCollapsed) {
            return true;
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: " + event.getActionMasked());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                eventX = event.getX();
                eventY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = Math.abs(event.getX() - eventX);
                float deltaY = Math.abs(event.getY() - eventY);
                eventX = event.getX();
                eventY = event.getY();
                if (deltaX > deltaY) {
                    return false;
                }
                break;
            default:
                break;
        }
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    private class PanelViewDragHelperCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return child == mSlideView;
        }

        @Override
        public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        @Override
        public void onViewDragStateChanged(int state) {

        }

        /*
            限制view竖直方向上的拖动位置,
            当view的top处于最小值和最大值之间，则返回top;
            当view的top超过最大值，则为最大值;
            当view的top小于最小值，则为最小值.
         */
        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if (top <= getMeasuredHeight() - mSlideView.getMeasuredHeight()) {
                return getMeasuredHeight() - mSlideView.getMeasuredHeight();
            } else if (top >= getMeasuredHeight() - mPanelHeight) {
                return getMeasuredHeight() - mPanelHeight;
            }
            return top;
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            mSlideOffsetPercent = (getMeasuredHeight() - mPanelHeight - changedView.getTop()) *
                    1.0f / (changedView.getMeasuredHeight() - mPanelHeight);
            invalidate();//为了绘制mMainView上的阴影
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            if (mSlideOffsetPercent > 0.25f) {
                if (yvel <= 0) {
                    mViewDragHelper.settleCapturedViewAt(getLeft(), getMeasuredHeight() - mSlideView.getMeasuredHeight());
                    isCollapsed = false;
                } else {
                    mViewDragHelper.settleCapturedViewAt(getLeft(), getMeasuredHeight() - mPanelHeight);
                    isCollapsed = true;
                }
            } else {
                mViewDragHelper.settleCapturedViewAt(getLeft(), getMeasuredHeight() - mPanelHeight);
                isCollapsed = true;
            }
            invalidate();
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return mSlideRange;
        }
    }
}
