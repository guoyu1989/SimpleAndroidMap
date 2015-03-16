package com.jiraiyeah.simpleandroidmap.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceJSONParser {

  public static final String KEY_PREDICTIONS = "predictions";
  public static final String KEY_DESCRIPTION = "description";
  public static final String KEY_REFERENCE = "reference";
  public static final String KEY_ID = "_id";

  /** Receives a JSONObject and returns a list */
  public static List<HashMap<String,String>> parse(JSONObject jObject){

    JSONArray jPlaces = null;
    try {
      /** Retrieves all the elements in the 'places' array */
      jPlaces = jObject.getJSONArray(KEY_PREDICTIONS);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    /** Invoking getPlaces with the array of json object
     * where each json object represent a place
     */
    return getPlaces(jPlaces);
  }

  private static List<HashMap<String, String>> getPlaces(JSONArray jPlaces){
    int placesCount = jPlaces.length();
    List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
    HashMap<String, String> place = null;

    /** Taking each place, parses and adds to list object */
    for(int i=0; i<placesCount;i++){
      try {
        /** Call getPlace with place JSON object to parse the place */
        place = getPlace((JSONObject)jPlaces.get(i));
        placesList.add(place);

      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    return placesList;
  }

  /** Parsing the Place JSON object */
  private static HashMap<String, String> getPlace(JSONObject jPlace){

    HashMap<String, String> place = new HashMap<String, String>();

    String id="";
    String reference="";
    String description="";

    try {

      description = jPlace.getString(KEY_DESCRIPTION);
      id = jPlace.getString("id");
      reference = jPlace.getString(KEY_REFERENCE);

      place.put(KEY_DESCRIPTION, description);
      place.put(KEY_ID,id);
      place.put(KEY_REFERENCE,reference);

    } catch (JSONException e) {
      e.printStackTrace();
    }
    return place;
  }
}
