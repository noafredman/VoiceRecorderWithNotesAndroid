package com.NoaoN.voiceRecorderWithNotes.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.NoaoN.voiceRecorderWithNotes.helper_classes.Delete;
import com.NoaoN.voiceRecorderWithNotes.helper_classes.DirectorySingleton;
import com.NoaoN.voiceRecorderWithNotes.helper_classes.NotificationVoiceRec;
import com.NoaoN.voiceRecorderWithNotes.helper_classes.SaveNote;
import com.NoaoN.voiceRecorderWithNotes.components.TimestampDeleteNote;
import com.NoaoN.voiceRecorderWithNotes.list_adapter.ExpandableListAdapter;
import com.NoaoN.voiceRecorderWithNotes.components.LinkLinedView;
import com.NoaoN.voiceRecorderWithNotes.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayRecWithNote extends AppCompatActivity implements IActivityWithNotification {
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private Chronometer timeLapsed;
    private boolean timeLapseStarted = false;
    private int mCurrentPosition;

    private ImageView imgStop;
    private ImageView imgPause;
    private ImageView imgPlay;
    private SeekBar seeker;

    private Handler mHandler = new Handler();
    private List<String> fileList = new ArrayList<>();
    private List<String> fileNameList = new ArrayList<>();
    private File selectedFile;
    private File curFolder;
    private TextView textFolder;
    private TextView textRecTime;

    private String fileName;
    private String fileNameToSave;
    private MediaPlayer mp = null;
    private File selectedRecordingToDeleteOrRename;
    private File selectedRecordingNoteToDeleteOrRename;

    private Vibrator vibe;
    //notification fields
    private NotificationManager notificationManager;
    private NotificationVoiceRec appNotification;

    private File root;
    private List<Pair<String, List<LinkLinedView>>> recAndNote;
    private boolean isExpanded;
    private boolean deleteNoteClicked = false;

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<LinkLinedView>> listDataChild;
    private static int lastGroupPosition = -1;
    private String fileType;
    private boolean checkBoxVisible = false;
    private TextView noRecInListTextView;
    private boolean NAME_CHANGED = false;

    //setters
    public void setSelectedRecordingToDeleteOrRename(File selectedRecordingToDeleteOrRename) {
        this.selectedRecordingToDeleteOrRename = selectedRecordingToDeleteOrRename;
    }

    public void setSelectedRecordingNoteToDeleteOrRename(File selectedRecordingNoteToDeleteOrRename) {
        this.selectedRecordingNoteToDeleteOrRename = selectedRecordingNoteToDeleteOrRename;
    }

    public void setDeleteNoteClicked(boolean deleteNoteClicked) {
        this.deleteNoteClicked = deleteNoteClicked;
    }

    //Getters
    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public List<String> getFileList() {
        return fileList;
    }

    public String getFileName() {
        return fileName;
    }

    public File getSelectedRecordingToDeleteOrRename() {
        return selectedRecordingToDeleteOrRename;
    }

    public File getSelectedRecordingNoteToDeleteOrRename() {
        return selectedRecordingNoteToDeleteOrRename;
    }

    public List<Pair<String, List<LinkLinedView>>> getRecAndNote() {
        return recAndNote;
    }

    public TextView getNoRecInListTextView() {
        return noRecInListTextView;
    }

    public Chronometer getTimeLapsed() {
        return timeLapsed;
    }

    public SeekBar getSeeker() {
        return seeker;
    }

    public boolean getCheckBoxVisible() {
        return checkBoxVisible;
    }

    public boolean getDeleteNoteClicked() {
        return deleteNoteClicked;
    }

    public String getFileNameToSave() {
        return this.fileNameToSave;
    }

    public ExpandableListAdapter getListAdapter() {
        return listAdapter;
    }

    public ExpandableListView getExpListView() {
        return expListView;
    }

    public HashMap<String, List<LinkLinedView>> getListDataChild() {
        return this.listDataChild;
    }

    public File getRoot() {
        return this.root;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DirectorySingleton ds = DirectorySingleton.getInstance();
        root = ds.getDirectory();
        setContentView(R.layout.recordings_list);
        Intent intent = new Intent(this, PlayRecWithNote.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        appNotification = new NotificationVoiceRec(this);
        notificationManager = appNotification.getNotificationManager();
        isExpanded = false;
        recAndNote = new ArrayList<>();
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        initComponents();
        setButtonsOnClickListener();
        //setting the seeker
        setSeekBarChangeListener();
        textRecTime.setVisibility(View.INVISIBLE);
        //set directory.
        curFolder = root;
        timeLapsed.setText(R.string.zero_time);
        //set expandable list view listeners.
        setExpListListeners();
        ShowRecList();
        List<String> l = listAdapter.getGroupList();
        if (l.size() == 0) {
            //no recordings in list
            timeLapsed.setVisibility(View.GONE);
            seeker.setVisibility(View.INVISIBLE);
            noRecInListTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set activity's components.
     */
    private void initComponents() {
        imgStop = findViewById(R.id.stopBtn2);
        imgPause = findViewById(R.id.pauseBtn2);
        imgPlay = findViewById(R.id.playBtn);
        seeker = findViewById(R.id.seeker);
        textRecTime = findViewById(R.id.recTime);
        timeLapsed = findViewById(R.id.chronometer);
        expListView = findViewById(R.id.dialogList);
        noRecInListTextView = findViewById(R.id.noRecInList);
    }

    /**
     * set layout's buttons' on click listeners.
     */
    private void setButtonsOnClickListener() {
        //set record, stop, pause buttons
        imgStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibe.vibrate(50);
                stopPlayingRecording();
            }
        });
        imgStop.setVisibility(View.INVISIBLE);

        imgPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibe.vibrate(50);
                pausePlayingRecording();
            }
        });
        imgPause.setVisibility(View.INVISIBLE);

        imgPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibe.vibrate(50);
                playRecordingViaNotificationBarOrImg();
            }
        });
    }

    /**
     * Set seeker's change listener.
     */
    public void setSeekBarChangeListener() {
        //if user moves seeker (seeks) - then change mp duration.
        seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //do nothing
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mp != null && fromUser) {
                    //if user drags/moves seeker - move mp accordingly
                    mp.seekTo(seeker.getProgress() * 1000);
                    timeLapsed.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                        @Override
                        public void onChronometerTick(Chronometer cArg) {
                            long time = seeker.getProgress();
                            cArg.setText(getTimeText(time));
                        }
                    });
                }
            }
        });
    }

    /**
     * Set expandable list listeners.
     */
    private void setExpListListeners() {
        setExpListOnGroupClickListener();
        setExpListOnGroupCollapseListener();
    }

    /**
     * Set expandable list OnGroupClick listener.
     */
    private void setExpListOnGroupClickListener() {
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                LinearLayout ll = (LinearLayout) v;
                String fileSelectedString = ((TextView) ll.getChildAt(1))
                        .getText().toString();
                //play recording only if not isExpanded yet.
                if (!isExpanded) {
                    isExpanded = true;
                    recClicked(fileSelectedString);
                    lastGroupPosition = groupPosition;
                    return false;
                } else {
                    //is expanded
                    if (lastGroupPosition != groupPosition) {
                        //different recording clicked
                        //close prev isExpanded
                        if (expListView.isGroupExpanded(lastGroupPosition)) {
                            //manual collapse last extended group.
                            expListView.collapseGroup(lastGroupPosition);
                        }
                        if (isPlaying) {
                            //stop playing current expanded recording.
                            stopPlayingRecording();
                            NAME_CHANGED = false;
                        } else {
                            SaveNote.writeToFile(lastGroupPosition, root, fileNameToSave,
                                    listAdapter);
                        }
                        lastGroupPosition = groupPosition;
                        //play clicked recording.
                        recClicked(fileSelectedString);
                        //need to expand manually to avoid exception due to manual collapse.
                        expListView.expandGroup(groupPosition);
                        isExpanded = true;
                        return true;
                    } else {
                        //expanded group clicked again.
                        if (isPlaying) {
                            //stop playing current expanded recording.
                            stopPlayingRecording();
                            NAME_CHANGED = false;
                        } else {
                            SaveNote.writeToFile(lastGroupPosition, root, fileNameToSave,
                                    listAdapter);
                        }
                        //collapse expanded group.
                        expListView.collapseGroup(lastGroupPosition);
                    }
                }
                return false;
            }
        });
    }

    /**
     * Set expandable list OnGroupCollapse listener.
     */
    private void setExpListOnGroupCollapseListener() {
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                try {
                    isExpanded = false;
                    //stop/pause
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        //listDataChild = new HashMap<String, List<String>>();
        listDataChild = new HashMap<>();
        recAndNote = new ArrayList<>();
        File[] files = curFolder.listFiles();
        if (files != null) {
            //sort alphabetically
            //  Arrays.sort(files);
            //sort most recently recorded
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                }
            });
            fileList.clear();
            fileNameList.clear();
            String filePath;
            String noteFp;
            List<LinkLinedView> listNote;
            //fill recAndNote
            for (File file : files) {
                if (file.getPath().endsWith(".m4a")) {
                    fileType = ".m4a";
                    filePath = file.getPath();
                    fileList.add(filePath);
                    String fName = filePath.substring(filePath.lastIndexOf('/') + 1,
                            filePath.lastIndexOf('.'));
                    fileNameList.add(fName);
                }
            }
            int i = 0;
            for (String fName : fileNameList) {
                filePath = fileList.get(i);
                noteFp = filePath.substring(0, filePath.lastIndexOf('.'));
                noteFp += ".txt";
                listNote = getNote(noteFp);
                recAndNote.add(new Pair<>(fName, listNote));
                i++;
            }
            for (Pair<String, List<LinkLinedView>> pair : recAndNote) {
                String header = pair.first;
                listDataHeader.add(header);
                listNote = pair.second;
                listDataChild.put(header, listNote); // Header, LinkLinedView
            }
        }
        textFolder.setText(getString(R.string.saved_in_folder, curFolder.getPath()));
    }

    @SuppressLint("ClickableViewAccessibility")
    public void ShowRecList() {
        textFolder = findViewById(R.id.folder);
        expListView = findViewById(R.id.dialogList);
        CreateRecList();
        expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long id) {
                long packedPosition = expListView.getExpandableListPosition(position);
                if (ExpandableListView.getPackedPositionType(packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    //iterate through fileList until reach same name as in recAndNote at position.
                    getRecPosition(position);
                    //get pair to change name from recAndNote
                    nameChangeAlert(fileName);
                    return true;
                }
                return true;
            }
        });
    }

    /**
     * Find index of given file.
     */
    int FindFileIndex(String f) {
        int pos = 0;
        for (String file : fileNameList) {
            if (file.equals(f)) {
                return pos;
            }
            pos++;
        }
        return -1;
    }

    /**
     * Create recordings' expandable list.
     */
    public void CreateRecList() {
        // preparing list data
        prepareListData();
        updateList();
    }

    /**
     * Update recordings' expandable list.
     */
    public void updateList() {
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild,
                fileList.size());
        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    /**
     * Create and show recording name change alert.
     *
     * @param fileNameToChange - recording's name to change.
     */
    private void nameChangeAlert(final String fileNameToChange) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        // Get the layout inflater
        // LayoutInflater inflater = requireActivity().getLayoutInflater();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.file_name_change_alert, null);
        final EditText nameChangET = view.findViewById(R.id.nameChangeET);
        nameChangET.setText(fileNameToChange);
        nameChangET.selectAll();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder1.setView(view);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String newName = nameChangET.getText().toString();
                        if (!newName.equals(fileNameToChange)) {
                            if (checkDir(newName)) {
                                fileAlreadyExistsDialog(newName);
                            } else {
                                saveNameChange(newName, fileNameToChange);
                                dialog.cancel();
                            }
                        }
                    }
                });
        builder1.setTitle("Rename Recording:");
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    /**
     * Save recording's (and it's note file) new name.
     *
     * @param newName - recording's new name to change to.
     * @param oldName - recording's name to change.
     */
    private void saveNameChange(String newName, String oldName) {
        //update actual files to new name.
        String fName, path;
        int groupToRename = 0;
        //find file to delete
        for (String filePath : fileList) {
            fName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf(
                    '.'));
            if (fName.equals(oldName)) {
                path = filePath.substring(0, filePath.lastIndexOf('/') + 1);
                selectedRecordingToDeleteOrRename = new File(filePath);
                selectedRecordingNoteToDeleteOrRename = new File(filePath.substring(0,
                        filePath.lastIndexOf('.')).concat(".txt"));
                fileList.set(groupToRename, path + newName + ".m4a");
                fileNameList.set(groupToRename, newName);
                //rename
                boolean renameResult = selectedRecordingToDeleteOrRename.renameTo(new File(
                        path + newName + ".m4a"));
                if (!renameResult) {
                    fileList.set(groupToRename, path + oldName + ".m4a");
                    fileNameList.set(groupToRename, oldName);
                    break;
                }
                renameResult = selectedRecordingNoteToDeleteOrRename.renameTo(new File(
                        path + newName + ".txt"));
                if (!renameResult) {
                    fileList.set(groupToRename, path + oldName + ".m4a");
                    fileNameList.set(groupToRename, oldName);
                    break;
                }
                groupToRename = listAdapter.getGroupPosition(oldName);
                if (listAdapter != null) {
                    List<LinkLinedView> l = listAdapter.getChildList(
                            oldName);
                    //update header name
                    this.listDataHeader.set(groupToRename, newName);
                    listAdapter.updateHeaderView(groupToRename, newName);
                    this.listDataChild.remove(oldName);
                    //update note & timeStamp name
                    this.listDataChild.put(newName, l);
                    listAdapter.setGroupList(listDataHeader);
                    listAdapter.setChildList(listDataChild);
                    updateRecAndNote(newName);
                }
                fileNameToSave = newName + ".txt";

                /*if (isPlaying || isPaused) {
                    //uncomment if want recording to stop playing after name changed
                  //  stopPlayingRecording();
                }*/
                //reload list
                updateList();
                if (isExpanded) {
                    expListView.expandGroup(groupToRename);
                }
                this.fileName = newName;
                break;
            }
            groupToRename++;
        }
    }

    /**
     * Get the notes of given recording file path.
     *
     * @param path - path to recording's note file.
     * @return list of linkLinedView containing recording's notes.
     */
    private List<LinkLinedView> getNote(String path) {
        BufferedReader reader;
        Pattern pattern = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");
        Matcher matcher;
        List<LinkLinedView> linkLinedViewList = new ArrayList<>();
        LinkLinedView llv = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(new File(path))));
            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                matcher = pattern.matcher(mLine);
                if (matcher.matches()) {
                    //line in note is timeStamp. get time. add Item with time, and note
                    llv = new LinkLinedView(this);
                    TimestampDeleteNote ts = new TimestampDeleteNote(this);
                    ts.setTimeStampText(mLine);
                    llv.setTS(ts);
                    linkLinedViewList.add(llv);
                } else {
                    //line in note is timeStamp. get time. add Item with time, and note
                    if (llv != null) {
                        if (!llv.getNoteText().equals("")) {
                            llv.setLinkLinedNoteText(llv.getNoteText() + "\n");
                        }
                        llv.setLinkLinedNoteText(llv.getNoteText() + mLine);
                    }
                }
            }
            return linkLinedViewList;
        } catch (IOException e) {
            Log.e("IO Exception", "File write failed: " + e.toString());
        } catch (Exception exc) {
            Log.e("Exception", "File write failed: " + exc.toString());
        }
        return null;
    }

    /**
     * Back button pressed in Android UI.
     */
    @Override
    public void onBackPressed() {
        if (isPlaying) {
            stopPlayingRecording();
        } else {
            int groupPos = getGroupPosition(fileName);
            if (groupPos != -1 && isExpanded) {
                SaveNote.writeToFile(groupPos, root, fileNameToSave,
                        listAdapter);
            }
        }
        //set keep, select all to invisible
        Button keep = findViewById(R.id.keepBtn);
        if (keep.getVisibility() == View.VISIBLE) {
            setVisibilityOf_SelectAllTV_CheckBoxes_KeepBtn(View.INVISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Get new MediaPlayer.
     *
     * @return new MediaPlayer.
     */
    private MediaPlayer getMediaPlayer() {
        if (mp != null) {
            try {
                mp.stop();
                mp.reset();
                mp.release();
                mp = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return new MediaPlayer();
        }
        return new MediaPlayer();
    }

    /**
     * Stop playing recording.
     */
    public void stopPlayingRecording() {
        if (mp != null) {
            isPlaying = false;
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
            mp = getMediaPlayer();
            //clear notification after user stopped recording.
            if (notificationManager.getActiveNotifications().length != 0) {
                notificationManager.cancel(appNotification.getNotificationActivityId());
            }
            timeLapsed.stop();
            timeLapseStarted = false;
            try {
                mp.setDataSource(PlayRecWithNote.this, Uri.fromFile(selectedFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mp.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //reset seeker
        seeker.setProgress(0);
        textRecTime.setVisibility(View.INVISIBLE);
        timeLapsed.setVisibility(View.INVISIBLE);
        imgPause.setVisibility(View.INVISIBLE);
        imgStop.setVisibility(View.INVISIBLE);
        if (fileNameToSave != null) {
            int groupPos = getGroupPosition(fileNameToSave.substring(0,
                    fileNameToSave.indexOf(".")));
            SaveNote.writeToFile(groupPos, root, fileNameToSave,
                    listAdapter);
        }
    }

    /**
     * Pause playing recording.
     */
    public void pausePlayingRecording() {
        if (mp != null) {
            mp.pause();
            timeLapsed.stop();
            timeLapseStarted = false;
            isPaused = true;
        }
        //change notification when user pauses rec.
        //set first and second Pending Intent Strings.
        appNotification.setNotificationMessage(getString(R.string.paused_recording_notif));
        appNotification.setFirstPendingIntentStr(getString(R.string.stop));
        appNotification.setSecondPendingIntentStr(getString(R.string.play));
        appNotification.createNotificationBuilder();
    }

    /**
     * Play recording via notification bar or play image clicked.
     */
    private void playRecordingViaNotificationBarOrImg() {
        if (mp != null) {
            if (!isPaused) {
                //stopped
                imgPause.setVisibility(View.VISIBLE);
                imgStop.setVisibility(View.VISIBLE);
                timeLapsed.setVisibility(View.VISIBLE);
                textRecTime.setVisibility(View.VISIBLE);
            } else {
                //paused
                isPaused = false;
            }
            mp.start();
            timeLapsed.start();
            //notification
            //change notification when user plays rec.
            //set first and second Pending Intent Strings.
            appNotification.setNotificationMessage(getString(R.string.playing_notif));
            appNotification.setFirstPendingIntentStr(getString(R.string.stop));
            appNotification.setSecondPendingIntentStr(getString(R.string.pause));
            appNotification.createNotificationBuilder();
        }
        isPlaying = true;
    }

    /**
     * Play recording.
     *
     * @param fileSelectedString - file selected name.
     */
    private void recClicked(String fileSelectedString) {
        if (mp != null) {
            mp.stop();
        }
        vibe.vibrate(20);
        isPlaying = true;
        if (fileSelectedString.contains(".txt")) {
            fileSelectedString = fileSelectedString.substring(0, fileSelectedString.indexOf(".txt"));
        }
        File selected = selectedFile = new File(curFolder.getPath() + "/" +
                fileSelectedString + fileType);
        this.fileNameToSave = fileSelectedString + ".txt";
        int position = FindFileIndex(fileSelectedString);
        if (-1 != position) {
            //show what activity_recording was selected
            textRecTime.setVisibility(View.VISIBLE);
            Toast.makeText(PlayRecWithNote.this, selected.getName() + " selected",
                    Toast.LENGTH_SHORT).show();
            imgStop.setVisibility(View.VISIBLE);
            imgPause.setVisibility(View.VISIBLE);
            mp = getMediaPlayer();
            try {
                mp.setDataSource(PlayRecWithNote.this, Uri.fromFile(selected));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mp.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //if user moves seeker (seeks) - then change mp DURATION.
            seeker.setProgress(0);
            final int DURATION = mp.getDuration() / 1000;
            //move seeker according to mp
            //connect seeker to mediaPlayer
            seeker.setMax(DURATION);
            //play selected activity_recording
            mp.start();
            //notification
            //change notification when user plays rec.
            //set first and second Pending Intent Strings.
            appNotification.setNotificationMessage(getString(R.string.playing_notif));
            appNotification.setFirstPendingIntentStr(getString(R.string.stop));
            appNotification.setSecondPendingIntentStr(getString(R.string.pause));
            appNotification.createNotificationBuilder();
            //update Seekbar on UI thread
            mHandler = new Handler();
            PlayRecWithNote.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mCurrentPosition = mp.getCurrentPosition() / 1000;
                    if (mp != null && mCurrentPosition <= seeker.getMax() && isPlaying) {
                        //if seeker didn't reach beyond the max then move it according to mp.
                        mCurrentPosition = mp.getCurrentPosition() / 1000;
                        seeker.setProgress(mCurrentPosition, true);
                        if (!timeLapseStarted) {
                            //set up time lapse.
                            timeLapsed.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                                @Override
                                public void onChronometerTick(Chronometer cArg) {
                                    long time = mCurrentPosition;
                                    cArg.setText(getTimeText(time));

                                }
                            });
                            timeLapsed.start();
                            timeLapseStarted = true;
                        }
                        if (mCurrentPosition >= seeker.getMax()) {
                            //reached end of recording
                            notificationManager.cancel(appNotification.getNotificationActivityId());
                            isPlaying = false;
                        }
                    }
                    //move seeker by 1 sec intervals.
                    mHandler.postDelayed(this, 1000);
                }
            });
            textRecTime.setText(getTimeText(DURATION));
            if (timeLapsed.getVisibility() == View.INVISIBLE) {
                timeLapsed.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Delete note.
     *
     * @param view - clicked note to delete.
     */
    public void onClickDeleteNote(View view) {
        if (!deleteNoteClicked) {
            vibe.vibrate(20);
            Delete.deleteNote(view, this);
        }
    }

    /**
     * Jump playing recording to current time clicked.
     *
     * @param view - time stamp TextView.
     */
    public void onClickTextViewTimeStamp(View view) {
        vibe.vibrate(20);
        //jump
        String timeText = ((TextView) view).getText().toString();
        int timeInt = getTimeText(timeText);
        if (mp != null) {
            //move mp accordingly to timeStamp clicked
            if (!isPlaying) {
                recClicked(fileNameToSave);
            }
            seeker.setProgress(timeInt);
            mp.seekTo(seeker.getProgress() * 1000);
            timeLapsed.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer cArg) {
                    long time = seeker.getProgress();
                    cArg.setText(getTimeText(time));
                }
            });
        }

    }


    /**
     * Get time as String.
     *
     * @param time - long representing time.
     * @return time as String.
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
     * Get time as int.
     *
     * @param time - string representing time.
     * @return time as int.
     */
    private int getTimeText(String time) {
        //time is of 00:00:00 pattern
        Pattern pattern = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");
        Matcher matcher = pattern.matcher(time);
        if (matcher.matches()) {
            String hh = matcher.group(1);
            String mm = matcher.group(2);
            String ss = matcher.group(3);
            return Integer.parseInt(hh) * 3600
                    + Integer.parseInt(mm) * 60
                    + Integer.parseInt(ss);
        } else {
            throw new IllegalArgumentException("Invalid format " + time);
        }
    }

    /**
     * Get group position.
     *
     * @param fileName - recording's name.
     * @return group position.
     */
    private int getGroupPosition(String fileName) {
        int groupPosition = -1;
        if (listAdapter != null && fileName != null && !fileName.equals("") &&
                listAdapter.getGroupCount() > 0) {
            groupPosition = listAdapter.getGroupPosition(fileName);
        }
        return groupPosition;
    }

    /**
     * Delete recordings.
     *
     * @param view - delete button.
     */
    public void onClickDeleteMultiple(View view) {
        expListView.setGroupIndicator(null);
        refreshExpandableListGroupIndicator();
        vibe.vibrate(20);
        Delete.deleteRecordingsClicked(this);
    }

    /**
     * Set checkBoxVisible.
     */
    public void setCheckBoxVisible() {
        checkBoxVisible = !checkBoxVisible;
    }

    /**
     * Get position of recording.
     *
     * @param position recordings supoosed position.
     * @return position of recording.
     */
    public int getRecPosition(int position) {
        int count = 0;
        int expandedPos = 0;
        //position is larger than expanded recording position.
        //in this case, when a recording is extended, then the following recordings
        // positions grow by the number of children the expanded recording has.
        if (isExpanded && listAdapter.getExpandedGroupPosition() < position) {
            expandedPos = listAdapter.getChildrenCount(listAdapter.getExpandedGroupPosition());
        }
        position -= expandedPos;
        for (Pair<String, List<LinkLinedView>> pair : recAndNote) {
            if (count++ == position) {
                //found position of rec and note to be deleted.
                this.fileName = pair.first;
                break;
            }
        }
        return position;
    }

    /**
     * Keep recordings.
     *
     * @param view - keep button.
     */
    public void onClickKeep(View view) {
        vibe.vibrate(20);
        //uncheck all, set all to invisible - check boxes, keep, select all
        //uncheck selectAll box
        checkAll();
        //set to invisible
        setVisibilityOf_SelectAllTV_CheckBoxes_KeepBtn(View.INVISIBLE);
        //reset group indicator.
        //obtain expandableListViewStyle from theme
        TypedArray expandableListViewStyle = this.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.expandableListViewStyle});
        //obtain attr from style
        TypedArray groupIndicator = this.getTheme().obtainStyledAttributes(expandableListViewStyle.
                getResourceId(0, 0), new int[]{android.R.attr.groupIndicator});
        expListView.setGroupIndicator(groupIndicator.getDrawable(0));
        refreshExpandableListGroupIndicator();
    }

    /**
     * sets visibility of SelectAllTV CheckBoxes and KeepBtn.
     *
     * @param visibility - visibility to set SelectAllTV CheckBoxes and KeepBtn.
     */
    public void setVisibilityOf_SelectAllTV_CheckBoxes_KeepBtn(int visibility) {
        if (visibility == View.INVISIBLE) {
            checkBoxVisible = false;
        }
        CheckBox cb = findViewById(R.id.selectAllCheckBox);
        TextView tv = findViewById(R.id.selectAllTextView);
        Button keep = findViewById(R.id.keepBtn);
        //set to invisible
        cb.setVisibility(visibility);
        tv.setVisibility(visibility);
        keep.setVisibility(visibility);
        listAdapter.setGroupCheckboxesVisibility(visibility);
    }

    /**
     * Selects or deselects all recordings.
     *
     * @param view - Select All button.
     */
    public void onClickSelectAll(View view) {
        vibe.vibrate(20);
        checkAll();
    }

    /**
     * @param view - Select All TextView.
     */
    public void onClickSelectAllTextView(View view) {
        vibe.vibrate(20);
        CheckBox cb = findViewById(R.id.selectAllCheckBox);
        boolean check = !cb.isChecked();
        cb.setChecked(check);
        checkAll();
    }

    /**
     * Select/deselect all checkboxes.
     */
    private void checkAll() {
        CheckBox cb = findViewById(R.id.selectAllCheckBox);
        boolean check = cb.isChecked();
        listAdapter.selectAllGroups(check);
    }

    /**
     * Updates recAndNote recording's name.
     *
     * @param newName - recording's new name.
     */
    private void updateRecAndNote(String newName) {
        int groupPosition = listAdapter.getGroupPosition(newName);
        Pair<String, List<LinkLinedView>> p = recAndNote.get(groupPosition);
        List<LinkLinedView> listNote = p.second;
        recAndNote.set(groupPosition, new Pair<>(newName, listNote));
    }

    /**
     * Checks if folder contains file with name fileName.
     *
     * @return true if file exists.
     * false otherwise.
     */
    private boolean checkDir(String fName) {
        File f = curFolder;
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
     * Check if recording exists containing given newName.
     *
     * @param newName - recording's new name.
     */
    private void fileAlreadyExistsDialog(final String newName) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("recording with given name \"" + newName + "\" already exists." +
                "\nChoose different name.");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    /**
     * Update Expandable list's indicator.
     */
    private void refreshExpandableListGroupIndicator() {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f;
        float y = 0.0f;
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        );
        // Dispatch touch event to view
        expListView.dispatchTouchEvent(motionEvent);
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
                pausePlayingRecording();
            } else if (action.equals(getString(R.string.stop))) {
                stopPlayingRecording();
            } else if (action.equals(getString(R.string.play))) {
                playRecordingViaNotificationBarOrImg();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //clear notification after user exits activity.
        notificationManager.cancel(appNotification.getNotificationActivityId());
        notificationManager.deleteNotificationChannel(appNotification.getChannelId());
        appNotification.unregisterReceiver();
    }
}

