package com.NoaoN.voiceRecorderWithNotes.activities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;

import com.NoaoN.voiceRecorderWithNotes.helper_classes.DirectorySingleton;
import com.NoaoN.voiceRecorderWithNotes.helper_classes.NotificationVoiceRec;
import com.NoaoN.voiceRecorderWithNotes.helper_classes.SaveNote;
import com.NoaoN.voiceRecorderWithNotes.components.TimestampDeleteNote;
import com.NoaoN.voiceRecorderWithNotes.list_adapter.ExpandableListAdapter;
import com.NoaoN.voiceRecorderWithNotes.list_adapter.ExpandableListAdapterNoDeleteBtn;
import com.NoaoN.voiceRecorderWithNotes.components.LinkLinedView;
import com.NoaoN.voiceRecorderWithNotes.R;
import com.NoaoN.voiceRecorderWithNotes.components.VisualizerView;

public class RecordingActivity extends Activity implements IActivityWithNotification {
    public static final int REPEAT_INTERVAL = 40;
    public static final int PERMISSIONS_MULTIPLE_REQUEST = 123;
    private static final int ACTIVITY_NOTIFICATION_ID = 0;
    private static final int INTERNAL_STORAGE = 0;
    private static final int EXTERNAL_STORAGE = 1;
    //default save to internal storage
    private int CHECKED_STORAGE = INTERNAL_STORAGE;
    private boolean permissionsAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //layout fields
    private ImageView imgStop;
    private ImageView imgPause;
    private ImageView imgRecord;
    private Button btnTimeStamp;
    private ImageView imgSettings;
    private Button btnRecordings;
    private EditText fileNameET;
    private String fileName;
    private Chronometer timeLapsed;
    VisualizerView visualizerView;

    private MediaRecorder recorder = null;
    private boolean isPaused = false;
    private boolean stoppedRecording = true;
    private boolean isRecording = false;
    private Vibrator vibe;
    private Handler handler; // Handler for updating the visualizer
    long timeWhenStopped = 0;
    private boolean noteStamped = false;
    //notification fields
    private NotificationManager notificationManager;
    BroadcastReceiver receiver;
    private Context thisContext;
    private NotificationVoiceRec appNotification;
    //expandable list fields
    private ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<LinkLinedView>> listDataChild;
    List<LinkLinedView> listNote;
    private final int GROUP_POSITION = 0;
    //file recordings fields
    public String directoryName;
    private static File root;
    private File[] filesDir;
    DirectorySingleton ds;
    Intent receiverRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filesDir = this.getExternalFilesDirs(null);//getExternalFilesDir(null);
        ds = DirectorySingleton.getInstance();
        directoryName = getString(R.string.app_name);
        if (getSaveToPreference() != CHECKED_STORAGE) {
            saveTo(CHECKED_STORAGE);
        }
        setContentView(R.layout.activity_recording);
        Intent intent = new Intent(this, RecordingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        thisContext = this;
        appNotification = new NotificationVoiceRec(this);
        notificationManager = appNotification.getNotificationManager();
        initComponents();
        setButtonsOnClickListener();
        //SOFT_INPUT_ADJUST_PAN - will move note up and only that, so user can see what he's typing.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //if sdk >= 23 need permissions
        //request from user multiple permissions which are listed in "permissions" array.
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_MULTIPLE_REQUEST);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Initialize activity's components.
     */
    private void initComponents() {
        fileNameET = findViewById(R.id.fileNameEditText);
        visualizerView = findViewById(R.id.visualizer);
        timeLapsed = findViewById(R.id.chronometer);
        btnRecordings = findViewById(R.id.buttonRecordings);
        btnTimeStamp = findViewById(R.id.buttonAddTimeStamp);
        imgStop = findViewById(R.id.stopBtn);
        imgPause = findViewById(R.id.pauseBtn);
        imgRecord = findViewById(R.id.recordBtn);
        imgSettings = findViewById(R.id.setBtn);
        expListView = findViewById(R.id.dialogListRecording);
        //initialize basic features.
        imgStop.setVisibility(View.INVISIBLE);
        imgPause.setVisibility(View.INVISIBLE);
        timeLapsed.setText(R.string.zero_time);
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        listNote = new ArrayList<>();
        fileName = "";
        // create the Handler for visualizer update
        handler = new Handler();
        initFileNameET();
    }

