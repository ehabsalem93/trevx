package trevx;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.SearchView;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;

import trevx.Appearance.OnSwipeTouchListener;
import trevx.Favourite.Favourite_Fragment;
import trevx.Favourite.Favourite_Main_API;
import trevx.Musicplayer.MusicService;
import trevx.Search.Search_Fragment;
import trevx.Song_Manager.Song;
import trevx.Song_Manager.get_Song_trevx;
import trevx.Song_Manager.trevx_api.Suggesion;
import trevx.Song_Manager.trevx_api.getSuggestion;
import trevx.com.trevx.R;
import trevx.full_Music_PLayer.media_player_visual;
import trevx.util.Internet_connectivity;
import trevx.util.String_ytil;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String REQUEST_CODE = "25";
    private static final int NOTIFICATION_ID = 11;
    public static RelativeLayout layout;
    public static Intent i1 = new Intent(MusicService.ACTION_URL);
    //VisualizerView mVisualizerView;
    public static Search_Fragment searchFragment;
    public static ProgressBar progressBar;
    public static String queryy;
    public static View view1;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public static ViewPager mViewPager;

    public static SearchViewLayout searchViewLayout;
    public static Context context;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    ProgressBar progressBar1;
    boolean is_completed = false;
    home home;
    Favourite_Fragment favouriteFragment;
    SearchView searchViewCompat;
    View view;
    TabLayout tabLayout;
    TextView Title, source;
    DiscreteSeekBar seek;
    Messenger mService = null;
    boolean mIsBound = false;
    AppCompatImageButton plaButton, puaButton;
    AppCompatImageView image;
    MenuItem fav;
    String sug;
    Runnable tunsug = new Runnable() {
        public void run() {
            new getSuggestion().execute(sug.toString());

        }
    };
    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Handler myHandler = new Handler();
    private String Name;
    private String Link;
    private String ids;
    private String imgs;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
       //    Toast.makeText(getApplicationContext(),"connected successfully to service",Toast.LENGTH_SHORT).show();
            try {
                Message msg = Message.obtain(null, MusicService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {

                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
         //   Toast.makeText(getApplicationContext(),"disconnected successfully to service",Toast.LENGTH_SHORT).show();
        }
    };
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Name = intent.getStringExtra("name");
            ids = intent.getStringExtra("id");
            Link = intent.getStringExtra("source");
            imgs = intent.getStringExtra("image");


            if (Link != null) {
                Title.setText(String.valueOf(Name));
                source.setText(new String_ytil().getHostName(Link));

                Glide.with(getApplicationContext()).load(imgs).asBitmap().into(image);
                Log.d(TAG, "Imassge" + imgs);
                //Picasso.with(context).load(imgs).into(image);

            }


        }
    };
    private boolean stop = false;
    Runnable UpdateSongTime = new Runnable() {
        public void run() {


            if (null != MusicService.mPlayer)

                if (MusicService.mState == MusicService.State.Retrieving || MusicService.mState == MusicService.State.Preparing) {

                    puaButton.setVisibility(View.GONE);
                    plaButton.setVisibility(View.GONE);
                    progressBar1.setVisibility(View.VISIBLE);


                } else if (MusicService.mState == MusicService.State.Playing) {
                    sendMessageToService(1);


                    if (mIsBound)

                        sendMessageToService(1);


                    progressBar1.setVisibility(View.GONE);
                    puaButton.setVisibility(View.VISIBLE);
                    plaButton.setVisibility(View.GONE);

                } else if (MusicService.mState == MusicService.State.Paused) {  //if(!ispause)
                    {
                        progressBar1.setVisibility(View.GONE);
                        puaButton.setVisibility(View.GONE);
                        plaButton.setVisibility(View.VISIBLE);

                    }
                } else if (MusicService.mState == MusicService.State.Stopped) {
                    progressBar1.setVisibility(View.GONE);
                    puaButton.setVisibility(View.GONE);
                    plaButton.setVisibility(View.VISIBLE);

                }


            if (!stop)
                myHandler.postDelayed(this, 1000);
        }
    };
    private Boolean exit = false;

    public static void doSearchs(final String queryStr) {
        // get a Cursor, prepare the ListAdapter
        // and set it
        //Toast.makeText(getApplicationContext(),"your voice are"+queryStr,Toast.LENGTH_LONG).show();
        queryy = queryStr;
        try {

        } catch (Exception e) {

        }
        mViewPager.setCurrentItem(1);


        new get_Song_trevx().execute();
        //   searchViewCompat.onActionViewCollapsed();

    }

    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (!MusicService.isRunning() && !mIsBound) {
            doBindService();
            Log.d(TAG, "Binding Main Activity into service");

        }
    }

    private void sendMessageToService(int intvaluetosend) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Log.d(TAG, "Sending message");
                    Message msg = Message.obtain(null, MusicService.MSG_SET_INT_VALUE, 1, 0);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }

    private void sendMessageToService_play_pause(int intvaluetosend) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Log.d(TAG, "Sending message to play_pause the song");
                    Message msg = Message.obtain(null, MusicService.MSG_SET_INT_VALUE, intvaluetosend, 0);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                }
            }
        }
    }

    public void send_action_url(String id, String song_name, String link, String pm) {
        Message msg = Message.obtain(null, MusicService.MSG_SET_INT_VALUE, 0, 0);
        Bundle i = new Bundle(0);
        Log.d(TAG, "sending thes data to musix serivce" + id + "  " + song_name + "   " + link);

        try {
            i.putString("id", id);
            i.putString("name", song_name);

            i.putString("source", link);
            i.putString("image", pm);
            msg.setData(i);

            Log.d(TAG, "id from mesanger: " + id);
            Log.d(TAG, "file name from mesanger: " + song_name);
            Log.d(TAG, "url from mesanger: " + link);
            Log.d(TAG, "imagefrom mesanger: " + pm);
            msg.replyTo = mMessenger;
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    void doBindService() {
        if (!mIsBound) {
            bindService(new Intent(this, MusicService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
        //Toast.makeText(getApplicationContext(),"Binding.....",Toast.LENGTH_SHORT).show();
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MusicService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            //Toast.makeText(getApplicationContext(),"Un Binding.....",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            try {
                doSearch(query);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.d(TAG, "Error Unsupporting encoding :" + e.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAG, "Error MalformedURLException  :" + e.toString());
            }
        }
    }

    private void doSearch(final String queryStr) throws UnsupportedEncodingException, MalformedURLException {
        // get a Cursor, prepare the ListAdapter
        // and set it
        //Toast.makeText(getApplicationContext(),"your voice are"+queryStr,Toast.LENGTH_LONG).show();
        queryy = queryStr;
        try {
            searchViewCompat.setQuery(queryy, false);
        } catch (Exception e) {

        }
        mViewPager.setCurrentItem(1);


        if (!Internet_connectivity.check_connection(this) && !Internet_connectivity.check_connection(this)) {
            return;
        } else {
            new get_Song_trevx().execute();
            //   new getSong().get_song(queryStr);

            //   searchViewCompat.onActionViewCollapsed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            context = this;
            if (!MusicService.isRunning) {
                Intent i = new Intent(MainActivity.this, MusicService.class);
                i.setPackage(getPackageName());

                startService(i);

                Log.d(TAG, "Service intieted");
            } else {
                Log.d(TAG, "Service  already intieted");

            }

            CheckIfServiceIsRunning();
            Suggesion.Suggestionword = new ArrayList<String>();

            //        ***********initiallizing the main component of the application**********
            init_whole_app_interface();


            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mMessageReceiver, new IntentFilter("SongUpdation"));


            //handleIntent(getIntent());

            //        ***********initiallizing small media player**********


            init_small_mediaPlayer();


            it();
            myHandler = new Handler();
            myHandler.postDelayed(UpdateSongTime, 100);

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Rorre occured" + e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    private void it() {
        searchViewLayout = (SearchViewLayout) findViewById(R.id.search_view_container);
        searchViewLayout.setBackgroundColor(Color.GREEN);
        searchViewLayout.invalidate();
        searchViewLayout.setBackgroundResource(R.color.black_overlay);
        searchViewLayout.setExpandedContentSupportFragment(this, new SearchStaticListSupportFragment());
        searchViewLayout.handleToolbarAnimation(tabLayout);

        searchViewLayout.setCollapsedHint(getApplicationContext(),"trevx");

        searchViewLayout.setExpandedHint("Search trevx...");

//        searchViewLayout.setHint("Global Hint");

        ColorDrawable collapsed = new ColorDrawable(ContextCompat.getColor(this,R.color.colorPrimary));
        ColorDrawable expanded = new ColorDrawable(ContextCompat.getColor(this, R.color.default_color_expanded));
        searchViewLayout.setTransitionDrawables(collapsed, expanded);
        searchViewLayout.setSearchListener(new SearchViewLayout.SearchListener() {
            @Override
            public void onFinished(String searchKeyword) {
                searchViewLayout.collapse();
              //  Snackbar.make(searchViewLayout, "Start Search for - " + searchKeyword, Snackbar.LENGTH_LONG).show();
                try {
                    doSearch(searchKeyword);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Error Unsuppoerted Encoding :" + e.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Error MalformedURLException Encoding :" + e.toString());
                }
            }
        });
        searchViewLayout.setOnToggleAnimationListener(new SearchViewLayout.OnToggleAnimationListener() {
            @Override
            public void onStart(boolean expanding) {
                if (expanding) {

                    // MainActivity.fab.hide();
                    tabLayout.setVisibility(View.GONE);
                    // mViewPager.setVisibility(View.GONE);
                } else {
                    //MainActivity.fab.show();
                    tabLayout.setVisibility(View.VISIBLE);
                    //  mViewPager.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFinish(boolean expanded) {
            }
        });
        searchViewLayout.setSearchBoxListener(new SearchViewLayout.SearchBoxListener() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: " + s + "," + start + "," + count + "," + after);


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: " + s + "," + start + "," + before + "," + count);

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: " + s);
                sug = s.toString();
                sug.trim();
                if (sug.length() >= 3) {
                    Suggesion.Suggestionword.clear();
              //      myHandler.post(tunsug);

                    new getSuggestion().execute(sug.toString());
                }
            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    private void init_small_mediaPlayer() {
        plaButton = (AppCompatImageButton) findViewById(R.id.play);
       puaButton = (AppCompatImageButton) findViewById(R.id.pause);
        image= (AppCompatImageView) findViewById(R.id.song_image1);
        Title = (TextView) findViewById(R.id.song_name);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call_full_player(ids,Name,Link,imgs);
            }
        });


        source= (TextView) findViewById(R.id.source);

        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        layout = (RelativeLayout) findViewById(R.id.small_player_main);
        plaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != MusicService.mPlayer) {

                    Log.d(TAG, "u clicked to pause the song");
                    sendMessageToService_play_pause(2);

                }
            }
        });
        puaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != MusicService.mPlayer) {
                    Log.d(TAG, "u clicked to play the song");
                    sendMessageToService_play_pause(2);

                }
            }
        });


        RelativeLayout playbar = (RelativeLayout) findViewById(R.id.small_player);

        playbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call_full_player(ids,Name,Link,imgs);
            }
        });
        playbar.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()){
            public void onSwipeTop() {
                // Toast.makeText(context.getApplicationContext(), "top", Toast.LENGTH_SHORT).show();
                call_full_player(ids,Name,Link,imgs);

            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
/*
                Intent i2 = new Intent(MusicService.ACTION_REWIND);
                i2.setPackage(getPackageName());
                startService(i2);*/
                sendMessageToService_play_pause(3);
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();

              /*  Intent i2 = new Intent(MusicService.ACTION_SKIP);
                i2.setPackage(getPackageName());
              //  startService(i2);
              */
                sendMessageToService_play_pause(3);

            }

            @Override
            public void onSwipeBottom() {
                super.onSwipeBottom();
                call_full_player(ids,Name,Link,imgs);

            }
            @Override
            public void onSwipeClick() {
                super.onSwipeClick();

                call_full_player(ids,Name,Link,imgs);

            }

        });


    }

    public void configure_small_player(String id, String song_name, String source, String pm, LinkedList<Song> songlist) {
        call_full_player(id, song_name, source, pm);
        RelativeLayout playbar = (RelativeLayout) findViewById(R.id.small_player_main);
        playbar.setVisibility(View.VISIBLE);
        Title.setText(new String_ytil().edit_songNamefor_small_player(song_name, 25));
        this.source.setText(new String_ytil().edit_songName(new String_ytil().getHostName(source)));

        MusicService.song_list = songlist;
        send_action_url(id, song_name, source, pm);
        Name = song_name;
         ids = id;
         imgs = pm;
        Link=source;


        Log.d(TAG,"id: "+id);
        Log.d(TAG,"file name: "+song_name);
        Log.d(TAG,"url: "+source);
        Log.d(TAG, "image: " + pm);


        //   call_full_player(id, song_name, source, pm);
       // Toast.makeText(getApplicationContext()," "+source+"   "+song_name,Toast.LENGTH_LONG).show();



    }

    public void call_full_player(String id,String song_name,String source,String image){
        Intent i = new Intent(getApplicationContext(), media_player_visual.class) ;
        i.setPackage(getPackageName());

        Log.d(TAG,"id: "+id);
        Log.d(TAG,"file name: "+song_name);
        Log.d(TAG,"url: "+source);
        i.putExtra("name",song_name);
        i.putExtra("source",source);
        i.putExtra("image",image);
        i.putExtra("id",id);
        startActivity(i);
    }

