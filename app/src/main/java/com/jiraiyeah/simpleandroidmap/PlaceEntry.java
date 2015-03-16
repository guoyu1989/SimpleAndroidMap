package com.jiraiyeah.simpleandroidmap;

import android.os.Parcel;
import android.os.Parcelable;

public class PlaceEntry implements Parcelable {
  public String name = "";
  public String address = "";
  public String reference = "";
  public float lat = 0;
  public float lng = 0;

  public PlaceEntry() {}

  private PlaceEntry(Parcel in) {
    name = in.readString();
    address = in.readString();
    reference = in.readString();
    lat = in.readFloat();
    lng = in.readFloat();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeString(address);
    dest.writeString(reference);
    dest.writeFloat(lat);
    dest.writeFloat(lng);
  }

  public static final Creator<PlaceEntry> CREATOR
      = new Creator<PlaceEntry>() {
    public PlaceEntry createFromParcel(Parcel in) {
      return new PlaceEntry(in);
    }
    public PlaceEntry[] newArray(int size) {
      return new PlaceEntry[size];
    }
  };

  @Override
  public String toString() {
    return String.format("%s:%s", String.valueOf(lat), String.valueOf(lng));
  }
}
