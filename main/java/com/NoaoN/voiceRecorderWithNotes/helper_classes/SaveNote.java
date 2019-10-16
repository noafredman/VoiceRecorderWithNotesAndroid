package com.NoaoN.voiceRecorderWithNotes.helper_classes;

import com.NoaoN.voiceRecorderWithNotes.components.LinkLinedView;
import com.NoaoN.voiceRecorderWithNotes.components.TimestampDeleteNote;
import com.NoaoN.voiceRecorderWithNotes.list_adapter.ExpandableListAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SaveNote {

    /**
     * Saves note file to memory.
     */
    public static void writeToFile(int groupPosition, File root, String fileNameToSave,
                             ExpandableListAdapter ela) {
        try {
            String note = "" , s = "";
            int childrenSize = (ela == null) ? 0 : ela.getChildrenCount(groupPosition);
            File fileNote = new File(root, "/" + fileNameToSave);
            FileWriter writer = new FileWriter(fileNote);
            for (int k = 0; k < childrenSize; k++) {
                //get k'th note
                List<LinkLinedView> linkLinedViewList = (List<LinkLinedView>) ela.getChild(groupPosition, k);
                if(linkLinedViewList != null){
                    LinkLinedView linkLV = linkLinedViewList.get(k);
                    TimestampDeleteNote ts = linkLV.getTS();
                    //get timeStamp text.
                    note += ((ts.getTimeTV()).getText().toString()) + ("\n");
                    //get note.
                    if ( linkLV.getLinedET() != null && (linkLV.getLinedET()).getText() != null) {
                        s = ((linkLV.getLinedET()).getText().toString());
                    }
                    note += (s);
                    if (k + 1 < childrenSize) {
                        note += ("\n");
                    }
                }
            }
            writer.append(note);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
