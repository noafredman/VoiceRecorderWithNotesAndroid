package com.NoaoN.voiceRecorderWithNotes.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * I've had some issues clicking on text view in the recordings list and notes, so I've
 * created this class to manage the click event.
 */
public class TimeStamp extends android.support.v7.widget.AppCompatTextView {
    public TimeStamp(Context context) {
        super(context);
    }

    public TimeStamp(Context context,  AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeStamp(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                // For this particular app we want the main work to happen
                // on ACTION_UP rather than ACTION_DOWN. So this is where
                // we will call performClick().
                performClick();
                return true;
        }
        return false;
    }


}
