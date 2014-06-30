package ru.rzn.gmyasoedov.MAtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.rzn.gmyasoedov.MAtest.bean.Marker;
import ru.rzn.gmyasoedov.MAtest.bean.RequestResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Async task for get request to url
 */
public class AsyncGetRequest extends AsyncTask<Void, Void, RequestResult> {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.9.2.24) Gecko/20111103 Firefox/3.6.24 ( .NET CLR 3.5.30729)";
    private static final String TAG = AsyncGetRequest.class.getSimpleName();

    private AsyncHandler<List<Marker>> handler;
    private String url;
    private Context context;
    private ProgressDialog dialog;

    public AsyncGetRequest(Context context, String url, AsyncHandler<List<Marker>> handler) {
        this.handler = handler;
        this.url = url;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        try {
            dialog = ProgressDialog.show(context, "", "Loading");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected RequestResult doInBackground(Void... params) {
        HttpGet httpRequest = new HttpGet(url);
        RequestResult result = new RequestResult();
        AndroidHttpClient client = AndroidHttpClient.newInstance(USER_AGENT, context);
        try {
            result.setResponse(client.execute(httpRequest, new BasicResponseHandler()));
        } catch (IOException e) {
            result.setException(e);
        } finally {
            client.close();
        }
        return result;
    }

    @Override
    protected void onPostExecute(RequestResult response) {
        try {
            dialog.dismiss();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        if (response.getResponse() != null && handler != null) {
            List<Marker> markers = new ArrayList<Marker>();
            try {
                JSONObject jsonObject = new JSONObject(response.getResponse());
                JSONArray jsonArray = jsonObject.getJSONObject("response")
                        .getJSONObject("GeoObjectCollection").getJSONArray("featureMember");
                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject markerJSON = jsonArray.getJSONObject(i).getJSONObject("GeoObject");
                    Marker marker = new Marker();
                    marker.setName(markerJSON.getString("name"));
                    marker.setDescription(markerJSON.getString("description"));
                    String coordinate[] = markerJSON.getJSONObject("Point").getString("pos").split(" ");
                    marker.setLatitude(Double.valueOf(coordinate[1]));
                    marker.setLongitude(Double.valueOf(coordinate[0]));
                    markers.add(marker);
                }
                handler.onSuccess(markers);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                handler.onError(e);
            }
        } else if (response.getException() != null && handler != null) {
            handler.onError(response.getException());
        }
    }


}
