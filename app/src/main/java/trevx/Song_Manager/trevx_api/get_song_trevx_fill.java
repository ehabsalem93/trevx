package trevx.Song_Manager.trevx_api;

/**
 * Created by ptk on 7/4/16.
 */

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import trevx.MainActivity;

public class get_song_trevx_fill extends AsyncTask<String, Void, String> {

    private static final String TAG = "get Song";

    @Override
    protected void onPreExecute() {

        MainActivity.progressBar.setVisibility(View.VISIBLE);

    }


    @Override
    protected String doInBackground(String... arrayLists) {

        try {


            Log.d(TAG, "inside thread user enter " + MainActivity.queryy);
            trevx_api.trevxApi.get_song_api(MainActivity.queryy,true);


        } catch (Exception e) {
            Thread.interrupted();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {

//            if (null != trevx_api.song_list && trevx_api.song_list.size() > 0 && null != searchFragment)

        //else
        {

        }
        MainActivity.progressBar.setVisibility(View.GONE);

    }


}

