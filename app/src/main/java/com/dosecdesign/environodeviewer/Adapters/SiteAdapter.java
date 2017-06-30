package com.dosecdesign.environodeviewer.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dosecdesign.environodeviewer.Model.JsonModel;
import com.dosecdesign.environodeviewer.R;

import java.util.List;

/**
 * Created by Michi on 5/05/2017.
 */

public class SiteAdapter extends BaseAdapter{

    private List<String> mDataItem;
    private Context mContext;
    private LayoutInflater mInflator;

    public SiteAdapter(Context mContext, List<String> mDataItem) {
        this.mContext = mContext;
        this.mDataItem = mDataItem;
    }


    @Override
    public int getCount() {
        return mDataItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.search_list_item, parent, false);

        String item = mDataItem.get(position);


        final TextView siteName = (TextView) rowView.findViewById(R.id.searchListItem);

        siteName.setText(item);



        return rowView;
    }
}
