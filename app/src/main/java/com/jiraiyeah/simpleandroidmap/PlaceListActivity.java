package com.jiraiyeah.simpleandroidmap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class PlaceListActivity
    extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

  public static final String EXTRA_REFERENCE = "extra_reference";

  private static final String KEY_PLACE_ENTRY = "place_entry";
  private static final String KEY_SEARCH_TEXT = "search_text";
  private static final String KEY_SEARCH_BAR_OPEN = "search_bar_open";
  private static final String KEY_REFERENCE = "reference";
  private static final String KEY_MAP_OPEN = "map_open";
  private static final String KEY_MAP_PLACE_ENTRY = "map_place_entry";

  private static final int PLACE_LOADER = 0;
  private static final int MAP_LOADER = 1;

  private Drawable mIconOpenSearch;
  private Drawable mIconCloseSearch;
  private PlaceListAdapter mAdapter;
  private MenuItem mSearchAction;
  private GoogleMap mGoogleMap;
  private ListView mLvPlaceEntries;
  private SupportMapFragment mMapFragment;

  private boolean mSearchBarOpen;
  private String mSearchText;
  private ArrayList<PlaceEntry> mPlaceEntries;
  private PlaceEntry mMapPlaceEntry;
  private boolean mMapOpen;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_place_list);

    mIconOpenSearch = getResources()
        .getDrawable(android.R.drawable.ic_menu_search);
    mIconCloseSearch = getResources()
        .getDrawable(R.drawable.ic_menu_close);

    mPlaceEntries = new ArrayList<>();
    if (savedInstanceState != null) {
      mMapOpen = savedInstanceState.getBoolean(KEY_MAP_OPEN);
      mMapPlaceEntry = savedInstanceState.getParcelable(KEY_MAP_PLACE_ENTRY);
      mSearchBarOpen = savedInstanceState.getBoolean(KEY_SEARCH_BAR_OPEN);
      if (mSearchBarOpen) {
        mSearchText = savedInstanceState.getString(KEY_SEARCH_TEXT);
        mPlaceEntries = savedInstanceState.getParcelableArrayList(KEY_PLACE_ENTRY);
        openSearchBar(mSearchText);
      }
    }
    if (mAdapter == null) {
      mAdapter = new PlaceListAdapter(this, R.layout.place_list_item, mPlaceEntries);
    }
    mLvPlaceEntries = (ListView) findViewById(R.id.place_list);
    mLvPlaceEntries.setAdapter(mAdapter);

    mLvPlaceEntries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PlaceEntry placeEntry = (PlaceEntry) mLvPlaceEntries.getItemAtPosition(position);
        openMap(placeEntry.reference);
      }
    });

    mMapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    mGoogleMap = mMapFragment.getMap();
    if (mMapOpen) {
      showLocations(mMapPlaceEntry);
    } else {
      hideLocation();
    }
  }

  private void hideLocation() {
    mMapOpen = false;
    mMapPlaceEntry = new PlaceEntry();
    mLvPlaceEntries.setVisibility(View.VISIBLE);
    mMapFragment.getView().setVisibility(View.GONE);
//    getSupportFragmentManager().beginTransaction().hide(mMapFragment).commit();
  }

  private void openMap(final String reference) {
    Bundle args = new Bundle();
    args.putString(KEY_REFERENCE, reference);
    getSupportLoaderManager().restartLoader(MAP_LOADER, args, this);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(KEY_MAP_OPEN, mMapOpen);
    outState.putParcelable(KEY_MAP_PLACE_ENTRY, mMapPlaceEntry);
    outState.putBoolean(KEY_SEARCH_BAR_OPEN, mSearchBarOpen);
    outState.putString(KEY_SEARCH_TEXT, mSearchText);
    outState.putParcelableArrayList(KEY_PLACE_ENTRY, mPlaceEntries);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_place_list, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    mSearchAction = menu.findItem(R.id.action_search);

    if (mSearchBarOpen) {
      mSearchAction.setIcon(mIconCloseSearch);
    } else {
      mSearchAction.setIcon(mIconOpenSearch);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_search) {
      if (mSearchBarOpen) {
        closeSearchBar();
      } else {
        openSearchBar("");
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void closeSearchBar() {
    mSearchAction.setIcon(mIconOpenSearch);
    mPlaceEntries = new ArrayList<>();
    mSearchText = "";
    mSearchBarOpen = false;
    getSupportActionBar().setDisplayShowCustomEnabled(false);
  }

  private void openSearchBar(String s) {
    mSearchText = s;
    mSearchBarOpen = true;

    // Set custom view on action bar.
    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayShowCustomEnabled(true);
    actionBar.setCustomView(R.layout.search_bar);

    // Search edit text field setup.
    EditText etSearch = (EditText) actionBar.getCustomView().findViewById(R.id.etSearch);
    etSearch.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (!mSearchText.equals(s.toString())) {
          mSearchText = s.toString();
          Bundle bundle = new Bundle();
          bundle.putString(KEY_SEARCH_TEXT, s.toString());
          getSupportLoaderManager().restartLoader(PLACE_LOADER, bundle, PlaceListActivity.this);
        }
      }
    });
    etSearch.setText(s);
    etSearch.requestFocus();

    if (mSearchAction != null) {
      mSearchAction.setIcon(mIconCloseSearch);
    }
    mSearchBarOpen = true;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    if (i == PLACE_LOADER) {
      return new CursorLoader(this,
          MapPlaceProvider.PLACE_LIST_URI, null, null,
          new String[]{ bundle.getString(KEY_SEARCH_TEXT) }, null);
    } else if (i == MAP_LOADER) {
      final String reference = bundle.getString(KEY_REFERENCE);
      return new CursorLoader(getBaseContext(), MapPlaceProvider.DETAILS_URI, null, null,
          new String[]{ reference }, null);
    }
    return null;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    switch (cursorLoader.getId()) {
      case PLACE_LOADER: {
        hideLocation();
        mPlaceEntries = getPlaceEntriesFromCursor(cursor);
        mAdapter.setPlaceEntries(mPlaceEntries);
        break;
      }
      case MAP_LOADER: {
        while(cursor.moveToNext()) {
          PlaceEntry placeEntry = new PlaceEntry();
          placeEntry.lat = Float.valueOf(
              cursor.getString(cursor.getColumnIndex(MapPlaceProvider.KEY_LATITUDE)));
          placeEntry.lng = Float.valueOf(
              cursor.getString(cursor.getColumnIndex(MapPlaceProvider.KEY_LONGITUDE)));
          placeEntry.name = cursor.getString(cursor.getColumnIndex(MapPlaceProvider.KEY_NAME));
          showLocations(placeEntry);
        }
        break;
      }
    }
  }

  private ArrayList<PlaceEntry> getPlaceEntriesFromCursor(Cursor cursor) {
    ArrayList<PlaceEntry> placeEntries = new ArrayList<>();
    while (cursor.moveToNext()) {
      PlaceEntry placeEntry = new PlaceEntry();
      placeEntry.name = cursor.getString(cursor.getColumnIndex(MapPlaceProvider.KEY_NAME));
      placeEntry.address = cursor.getString(cursor.getColumnIndex(MapPlaceProvider.KEY_ADDRESS));
      placeEntry.reference =
          cursor.getString(cursor.getColumnIndex(MapPlaceProvider.KEY_REFERENCE));
      placeEntries.add(placeEntry);
    }
    return placeEntries;
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader) {
    mPlaceEntries = new ArrayList<>();
  }

  private void showLocations(PlaceEntry placeEntry) {
    mMapOpen = true;

    mMapPlaceEntry = placeEntry;

    mGoogleMap.clear();
    MarkerOptions markerOptions = new MarkerOptions();
    LatLng position = new LatLng(placeEntry.lat, placeEntry.lng);
    markerOptions.position(position);
    markerOptions.title(placeEntry.name);
    mGoogleMap.addMarker(markerOptions);
    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));

    // Hide the list view
    mLvPlaceEntries.setVisibility(View.GONE);
    // Show the map fragment
    mMapFragment.getView().setVisibility(View.VISIBLE);
    // Hide the keyboard
    EditText etSearch =
        (EditText) getSupportActionBar().getCustomView().findViewById(R.id.etSearch);
    if (etSearch != null) {
      InputMethodManager inputManager =
          (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputManager.hideSoftInputFromWindow(
          etSearch.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
  }
}
