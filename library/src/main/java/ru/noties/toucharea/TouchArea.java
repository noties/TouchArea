package ru.noties.toucharea;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;

public class TouchArea {

    private TouchArea() {}

    /**
     *
     * Values `insetX` and `insetY` can be negative (will expand touch area)
     * or positive (will decrease touch area).
     *
     * This method returns {@link android.view.View.OnTouchListener} that must be applied to the
     * `parent` view. This library doesn't do it implicitly in case there is some other
     * logic with {@link android.view.View.OnTouchListener}. Returning listener and not applying it
     * gives ability, for example, to combine multiple listeners
     *
     * @param parent {@link View} that should be considered parent. It must contain `view` (at any level)
     * @param view  {@link View} to change the touch area
     * @param insetX value to be applied to the expanded area {@link android.graphics.Rect#inset(int, int)}
     * @param insetY value to be applied to the expanded area {@link android.graphics.Rect#inset(int, int)}
     * @return {@link android.view.View.OnTouchListener} to be set for the `parent` view
     */
    public static View.OnTouchListener touchListener(View parent, View view, int insetX, int insetY) {
        return new TouchAreaOnTouchListener(parent, view, insetX, insetY);
    }

    /**
     *
     * All parameters must be NON-NULL
     *
     * @param parent
     * @param view
     * @param rect
     * @return
     */
    public static boolean getViewBoundsInsideParent(View parent, View view, Rect rect) {
        final boolean out;
        if (parent.getWidth() == 0
                || parent.getHeight() == 0
                || view.getWidth() == 0
                || view.getHeight() == 0) {
            out = false;
        } else {
            final int left = getRelativeLeft(parent, view);
            final int top = getRelativeTop(parent, view);
            rect.set(left, top, left + view.getWidth(), top + view.getHeight());
            out = true;
        }
        return out;
    }

    private static class TouchAreaOnTouchListener implements View.OnTouchListener {

        // okay, what to do if views or positions has changed?
        // should we recalculate the bounds?

        private final View mParent;
        private final View mView;
        private final int mInsetX;
        private final int mInsetY;

        private Rect mRect;
        private TouchDelegate mTouchDelegate;

        TouchAreaOnTouchListener(View parent, View view, int insetX, int insetY) {
            mParent = parent;
            mView = view;
            mInsetX = insetX;
            mInsetY = insetY;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            final boolean handled;

            if (mTouchDelegate == null) {
                if (mRect == null) {
                    mRect = new Rect();
                }
                mTouchDelegate = new TouchDelegate(mRect, mView);
            }

            if (!getViewBoundsInsideParent(mParent, mView, mRect)) {
                handled = false;
            } else {
                mRect.inset(mInsetX, mInsetY);
                handled = mTouchDelegate.onTouchEvent(event);
            }

            return handled;
        }
    }


    private static int getRelativeLeft(View parent, View view) {
        final int out;
        if (parent == null) {
            out = 0;
        } else if (parent == view.getParent()) {
            out = view.getLeft();
        } else {
            out = view.getLeft() + getRelativeLeft(parent, (View) view.getParent());
        }
        return out;
    }

    private static int getRelativeTop(View parent, View view) {
        final int out;
        if (parent == null) {
            out = 0;
        } else if (parent == view.getParent()) {
            out = view.getTop();
        } else {
            out = view.getTop() + getRelativeTop(parent, (View) view.getParent());
        }
        return out;
    }
}
