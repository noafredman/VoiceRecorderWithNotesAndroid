package com.NoaoN.voiceRecorderWithNotes.components;
//core of this class was taken from stackoverflow website.
/**
 * Created by Noa on 9/19/2016.
 */
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.NoaoN.voiceRecorderWithNotes.R;

public class VisualizerView extends View {
    private static final int LINE_WIDTH = 1; // width of visualizer lines
    private static final int LINE_SCALE = 75; // scales visualizer lines
    private List<Float> amplitudes; // amplitudes for line lengths
    private int width; // width of this View
    private int height; // height of this View
    private Paint linePaint; // specifies line drawing characteristics

    /**
     * constructor
     * @param context - current context.
     * @param attrs - visualizer attributes.
     */
    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs); // call superclass constructor
        linePaint = new Paint(); // create Paint for lines
        int c = ContextCompat.getColor(context, R.color.greyPurpleLight);
        linePaint.setColor(c); // set color to greyPurpleLight
        linePaint.setStrokeWidth(LINE_WIDTH); // set stroke width
    }


    /**
     * called when the dimensions of the View change.
     * @param w - width
     * @param h - height
     * @param oldw - old width
     * @param oldh - old height
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w; // new width of this View
        height = h; // new height of this View
        amplitudes = new ArrayList<>(width / LINE_WIDTH);
    }

    /**
     * clear all amplitudes to prepare for a new visualization.
     */
    public void clear() {
        amplitudes.clear();
    }

    /**
     * add the given amplitude to the amplitudes ArrayList.
     * @param amplitude - given amplitude
     */
    public void addAmplitude(float amplitude) {
        amplitudes.add(amplitude); // add newest to the amplitudes ArrayList

        // if the power lines completely fill the VisualizerView
        if (amplitudes.size() * LINE_WIDTH >= width) {
            amplitudes.remove(0); // remove oldest power value
        }
    }

    /**
     * draw the visualizer with scaled lines representing the amplitudes.
     * @param canvas - canvas to draw on.
     */
    @Override
    public void onDraw(Canvas canvas) {
        int middle = height / 2; // get the middle of the View
        float curX = 0; // start curX at zero

        // for each item in the amplitudes ArrayList
        for (float power : amplitudes) {
            float scaledHeight = power / LINE_SCALE; // scale the power
            curX += LINE_WIDTH; // increase X by LINE_WIDTH

            // draw a line representing this item in the amplitudes ArrayList
            canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle
                    - scaledHeight / 2, linePaint);
        }
    }

}
