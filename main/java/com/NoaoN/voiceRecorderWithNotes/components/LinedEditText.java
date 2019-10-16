package com.NoaoN.voiceRecorderWithNotes.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 *
 */
public class LinedEditText extends android.support.v7.widget.AppCompatEditText {
    private int position;
    private View convertView;
    private Paint mPaint;
    {
        mPaint = new Paint();
    }

    /**
     * Constructor.
     * @param context - current context.
     */
    public LinedEditText(Context context) {
        super(context);
        initPaint();
    }

    /**
     * Constructor.
     * @param context - current context.
     * @param attrs - attributes.
     */
    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    /**
     * Constructor.
     * @param context - current context.
     * @param attrs - attributes.
     * @param defStyle - style.
     */
    public LinedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaint();
    }

    /**
     * initializes paint style and color.
     */
    private void initPaint() {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0x00000000);
    }

    /**
     * Draws the underlines in notes.
     * @param canvas - canvas to draw on.
     */
    @Override protected void onDraw(Canvas canvas) {
        int right = getRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int height = getHeight();
        int lineHeight = getLineHeight();
        int count = (height-paddingTop-paddingBottom) / lineHeight;
        //draw the underlines
        for (int i = 0; i < count; i++) {
            int baseline = lineHeight * (i+1) + paddingTop;
            canvas.drawLine(0, baseline, right, baseline, mPaint);
        }
        super.onDraw(canvas);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        super.onTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                // For this particular app we want the main work to happen
                // on ACTION_UP rather than ACTION_DOWN. So this is where
                // we will call performClick().
                this.performClick();
                return true;
        }
        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        //want cursor to be at text end when editText clicked.
        this.setSelection(this.getText().length());
        return true;
    }

    /**
     *Set note position.
     * @param pos - note position.
     */
    public void setPosition(int pos){
        this.position = pos;
    }

    /**
     * Get note position.
     * @return note position.
     */
    public int getPosition(){
        return this.position;
    }

    /**
     * Set view.
     * @param v - view.
     */
    public void setView(View v){
        this.convertView = v;
    }

    /**
     * Get view.
     * @return view.
     */
    public View getView(){
        return this.convertView;
    }
}
