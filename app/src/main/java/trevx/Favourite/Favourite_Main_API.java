package trevx.Favourite;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import trevx.Song_Manager.Song;

/**
 * Created by ptk on 6/15/16.
 */
public class Favourite_Main_API {

    private static final String TAG = "Favourite_Main_API";
    public static LinkedList<Song> favouriteList;
    public static SharedPreferences favourite_list;
    public static String favourite_list_id="Favourite_Fragment";

static Context context1;
    public static void define_lis(Context context)
    {
        context1=context;
        try{


             favourite_list = PreferenceManager
                     .getDefaultSharedPreferences(context.getApplicationContext());;
            Gson gson = new Gson();

        String json = favourite_list.getString(favourite_list_id,"");

            if (json.isEmpty()) {
                favouriteList = new LinkedList<>();
            } else {
                Type type = new TypeToken<List<Song>>() {
                }.getType();
                favouriteList = gson.fromJson(json, type);
            }
        }catch (Exception e)
        {
            Log.d(TAG,"Error in Facourite callee the Error says:"+e.toString());
        }


    }
    public static void addtolist(Song info){

        if(favouriteList == null)
        {
            favouriteList=new LinkedList<>();
        }

        favouriteList.add(info);
        SharedPreferences.Editor editor = favourite_list.edit();
        Gson gson = new Gson();
        String json = gson.toJson(favouriteList);
        editor.putString(favourite_list_id, json);
        editor.commit();


    }
    public static  void removesong(int index)
    {
        {
            if(favouriteList==null)
                return;;
if(index >favouriteList.size())
    return;
            favouriteList.remove(index);
            SharedPreferences.Editor editor = favourite_list.edit();
            Gson gson = new Gson();
            String json = gson.toJson(favouriteList);
            editor.putString(favourite_list_id, json);
            editor.commit();
        }
    }

    public static  void removewithid(String id)
    {
        try{
            favouriteList.remove(get_index_of_song(id));

            if(favouriteList ==null)
                favouriteList=new LinkedList<>();
            SharedPreferences.Editor editor = favourite_list.edit();
            Gson gson = new Gson();
            String json = gson.toJson(favouriteList);
            editor.putString(favourite_list_id, json);
            editor.commit();
        }catch(Exception e){
            Log.d(TAG,"Trying to removed un exsisting song from favourite");
        }
    }
    public static  int get_index_of_song(String id)
    {
        if(favouriteList!=null)
            for(int x=0;x<favouriteList.size();x++)
            {
                if(favouriteList.get(x).getId().equals(id))
                {
                    return x;

                }
            }
        return  -9;
    }

public static boolean check_exist(String id)
{
    return get_index_of_song(id)==-9?false:true;
}

}
