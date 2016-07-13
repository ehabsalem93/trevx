package trevx.Search;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import trevx.com.trevx.R;
import trevx.downloadManager.Song_Download_Manager;
import trevx.util.Internet_connectivity;
import trevx.util.String_ytil;

/**
 * Created by ptk on 6/6/16.
 */
public class Search_FRagment_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "search_custom_apater";
    public static Context context;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    public int visibleThreshold = 10;
    public int lastVisibleItem, totalItemCount;
    LinkedList<Song> song_list;
    MainActivity activity;
    ViewGroup go;
    private OnLoadMoreListnert mOnLoadMoreListener;
    private boolean isLoading = false;

    public Search_FRagment_Adapter(LinkedList<Song> song_list, Context context) {
        this.song_list = song_list;
        Search_FRagment_Adapter.context = context;
        activity = (MainActivity) context;


        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) Search_Fragment.rv.getLayoutManager();
        Search_Fragment.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                    }
                    //   isLoading = true;
                }
            }
        });
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        PersonViewHolder pvh;
        go = parent;
        if (viewType == VIEW_TYPE_ITEM) {
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
        if (song_list.size() == Search_Fragment.Song_count)
            return song_list.size();
        return song_list == null ? 0 : song_list.size() + 1;
    }

    public void setLoaded(boolean loadd) {
        isLoading = loadd;
    }

    @Override
    public int getItemViewType(int position) {

        return position < song_list.size() ? VIEW_TYPE_ITEM : VIEW_TYPE_LOADING;
    }

    public void setOnLoadMoreListener(OnLoadMoreListnert mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

if(holder instanceof PersonViewHolder) {
    if (holder instanceof PersonViewHolder) {
        final PersonViewHolder personViewHolder = (PersonViewHolder) holder;

        Log.d(TAG, "possssss" + position + "song list possss    " + song_list.size() + "trevx list sie    ");
        personViewHolder.personName.setText(new String_ytil().edit_songName(song_list.get(position).getTitle()));
        personViewHolder.personAge.setText(new String_ytil().edit_songName(new String_ytil().getHostName(song_list.get(position).getLink())));
        personViewHolder.setImage(song_list.get(position).getImage());

        if (Favourite_Main_API.check_exist(song_list.get(position).getId())) {
            personViewHolder.personfavouriteunfilled.setVisibility(View.GONE);
            personViewHolder.personfavouritefilled.setVisibility(View.VISIBLE);
//            Toast.makeText(context, "favo in " + position + "   " + song_list.get(position).getId(), Toast.LENGTH_LONG).show();

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
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                if (!Internet_connectivity.check_connection(context) && !Internet_connectivity.check_connection(context)) {
                    return;
                }
                //download(song_list.get(position).getTitle(), song_list.get(position).getLink());

                final Scene scene = Scene.getSceneForLayout((ViewGroup) view, R.layout.fragment_search__fragments, context);
                TransitionManager.go(scene);


            }
        });

        personViewHolder.personfavouriteunfilled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
                    view.startAnimation(shake);
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
                Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
                view.startAnimation(shake);
                personViewHolder.personfavouriteunfilled.setVisibility(View.VISIBLE);
                personViewHolder.personfavouritefilled.setVisibility(View.GONE);
                sendMessageToremovefromFavourite(song_list.get(position).getId());

            }
        });

    }
}
        else {
    ((ProgressViewHolder)holder).progressBar.setIndeterminate(true);
}
    }

    public void play(String id, String fileName, String fileurl, String fileimg) {
        try {
            String source = fileurl;
            //  String title=new String_ytil().edit_songName((fileName));
            //     Toast.makeText(context, "Source is " + fileurl, Toast.LENGTH_LONG).show();
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


    public interface OnLoadMoreListener {
        void onLoadMore();
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
                        .load(link).skipMemoryCache(false).into(personPhoto);

                // Picasso.with(context).load(link).into(personPhoto);
                // new Song_Image_loader(link,personPhoto);
                Log.d(TAG, "link loaded ->" + link);
            } catch (Exception e) {
                Log.d(TAG, "link loading Error " + e.toString());
            }
        }
    }


}