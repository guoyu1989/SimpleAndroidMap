package com.jiraiyeah.simpleandroidmap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import com.jiraiyeah.simpleandroidmap.utils.PlaceDetailsJSONParser;
import com.jiraiyeah.simpleandroidmap.utils.PlaceJSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

public class MapPlaceProvider extends ContentProvider {

  public static final String AUTHORITY = "com.jiraiyeah.simpleandroidmap.MapPlaceProvider";
  public static final String SERVER_KEY = "YOUR_SERVER_KEY";

  public static final String PLACE_LIST_PATH = "place_list";
  public static final String DETAILS_PATH = "details";
  public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
  public static final Uri PLACE_LIST_URI =
      BASE_CONTENT_URI.buildUpon().appendPath(PLACE_LIST_PATH).build();
  public static final Uri DETAILS_URI =
      BASE_CONTENT_URI.buildUpon().appendPath(DETAILS_PATH).build();

  public static final String KEY_NAME = "name";
  public static final String KEY_ADDRESS = "address";
  public static final String KEY_REFERENCE = "reference";
  public static final String KEY_LATITUDE = "latitude";
  public static final String KEY_LONGITUDE = "longitude";

  private static final int PLACE_LIST = 1;
  private static final int DETAILS = 2;
  private static final String TAG = "com.jiraiyeah.simpleandroidmap.MapPlaceProvider";

  // Obtain browser key from https://code.google.com/apis/console
  String mKey = String.format("key=%s", SERVER_KEY);

  // Defines a set of uris allowed with this content provider
  private static final UriMatcher mUriMatcher = buildUriMatcher();

  private static UriMatcher buildUriMatcher() {

    UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    uriMatcher.addURI(AUTHORITY, PLACE_LIST_PATH, PLACE_LIST);
    uriMatcher.addURI(AUTHORITY, DETAILS_PATH, DETAILS);

    return uriMatcher;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sortOrder) {
    String jsonString = "";
    String jsonPlaceDetails = "";

    MatrixCursor matrixCursor = null;

    switch (mUriMatcher.match(uri)) {
      case PLACE_LIST: {
        matrixCursor = new MatrixCursor(new String[]{
            "_id", KEY_NAME, KEY_ADDRESS, KEY_REFERENCE});

        // Get Places from Google Places API
        jsonString = getPlaces(selectionArgs);

        try {
          // Parse the places ( JSON => List )
           List<HashMap<String, String>> list = PlaceJSONParser.parse(new JSONObject(jsonString));

          // Creating cursor object with places
          for (int i = 0; i < list.size(); i++) {
            HashMap<String, String> hMap = list.get(i);

            final String description = hMap.get(PlaceJSONParser.KEY_DESCRIPTION);
            Log.d(TAG, description);
            String name = description;
            String address = "";
            if (description.contains(",")) {
              name = description.substring(0, description.indexOf(","));
              address = description.substring(description.indexOf(",") + 1);
            }
            // Adding place details to cursor
            matrixCursor.addRow(new String[]{
                Integer.toString(i), name, address, hMap.get(PlaceJSONParser.KEY_REFERENCE)});
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
        break;
      }
      case DETAILS: {
        // Defining a cursor object with columns description, lat and lng
        matrixCursor = new MatrixCursor(new String[]{KEY_NAME, KEY_LATITUDE, KEY_LONGITUDE});

        jsonPlaceDetails = getPlaceDetails(selectionArgs[0]);
        List<HashMap<String, String>> detailsList = null;
        try {
          detailsList = PlaceDetailsJSONParser.parse(new JSONObject(jsonPlaceDetails));
        } catch (JSONException e) {
          e.printStackTrace();
        }
        if (detailsList != null) {
          for (int j = 0; j < detailsList.size(); j++) {
            HashMap<String, String> hMapDetails = detailsList.get(j);
            matrixCursor.addRow(new String[]{hMapDetails.get(KEY_NAME), hMapDetails.get("lat"),
                hMapDetails.get("lng")});
          }
        }
        break;
      }
    }
    return matrixCursor;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public boolean onCreate() {
    return false;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
                    String[] selectionArgs) {
    return 0;
  }

  /** A method to download json data from url */
  private String downloadUrl(String strUrl) throws IOException{
    String data = "";
    InputStream iStream = null;
    HttpURLConnection urlConnection = null;
    try{
      URL url = new URL(strUrl);

      // Creating an http connection to communicate with url
      urlConnection = (HttpURLConnection) url.openConnection();

      // Connecting to url
      urlConnection.connect();

      // Reading data from url
      iStream = urlConnection.getInputStream();

      BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

      StringBuffer sb = new StringBuffer();

      String line = "";
      while( ( line = br.readLine()) != null){
        sb.append(line);
      }

      data = sb.toString();

      br.close();

    }catch(Exception e){
      Log.d("Exception while downloading url", e.toString());
    } finally {
      iStream.close();
      urlConnection.disconnect();
    }
    return data;
  }

  private String getPlaceDetailsUrl(String ref){

    // reference of place
    String reference = "reference="+ref;

    // Sensor enabled
    String sensor = "sensor=false";

    // Building the parameters to the web service
    String parameters = reference+"&"+sensor+"&"+mKey;

    // Output format
    String output = "json";

    // Building the url to the web service
    String url = "https://maps.googleapis.com/maps/api/place/details/"+output+"?"+parameters;

    return url;
  }

  private String getPlacesUrl(String qry){

    try {
      qry = "input=" + URLEncoder.encode(qry, "utf-8");
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    }

    // Sensor enabled
    String sensor = "sensor=false";

    // Building the parameters to the web service
    String parameters = qry+"&"+sensor+"&"+mKey;

    // Output format
    String output = "json";
    // Building the url to the web service
    String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;
    return url;
  }

  private String getPlaces(String[] params){
    // For storing data from web service
    String data = "";
    String url = getPlacesUrl(params[0]);
    try{
      // Fetching the data from web service in background
      data = downloadUrl(url);
    }catch(Exception e){
      Log.d("Background Task",e.toString());
    }
    return data;
  }

  private String getPlaceDetails(String reference){
    String data = "";
    String url = getPlaceDetailsUrl(reference);
    try {
      data = downloadUrl(url);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return data;
  }
}

