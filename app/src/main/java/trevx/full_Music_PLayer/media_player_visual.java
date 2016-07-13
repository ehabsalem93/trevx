package trevx.full_Music_PLayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.liuguangqiang.swipeback.SwipeBackActivity;
import com.liuguangqiang.swipeback.SwipeBackLayout;

import java.net.MalformedURLException;
import java.net.URL;

import trevx.Appearance.OnSwipeTouchListener;
import trevx.Favourite.Favourite_Fragment;
import trevx.Favourite.Favourite_Main_API;
import trevx.Musicplayer.MusicService;
import trevx.Song_Manager.Song;
import trevx.imageLoader.Song_Image_loader;
import trevx.com.trevx.R;
import trevx.util.String_ytil;


/**

 */
public class media_player_visual extends SwipeBackActivity {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static Context context;
    private SeekBar bar ;
    private int i = 0;

    boolean issekset = false;
    boolean ispause = false;
    private Handler myHandler = new Handler();
    boolean stop = false;
    TextView current_pos;
    ImageView repeat, repeat1, shuffle, shuffle1, fav, fav1, share, share1, play, pause,Next,Pre,song_image;
    TextView source,song_name,duration;
    private RelativeLayout baseLayout;
    public static Intent i1 = new Intent(MusicService.ACTION_URL);
    boolean is_completed = false;
    int bufferef = 0;
    ProgressBar waiting;
    private int previousFingerPosition = 0;
    private int baseLayoutPosition = 0;
    private int defaultViewHeight;

    private boolean isClosing = false;
    private boolean isScrollingUp = false;
    private boolean isScrollingDown = false;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String id,song_names,sources,image;

    public media_player_visual() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * <p/>
     * //  * @param param1 Parameter 1.
     * //  * @param param2 Parameter 2.
     *
     * @return A new instance of fragment media_player_visual.
     */


