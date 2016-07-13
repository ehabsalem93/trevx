package trevx.Search;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.LinkedList;

import trevx.Favourite.Favourite_Main_API;
import trevx.MainActivity;
import trevx.Musicplayer.MusicService;
import trevx.Song_Manager.Song;
import trevx.downloadManager.Song_Download_Manager;
import trevx.com.trevx.R;
import trevx.util.Internet_connectivity;
import trevx.util.String_ytil;

/**
 * Created by ptk on 6/6/16.
 */
public class Search_FRagment_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "search_custom_apater_1";
    public static Context context;
    LinkedList<Song> song_list;
    MainActivity activity;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;



    public Search_FRagment_Adapter(LinkedList<Song> song_list, Context context) {
        this.song_list = song_list;
        Search_FRagment_Adapter.context = context;
        activity = (MainActivity) context;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        PersonViewHolder pvh;
        if(viewType==VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result, parent, false);
             pvh = new PersonViewHolder(v);
            return pvh;
        }else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.progress_loading_fill_search, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    private void sendMessageToFavourite(String Name,String Link,String image,String id) {
        Toast.makeText(context,"inside trhe function",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent("AddtoFavourite");
        // You can also include some extra data.
        intent.putExtra("name", Name);
        intent.putExtra("source", Link);
        intent.putExtra("image", image);
        intent.putExtra("id", id);
        // Bundle b = new Bundle();
        //  b.putParcelable("Location", l);
        //  intent.putExtra("Location", b);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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
    @Override
    public int getItemCount() {
        return song_list.size()+1;
    }


    @Override
    public int getItemViewType(int position) {
        return position <song_list.size()? VIEW_ITEM: VIEW_PROG;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

if(holder instanceof PersonViewHolder) {
    final PersonViewHolder personViewHolder=(PersonViewHolder)holder;
    personViewHolder.personName.setText(new String_ytil().edit_songName(song_list.get(position).getTitle()));
    personViewHolder.personAge.setText(new String_ytil().edit_songName(new String_ytil().getHostName(song_list.get(position).getLink())));
    personViewHolder.setImage(song_list.get(position).getImage());

    if (Favourite_Main_API.check_exist(song_list.get(position).getId())) {
        personViewHolder.personfavouriteunfilled.setVisibility(View.GONE);
        personViewHolder.personfavouritefilled.setVisibility(View.VISIBLE);
        Toast.makeText(context, "favo in " + position + "   " + song_list.get(position).getId(), Toast.LENGTH_LONG).show();

    } else {
        personViewHolder.personfavouriteunfilled.setVisibility(View.VISIBLE);
        personViewHolder.personfavouritefilled.setVisibility(View.GONE);
    }


    personViewHolder.Container.setOnClickListener(new View.OnClickListener() {


        @Override
        public void onClick(View view) {
            MusicService.mState = MusicService.State.Stopped;
            play(song_list.get(position).getId(), song_list.get(position).getTitle(), song_list.get(position).getLink(), song_list.get(position).getImage());


        }
    });
    personViewHolder.persondownload.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (!Internet_connectivity.check_connection(context) && !Internet_connectivity.check_connection(context)) {
                return;
            }
            download(song_list.get(position).getTitle(), song_list.get(position).getLink());


        }
    });

    personViewHolder.personfavouriteunfilled.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            {
                personViewHolder.personfavouriteunfilled.setVisibility(View.GONE);
                personViewHolder.personfavouritefilled.setVisibility(View.VISIBLE);
                sendMessageToFavourite(song_list.get(position).getTitle(), song_list.get(position).getLink(), song_list.get(position).getImage(), song_list.get(position).getId());
            }
            //  else
            {
                //  activity.print_Message("Song already exist in your favourite list");
                //    Snackbar.make(MainActivity.layout,"Song already exist in your favourite list",Snackbar.LENGTH_SHORT ).show();
            }

        }
    });

    personViewHolder.personfavouritefilled.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            personViewHolder.personfavouriteunfilled.setVisibility(View.VISIBLE);
            personViewHolder.personfavouritefilled.setVisibility(View.GONE);
            sendMessageToremovefromFavourite(song_list.get(position).getId());
        }
    });

}
        else {
    ((ProgressViewHolder)holder).progressBar.setIndeterminate(true);
}
    }

    public void play(String id, String fileName, String fileurl, String fileimg) {
        try {
            String source = fileurl;
            //  String title=new String_ytil().edit_songName((fileName));
            Toast.makeText(context, "Source is " + fileurl, Toast.LENGTH_LONG).show();
            Log.d(TAG, "id: " + id);
            Log.d(TAG, "file name: " + fileName);
            Log.d(TAG, "url: " + fileurl);

            activity.configure_small_player(id, fileName, source, fileimg, song_list);

        } catch (Exception e) {
        }




    }

    public void download(String Name, String url) {
        new Song_Download_Manager().download(Name, url, context);
        activity.print_Message("start downloading " + "trevx_" + Name);
        //   Toast.makeText(context, "start downloading " + "trevx_" + Name, Toast.LENGTH_SHORT).show();


    }


    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar)v.findViewById(R.id.progressBar);
        }
    }


    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView personName;
        TextView personAge;
        ImageView personPhoto;
        ImageView persondownload;
        ImageView personfavouritefilled;
        ImageView personfavouriteunfilled;


        RelativeLayout Container;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            personName = (TextView) itemView.findViewById(R.id.person_name);

            personAge = (TextView) itemView.findViewById(R.id.person_age);
            personPhoto = (ImageView) itemView.findViewById(R.id.person_photo);
            Container = (RelativeLayout) itemView.findViewById(R.id.container);
            persondownload = (ImageView) itemView.findViewById(R.id.download);
            personfavouriteunfilled = (ImageView) itemView.findViewById(R.id.heart_unfilled);
            personfavouritefilled = (ImageView) itemView.findViewById(R.id.heart_filled);
            //personPhoto.setImageResource(R.drawable.bg);


        }

        public void setImage(String link) {
            //new Song_Image_loader(link,personPhoto);
            try {

                Glide.with(context)
                        .load(link).into(personPhoto);

                // Picasso.with(context).load(link).into(personPhoto);
                // new Song_Image_loader(link,personPhoto);
                Log.d(TAG, "link loaded ->" + link);
            } catch (Exception e) {
                Log.d(TAG, "link loading Error " + e.toString());
            }
        }
    }



}