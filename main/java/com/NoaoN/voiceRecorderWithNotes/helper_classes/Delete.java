package com.NoaoN.voiceRecorderWithNotes.helper_classes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.Pair;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.NoaoN.voiceRecorderWithNotes.R;
import com.NoaoN.voiceRecorderWithNotes.activities.PlayRecWithNote;
import com.NoaoN.voiceRecorderWithNotes.components.LinedEditText;
import com.NoaoN.voiceRecorderWithNotes.components.LinkLinedView;
import com.NoaoN.voiceRecorderWithNotes.components.TimestampDeleteNote;
import com.NoaoN.voiceRecorderWithNotes.list_adapter.ExpandableListAdapter;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Holds static methods that delete recordings, notes etc.
 */
public class Delete {

    private static final int TEXT_VIEW_INDEX = 0;
    private static final int LINED_EDIT_TEXT_INDEX = 1;

    /**
     * Delete button clicked.
     * Lets user choose recordings to delete, or delete chosen recordings.
     * @param p - PlayRecWithNote Activity instance.
     */
    public static void deleteRecordingsClicked(PlayRecWithNote p){
        ExpandableListView expListView = p.getExpListView();
        ExpandableListAdapter ela = p.getListAdapter();
        expListView.setGroupIndicator(null);
        List<String> l = ela.getGroupList();
        if(l.size() > 0) {
            //recordings available to delete.
            p.setCheckBoxVisible();
            if (p.getCheckBoxVisible()) {
                //user clicked btn first time - let user choose groups to delete.
                deleteOddClick(p);
            } else {
                //user wants to delete - clicked btn again.
                deleteEvenClick(p);
            }
        }else{
            //no recordings.
            Toast.makeText(p.getApplicationContext(),
                    "No Recordings Saved",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Give use option to choose recordings to delete.
     * @param p - PlayRecWithNote Activity instance.
     */
    private static void deleteOddClick(PlayRecWithNote p){
        //change all check boxes to visible.
        //change selectAll textView and checkBox, and keep btn to visible
        final ExpandableListAdapter ela = p.getListAdapter();
        ExpandableListView expListView = p.getExpListView();
        if (p.isExpanded()) {
            //there's an expanded recording.
            //get its position
            int groupPosition = ela.getGroupPosition(p.getFileName());
            if (groupPosition != -1) {
                expListView.collapseGroup(groupPosition);
                boolean ex = expListView.isGroupExpanded(groupPosition);
                if (!ex) {
                    p.ShowRecList();
                }
            }
            if (p.isPlaying()) {
                //if expanded recording is playing - stop it.
                p.stopPlayingRecording();
            }
        }
        //set all checkBoxes check to false, and visibility to Visible
        p.setVisibilityOf_SelectAllTV_CheckBoxes_KeepBtn(View.VISIBLE);
        CheckBox cb = p.findViewById(R.id.selectAllCheckBox);
        //set all checkBoxes check to false
        cb.setChecked(false);
        ela.selectAllGroups(false);
    }

    /**
     * Delete chosen recordings.
     * @param p - PlayRecWithNote Activity instance.
     */
    private static void deleteEvenClick(PlayRecWithNote p){
        HashMap<Integer, String> recHeadersToDelete = p.getListAdapter().
                getDeleteCheckedGroupsHeaders();
        if(recHeadersToDelete.size() > 0) {
            deleteMultipleDialog(p);
        }else{
            p.setCheckBoxVisible();
            Toast.makeText(p.getApplicationContext(),
                    "No Recordings chosen",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show delete recordings dialog.
     * @param p - PlayRecWithNote Activity instance.
     */
    private static void deleteMultipleDialog(final PlayRecWithNote p){
        final ExpandableListAdapter ela = p.getListAdapter();
        final int numOfRecToDelete = ela.getDeleteCheckedGroupsTotal();
        AlertDialog.Builder builder1 = new AlertDialog.Builder(p);
        builder1.setMessage("are you sure you want to delete " + numOfRecToDelete +
                " recordings?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Keep",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        p.setCheckBoxVisible();
                        dialog.dismiss();
                    }
                });

        builder1.setNegativeButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteMultiple(p);
                        //set keep, select all to invisible
                        p.setVisibilityOf_SelectAllTV_CheckBoxes_KeepBtn(View.INVISIBLE);
                        checkNoRecInList(p, numOfRecToDelete);
                        //reset group indicator.
                        //obtain expandableListViewStyle  from theme
                        TypedArray expandableListViewStyle = p.getTheme().obtainStyledAttributes(
                                new int[]{android.R.attr.expandableListViewStyle});
                        //obtain attr from style
                        TypedArray groupIndicator = p.getTheme().obtainStyledAttributes(expandableListViewStyle.
                                getResourceId(0,0),new int[]{android.R.attr.groupIndicator});
                        p.getExpListView().setGroupIndicator(groupIndicator.getDrawable(0));
                        //pop dialog box
                        dialog.dismiss();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                p.setCheckBoxVisible();
            }
        });
        alert11.show();
    }

    /**
     * Delete multiple recordings.
     * @param p - PlayRecWithNote Activity instance.
     */
    private static void deleteMultiple(PlayRecWithNote p) {
        ExpandableListAdapter ela = p.getListAdapter();
        List<String> l = ela.getGroupList();
        HashMap<Integer, String> recHeadersToDelete = ela.getDeleteCheckedGroupsHeaders();
        int size = l.size();
        for (int position = size - 1; position >= 0; position--){
            //iterate through fileList until reach same name as in recAndNote at position.
            if(recHeadersToDelete.containsKey(position)) {
                //need to delete recording in current position
                position = p.getRecPosition(position);
                //get pair to delete from recAndNote
                String fName = getRecAndNoteNameToDelete(p, position);
                setFilesToDelete(p, fName);
                deleteRecordings(p);
            }
        }
    }

    /**
     * Set the name of the file to be deleted.
     * @param p - PlayRecWithNote Activity instance.
     * @param fileToDelete - file to delete.
     */
    private static void setFilesToDelete(PlayRecWithNote p, String fileToDelete) {
        String fName;
        List<String> fileList = p.getFileList();
        //find file to delete
        for (String file : fileList) {
            fName = file.substring(file.lastIndexOf('/') + 1, file.lastIndexOf(
                    '.'));
            if (fName.equals(fileToDelete)) {
                p.setSelectedRecordingToDeleteOrRename(new File(file));
                p.setSelectedRecordingNoteToDeleteOrRename(new File(file.substring(0,
                        file.lastIndexOf('.')).concat(".txt")));
                if (p.isPlaying() || p.isPaused()) {
                    p.stopPlayingRecording();
                }
                break;
            }
        }
    }

    /**
     * Deletes chosen recordings.
     * @param p - PlayRecWithNote Activity instance.
     */
    private static void deleteRecordings(PlayRecWithNote p) {
        p.getSelectedRecordingToDeleteOrRename().delete();
        p.getSelectedRecordingNoteToDeleteOrRename().delete();
        String fName = p.getSelectedRecordingToDeleteOrRename().getName();
        fName = fName.substring(0, fName.indexOf("."));
        p.getRecAndNote().remove(fName);
        p.CreateRecList();
    }

    /**
     * Get name of recording to be deleted.
     * @param p - PlayRecWithNote Activity instance.
     * @param position - position of recording and note to be deleted.
     * @return
     */
    private static String getRecAndNoteNameToDelete(PlayRecWithNote p, int position) {
        String fName = "";
        int count = 0;
        List<Pair<String, List<LinkLinedView>>> recAndNote = p.getRecAndNote();
        for (Pair<String, List<LinkLinedView>> pair : recAndNote) {
            if (count++ == position) {
                //found position of rec and note to be deleted.
                fName = pair.first;
                break;
            }
        }
        return fName;
    }

    /**
     * Updates time lapse, seeker, and text view visibility.
     * @param p - PlayRecWithNote Activity instance.
     * @param numberOfFilesChosen - number of files chosen to delete.
     */
    private static void checkNoRecInList(PlayRecWithNote p,
                                         int numberOfFilesChosen){
        List<String> l = p.getListAdapter().getGroupList();
        if(l.size() == numberOfFilesChosen) {
            //user chose to delete all recordings.
            p.getTimeLapsed().setVisibility(View.GONE);
            p.getSeeker().setVisibility(View.INVISIBLE);
            p.getNoRecInListTextView().setVisibility(View.VISIBLE);
        }
    }

    /**
     * Delete note button clicked.
     * @param view - delete note button clicked.
     * @param p - PlayRecWithNote Activity instance.
     */
    public static void deleteNote(View view, PlayRecWithNote p) {
        if(!p.getDeleteNoteClicked()) {
            p.setDeleteNoteClicked(true);
            LinearLayout ll = (LinearLayout) ((TimestampDeleteNote) ((LinearLayout) (view).
                    getParent()).getParent()).getParent();
            LinedEditText let = ((LinedEditText) ll.getChildAt(LINED_EDIT_TEXT_INDEX));
            int childPos = let.getPosition();
            //get group position.
            int groupPos = p.getListAdapter().getExpandedGroupPosition();
            //get child position.
            TextView tv = ((TextView) ((LinearLayout) (view).getParent()).getChildAt(
                    TEXT_VIEW_INDEX));
            deleteTimeAndNoteAlert(groupPos, childPos, tv.getText().toString(), p);
        }
    }

    /**
     * Delete chosen note alert.
     * @param groupPosition - recording's position of chosen note to delete.
     * @param childPosition - position of chosen note to delete.
     * @param timeStamp - time stamp of chosen note to delete.
     * @param p - PlayRecWithNote Activity instance.
     */
    private static void deleteTimeAndNoteAlert(final int groupPosition, final int childPosition,
                                               String timeStamp, final PlayRecWithNote p) {
        final ExpandableListView expListView = p.getExpListView();
        AlertDialog.Builder builder1 = new AlertDialog.Builder(p);
        builder1.setMessage("are you sure you want to delete time stamp " + timeStamp + " and its note?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Keep",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        String fileToDelete = p.getFileNameToSave().substring(0,
                                p.getFileNameToSave().indexOf(".txt"));
                        List<LinkLinedView> l =
                                p.getListDataChild().get(fileToDelete);
                        if(l != null){
                            //remove chosen note from list.
                            l.remove(childPosition);
                        }
                        //save notes
                        SaveNote.writeToFile(groupPosition, p.getRoot(), p.getFileNameToSave(),
                                p.getListAdapter());
                        //update list after removed deleted note.
                        p.updateList();
                        expListView.expandGroup(groupPosition);
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                //set to false - next time delete note button pressed then show alert.
                p.setDeleteNoteClicked(false);
            }
        });
        alert11.show();
    }
}
