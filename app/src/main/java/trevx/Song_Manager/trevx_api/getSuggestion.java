package trevx.Song_Manager.trevx_api;

import android.os.AsyncTask;
import android.util.Log;

import trevx.ListViewAdapter;
import trevx.SearchStaticListSupportFragment;

/**
 * Created by ptk on 7/1/16.
 */
public class getSuggestion extends AsyncTask<String, Void, String> {

    private static final String TAG = "get Song";

    @Override
    protected void onPreExecute() {

        // MainActivity.progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    protected String doInBackground(String... o) {

        try {

            Log.d(TAG, "inside thread user enter " + o[0]);
            Suggesion.get_song_api(o[0]);


        } catch (Exception e) {
            Thread.interrupted();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {

        SearchStaticListSupportFragment.adapter = new ListViewAdapter(SearchStaticListSupportFragment.context, Suggesion.Suggestionword);
        SearchStaticListSupportFragment.listView.setAdapter(SearchStaticListSupportFragment.adapter);
        Log.d(TAG, "Result" + Suggesion.Suggestionword.toString());
        SearchStaticListSupportFragment.adapter.notifyDataSetChanged();
//            if (null != trevx_api.song_list && trevx_api.song_list.size() > 0 && null != searchFragment)
        // if (MainActivity.searchFragment != null) {
        //    MainActivity.searchFragment.setadapter1();


        //else
        {

        }
        //    MainActivity.progressBar.setVisibility(View.GONE);

    }
}