    Messenger mService = null;
    boolean mIsBound=false;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MusicService.MSG_SET_INT_VALUE:
                    int cur=msg.getData().getInt("cur");
                    int max=msg.getData().getInt("max");
                    if(bar!=null){
                    bar.setProgress(cur);
                    bar.setMax(max);}
                    setCurrentPostion(cur);
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

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            //Toast.makeText(getApplicationContext(),"connected successfully to service",Toast.LENGTH_SHORT).show();
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
          //  Toast.makeText(getApplicationContext(),"disconnected successfully to service",Toast.LENGTH_SHORT).show();
        }
    };
    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (MusicService.isRunning() && !mIsBound) {
            doBindService();
            mIsBound=true;
        }
    }
    private void sendMessageToService(int intvaluetosend) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Log.d(TAG,"Sending message");
                    Message msg = Message.obtain(null, MusicService.MSG_SET_INT_VALUE, intvaluetosend, 0);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                }
            }
        }
    }
    void doBindService() {
        if(!mIsBound) {
            bindService(new Intent(this, MusicService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
      //  Toast.makeText(getApplicationContext(),"Binding.....",Toast.LENGTH_SHORT).show();
    }
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MusicService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
      //      Toast.makeText(getApplicationContext(),"Un Binding.....",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        doUnbindService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckIfServiceIsRunning();
    }

    // TODO: Rename and change types and number of parameters
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.fragment_media_player_visual);
        setDragEdge(SwipeBackLayout.DragEdge.TOP);
        getSupportActionBar().hide();

        //setDragEdge(SwipeBackLayout.DragEdge.LEFT);
        if (MusicService.mPlayer != null) {


            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mMessageReceiver, new IntentFilter("SongUpdation"));


            //     if(!isRunning)
            song_names=getIntent().getStringExtra("name");
            image=getIntent().getStringExtra("image");
            sources=getIntent().getStringExtra("source");
            id=getIntent().getStringExtra("id");

            init();


            bar = (SeekBar) findViewById(R.id.progressBar1);

            bar.setMax(MusicService.mPlayer.getDuration());
            bar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    if (MusicService.mPlayer.isPlaying()) {
                        //  P sb= (DiscreteSeekBar) view;
                        int playPositionInMillisecconds = bar.getProgress();

                        Log.d(TAG, "duraion" + MusicService.mPlayer.getDuration());
                        MusicService.mPlayer.seekTo(playPositionInMillisecconds);
                    }
                    return false;
                }
            });


            //        ***********REPEAT**********

            repeat = (ImageView) findViewById(R.id.repeatr);
            repeat1 = (ImageView) findViewById(R.id.repeat1);

            if (MusicService.mPlayer.isLooping()) {
                repeat1.setVisibility(View.VISIBLE);
                repeat.setVisibility(View.GONE);
            } else {
                repeat1.setVisibility(View.GONE);
                repeat.setVisibility(View.VISIBLE);
            }

            repeat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    repeat1.setVisibility(View.VISIBLE);
                    repeat.setVisibility(View.GONE);
                    MusicService.mPlayer.setLooping(true);

                }
            });

            repeat1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    repeat1.setVisibility(View.GONE);
                    repeat.setVisibility(View.VISIBLE);
                    MusicService.mPlayer.setLooping(false);


                }
            });

            //        ***********SHUFFLE**********

            shuffle = (ImageView) findViewById(R.id.shuffle);
            shuffle1 = (ImageView) findViewById(R.id.shuffle1);


            shuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    shuffle1.setVisibility(View.VISIBLE);
                    shuffle.setVisibility(View.GONE);
                    MusicService.is_shuffeled=true;

                }
            });

            shuffle1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    shuffle1.setVisibility(View.GONE);
                    shuffle.setVisibility(View.VISIBLE);
                    MusicService.is_shuffeled=false;


                }
            });

            if(MusicService.is_shuffeled)
            {
                shuffle1.setVisibility(View.VISIBLE);
                shuffle.setVisibility(View.GONE);

            }else
            {
                shuffle1.setVisibility(View.GONE);
                shuffle.setVisibility(View.VISIBLE);

            }
            //        ***********FAV**********


            fav = (ImageView) findViewById(R.id.fav);
            fav1 = (ImageView) findViewById(R.id.fav1);


            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    fav1.setVisibility(View.VISIBLE);
                    fav.setVisibility(View.GONE);

                    Favourite_Main_API.addtolist(new Song(song_names,id,image,sources));
                  //  Toast.makeText(getApplicationContext(),MusicService.Name+" removed sucessfully",Toast.LENGTH_SHORT).show();

                    if(Favourite_Fragment.adapter!=null)
                        Favourite_Fragment.adapter.notifyDataSetChanged();

                }
            });

            fav1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    fav1.setVisibility(View.GONE);
                    fav.setVisibility(View.VISIBLE);


                    Favourite_Main_API.removewithid(id);
                   // Toast.makeText(getApplicationContext(),MusicService.Name+" removed sucessfully",Toast.LENGTH_SHORT).show();

                    if(Favourite_Fragment.adapter!=null)
                        Favourite_Fragment.adapter.notifyDataSetChanged();
                }
            });
            if(Favourite_Main_API.check_exist(id))
            {

                fav1.setVisibility(View.VISIBLE);
                fav.setVisibility(View.GONE);

            }else{
                fav1.setVisibility(View.GONE);
                fav.setVisibility(View.VISIBLE);
            }

            //        ***********FAV**********

            share = (ImageView) findViewById(R.id.share);
            share1 = (ImageView) findViewById(R.id.share1);


            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    share1.setVisibility(View.VISIBLE);
                    share.setVisibility(View.GONE);

                }
            });

            share1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    share1.setVisibility(View.GONE);
                    share.setVisibility(View.VISIBLE);


                }
            });

            //        ***********PlAY/PAUSE**********

            play = (ImageView) findViewById(R.id.play);
            pause = (ImageView) findViewById(R.id.pause);

            if (MusicService.mPlayer.isPlaying()) {
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
            } else {
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
            }

            //        ***********PlAY click listener**********

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != MusicService.mPlayer) {

                        if (is_completed) {
                            i1.setPackage(getPackageName());
                            Uri uri = Uri.parse(sources);
                            i1.setData(uri);

                            startService(i1);
                            is_completed = false;
                        }

                        Log.d(TAG, "u clicked to pause the song");
                        Intent serviceIntent = new Intent(MusicService.ACTION_PLAY);
                        serviceIntent.setPackage(getPackageName());
                        startService(serviceIntent);

                        if (MusicService.mPlayer.isPlaying())
                            MusicService.mState = MusicService.State.Playing;


                    }
                }
            });

            //        ***********PAUSE click listener**********
            pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if (null != MusicService.mPlayer) {
                        Log.d(TAG, "u clicked to play the song");
                        Intent serviceIntent = new Intent(MusicService.ACTION_PAUSE);
                        serviceIntent.setPackage(getPackageName());
                        startService(serviceIntent);

                        if (!MusicService.mPlayer.isPlaying())
                            MusicService.mState = MusicService.State.Paused;
                    }



                }
            });

            myHandler.postDelayed(UpdateSongTime, 100);
        }


        //        ***********if Media PLayer not initialized then finish this activity and go back to Main Activity*********

        else {
            finish();
        }

        //************** check media player when complete song

        //        ***********check Media PLayer when buffering song**********
        if(MusicService.mPlayer!=null)
        MusicService.mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                bufferef = i;

                Log.d(TAG, bufferef + "45s");
                bar.setSecondaryProgress(bufferef * 10000);

            }
        });


    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            song_names = intent.getStringExtra("name");
            id = intent.getStringExtra("id");
            sources = intent.getStringExtra("source");
            image = intent.getStringExtra("image");


            if (sources != null) {
                song_name.setText(String.valueOf(song_names));
                source.setText(new String_ytil().getHostName(sources));
                Glide.with(context)
                        .load(image).asBitmap().into(song_image);

            }


        }
    };
    private void init() {

        waiting = (ProgressBar) findViewById(R.id.waiting);

        Next= (ImageView) findViewById(R.id.next);
        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 Intent i2 = new Intent(MusicService.ACTION_SKIP);
                i2.setPackage(getPackageName());
                startService(i2);
            }
        });

        Pre= (ImageView) findViewById(R.id.back);
        Pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i2 = new Intent(MusicService.ACTION_REWIND);
                i2.setPackage(getPackageName());
                startService(i2);
            }
        });


    RelativeLayout relativeLayout= (RelativeLayout) findViewById(R.id.full_player);

        relativeLayout.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()){
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();

                Intent i2 = new Intent(MusicService.ACTION_REWIND);
                i2.setPackage(getPackageName());
                startService(i2);
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();

                Intent i2 = new Intent(MusicService.ACTION_SKIP);
                i2.setPackage(getPackageName());
                startService(i2);

            }
        });

        Log.d(TAG, "Song Name" + song_names);
        current_pos = (TextView) findViewById(R.id.current_pos);
        current_pos.setText(getTimeString(MusicService.mPlayer.getCurrentPosition()));
         song_name = (TextView) findViewById(R.id.song_name);
        song_name.setText(song_names);
        duration = (TextView) findViewById(R.id.duration);
        duration.setText(getTimeString(MusicService.mPlayer.getDuration()));
         source = (TextView) findViewById(R.id.source);
        source.setText(getHostName(sources));

        android.support.v7.widget.AppCompatImageButton close = (android.support.v7.widget.AppCompatImageButton) findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


         song_image = (ImageView) findViewById(R.id.song_images);
        new Song_Image_loader(image, song_image).execute();
    }


    private String TAG = "MediaPLayervisual";
    Runnable UpdateSongTime = new Runnable() {


        public void run() {


            if (null != MusicService.mPlayer)


                if (MusicService.mState == MusicService.State.Retrieving || MusicService.mState == MusicService.State.Preparing) {
                    bar.setSecondaryProgress(0);
                    pause.setVisibility(View.GONE);
                    play.setVisibility(View.GONE);
                    waiting.setVisibility(View.VISIBLE);
                    Log.d(TAG,"inside retrieving or preparing ");



                    //issekset=false;
                } else if (MusicService.mState == MusicService.State.Playing) {

                  //  int startTime = MusicService.mPlayer.getCurrentPosition();
//
                    pause.setVisibility(View.VISIBLE);
                    play.setVisibility(View.GONE);
                    waiting.setVisibility(View.GONE);
                   // bar.setProgress(startTime);

                    if(mIsBound)
                    sendMessageToService(1);

                    //    current_pos.setText(startTime + "1");


                    Log.d(TAG,"inside playing ");



                } else if (MusicService.mState == MusicService.State.Paused) {

                        pause.setVisibility(View.GONE);
                        play.setVisibility(View.VISIBLE);
                        waiting.setVisibility(View.GONE);
//                        Toast.makeText(context, "pause Music", Toast.LENGTH_SHORT).show();

                } else if (MusicService.mState == MusicService.State.Stopped) {
                    pause.setVisibility(View.GONE);
                    play.setVisibility(View.VISIBLE);
                    waiting.setVisibility(View.GONE);

                }
            if (!stop)
                myHandler.postDelayed(this, 100);
        }
    };


    public void setCurrentPostion(int current) {
//        int startTime = MusicService.mPlayer.getCurrentPosition();
if(current_pos!=null) {
    current_pos.setText(getTimeString(current));
    duration.setText(getTimeString(MusicService.mPlayer.getDuration()))
    ;
}

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop = true;
        //isRunning=false;

        mIsBound=false;
        doUnbindService();

    }

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf
                //    .append(String.format("%02d", hours))
                //    .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }

    private String getHostName(String urlInput) {
        urlInput = urlInput.toLowerCase();
        String hostName = urlInput;
        if (!urlInput.equals("")) {
            if (urlInput.startsWith("http") || urlInput.startsWith("https")) {
                try {
                    URL netUrl = new URL(urlInput);
                    String host = netUrl.getHost();
                    if (host.startsWith("www")) {
                        hostName = host.substring("www".length() + 1);
                    } else {
                        hostName = host;
                    }
                } catch (MalformedURLException e) {
                    hostName = urlInput;
                }
            } else if (urlInput.startsWith("www")) {
                hostName = urlInput.substring("www".length() + 1);
            }
            return hostName;
        } else {
            return "unDetected";
        }
    }

}