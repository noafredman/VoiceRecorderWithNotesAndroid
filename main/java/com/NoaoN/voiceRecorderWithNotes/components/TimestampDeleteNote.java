package com.NoaoN.voiceRecorderWithNotes.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.NoaoN.voiceRecorderWithNotes.R;

/**
 * A container component class - contains TimeStamp and DeleteButton.
 */
public class TimestampDeleteNote extends LinearLayout {
    Context context;
    private TimeStamp timeStamp;
    private DeleteButton delBtn;

    public TimestampDeleteNote(Context context) {
        super(context);
        init(context);
    }

    public TimestampDeleteNote(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Initialize components.
     *
     * @param context - context
     */
    private void init(Context context) {
        this.context = context;
        inflate(context, R.layout.timestamp_delete_note_layout, this);
        this.timeStamp = findViewById(R.id.timeStampTimeTVInList);
        this.delBtn = findViewById(R.id.deleteNoteBTN);
    }

    //getters and setters.
    public Button getDelBtn() {
        return this.delBtn;
    }

    public void setTimeStampText(String time) {
        this.timeStamp.setText(time);
    }

    public String getTimeStampText() {
        return this.timeStamp.getText().toString();
    }

    public TextView getTimeTV() {
        return this.timeStamp;
    }
}
