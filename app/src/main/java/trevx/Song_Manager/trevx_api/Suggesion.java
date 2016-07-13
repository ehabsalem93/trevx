/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trevx.Song_Manager.trevx_api;

import trevx.util.String_ytil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author ptk
 */
public class Suggesion {

    /**
     * @param args the command line arguments
     */

    public static ArrayList <String> Suggestionword;
    final String TAG = "Suggestion";
    public  void main(String[] args) {
        try {
            // TODO code application logic here
            Suggestionword=new ArrayList<>();
            get_song_api("adele");
        } catch (IOException ex) {
            Logger.getLogger(Suggesion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



    /**
     * Created by ptk on 6/8/16.
     */


    public static void get_song_api(String query) throws IOException {
        //query = query.replace(" ", "-");

        URL url = new URL("http://trevx.com/v1/suggestion/" + URLEncoder.encode(query.replace(" ","%20"), "UTF-8") + "/?format=json");
        // URL url = new URL("http://trevx.com/v1/"+query+"/1/40/?format=json");
        System.out.println(url+"\n\n\n");
      //  Log.d(TAG,url+"");
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

    public static void Reader_Json(StringBuilder JSON_Builder) {
        try {
            JSONArray json = new JSONArray(JSON_Builder.toString());
            //   System.out.println(json.toString());
            JSONObject results;
            if(Suggestionword!= null)
            Suggestionword.clear();
            else
            Suggestionword=new ArrayList<>();

            //  results=json.getJSONObject(1);
            //     System.err.println("s\n\n\n"+json.get(1).toString()+"\n\n\rs");
            for (int i = 0; i < (json.length() - 4); i++)
            {
                Suggestionword.add(new String_ytil().edit_songNamefor_small_player(json.getString(i),25));
                String x = (String) json.get(i);
                System.err.println(x);
                //  System.out.println(results.get("id")+"->"+results.getString("title")+"->"+results.getString("link")+"->"+results.get("image"));
                //   song_list.add(new Song(results.get("title").toString(),results.get("id").toString(),results.get("image").toString(),results.getString("link").toString()));
            }

        } catch (Exception ex) {
            // Logger.getLogger(Suggesion.getName()).log(Level.SEVERE, null, ex);
        }


    }
}