    /**
     * Initialize fileNameET.
     */
    private void initFileNameET() {
        fileNameET.setSingleLine(true);
        fileNameET.clearFocus();
        //get number of activity_recording/files starting with "My activity_recording #"
        fileNameET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (i == KeyEvent.KEYCODE_ENTER)) {
                    fileNameET.clearFocus();
                    return true;
                }
                return false;
            }
        });
        fileNameET.clearFocus();
    }

    /**
     * set layout's buttons' on click listeners.
     */
    private void setButtonsOnClickListener() {
        btnRecordings.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                vibe.vibrate(20);
                //clear notification after user exits activity.
                notificationManager.cancel(appNotification.getNotificationActivityId());
                notificationManager.deleteNotificationChannel(appNotification.getChannelId());
                appNotification.unregisterReceiver();
                Intent intent = new Intent(RecordingActivity.this, PlayRecWithNote.class);
                startActivity(intent);
            }
        });
        //set record, stop, pause, settings buttons on click listeners
        imgStop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                vibe.vibrate(10);
                stopRec();
            }
        });
        imgPause.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                vibe.vibrate(10);
                pauseRec();
            }
        });
        imgRecord.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                vibe.vibrate(VibrationEffect.createOneShot(10, 10));
                rec();
            }
        });
        imgSettings.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                vibe.vibrate(10);
                saveToAlert();
            }
        });
        imgSettings.setOnLongClickListener(new Button.OnLongClickListener() {
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        setTimeStampBtnClick();
    }

    /**
     * Set timestamp btn click listener.
     */
    private void setTimeStampBtnClick() {
        btnTimeStamp.setOnClickListener((new OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(10);
                if (fileName.equals("") || !fileName.equals(fileNameET.getText().toString())) {
                    fileName = fileNameET.getText().toString();
                }
                String time = timeLapsed.getText().toString();
                final LinkLinedView llv = new LinkLinedView(thisContext);
                final TimestampDeleteNote ts = new TimestampDeleteNote(thisContext);
                ts.setTimeStampText(time);
                llv.setTS(ts);
                if (isRecording) {
                    if (!noteStamped) {
                        //first noteStamp - create new list of notes.
                        listNote = new ArrayList<>();
                        expListView.setVisibility(View.VISIBLE);
                    }
                    //recording - note stamp will always work.
                    if (listDataHeader.size() == 0) {
                        //first noteStamp - create new list of header.
                        listDataHeader.add(fileName);
                    }
                    listNote.add(llv);
                    listDataChild.put(fileName, listNote); // Header, Child data
                    if (!noteStamped) {
                        //first noteStamp - create new ExpandableListAdapter.
                        noteStamped = true;
                        updateListView();
                    } else {
                        //adding note - update existing ExpandableListAdapter.
                        updateListViewAddedNote();
                    }
                    expListView.expandGroup(0);
                } else {
                    //not recording - note stamp will work once with 00:00:00.
                    if (!noteStamped) {
                        boolean fileExistsAlready = checkIfRecordingExistsAlready();
                        String newName;
                        if (fileExistsAlready) {
                            newName = getNextRecordingName();
                            recordingAlreadyExistsDialog(fileNameET.getText().toString(), newName);
                        } else {
                            //first noteStamp - create new list of notes.
                            listNote = new ArrayList<>();
                            listDataHeader.add(fileName);
                            listNote.add(llv);
                            listDataChild.put(fileName, listNote); // Header, Child data
                            updateListView();
                            noteStamped = true;
                            expListView.setVisibility(View.VISIBLE);
                            expListView.expandGroup(0);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "To add another note - " +
                                        "Start recording",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        }));
    }

    /**
     * Requesting permission to RECORD_AUDIO.
     *
     * @param requestCode  - request code
     * @param permissions  - permissions
     * @param grantResults - grant results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_MULTIPLE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionsAccepted = true;
            }
        }
        if (!permissionsAccepted) {
            finish();
        } else {
            fileNameET.setText(getNextRecordingName());
        }
    }

    /**
     * Start recording.
     */
    @TargetApi(24)
    public void rec() {
        if (!isPaused) {
            //rec btn pressed for first time - to start recording.
            boolean fileExistsAlready = checkIfRecordingExistsAlready();
            String newName;
            if (fileExistsAlready) {
                newName = getNextRecordingName();
                recordingAlreadyExistsDialog(fileNameET.getText().toString(), newName);
            } else {
                record();
            }
        } else {
            //is paused and pressed record btn, will resume recording.
            isPaused = false;
            try {
                recorder.resume();
                timeLapsed.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                imgRecord.setVisibility(View.INVISIBLE);
                imgStop.setVisibility(View.VISIBLE);
                imgPause.setVisibility(View.VISIBLE);
                handler.post(updateVisualizer);
                timeLapsed.start();
                //change notification when user records.
                //set first and second Pending Intent Strings.
                appNotification.setNotificationMessage(getString(R.string.recording_notif));
                appNotification.setFirstPendingIntentStr(getString(R.string.stop));
                appNotification.setSecondPendingIntentStr(getString(R.string.pause));
                appNotification.createNotificationBuilder();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * when stop button is clicked
     */
    public void stopRec() {
        stopRecSetButtonsVis();
        timeWhenStopped = 0;
        timeLapsed.stop();
        isPaused = false;
        stoppedRecording = true;

        releaseRecorder();
        //clear notification after user stopped recording.
        notificationManager.cancel(ACTIVITY_NOTIFICATION_ID);
        String fileNameToSave = this.fileName + ".txt";
        SaveNote.writeToFile(GROUP_POSITION, root, fileNameToSave, listAdapter);
        //reset data
        visualizerView.invalidate();
        fileNameET.setText(getNextRecordingName());
        //reset to 00:00:00
        timeLapsed.setText(R.string.zero_time);
        //enable name change
        fileNameET.setInputType(InputType.TYPE_CLASS_TEXT);
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        noteStamped = false;
        updateListView();
    }

    /**
     * Pause recording.
     */
    @TargetApi(24)
    private void pauseRec() {
        timeWhenStopped = timeLapsed.getBase() - SystemClock.elapsedRealtime();
        isPaused = true;
        timeLapsed.stop();
        handler.removeCallbacks(updateVisualizer);
        if (imgRecord.getVisibility() == View.INVISIBLE) {
            imgRecord.setVisibility(View.VISIBLE);
            imgPause.setVisibility(View.INVISIBLE);
        }
        if (recorder != null) {
            try {
                recorder.pause();
            } catch (Exception e) {
                System.out.println("in Pause: " + e);
            }
        }
        //change notification when user pauses rec.
        //set first and second Pending Intent Strings.
        appNotification.setNotificationMessage(getString(R.string.paused_recording_notif));
        appNotification.setFirstPendingIntentStr(getString(R.string.stop));
        appNotification.setSecondPendingIntentStr(getString(R.string.record));
        appNotification.createNotificationBuilder();
    }

    /**
     * Set buttons visibility upon stop recording.
     */
    private void stopRecSetButtonsVis() {
        //set buttons visibility.
        if (imgStop.getVisibility() == View.VISIBLE) {
            imgStop.setVisibility(View.INVISIBLE);
        }
        if (imgPause.getVisibility() == View.VISIBLE) {
            imgPause.setVisibility(View.INVISIBLE);
        }
        if (imgRecord.getVisibility() == View.INVISIBLE) {
            imgRecord.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Release recorder upon stopped recording.
     */
    private void releaseRecorder() {
        if (recorder != null) {
            isRecording = false; // stop activity_recording
            handler.removeCallbacks(updateVisualizer);
            visualizerView.clear();
            if (!isPaused) {
                //stop btn pressed
                if (recorder != null) {
                    try {
                        recorder.stop();
                        recorder.reset();
                        //saves recording
                        recorder.release();
                    } catch (RuntimeException stopException) {
                        stopException.printStackTrace();
                        try {
                            recorder.reset();
                            //saves recording
                            recorder.release();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        recorder = null;
                        Toast.makeText(getApplicationContext(), "couldn't save rec: " + fileName,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    // updates the visualizer every 50 milliseconds
    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (isRecording) // if we are already activity_recording
            {
                // get the current amplitude
                int x = recorder.getMaxAmplitude();
                visualizerView.addAmplitude(x * 2); // update the VisualizeView
                visualizerView.invalidate(); // refresh the VisualizerView
                // update in 40 milliseconds
                handler.postDelayed(this, REPEAT_INTERVAL);
            }
        }
    };

    /**
     * Get next automatic recording name.
     */
    String getNextRecordingName() {
        File f = root;
        int num = 1;
        if (null != f) {
            File file;
            String end;
            int filesLength = 0;
            File[] files = f.listFiles();
            if (files != null) {
                filesLength = files.length;
            }
            String filePath;
            int j;
            for (int i = 0; i < filesLength; i++) {
                for (j = 0; j < filesLength; j++) {
                    //go over files in dir and find latest Recording # used.
                    file = files[j];
                    filePath = file.getPath();
                    int start = filePath.lastIndexOf('/') + 1;
                    int endSub = filePath.lastIndexOf('.');
                    filePath = filePath.substring(start, endSub);
                    if (filePath.contains("Recording #")) {
                        end = filePath.substring(filePath.lastIndexOf('#') + 1);
                        try {
                            if (num == Integer.parseInt(end)) {
                                //reached file ending with Recording #num
                                num++;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            //do nothing
                            System.out.println("error");
                        }
                    }
                }
                //if went through all recordings and couldn't find file
                if (j == filesLength) {
                    //exit search for file loops
                    break;
                }
            }
        }
        return "Recording #" + num;
    }

    /**
     * Updates the note list view.
     */
    void updateListView() {
        //create adapter
        listAdapter = new ExpandableListAdapterNoDeleteBtn(this, listDataHeader, listDataChild);
        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    /**
     * Updates the note list view.
     */
    void updateListViewAddedNote() {
        //create adapter
        listAdapter.setChildList(listDataChild);
        listAdapter.setGroupList(listDataHeader);
        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    /**
     * @param time - number representing recording time.
     * @return string representing recording time.
     */
    private String getTimeText(long time) {
        int h = (int) (time / 3600);
        int m = (int) (time / 60);
        int s = (int) (time % 60);
        String hh = h < 10 ? "0" + h + ":" : h + ":";
        String mm = m < 10 ? "0" + m + ":" : m + ":";
        String ss = s < 10 ? "0" + s : s + "";
        return hh + mm + ss;
    }

    /**
     * Check if recording exists.
     *
     * @return true if recording exists; else return false.
     */
    private boolean checkIfRecordingExistsAlready() {
        if ((fileName == null || !fileName.equals(
                fileNameET.getText().toString()))) {
            //change key in list
            ExpandableListAdapterNoDeleteBtn ela = (ExpandableListAdapterNoDeleteBtn) expListView.getExpandableListAdapter();
            if (ela != null && listDataHeader.size() > 0) {
                List<LinkLinedView> l = ela.getChildList(fileName);
                fileName = fileNameET.getText().toString();

                listDataHeader.remove(this.GROUP_POSITION);
                listDataHeader.add(fileName);
                ela.updateHeaderView(this.GROUP_POSITION, fileName);
                this.listDataChild.remove(fileName);
                this.listDataChild.put(fileName, l);
                ela.setGroupList(listDataHeader);
                ela.setChildList(listDataChild);
                expListView.expandGroup(this.GROUP_POSITION);
            } else {
                fileName = fileNameET.getText().toString();
            }
        }
        return checkDir();
    }

    /**
     * Checks if folder contains file with name fileName.
     *
     * @return true if file exists.
     * false otherwise.
     */
    private boolean checkDir() {
        File f = root;
        if (null != f) {
            File file;
            int filesLength;
            File[] files = f.listFiles();
            if (files != null) {
                filesLength = files.length;
            } else {
                //doesn't contain files - doesn't contain fileName
                return false;
            }
            String fName = fileNameET.getText().toString();
            String filePath;
            for (int i = 0; i < filesLength; i++) {
                file = files[i];
                filePath = file.getPath();
                int start = filePath.lastIndexOf('/') + 1;
                int endSub = filePath.lastIndexOf('.');
                filePath = filePath.substring(start, endSub);
                if (filePath.equals(fName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Alert user that recording with new name already exists.
     *
     * @param existingFileName - recording's name to change.
     * @param newName          - recording's new name to change to.
     */
    private void recordingAlreadyExistsDialog(String existingFileName, final String newName) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("recording with given name \"" + existingFileName + "\" already exists. " +
                "\ncontinue recording with new name \"" + newName + "\"?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Continue",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fileNameET.setText(newName);
                        record();
                    }
                });

        builder1.setNegativeButton(
                "Cancel recording",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //cancel notification
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    /**
     * Start recording.
     */
    private void record() {
        if (!root.exists()) {
            root.mkdirs();
        }
        //change notification when user records.
        //set first and second Pending Intent Strings.
        appNotification.setNotificationMessage(getString(R.string.recording_notif));
        appNotification.setFirstPendingIntentStr(getString(R.string.stop));
        appNotification.setSecondPendingIntentStr(getString(R.string.pause));
        appNotification.createNotificationBuilder();
        //get fileName from EditText
        if (noteStamped && (fileName == null || !fileName.equals(
                fileNameET.getText().toString()))) {
            //user wants to change name before recording starts.
            //change key in list
            ExpandableListAdapterNoDeleteBtn ela = (ExpandableListAdapterNoDeleteBtn) expListView.getExpandableListAdapter();
            if (ela != null) {
                List<LinkLinedView> l = ela.getChildList(fileName);
                fileName = fileNameET.getText().toString();
                listDataHeader.remove(this.GROUP_POSITION);
                listDataHeader.add(fileName);
                ela.updateHeaderView(this.GROUP_POSITION, fileName);
                this.listDataChild.remove(fileName);
                this.listDataChild.put(fileName, l);
                ela.setGroupList(listDataHeader);
                ela.setChildList(listDataChild);
                expListView.expandGroup(this.GROUP_POSITION);
            } else {
                fileName = fileNameET.getText().toString();
            }
        }
        this.fileName = fileNameET.getText().toString();
        //reset to 00:00:00
        timeLapsed.setBase(SystemClock.elapsedRealtime());

        timeLapsed.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = (SystemClock.elapsedRealtime() - cArg.getBase()) / 1000;
                cArg.setText(getTimeText(time));
            }
        });
        if (timeLapsed.getVisibility() == View.INVISIBLE) {
            timeLapsed.setVisibility(View.VISIBLE);
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        //record stereo
        recorder.setAudioEncodingBitRate(16 * 44100);
        recorder.setAudioSamplingRate(44100);
        //disable name change after activity_recording has started
        fileNameET.setInputType(InputType.TYPE_NULL);
        //update key in childList in expListView Adapter to current name if name has changed
        //before rec clicked
        //set output file to be saved once recorder is released.
        File mFile = new File(root + "/" + fileName
                + ".m4a");
        recorder.setOutputFile(mFile.getPath());
        Toast.makeText(getApplicationContext(), "recording file: " + root + "/" + fileName
                + ".m4a", Toast.LENGTH_LONG).show();

        OnErrorListener errorListener = null;
        recorder.setOnErrorListener(errorListener);
        OnInfoListener infoListener = null;
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
            isRecording = true; // we are currently activity_recording
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imgRecord.setVisibility(View.INVISIBLE);
        imgStop.setVisibility(View.VISIBLE);
        imgPause.setVisibility(View.VISIBLE);
        handler.post(updateVisualizer);
        timeLapsed.start();
    }

    /**
     * Save recordings to given location.
     *
     * @param saveToLocation - saved recordings location.
     */
    public void saveTo(int saveToLocation) {
        String state = Environment.getExternalStorageState();
        int size = filesDir.length;
        switch (saveToLocation) {
            case EXTERNAL_STORAGE:
                //external storage availability check
                if (Environment.MEDIA_MOUNTED.equals(state) && size > 1) {
                    root = new File(filesDir[1], directoryName);
                    Toast.makeText(getApplicationContext(),
                            "folder: " + filesDir[1].toString(), Toast.LENGTH_SHORT).show();
                    //update chosen choice in save to dialog alert.
                    this.CHECKED_STORAGE = EXTERNAL_STORAGE;
                    break;
                }//else no external storage available - will continue to case INTERNAL_STORAGE.
            case INTERNAL_STORAGE:
                //internal storage
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    root = new File(Environment.getExternalStorageDirectory(),
                            directoryName);
                } else {
                    root = new File(Environment.getExternalStorageDirectory(),
                            directoryName);
                }
                //update chosen choice in save to dialog alert.
                this.CHECKED_STORAGE = INTERNAL_STORAGE;
                break;
            default:
                break;
        }
        if (!root.exists()) {
            root.mkdirs();
        }
        ds.setDirectory(root);
        if (fileNameET != null) {
            String name = getNextRecordingName();
            this.fileNameET.setText(name);
        }
        setSaveToPreference();
    }

    /**
     * Save users saved location preference.
     */
    private void setSaveToPreference() {
        SharedPreferences sharedPref = getSharedPreferences("save to", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("save to", CHECKED_STORAGE);
        editor.apply();
    }

    /**
     * Get saved location preference.
     *
     * @return saved location preference.
     */
    private int getSaveToPreference() {
        SharedPreferences sharedPref = getSharedPreferences("save to", MODE_PRIVATE);
        int pref = sharedPref.getInt("save to", CHECKED_STORAGE);
        saveTo(pref);
        return pref;
    }

    /**
     * Does nothing in current activity.
     *
     * @param view - delete note btn
     */
    public void onClickDeleteNote(View view) {
        //do nothing.
    }

    /**
     * Does nothing in current activity.
     *
     * @param view - timestamp TextView
     */
    public void onClickTextViewTimeStamp(View view) {
        //do nothing.
    }

    /**
     * Save recording menu options.
     */
    private void saveToAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(R.array.pref_save_to, this.CHECKED_STORAGE,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //choice clicked
                        saveTo(i);
                    }
                });
        builder.setPositiveButton(
                "ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //exit alert
                        dialog.cancel();
                    }
                });
        builder.setCancelable(true);
        builder.setTitle("Save To:");
        AlertDialog alert11 = builder.create();
        alert11.show();
    }

    /**
     * Back button pressed in Android UI.
     */
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    /**
     * User pressed button in notification.
     *
     * @param intent - holds action to do.
     */
    public void BroadcastReceived(Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(getString(R.string.pause))) {
                pauseRec();
            } else if (action.equals(getString(R.string.stop))) {
                stopRec();
            } else if (action.equals(getString(R.string.record))) {
                rec();
            }
        }
    }

    /**
     * This will run when return from PlayRecWithNote or return to app to this page.
     */
    @Override
    public void onRestart() {
        super.onRestart();
        appNotification = new NotificationVoiceRec(this);
        notificationManager = appNotification.getNotificationManager();
        if (!isRecording) // if we are already activity_recording
        {
            // if not recording and returned to this page
            if (stoppedRecording) {
                stoppedRecording = false;
                fileNameET.setText(getNextRecordingName());
            }
        }
    }

    /**
     * This will run when app stops running.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //clear notification after user stopped recording.
        notificationManager.cancel(ACTIVITY_NOTIFICATION_ID);
        releaseRecorder();
        if (receiverRegistered != null) {
            unregisterReceiver(receiver);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel("1");
        }
    }
}