package ru.rzn.gmyasoedov.MAtest;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

public class MapActivity extends Activity{
    private static final String TAG_MAP_FRAGMENT = "tag-map-fragment";
    private MapFragment mapFragment;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        FragmentManager fm = getFragmentManager();
        mapFragment = (MapFragment) fm.findFragmentByTag(TAG_MAP_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mapFragment == null) {
            mapFragment = new MapFragment();
            fm.beginTransaction().add(R.id.fragment_container, mapFragment, TAG_MAP_FRAGMENT).commit();
        }
    }


}
