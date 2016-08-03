package trevx.Song_Manager.trevx_api;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import trevx.Search.Search_Fragment;
import trevx.Song_Manager.Song;
import trevx.trevx_home_api.home_artist;
import trevx.trevx_home_api.home_category;
import trevx.trevx_home_api.home_song;
import trevx.trevx_home_api.home_word_discovery;


/**
 *
 * @author ptk
 */
public class trevx_api implements Serializable{

    private static final String TAG = "trevx api";
    public static LinkedList<Song> song_list;
    public static trevx_api trevxApi;
    public static JSONArray Tag_Json_Array;

    public trevx_api() {
    }

    public static LinkedList<Song> getSong_list() {
        return song_list;
    }

    public static void setSong_list(LinkedList<Song> song_list) {
        trevx_api.song_list = song_list;
    }

    public static trevx_api getTrevxApi() {
        return trevxApi;
    }

    public static void setTrevxApi(trevx_api trevxApi) {
        trevx_api.trevxApi = trevxApi;
    }

    public static void get_song_api(String query) throws IOException{
    query=query.replace(" ","-");
        song_list=new LinkedList<>();
        Log.d(TAG, "inside api" + "http://trevx.com/v1/" + URLEncoder.encode(query, "UTF-8") + "/20/20/?format=json");
        URL url = new URL("http://trevx.com/v1/"+ URLEncoder.encode(query, "UTF-8")+"/0/20/?format=json");
       // URL url = new URL("http://trevx.com/v1/"+query+"/1/40/?format=json");
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10000);
        // connection.setRequestProperty("Authorization", "Basic " + );
        String inputLine;
        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);

        }
         Reader_Json(response);
    }

    public static void get_song_api(String query ,boolean fill) throws IOException{
        query=query.replace(" ","-");
        //
        int current =song_list.size();
     //

        Log.d(TAG,"Size of song before loading is "+song_list.size());
       // Log.d(TAG, "inside api" + "http://trevx.com/v1/" + URLEncoder.encode(query, "UTF-8") + "/20/20/?format=json");
        URL url = new URL("http://trevx.com/v1/" + URLEncoder.encode(query, "UTF-8") + "/" + current + "/20/?format=json");

       Log.d(TAG,"Loading uyrls     "+url);

        // URL url = new URL("http://trevx.com/v1/"+query+"/1/40/?format=json");
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10000);
        // connection.setRequestProperty("Authorization", "Basic " + );
        String inputLine;
        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);

        }
        Log.d(TAG,"response is"+response.toString());

        Reader_Json(response);
    }

    public static void get_tag_api() throws IOException {
        //
        URL url = new URL("http://trevx.com/discover-api.php?type=search&lan=en&country=jo&order=random&limit=20");

        Log.d(TAG, "Loading tags.......");

        // URL url = new URL("http://trevx.com/v1/"+query+"/1/40/?format=json");
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10000);
        // connection.setRequestProperty("Authorization", "Basic " + );
        String inputLine;
        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);

        }
        Log.d(TAG, "response is" + response.toString());

        Reader_tag_json(response);
    }




    public static void Reader_Json(StringBuilder JSON_Builder) {
       // ArrayList<Song> song_list=new ArrayList<>();
        try {
            JSONArray json = new JSONArray(JSON_Builder.toString());

            JSONObject results ;
            String size=json.get(json.length()-6).toString().replace("resultCounter:","");
            Search_Fragment.Song_count=Integer.valueOf(size);

            Log.d("query song count",Integer.valueOf(size)+"");

            for(int i=0;i<(json.length()-5);i++){

              ;if(i<json.length()-7) {
                    results = json.getJSONObject(i);
                    song_list.add(new Song(results.get("title").toString(), results.get("id").toString(), results.get("image").toString(), results.getString("link").toString()));
                } else {


                }

            }

            Log.d(TAG, "song list after loading are" + song_list.size() + "   " + song_list.toString());

        } catch (JSONException ex) {
            Logger.getLogger(trevx_api.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void Reader_tag_json(StringBuilder JSON_Builder) {
        //ArrayList<Song> song_list=new ArrayList<>();
        try {

            search_tag.searched_tag_list = new LinkedList<>();
            JSONArray json = new JSONArray(JSON_Builder.toString());
            Tag_Json_Array = json;
            JSONObject results;

            for (int i = 0; i < (json.length()); i++) {

                ;
                if (i < json.length()) {
//                    results = json.getJSONObject(i);
                    search_tag.searched_tag_list.add(json.get(i).toString());
                    Log.d(TAG, "trevxx  " + json.get(i).toString());
                    //   song_list.add(new Song(results.get("title").toString(), results.get("id").toString(), results.get("image").toString(), results.getString("link").toString()));
                }else{


                }

            }

            Log.d(TAG,"song list after loading are"+song_list.size()+"   "+song_list.toString());

        } catch (JSONException ex) {
            Logger.getLogger(trevx_api.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void get_home_api() {
        Log.d(TAG, "start loading url .......");
        URL url = null;
        try {
            url = new URL("http://trevx.com/discover-api.php?type=all&lan=en&country=jo&order=random&categories_limit=6&songs_limit=6&artists_limit=6&world_discover_limit=6");

            Log.d(TAG, "Loading home content through intenrnet connection .......");

            // URL url = new URL("http://trevx.com/v1/"+query+"/1/40/?format=json");
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            // connection.setRequestProperty("Authorization", "Basic " + );
            String inputLine;
            StringBuilder response = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);

            }
            Log.d(TAG, "response is" + response.toString());

            Reader_home_json(response);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG, "home -> Malformed Excpetion is " + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "home -> IOExcpetion is " + e.toString());
        }


    }

    private static void Reader_home_json(StringBuilder response) {

        try {
            JSONObject jsonObject = new JSONObject(String.valueOf(response));
            JSONArray songs = jsonObject.getJSONArray("songs");
            JSONArray cat = jsonObject.getJSONArray("categories");
            JSONArray artists = jsonObject.getJSONArray("artists");
            JSONArray word_discovery = jsonObject.getJSONArray("world_discover");
            for (int x = 0; x < songs.length(); x++) {
                home_song.add_song(new Song(songs.getJSONObject(x).getString("title"), songs.getJSONObject(x).getString("id"), songs.getJSONObject(x).getString("image"), songs.getJSONObject(x).getString("link")));

            }

            for (int x = 0; x < cat.length(); x++) {
                home_category.add_to_category_list(cat.getJSONObject(x).getString("id"), cat.getJSONObject(x).getString("Qtitle"), cat.getJSONObject(x).getString("Searchq"), cat.getJSONObject(x).getString("Imgurl"));
                Log.d(TAG, "whome category --->" + cat.getJSONObject(x).getString("Qtitle"));
            }

            for (int x = 0; x < artists.length(); x++) {
                home_artist.add_to_home_artist(artists.getJSONObject(x).getString("id"), artists.getJSONObject(x).getString("Qtitle"), artists.getJSONObject(x).getString("Searchq"), artists.getJSONObject(x).getString("Imgurl"));
                Log.d(TAG, "whome artist --->" + artists.getJSONObject(x).getString("Qtitle"));
            }

            for (int x = 0; x < word_discovery.length(); x++) {
                home_word_discovery.add_to_home_word_discovery(word_discovery.getJSONObject(x).getString("id"), word_discovery.getJSONObject(x).getString("Qtitle"), word_discovery.getJSONObject(x).getString("Searchq"), word_discovery.getJSONObject(x).getString("Imgurl"));
                Log.d(TAG, "word discovery --->" + word_discovery.getJSONObject(x).getString("Qtitle"));
            }

            Log.d(TAG, new home_song().toString());
            Log.d(TAG, "Reader home_jsonme" + cat.get(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
