package trevx.Song_Manager;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import trevx.MainActivity;
import trevx.Song_Manager.trevx_api.trevx_api;

/**
 * Created by ptk on 6/19/16.
 */
public class get_Song_trevx extends AsyncTask<String, Void, String> {

    private static final String TAG = "get Song";

    @Override
    protected void onPreExecute() {

        MainActivity.progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    protected String doInBackground(String... arrayLists) {

        try {

            Log.d(TAG, "inside thread user enter " + MainActivity.queryy);
            trevx_api.trevxApi.get_song_api(MainActivity.queryy);


        } catch (Exception e) {
            Thread.interrupted();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {

//            if (null != trevx_api.song_list && trevx_api.song_list.size() > 0 && null != searchFragment)
       if(MainActivity.searchFragment !=null)
        {
            MainActivity.searchFragment.setadapter2(true);

        }
        //else
        {

        }
        MainActivity.progressBar.setVisibility(View.GONE);

    }


}

