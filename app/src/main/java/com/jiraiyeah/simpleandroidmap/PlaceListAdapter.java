package com.jiraiyeah.simpleandroidmap;

import android.content.Context;
import android.content.pm.LabeledIntent;
import android.database.Cursor;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PlaceListAdapter extends ArrayAdapter<PlaceEntry> {

  private Context mContext;
  private List<PlaceEntry> mPlaceEntries;

  public PlaceListAdapter(
      Context context, int resource, List<PlaceEntry> placeEntries) {
    super(context, resource, placeEntries);
    mContext = context;
    mPlaceEntries = placeEntries;
  }

  @Override
  public int getCount() {
    return mPlaceEntries.size();
  }

  @Override
  public PlaceEntry getItem(int position) {
    return mPlaceEntries.get(position);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      convertView = LayoutInflater.from(mContext).inflate(R.layout.place_list_item, parent, false);
      viewHolder = new ViewHolder();
      viewHolder.tvName = (TextView) convertView.findViewById(R.id.place_name);
      viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.place_address);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    PlaceEntry placeEntry = getItem(position);
    viewHolder.placeEntry = placeEntry;
    viewHolder.tvName.setText(placeEntry.name);
    viewHolder.tvAddress.setText(placeEntry.address);

    return convertView;
  }

  public void setPlaceEntries(ArrayList<PlaceEntry> placeEntries) {
    mPlaceEntries = placeEntries;
    notifyDataSetChanged();
  }

  private static class ViewHolder {
    TextView tvName;
    TextView tvAddress;
    PlaceEntry placeEntry;
  }
}
