package ru.rzn.gmyasoedov.MAtest;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import ru.rzn.gmyasoedov.MAtest.bean.Marker;
import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.overlay.balloon.BalloonItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;

import java.util.List;

/**
 * Map fragment
 */
public class MapFragment extends Fragment implements AsyncHandler<List<Marker>>{
    private static final String TAG = MapFragment.class.getSimpleName();
    private static final long RENDER_TIMEOUT = 100L;
    private static final double OFFSET = 5d;

    private String url[] = {"http://geocode-maps.yandex.ru/1.x/?format=json&geocode=Москва",
            "http://geocode-maps.yandex.ru/1.x/?format=json&geocode=Рязань",
            "http://geocode-maps.yandex.ru/1.x/?format=json&geocode=Касимов"};
    private MapController mapController;
    private Overlay currentOverlay;
    private List<Marker> markers;
    private int currentUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
        new AsyncGetRequest(getActivity(), url[currentUrl], this).execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment, container, false);
        MapView mapView = (MapView) view.findViewById(R.id.map);
        mapController = mapView.getMapController();
        mapController.getOverlayManager().getMyLocation().setEnabled(false);
        return view;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (currentUrl >= 2) {
                    currentUrl = 0;
                } else {
                    currentUrl++;
                }
                new AsyncGetRequest(getActivity(), url[currentUrl], this).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (markers != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        //need time to map render
                        Thread.sleep(RENDER_TIMEOUT);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    onSuccess(markers);
                }
            }.execute();
        }
    }

    @Override
    public void onSuccess(List<Marker> result) {
        //remove old overlay
        if (currentOverlay != null) {
            mapController.getOverlayManager().removeOverlay(currentOverlay);
        }

        markers = result;
        Resources res = getResources();

        //create new overlay
        currentOverlay = new Overlay(mapController);
        for(Marker marker : result) {
            OverlayItem overlayItem = new OverlayItem(new GeoPoint(marker.getLatitude(), marker.getLongitude()),
                    res.getDrawable(R.drawable.a));
            BalloonItem balloon = new BalloonItem(getActivity(), overlayItem.getGeoPoint());
            balloon.setText(marker.getName() + " " + marker.getDescription());
            overlayItem.setBalloonItem(balloon);
            currentOverlay.addOverlayItem(overlayItem);
        }
        mapController.getOverlayManager().addOverlay(currentOverlay);
        // auto zoom - all pins visible
        setZoomSpan(currentOverlay, mapController);
    }

    @Override
    public void onError(Exception e) {
        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
    }

    /**
     * Set zoom for map
     * @param overlay overlay with items for zoom
     * @param mapController mapController
     */
    private void setZoomSpan(Overlay overlay, MapController mapController){
        List<OverlayItem> list = overlay.getOverlayItems();
        double maxLat, minLat, maxLon, minLon;
        maxLat = maxLon = Double.MIN_VALUE;
        minLat = minLon = Double.MAX_VALUE;
        for (OverlayItem item : list){
            GeoPoint geoPoint = item.getGeoPoint();
            double lat = geoPoint.getLat();
            double lon = geoPoint.getLon();

            maxLat = Math.max(lat, maxLat);
            minLat = Math.min(lat, minLat);
            maxLon = Math.max(lon, maxLon);
            minLon = Math.min(lon, minLon);
        }
        double offset = Math.max((maxLat - minLat) / OFFSET, (maxLon - minLon) / OFFSET);

        maxLat += offset;
        minLat -= offset;

        mapController.setZoomToSpan(maxLat - minLat, maxLon - minLon);
        mapController.setPositionAnimationTo(new GeoPoint((maxLat + minLat)/2, (maxLon + minLon)/2));
    }
}
