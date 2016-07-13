package trevx.Favourite;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.LinkedList;

import trevx.MainActivity;
import trevx.Song_Manager.Song;
import trevx.com.trevx.R;
import trevx.util.Internet_connectivity;
import trevx.util.String_ytil;

/**
 * Created by ptk on 6/6/16.
 */
public class Favourite_Fragment_Adapter extends RecyclerView.Adapter<Favourite_Fragment_Adapter.PersonViewHolder> {
    private static final String TAG = "favourite_custom_apater_1";
    public static Context context;
    LinkedList<Song> favouriteList;
    MainActivity activity;


    public Favourite_Fragment_Adapter(LinkedList<Song> song_list, Context context) {
        this.favouriteList = song_list;
        Favourite_Fragment_Adapter.context = context;

    }


    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favourite_list, parent, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }


    @Override
    public int getItemCount() {
        return favouriteList.size();
    }

    @Override
    public void onBindViewHolder(final PersonViewHolder personViewHolder, final int position) {


        personViewHolder.personName.setText(new String_ytil().edit_songName(favouriteList.get(position).getTitle()));
        personViewHolder.personAge.setText(new String_ytil().edit_songName(new String_ytil().getHostName(favouriteList.get(position).getLink())));
        personViewHolder.setImage(favouriteList.get(position).getImage());


        personViewHolder.Container.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                if (!Internet_connectivity.check_connection(context) && !Internet_connectivity.check_connection(context)) {
                    return;
                }
                play(favouriteList.get(position).getId(), favouriteList.get(position).getTitle(), favouriteList.get(position).getLink(), favouriteList.get(position).getImage());


            }
        });

        personViewHolder.fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendMessageToremovefromFavourite(favouriteList.get(position).getId());
                } catch (Exception e) {
                    Log.d(TAG, "Error in reomving through fa image Error:" + e.toString());
                    Toast.makeText(context, "Roorre in removing from Favourite:" + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private void sendMessageToremovefromFavourite(String id) {
        Intent intent = new Intent("removefromfavourite");
        // You can also include some extra data.
        intent.putExtra("id", id);
        // Bundle b = new Bundle();
        //  b.putParcelable("Location", l);
        //  intent.putExtra("Location", b);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void play(String id,String fileName, String fileurl, String fileimg) {


        try {
            String source=fileurl;
            String title=new String_ytil().edit_songName((fileName));
            activity = (MainActivity) context;
            // activity.print_Message("playing "+fileName);
            Log.d(TAG, "iddd: " + id);
            Log.d(TAG, "fildde name: " + fileName);
            Log.d(TAG, "urldd: " + fileurl);

            activity.configure_small_player(id, title, source, fileimg, favouriteList);

        } catch (Exception e) {
        }


    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView personName;
        TextView personAge;
        ImageView personPhoto;
        ImageButton fav;


        RelativeLayout Container;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            personName = (TextView) itemView.findViewById(R.id.person_name);

            personAge = (TextView) itemView.findViewById(R.id.person_age);
            personPhoto = (ImageView) itemView.findViewById(R.id.person_photo);
            Container = (RelativeLayout) itemView.findViewById(R.id.container);
            fav = (ImageButton) itemView.findViewById(R.id.heart_filled);





        }

        public void setImage(String link) {
            // new Song_Image_loader(link,personPhoto);
            try {


                Glide.with(context)
                        .load(link).into(personPhoto);

                Log.d(TAG, "link loaded ->" + link);
            } catch (Exception e) {
                Log.d(TAG, "link loading Error " + e.toString());
                Toast.makeText(context, "Rorre in loading image to favourite" + link + " " + e.toString(), Toast.LENGTH_LONG).show();


            }
        }
    }
}