package trevx.Song_Manager;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import trevx.Song_Manager.trevx_api.trevx_api;
import trevx.ptk.an.app.FullscreenActivity;

/**
 * Created by ptk on 7/26/16.
 */
public class trevx_home_api extends AsyncTask<String, Void, String> {

    private static final String TAG = "get home";
    private ProgressDialog dialog;

    @Override
    protected void onPreExecute() {

        FullscreenActivity.progressBar.setVisibility(View.VISIBLE);
     /*   dialog= new ProgressDialog(MainActivity.context);
        dialog.setMessage("Loading...");
        dialog.show();*/
    }


    @Override
    protected String doInBackground(String... arrayLists) {

        try {
            trevx_api.trevxApi = new trevx_api();
            Log.d(TAG, "inside thread user enter ");
            trevx_api.trevxApi.get_home_api();


        } catch (Exception e) {
            Log.d(TAG, "Excpetion insiode thread home" + e.toString());
            Thread.interrupted();
            ;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {


//            if (null != trevx_api.song_list && trevx_api.song_list.size() > 0 && null != searchFragment)

        FullscreenActivity.progressBar.setVisibility(View.GONE);

    }


}

