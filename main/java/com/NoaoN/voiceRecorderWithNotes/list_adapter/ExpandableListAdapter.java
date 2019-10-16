package com.NoaoN.voiceRecorderWithNotes.list_adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.NoaoN.voiceRecorderWithNotes.components.LinedEditText;
import com.NoaoN.voiceRecorderWithNotes.components.LinkLinedView;
import com.NoaoN.voiceRecorderWithNotes.R;
import com.NoaoN.voiceRecorderWithNotes.components.TimestampDeleteNote;


public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private int childCount = -1;
    private Context context;
    private List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<LinkLinedView>> hashMapDataChild;
    private int expandedGroupPosition = -1;
    //childrenToGroupViewMap - maps from group to children list of convertViews.
    protected HashMap<Integer, HashMap<Integer, LinkLinedView>> childrenToGroupViewMap;
    private HashMap<Integer, View> groupViewMap;
    private boolean selectAll = false;
    private int vis = View.INVISIBLE;

    /**
     * Sets hashMapDataChild field.
     * @param hashMapChild - hash map of list of child views.
     */
    public void setChildList(HashMap<String, List<LinkLinedView>> hashMapChild){
        this.hashMapDataChild = hashMapChild;
    }

    /**
     * Sets listDataHeader field.
     * @param listHeader - list of group headers.
     */
    public void setGroupList(List<String> listHeader){
        this.listDataHeader = listHeader;
    }

    /**
     * Get listDataHeader field.
     * @return listDataHeader field.
     */
    public List<String> getGroupList(){
        return this.listDataHeader;
    }

    /***
     * Constructor.
     * @param context - current context.
     * @param listDataHeader - list of group headers.
     * @param hashMapChildData - hash map of list of child views.
     * @param totalOfRecordings - total number of groups to create.
     */
    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<LinkLinedView>>
                                         hashMapChildData, int totalOfRecordings) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.hashMapDataChild = hashMapChildData;
        this.childrenToGroupViewMap = new HashMap<>();
        this.groupViewMap = new HashMap<>();
        fillViewMap();
        if(this.listDataHeader.size() > 0){
            fillGroupViewMap(totalOfRecordings);
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<LinkLinedView> list =  this.hashMapDataChild.get(this.listDataHeader.get(groupPosition));
        if(list != null) {
            return this.hashMapDataChild.get(this.listDataHeader.get(groupPosition));
        }
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        List<LinkLinedView> childText = (List<LinkLinedView>) getChild(groupPosition, childPosition);
        if(-1 == this.childCount) {
            this.childCount = getChildrenCount(groupPosition);
        }
        if (this.childrenToGroupViewMap.get(groupPosition) != null &&
                !Objects.requireNonNull(this.childrenToGroupViewMap.get(groupPosition)).containsKey(childPosition)) {
            //create child (note) view.
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_items, null);
            LinkLinedView llv= convertView
                    .findViewById(R.id.linkLinedV);
            LinedEditText let = childText.get(childPosition).getLinedET();
            let.setView(convertView);
            let.setPosition(childPosition);
            llv.setLinkLinedFields(let);
            TimestampDeleteNote tsLayout = convertView
                    .findViewById(R.id.tsLayout);
            tsLayout.setTimeStampText(childText.get(childPosition).getTimeStampText());
            llv.setTS(tsLayout);
            Objects.requireNonNull(this.hashMapDataChild.get(this.listDataHeader.get(groupPosition))).set(childPosition, llv);
            Objects.requireNonNull(this.childrenToGroupViewMap.get(groupPosition)).put(childPosition, llv);
        }
        return (this.childrenToGroupViewMap.get(groupPosition) != null) ?
                this.childrenToGroupViewMap.get(groupPosition).get(childPosition) : null;
    }



    @Override
    public int getChildrenCount(int groupPosition) {
        if(this.hashMapDataChild == null || this.hashMapDataChild.size() == 0 ||
                this.listDataHeader == null || this.listDataHeader.size() == 0){
            return 0;
        }
        String key = this.listDataHeader.get(groupPosition);
        return this.hashMapDataChild.get(key) == null ? 0 : this.hashMapDataChild.get(key).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        if(this.listDataHeader == null){
            return null;
        }
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if(isExpanded){
            expandedGroupPosition = groupPosition;
        }
        if (!this.groupViewMap.containsKey(groupPosition) ) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, null);
            groupViewMap.put(groupPosition, convertView);
            TextView lblListHeader = convertView
                    .findViewById(R.id.lblListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);
            CheckBox cb = convertView.findViewById(R.id.selectCheckBox);
            cb.setTag(groupPosition);
            cb.setChecked(selectAll);
            cb.setVisibility(vis);
        }else{
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, null);
            TextView lblListHeader =  convertView
                    .findViewById(R.id.lblListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);
            CheckBox cbNew = convertView.findViewById(R.id.selectCheckBox);
            CheckBox cbGroupView;
            LinearLayout ll = (LinearLayout)groupViewMap.get(groupPosition);
            if(ll != null) {
                cbGroupView = ((CheckBox) ll.getChildAt(0));
                cbNew.setChecked(cbGroupView.isChecked());
                cbNew.setVisibility(vis);
            }
            groupViewMap.replace(groupPosition, convertView);
        }
        return groupViewMap.get(groupPosition);
    }

    /**
     * Creates all groups.
     * @param numOfRecordings - number of groups to create.
     */
    private void fillGroupViewMap(int numOfRecordings){
        for(int i = 0; i< numOfRecordings; i++){
            createGroupView(i);
        }
    }

    /**
     * Create group view of given position.
     * @param groupPosition - position of group view to create.
     */
    private void createGroupView(int groupPosition) {
        String headerTitle = (String) getGroup(groupPosition);
        View convertView;
        if (!this.groupViewMap.containsKey(groupPosition) ) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, null);
            TextView lblListHeader = convertView
                    .findViewById(R.id.lblListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);
            CheckBox cb = convertView.findViewById(R.id.selectCheckBox);
            cb.setTag(groupPosition);
            cb.setChecked(selectAll);
            cb.setVisibility(vis);
            groupViewMap.put(groupPosition, convertView);
        }

    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    public int getExpandedGroupPosition(){
        return this.expandedGroupPosition;
    }

    /**
     * Get list of child views of given header.
     * @param keyFileName - header of list to be returned.
     * @return list of child views of given header.
     */
    public List<LinkLinedView> getChildList(String keyFileName){
        return this.hashMapDataChild.get(keyFileName);
    }

    /**
     * Get group (recording) of given header (file name)
     * @param keyFileName - header of group to be returned.
     * @return position of group of given header.
     */
    public int getGroupPosition(String keyFileName){
        int pos = -1;
        if(keyFileName == null){
            return pos;
        }
        if(keyFileName.contains(".txt")){
            keyFileName = keyFileName.substring(0, keyFileName.indexOf(".txt"));

        }
        pos = 0;
        if(this.listDataHeader != null){
            for (String header : this.listDataHeader) {
                if(header.equals(keyFileName)){
                    return pos;
                }
                pos++;
            }
        }
        return pos;
    }


    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void onGroupExpanded(int i) {

    }

    @Override
    public void onGroupCollapsed(int i) {

    }

    @Override
    public long getCombinedChildId(long l, long l1) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long l) {
        return 0;
    }

    /**
     * Creates all the child views (aka notes) of all groups (aka recording).
     */
    private void fillViewMap(){
        int groupPosition, size = this.listDataHeader.size();
        for(groupPosition = 0; groupPosition < size; groupPosition++){
            HashMap<Integer, LinkLinedView> childViews = createChildView(groupPosition);
            this.childrenToGroupViewMap.put(groupPosition, childViews);
        }
    }

    /**
     * Creates all the child views (aka notes) of group (aka recording) at given position/index.
     * @param groupPosition - position of group to create its children views.
     * @return all child views of group at given position.
     */
    private HashMap<Integer, LinkLinedView> createChildView(int groupPosition){
        HashMap<Integer, LinkLinedView> hm = new HashMap<Integer, LinkLinedView>();
        List<LinkLinedView> linkLinedViewList = hashMapDataChild.get(listDataHeader.get(groupPosition));
        if(linkLinedViewList != null) {
            int childPosition = 0;
            for (LinkLinedView llv : linkLinedViewList) {
                LayoutInflater inflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View convertView = inflater.inflate(R.layout.list_items, null);
                ConstraintLayout view = (ConstraintLayout)convertView;
                LinearLayout ll = (LinearLayout)(view).getChildAt(0);
                LinkLinedView llvConView = (LinkLinedView)ll.getChildAt(0);
                LinedEditText let = llv.getLinedET();
                let.setView(convertView);
                let.setPosition(childPosition);
                llvConView.setLinkLinedFields(let);
                if(let.getText() != null) {
                    llvConView.setLinkLinedNoteText(let.getText().toString());
                }
                llvConView.setView(let.getView());
                TimestampDeleteNote tsLayout = llvConView.getTS();
                tsLayout.setTimeStampText(linkLinedViewList.get(childPosition).getTimeStampText());
                llvConView.setTS(tsLayout);
                hm.put(childPosition, llvConView);
                List<LinkLinedView> updateLinkLinedViewList =this.hashMapDataChild.
                        get(this.listDataHeader.get(groupPosition));
                if(updateLinkLinedViewList != null) {
                    updateLinkLinedViewList.set(childPosition, llvConView);
                }
                childPosition++;
            }
        }
        return hm;
    }

    /**
     * Set visibility of all checkboxes to given visibility parameter.
     * @param visibility - set visibility of checkbox.
     */
    public void setGroupCheckboxesVisibility(int visibility){
        int size = groupViewMap.size();
        vis = visibility;
        LinearLayout ll;
        for(int i = 0; i < size; i++){
            ll = (LinearLayout)groupViewMap.get(i);
            if(ll != null) {
                ll.getChildAt(0).setVisibility(visibility);
            }
        }
    }

    /**
     * Select/deselect all checkboxes.
     * @param check - select/deselect checkboxes.
     */
    public void selectAllGroups(boolean check){
        int size = groupViewMap.size();
        selectAll = check;
        for(int i = 0; i < size; i++){
            selectGroup(check, i);
        }
    }

    /**
     * Select/deselect checkbox of group at given index.
     * @param check - select/deselect checkbox.
     * @param groupIndex - group (header/recording) index to select.
     */
    private void selectGroup(boolean check, int groupIndex){
        LinearLayout ll;
        CheckBox cb;
        ll = (LinearLayout)groupViewMap.get(groupIndex);
        if(ll != null) {
            cb = ((CheckBox) ll.getChildAt(0));
            cb.setChecked(check);
            LinearLayout ll_group = (LinearLayout) groupViewMap.get(groupIndex);
            if(ll_group != null) {
                ((CheckBox) ll_group.getChildAt(0)).
                        setChecked(check);
            }
        }
    }

    /**
     * Get the list of recordings user wants to delete.
     * @return list of headers (groups/recordings) to delete.
     */
    public HashMap<Integer, String> getDeleteCheckedGroupsHeaders(){
        int size = groupViewMap.size();
        HashMap<Integer, String> headersToDelete =new HashMap<>();
        LinearLayout ll;
        CheckBox cb;
        String fName;
        TextView tv;
        for(int i = 0; i < size; i++){
            ll = ((LinearLayout)groupViewMap.get(i));
            if(ll != null) {
                cb = ((CheckBox) (ll.getChildAt(0)));
                if (cb.isChecked()) {
                    //recording to be deleted.
                    tv = (TextView) (ll.getChildAt(1));
                    fName = tv.getText().toString();
                    headersToDelete.put(i, fName);
                }
            }
        }
        return headersToDelete;
    }

    /**
     * Get number of groups (headers/recordings) to delete.
     * @return number of groups to delete.
     */
    public int getDeleteCheckedGroupsTotal(){
        int size = groupViewMap.size();
        int count = 0;
        for(int i = 0; i < size; i++){
            if(isGroupChecked(i)){
                count++;
            }
        }
        return count;
    }

    /**
     * Checks if group view (header) at given position is checked.
     * @param groupPosition - group view position.
     * @return true if group is checked - false otherwise.
     */
    private boolean isGroupChecked(int groupPosition){
        LinearLayout ll = (LinearLayout)groupViewMap.get(groupPosition);
        CheckBox cb = ll != null ? (CheckBox)ll.getChildAt(0) : null;
        return cb != null && cb.isChecked();

    }

    /**
     * Updates text of group view (header).
     * @param groupPosition - group view position.
     * @param newHeaderTitle - the new header text.
     */
    public void updateHeaderView(int groupPosition, String newHeaderTitle){
        View convertView = groupViewMap.get(groupPosition);
        if(convertView != null) {
            TextView lblListHeader = convertView
                    .findViewById(R.id.lblListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(newHeaderTitle);
        }
    }

}