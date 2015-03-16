package com.jiraiyeah.simpleandroidmap.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceDetailsJSONParser {

  public static final String KEY_RESULT = "result";
  public static final String KEY_NAME = "name";
  public static final String KEY_GEOMETRY = "geometry";
  public static final String KEY_ADDRESS = "location";
  public static final String KEY_LATITUDE = "lat";
  public static final String KEY_LONGITUDE = "lng";
  public static final String KEY_FORMATTED_ADDRESS = "formatted_address";

  /** Receives a JSONObject and returns a list */
  public static List<HashMap<String,String>> parse(JSONObject jObject){

    double lat = 0;
    double lng = 0;
    String formattedAddress = "";
    String name = "";

    HashMap<String, String> hm = new HashMap<String, String>();
    List<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();

    try {
      lat = jObject.getJSONObject(KEY_RESULT).getJSONObject(KEY_GEOMETRY)
          .getJSONObject(KEY_ADDRESS).getDouble(KEY_LATITUDE);
      lng = jObject.getJSONObject(KEY_RESULT).getJSONObject(KEY_GEOMETRY)
          .getJSONObject(KEY_ADDRESS).getDouble(KEY_LONGITUDE);
      formattedAddress = jObject.getJSONObject(KEY_RESULT).getString(KEY_FORMATTED_ADDRESS);
      name = jObject.getJSONObject(KEY_RESULT).getString(KEY_NAME);
    } catch (JSONException e) {
      e.printStackTrace();
    }catch(Exception e){
      e.printStackTrace();
    }

    hm.put(KEY_LATITUDE, Double.toString(lat));
    hm.put(KEY_LONGITUDE, Double.toString(lng));
    hm.put(KEY_FORMATTED_ADDRESS,formattedAddress);
    hm.put(KEY_NAME, name);

    list.add(hm);

    return list;
  }
}