public void send_email(String text)
{
    Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_SUBJECT, "trevx");
    intent.putExtra(Intent.EXTRA_TEXT, text);
    intent.setData(Uri.parse("mailto:ptechnotech.es@gmail.com")); // or just "mailto:" for blank
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your trevx, your trevx is displayed, instead of the email trevx.
    startActivity(intent);

}

    public void print_Message(String text)
{
    Snackbar.make(layout,text,Snackbar.LENGTH_SHORT).show();
}

    private void init_whole_app_interface() {
        //resize the screen to full
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_container);

        Favourite_Main_API.define_lis(getApplicationContext());


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        progressBar = (ProgressBar) findViewById(R.id.progressbars);
         tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_favorite_border_white_24dp);
        tabLayout.getTabAt(2).setText("");
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_view_list_white_24dp);
        tabLayout.getTabAt(1).setText("");

        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_whatshot_white_24dp);
        tabLayout.getTabAt(0).setText("");
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        String countryCodeValue = tm.getNetworkCountryIso();
        seek= (DiscreteSeekBar) findViewById(R.id.seek);

        seek.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (MusicService.mPlayer.isPlaying()) {
                    //  P sb= (DiscreteSeekBar) view;
                    int playPositionInMillisecconds = seek.getProgress();

                    Log.d(TAG, "duraion" + MusicService.mPlayer.getDuration());
                    MusicService.mPlayer.seekTo(playPositionInMillisecconds);
                }
                return false;
            }
        });

      //  Toast.makeText(getApplicationContext(),countryCodeValue,Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        init_searchview(menu);
         fav= menu.findItem(R.id.favourite);
        return true;
    }

    private void init_searchview(Menu menu) {
        final MenuItem search_view = menu.findItem(R.id.search_view_header);
         searchViewCompat = (SearchView) search_view.getActionView();
        searchViewCompat.setQueryHint("Search Trevx");
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchViewCompat.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchViewCompat.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {


                search_view.collapseActionView();
                try {
                    queryy = query;
                    Log.d(TAG, "query is" + query);
                  //  new get_Song_trevx().execute();
                    doSearch(query);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }



                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchViewCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(1);
            }
        });
        searchViewCompat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mViewPager.setCurrentItem(1);
                return false;
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {
            case R.id.favourite: {

                mViewPager.setCurrentItem(0);
                fav.setIcon(R.drawable.favorite);
                break;
            }
            case R.id.search_view_header: {
                mViewPager.setCurrentItem(1);
                fav.setIcon(R.drawable.favorite_brdr);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }

    @Override
    public void onStart() {
        super.onStart();

        if (queryy != null) {
            if (Search_Fragment.adapter != null) {
                Search_Fragment.adapter.notifyDataSetChanged();
                return;
            }
            try {
                doSearch(queryy);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAG, "Error in OnSTart: " + e.toString());
            }
            mViewPager.setCurrentItem(1);

        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        //  stop = false;

        //  CheckIfServiceIsRunning();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            mIsBound=false;
            doUnbindService();
            stopService(new Intent(this,MusicService.class));
        stop=true;
            Glide.get(MainActivity.this).clearMemory();

       // doUnbindService();
        }catch(Exception e)


        {
            Toast.makeText(getApplicationContext(),"Exception while unbinding the serviec, please restart the Application",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private Handler myHandler;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            view1 = inflater.inflate(R.layout.activity_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
        }
    }


    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MusicService.MSG_SET_INT_VALUE:
                    if (seek != null) {
                        seek.setProgress(msg.getData().getInt("cur"));
                        seek.setMax(msg.getData().getInt("max"));
                    }
                    break;
                case MusicService.MSG_SET_STRING_VALUE:
                    //  String str1 = msg.getData().getString("str1");
                    //   MusicService.setText("Str Message: " + str1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 1) {
                if (null == searchFragment) {
                    {
                        return searchFragment = new Search_Fragment();
                    }

                } else
                    return searchFragment;
            } else if (position == 2) {
                if (null == favouriteFragment) {
                    return favouriteFragment = new Favourite_Fragment();
                } else {
                    return new home();
                }
            }

            return new home();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Favourite_Fragment";
                case 1:
                    return "Search";
                case 2:
                    return "home";

            }
            return null;
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }


}
