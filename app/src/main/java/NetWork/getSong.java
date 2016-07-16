package NetWork;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;

import trevx.MainActivity;
import trevx.Search.Search_Fragment;
import trevx.Song_Manager.Song;

/**
 * Created by ptk on 7/13/16.
 */
public class getSong {
    private static final String TAG = "trevx api";
    public static LinkedList<Song> song_list;
    String query;
    JsonArrayRequest get_jsonarray_Song;
    getSong getSong;


    public void get_song(String query) throws UnsupportedEncodingException, MalformedURLException {
        if (null != get_jsonarray_Song)
            AppController.getInstance().cancelPendingRequests(get_jsonarray_Song);

        this.query = query.replace(" ", "-");
        song_list = new LinkedList<>();
        Log.d(TAG, "inside api" + "http://trevx.com/v1/" + URLEncoder.encode(this.query, "UTF-8") + "/20/20/?format=json");
        URL url = new URL("http://trevx.com/v1/" + URLEncoder.encode(query, "UTF-8") + "/0/20/?format=json");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(query, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                try {
                    Reader_Json(jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);


    }

    public void get_more_song() throws UnsupportedEncodingException, MalformedURLException {
        if (null == query)
            query = MainActivity.queryy;
        int current = song_list.size();

        URL url = new URL("http://trevx.com/v1/" + URLEncoder.encode(query, "UTF-8") + "/" + current + "/20/?format=json");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(query, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                try {
                    Reader_Json(jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);
    }


    private void Reader_Json(JSONArray json) throws JSONException {
        JSONObject results;
        String size = json.get(json.length() - 6).toString().replace("resultCounter:", "");
        Search_Fragment.Song_count = Integer.valueOf(size);
        song_list = new LinkedList<>();
        Log.d("query song count", Integer.valueOf(size) + "");

        for (int i = 0; i < (json.length() - 5); i++) {

            ;
            if (i < json.length() - 7) {
                results = json.getJSONObject(i);
                song_list.add(new Song(results.get("title").toString(), results.get("id").toString(), results.get("image").toString(), results.getString("link").toString()));
            } else {


            }

        }


    }
}
