package com.NoaoN.voiceRecorderWithNotes.list_adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.NoaoN.voiceRecorderWithNotes.components.LinkLinedView;
import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapterNoDeleteBtn extends ExpandableListAdapter {

    /***
     * Constructor.
     * @param context - current context.
     * @param listDataHeader - list of group headers.
     * @param hashMapChildData - hash map of list of child views.
     */
    public ExpandableListAdapterNoDeleteBtn(Context context, List<String> listDataHeader,
                                            HashMap<String, List<LinkLinedView>> hashMapChildData) {
        super(context, listDataHeader, hashMapChildData, 1);
        int size = childrenToGroupViewMap.size();
        for(int i = 0; i < size; i ++){
            //size will always be 1. takes care of first timeStamp del btn.
            LinkLinedView view = childrenToGroupViewMap.get(0).get(0);
            view.getTS().getDelBtn().setVisibility(View.GONE);
        }
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        convertView = super.getChildView(groupPosition, childPosition, isLastChild, convertView,
                parent);
        LinkLinedView view = (LinkLinedView)convertView;
        view.getTS().getDelBtn().setVisibility(View.GONE);
        return convertView;
    }


}
