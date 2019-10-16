package com.NoaoN.voiceRecorderWithNotes.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.NoaoN.voiceRecorderWithNotes.R;

/**
 * Container View which contains LinedEditText (note) and TimestampDeleteNote (time stamp &
 * delete button).
 */
public class LinkLinedView extends FrameLayout {
    private LinedEditText linedET;
    private TimestampDeleteNote ts;
    private View view;

    /**
     * Constructor
     * @param context - current context.
     */
    public LinkLinedView( Context context) {
        super(context);
        init(context);
    }

    /**
     * Constructor
     * @param context - current context.
     * @param attrs - view's attributes.
     */
    public LinkLinedView( Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /***
     * Initializes class fields.
     * @param context - activity context.
     */
    private void init(Context context){
        inflate(context, R.layout.link_lined_edit_text, this);
        this.linedET = findViewById(R.id.noteHiddenTextEdit);
        this.linedET.setText("");
        ts = findViewById(R.id.tsLayout);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.linedET = findViewById(R.id.noteHiddenTextEdit);
        this.linedET.setText("");
    }

    /**
     * Get LinedEditText field.
     * @return LinedEditText field.
     */
    public LinedEditText getLinedET() {
        return linedET;
    }

    /**
     * Set text of LinedEditText field.
     * @param note - note text.
     */
    public void setLinkLinedNoteText(String note){
        this.linedET.setText(note);
    }

    /**
     * Set the values of LinedEditText field (other than text).
     * @param let - LinedEditText to store its values.
     */
    public void setLinkLinedFields(LinedEditText let){
        this.linedET.setPosition(let.getPosition());
        View v = let.getView();
        this.linedET.setView(v);
    }

    /**
     * Get the LinedEditText field's text.
     * @return the LinedEditText field's text.
     */
    public String getNoteText(){
        if(this.linedET != null && this.linedET.getText() != null) {
            return this.linedET.getText().toString();
        }
        return null;
    }

    /**
     * Set the saved view field.
     * @param view - view to store.
     */
    public void setView(View view){
        this.view = view;
    }

    /**
     * Get the saved view field.
     * @return view field.
     */
    public View getView(){
        return this.view;
    }

    /**
     * Get time stamp field.
     * @return time stamp field.
     */
    public TimestampDeleteNote getTS(){ return this.ts;}

    /**
     * Set time stamp field.
     * @param ts - time stamp.
     */
    public void setTS(TimestampDeleteNote ts){ this.ts = ts;}

    /**
     * Get time stamp text.
     * @return time stamp text.
     */
    public String getTimeStampText(){ return this.ts.getTimeStampText(); }

}
