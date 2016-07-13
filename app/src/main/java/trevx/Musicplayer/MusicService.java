/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package trevx.Musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RemoteControlClient;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import trevx.MainActivity;
import trevx.Song_Manager.Song;
import trevx.com.trevx.R;

/**
 * Service that handles media playback. This is the Service through which we perform all the media
 * handling in our application. Upon initialization, it starts a {@link MusicRetriever} to scan
 * the user's media. Then, it waits for Intents (which come from our main activity,
 * {@link MainActivity}, which signal the service to perform specific operations: Play, Pause,
 * Rewind, Skip, etc.
 */
public class MusicService extends Service implements MediaPlayer.OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener,
        OnErrorListener, MusicFocusable,
        PrepareMusicRetrieverTask.MusicRetrieverPreparedListener {

    // These are the Intent actions that we are prepared to handle. Notice that the fact these
    // constants exist in our class is a mere convenience: what really defines the actions our
    // service can handle are the <action> tags in the <intent-filters> tag for our service in
    // AndroidManifest.xml.
    public static final String ACTION_TOGGLE_PLAYBACK =
            "com.example.android.musicplayer.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.example.android.musicplayer.action.PLAY";
    public static final String ACTION_PAUSE = "com.example.android.musicplayer.action.PAUSE";
    public static final String ACTION_STOP = "com.example.android.musicplayer.action.STOP";
    public static final String ACTION_SKIP = "com.example.android.musicplayer.action.SKIP";
    public static final String ACTION_REWIND = "com.example.android.musicplayer.action.REWIND";
    public static final String ACTION_URL = "com.example.android.musicplayer.action.URL";
    // The volume we set the media player to when we lose audio focus, but are allowed to reduce
    // the volume instead of stopping playback.
    public static final float DUCK_VOLUME = 0.1f;
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_INT_VALUE = 3;
    public static final int MSG_SET_STRING_VALUE = 4;
    // The tag we put on debug messages
    final static String TAG = "trevxMusicPLayer";
    public static Bitmap pm;
    // our media player
    public static MediaPlayer mPlayer = null;
    public static State mState = State.Retrieving;
    public static boolean isRunning;
    // The component name of MusicIntentReceiver, for use with media button and remote control
    // APIs
    public static boolean is_looping = false;
    public static boolean is_shuffeled = false;
    public static LinkedList<Song> song_list;
    public static Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            pm = bitmap;
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
    // The ID we use for the notification (the onscreen alert that appears at the notification
    // area at the top of the screen as an icon -- and as text as well if the user expands the
    // notification area).
    final int NOTIFICATION_ID = 1;
    public String Name, id;
    public String image, Link;
    MediaSessionCompat _mediaSession;
    Intent actionTogglePlayBack;

    // our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
    // If not available, this will be null. Always check for null before using!
    AudioFocusHelper mAudioFocusHelper = null;
    // if in Retrieving mode, this flag indicates whether we should start playing immediately
    // when we are ready or not.
    boolean mStartPlayingAfterRetrieve = false;
    // if mStartPlayingAfterRetrieve is true, this variable indicates the URL that we should
    // start playing when we are ready. If null, we should play a random song from the device
    Uri mWhatToPlayAfterRetrieve = null;
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    int mValue = 0; // Holds last value set by a client.
    // why did we pause? (only relevant if mState == State.Paused)
    PauseReason mPauseReason = PauseReason.UserRequest;
    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;
    // whether the song we are playing is streaming from the network
    boolean mIsStreaming = false;
    // Wifi lock that we hold when streaming files from the internet, in order to prevent the
    // device from shutting off the Wifi radio
    WifiLock mWifiLock;
    // Our instance of our MusicRetriever, which handles scanning for media and
    // providing titles and URIs as we need.
    // MusicRetriever mRetriever;
    // our RemoteControlClient object, which will use remote control APIs available in
    // SDK level >= 14, if they're available.
    RemoteControlClientCompat mRemoteControlClientCompat;
    // Dummy album art we will pass to the remote control (if the APIs are available).
    Bitmap mDummyAlbumArt;
    ComponentName mMediaButtonReceiverComponent;
    // title of the song we are currently playing
    AudioManager mAudioManager;
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mNotificationBuilder = null;
    private int counter = 0, incrementby = 1;

    public static boolean isRunning() {

        return isRunning;
    }

    public static int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public static PendingIntent getActionIntent(
            Context context, int mediaKeyEvent) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setPackage(context.getPackageName());
        intent.putExtra(Intent.EXTRA_KEY_EVENT,
                new KeyEvent(KeyEvent.ACTION_DOWN, mediaKeyEvent));
        return PendingIntent.getBroadcast(context, mediaKeyEvent, intent, 0);
    }

    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(x);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        mState = State.Preparing;
        Log.d(TAG, "Buffering");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private void sendMessageToUI() {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                // Send data as an Integer
                //  mClients.get(i).send(Message.obtain(null, MSG_SET_INT_VALUE, mPlayer.getCurrentPosition()));

                //Send data as a String
                Bundle b = new Bundle();
                try {
                    if (mPlayer != null && (mState != State.Stopped || mState != State.Preparing || mState != State.Retrieving)) {
                        b.putInt("cur", mPlayer.getCurrentPosition());
                        b.putInt("max", mPlayer.getDuration());
                    }
                } catch (Exception e) {
                    b.putInt("cur", 0);
                    b.putInt("max", 0);
                }
                Message msg = Message.obtain(null, MSG_SET_INT_VALUE, 0, 0);
                msg.setData(b);
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    private void sendMessageToActivity(String Name, String Link, String image, String id) {
        Intent intent = new Intent("SongUpdation");
        // You can also include some extra data.
        intent.putExtra("name", Name);
        intent.putExtra("source", Link);
        intent.putExtra("image", image);
        intent.putExtra("id", id);
        // Bundle b = new Bundle();
        //  b.putParcelable("Location", l);
        //  intent.putExtra("Location", b);
        Log.d(TAG, "Recieved url action" + image);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            // we want the media player to notify us when it's ready preparing, and when it's done
            // playing:
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        } else
            mPlayer.reset();
    }

    public int get_index_of_song(String id) {
        if (song_list != null)
            for (int x = 0; x < song_list.size(); x++) {
                if (song_list.get(x).getId().equals(id)) {
                    return x;

                }
            }
        return -9;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "debug: Creating service");
        isRunning = true;
        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        // Create the retriever and start an asynchronous task that will prepare it.

        // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
        if (android.os.Build.VERSION.SDK_INT >= 8)
            mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        else
            mAudioFocus = AudioFocus.Focused; // no focus feature, so we always "have" audio focus
        mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.logo);

        mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);
    }

    /**
     * Called when we receive an Intent. When we receive an intent sent to us via startService(),
     * this is the method that gets called. So here we react appropriately depending on the
     * Intent's action, which specifies what is being requested of us.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
if(action!=null){
    if (action.equals(ACTION_TOGGLE_PLAYBACK))
        processTogglePlaybackRequest();
        else if (action.equals(ACTION_PLAY)) processPlayRequest();
        else if (action.equals(ACTION_PAUSE)) processPauseRequest();
        else if (action.equals(ACTION_SKIP)) processSkipRequest();
        else if (action.equals(ACTION_STOP)) processStopRequest();
        else if (action.equals(ACTION_REWIND)) processRewindRequest();
        else if (action.equals(ACTION_URL)) processAddRequest(new Bundle());
}
        return START_NOT_STICKY; // Means we started the service, but don't want it to
        // restart in case it's killed.
    }

    void processTogglePlaybackRequest() {
        if (mState == State.Paused || mState == State.Stopped) {
            processPlayRequest();
        } else {
            processPauseRequest();
        }
        setUpAsForeground(Name + " (playing)");
    }

    void processPlayRequest() {
        if (mState == State.Retrieving) {
            // pIf we are still retrieving media, just set the flag to start playing when we're
            // ready
            mWhatToPlayAfterRetrieve = null; // play a random song
            mStartPlayingAfterRetrieve = true;
            return;
        }
        tryToGetAudioFocus();
        // actually play the song
        if (mState == State.Stopped) {
            // If we're stopped, just go ahead to the next song and start playing
            playNextSong(song_list.get(get_index_of_song(id)).getLink());

        } else if (mState == State.Paused) {
            // If we're paused, just continue playback and restore the 'foreground service' state.
            mState = State.Playing;

            configAndStartMediaPlayer();
        }
        // Tell any remote controls that our playback state is 'playing'.
        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat
                    .setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }
        //  setUpAsForeground("");
        setUpAsForeground(Name + " (playing)");
    }

    void processPauseRequest() {
        if (mState == State.Retrieving) {
            // If we are still retrieving media, clear the flag that indicates we should start
            // playing when we're ready
         //   setUpAsForeground(Name + " (paused)");
            mStartPlayingAfterRetrieve = false;
            return;
        }
        if (mState == State.Playing) {
            // Pause media player and cancel the 'foreground service' state.

            mPlayer.pause();
            mState = State.Paused;
            if (mPlayer.isPlaying())
                //       Shaked_Device.shaked_device.start_Detection(this);
                relaxResources(false); // while paused, we always retain the MediaPlayer
            // do not give up audio focus

        }
        // Tell any remote controls that our playback state is 'paused'.
        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat
                    .setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        }
        setUpAsForeground("");
    }

    void processRewindRequest() {

        Log.d(TAG, "Skip called");
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus();
            Bundle i;
            Log.d(TAG, "inside skip");
            i = new Bundle(1);
            int index;
            if (is_shuffeled) {
                index = randInt(0, song_list.size() - 1);


            } else {


                index = get_index_of_song(id) - 1;
                if (song_list != null)
                    if (index <= 0)
                        index = 0;


            }
            id = song_list.get(index).getId();
            i.putString("id", id);
            i.putString("name", song_list.get(index).getTitle());
            i.putString("source", song_list.get(index).getLink());
            i.putString("image", song_list.get(index).getImage());

            Log.d(TAG, "pressed Next Button " + song_list.get(get_index_of_song(id)).getTitle());
            try {
                Picasso.with(getApplicationContext()).load(song_list.get(get_index_of_song(id)).getImage()).into(MusicService.target);
            } catch (Exception e) {
            }
            processAddRequest(i);

        }

        //  setUpAsForeground("");
    }

    void processSkipRequest() {
        Log.d(TAG, "Skip called");
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus();
            Bundle i;
            Log.d(TAG, "inside skip");
            i = new Bundle(1);
            int index;
            if (is_shuffeled) {
                index = randInt(0, song_list.size() - 1);


            } else {


                index = get_index_of_song(id) + 1;
                if (song_list != null)
                    if (index >= song_list.size())
                        index = 0;


            }
            id = song_list.get(index).getId();
            i.putString("id", id);
            i.putString("name", song_list.get(index).getTitle());
            i.putString("source", song_list.get(index).getLink());
            i.putString("image", song_list.get(index).getImage());
            Log.d(TAG, "pressed Next Button " + song_list.get(get_index_of_song(id)).getTitle());
            try {
                Picasso.with(getApplicationContext()).load(song_list.get(get_index_of_song(id)).getImage()).into(MusicService.target);
            } catch (Exception e) {
            }
            processAddRequest(i);

        }

    }

    void processStopRequest() {
        processStopRequest(false);
    }

    void processStopRequest(boolean force) {
        if (mState == State.Playing || mState == State.Paused || force) {
            mState = State.Stopped;
            // let go of all resources...
            relaxResources(true);
            giveUpAudioFocus();
            // Tell any remote controls that our playback state is 'paused'.
            if (mRemoteControlClientCompat != null) {
                mRemoteControlClientCompat
                        .setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
            }
            stopForeground(true);
            // service` is no longer necessary. Will be started again if needed.
            stopSelf();
        }
    }

    /**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
     */
    void relaxResources(boolean releaseMediaPlayer) {
        // stop being a foreground service
        //   stopForeground(true);
        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mPlayer != null) {
            stopForeground(true);
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
            cancelNotification(this);
        }
        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) mWifiLock.release();
    }

    public void cancelNotification(Context ctx) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(NOTIFICATION_ID);
    }

    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
     * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
     * we have focus, it will play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
     * current focus settings. This method assumes mPlayer != null, so if you are calling it,
     * you have to do so from a context where you are sure this is the case.
     */
    void configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we know we have to resume
            // playback once we get the focus back.
            if (mPlayer.isPlaying()) mPlayer.pause();
            mState = State.Paused;
            return;
        } else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
        else
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud
        if (!mPlayer.isPlaying()) {
            mPlayer.start();
            setUpAsForeground("");
            mState = State.Playing;
            //    if(mPlayer.isPlaying())
            //        Shaked_Device.shaked_device.start_Detection(this);
        }
    }

    void processAddRequest(Bundle intent) {
        id = intent.getString("id");
        Name = intent.getString("name");
        image = intent.getString("image");
        Link = intent.getString("source");
        Log.d(TAG, "Process Add Request");
        sendMessageToActivity(Name, Link, image, id);
        if (mState == State.Retrieving) {
            mWhatToPlayAfterRetrieve = Uri.parse(Link);
            mStartPlayingAfterRetrieve = true;
        } else if (mState == State.Playing || mState == State.Paused || mState == State.Stopped) {
            Log.i(TAG, "Playing from URL/path: " + Name);
            mState = State.Retrieving;
            tryToGetAudioFocus();
            playNextSong(Link);
        }
    }

    void tryToGetAudioFocus() {
        try {
            if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                    && mAudioFocusHelper.requestFocus())
                mAudioFocus = AudioFocus.Focused;

        } catch (Exception e) {
            Log.d(TAG, "Exception in gettting audio focus :" + e.toString());
        }
    }

    /**
     * Starts playing the next song. If manualUrl is null, the next song will be randomly selected
     * from our Media Retriever (that is, it will be a random song in the user's device). If
     * manualUrl is non-null, then it specifies the URL or path to the song that will be played
     * next.
     */

    void playNextSong(String manualUrl) {
        mState = State.Retrieving;

        relaxResources(false); // release everything except MediaPlayer

        try {
            Picasso.with(this).load(image).into(target);

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Roore occured in loading Image:" + e.toString(), Toast.LENGTH_SHORT).show();

        }

        // Use the media button APIs (if available) to register ourselves for media button
        // events

        // starts preparing the media player in the background. When it's done, it will call
        // our OnPreparedListener (that is, the onPrepared() method on this class, since we set
        // the listener to 'this').
        //
        // Until the media player is prepared, we *cannot* call start() on it!
        // if (mPlayer != null)
        try {
            createMediaPlayerIfNeeded();
            mPlayer.setDataSource(manualUrl);
            mIsStreaming = manualUrl.startsWith("http:") || manualUrl.startsWith("https:");
            mState = State.Preparing;
            mPlayer.prepareAsync();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error" + manualUrl + e.toString(), Toast.LENGTH_SHORT).show();
        }


        // If we are streaming from the internet, we want to hold a Wifi lock, which prevents
        // the Wifi radio from going to sleep while the song is playing. If, on the other hand,
        // we are *not* streaming, we want to release the lock if we were holding it before.
        if (mIsStreaming) mWifiLock.acquire();
        else if (mWifiLock.isHeld()) mWifiLock.release();
    }

    /**
     * Called when media player is done playing current song.
     */
    public void onCompletion(MediaPlayer player) {
        // The media player finished playing the current song, so we go ahead and start the next.

        //  mState = State.Stopped;
        if (!is_looping) {

            processSkipRequest();
        } else {
            mState = State.Stopped;
            mPlayer.reset();
            mPlayer.start();
            setUpAsForeground("");

            //  Shaked_Device.shaked_device.start_Detection(this);
            //playNextSong(song_list.get(get_index_of_song(id)).getLink());
        }
    }

    /**
     * Called when media player is done preparing.
     */
    public void onPrepared(MediaPlayer player) {
        // The media player is done preparing. That means we can start playing!


        // updateNotification(Name + " (playing)");
        configAndStartMediaPlayer();
    }

    /**
     * Updates the notification.
     */
    void updateNotification(String text) {
        Intent switchIntent = new Intent("trevx.ptk.trevx.ACTION_PLAY");
        // PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 50,   switchIntent,   PendingIntent.FLAG_UPDATE_CURRENT);
        // PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 50, switchIntent, 0);

        mNotificationBuilder.setContentText(text);
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());

    }

    /**
     * Configures service as a foreground service. A foreground service is a service that's doing
     * something the user is actively aware of (such as playing music), and must appear to the
     * user as a notification. That's why we create the notification here.
     */
    void setUpAsForeground(String text) {


   //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mPlayer != null)
        {


            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && mPlayer != null) {

                loli_and_less();
            } else {

              after_lolipop();

            }
        }
        /*else {

        }*/

    }

    private void   after_lolipop() {

        Notification notificationa=null;


        MediaSession m;
        m = new MediaSession(getApplicationContext(), "PTK" + "." + TAG);

               // MediaSession.Token _mediaSessionToken = m.getSessionToken();


        m.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackState state = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY)
                .setState(PlaybackState.STATE_STOPPED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, mPlayer.getDuration())
                .setActions(PlaybackState.ACTION_SKIP_TO_NEXT)
                .setActions(PlaybackState.ACTION_SKIP_TO_PREVIOUS)
                .build();

        m.setPlaybackState(state);

        m.setActive(true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        if (mPlayer.isPlaying()) {
            notificationa = new Notification.Builder(this)
                    // Show controls on lock screen even when user hides sensitive content.
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    //
                    .setColor(getResources().getColor(R.color.progress_fron))
                    .setLargeIcon(pm)
                    .setSmallIcon(R.drawable.logo, 2)

                    // Add media control buttons that invoke intents in your media service
                    //    .addAction(R.drawable.back, "Previous", prevPendingIntent) // #0
                    .addAction(R.drawable.back, "", getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PREVIOUS)) // #0

                    .addAction(R.drawable.pause_player, "", getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))  // #1
                    .addAction(R.drawable.next, "", getActionIntent(this, KeyEvent.KEYCODE_MEDIA_NEXT)) // #0

                    //           .addAction(R.drawable.next, "Next", nextPendingIntent)     // #2 actionSKIP nextPendingIntent
                    // Apply the media style template
                    .setStyle(new Notification.MediaStyle()
                            .setShowActionsInCompactView(0,1,2)
                            .setMediaSession(m.getSessionToken()))
                    .setContentTitle(song_list.get(get_index_of_song(id)).getTitle())
                    .setContentText(" " + getHostName(song_list.get(get_index_of_song(id)).getLink()))
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)

                    .build();
            startForeground(NOTIFICATION_ID, notificationa);
       //     startForeground(NOTIFICATION_ID, notificationa);
        } else if (!mPlayer.isPlaying()) {
            notificationa = new Notification.Builder(this)
                    // Show controls on lock screen even when user hides sensitive content.
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    //
                    .setColor(getResources().getColor(R.color.progress_fron))
                    .setLargeIcon(pm)
                    .setSmallIcon(R.drawable.logo, 2)

                    // Add media control buttons that invoke intents in your media service
                      .addAction(R.drawable.back, "", getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PREVIOUS)) // #0
                    .addAction(R.drawable.play_player, "", getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))  // #1
                    .addAction(R.drawable.next, "", getActionIntent(this, KeyEvent.KEYCODE_MEDIA_NEXT)) // #0

                    //           .addAction(R.drawable.next, "Next", nextPendingIntent)     // #2 actionSKIP nextPendingIntent
                    // Apply the media style template
                    .setStyle(new Notification.MediaStyle()
                            .setShowActionsInCompactView(0,1,2)
                            .setMediaSession(m.getSessionToken()))
                    .setContentTitle(song_list.get(get_index_of_song(id)).getTitle())
                    .setContentText(" " + getHostName(song_list.get(get_index_of_song(id)).getLink()))
                    .setOngoing(false)
                    .setContentIntent(pendingIntent)

                    .build();


            notificationa.flags |= Notification.FLAG_AUTO_CANCEL;


            notificationa.defaults |= Notification.DEFAULT_LIGHTS;
            notificationa.flags = Notification.DEFAULT_LIGHTS;

            stopForeground(true);
            mNotificationManager.notify(NOTIFICATION_ID, notificationa);

        }



    }

    private void loli_and_less() {
        _mediaSession = new MediaSessionCompat(getApplicationContext(), "PTK" + "." + TAG);

        if (_mediaSession == null) {
            Log.e(TAG, "initMediaSession: _mediaSession = null");
            return;
        }

        MediaSessionCompat.Token _mediaSessionToken = _mediaSession.getSessionToken();
        mNotificationBuilder = new NotificationCompat.Builder(this);

        _mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY)
                .setActions(PlaybackStateCompat.ACTION_PAUSE)
                .setState(PlaybackStateCompat.STATE_STOPPED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                       mPlayer.getDuration())
                .setActions(PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
                .setActions(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)

                .build();

        _mediaSession.setPlaybackState(state);

        _mediaSession.setActive(true);
        actionTogglePlayBack = new Intent(MusicService.ACTION_TOGGLE_PLAYBACK);
        actionTogglePlayBack.setPackage(getPackageName());

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        //    PendingIntent play = PendingIntent.get(this, 0, new Intent( MusicService.ACTION_TOGGLE_PLAYBACK), 0);
        //  PendingIntent pause = PendingIntent.getService(this, 0, new Intent(MusicService.ACTION_PAUSE), 0);
        // if (mPlayer.isPlaying())


        Notification notification;
        if (mPlayer.isPlaying()) {
            notification = new NotificationCompat.Builder(this)
                    // Show controls on lock screen even when user hides sensitive content.
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    //
                    .setColor(getResources().getColor(R.color.progress_fron))
                    .setLargeIcon(pm)
                    .setSmallIcon(R.drawable.logo, 2)


                    .addAction(R.drawable.back, "", getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
                    .addAction(R.drawable.pause_player, "", getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))  // #1

                    //    mNotificationBuilder.addAction(R.drawable.play_player, "",  getActionIntent(this,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)) ; // #1

                    .addAction(R.drawable.next, "", getActionIntent(this, KeyEvent.KEYCODE_MEDIA_NEXT))

                    .setStyle(new android.support.v7.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0,1,2)

                            .setMediaSession(_mediaSession.getSessionToken()))
                    .setContentTitle(song_list.get(get_index_of_song(id)).getTitle())
                    .setContentText(" " + "trevx.com")

                    .setOngoing(true)

                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(pendingIntent)
                    .setDeleteIntent(getActionIntent(this, KeyEvent.KEYCODE_MEDIA_STOP)).build();
            startForeground(NOTIFICATION_ID, notification);
        } else {
            notification = new NotificationCompat.Builder(this)
                    // Show controls on lock screen even when user hides sensitive content.
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    //
                    .setColor(getResources().getColor(R.color.progress_fron))
                    .setLargeIcon(pm)
                    .setSmallIcon(R.drawable.logo, 2)
                    .addAction(R.drawable.back, "", getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
                    //.addAction(R.drawable.pause_player, "", getActionIntent(this,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))  // #1

                    .addAction(R.drawable.play_player, "", getActionIntent(this, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))  // #1

                    .addAction(R.drawable.next, "", getActionIntent(this, KeyEvent.KEYCODE_MEDIA_NEXT))

                    .setStyle(new android.support.v7.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0,1,2)

                            .setMediaSession(_mediaSession.getSessionToken()))
                    .setContentTitle(song_list.get(get_index_of_song(id)).getTitle())
                    .setContentText(" " + "trevx.com")

                    .setOngoing(false)

                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(pendingIntent)
                    .setDeleteIntent(getActionIntent(this, KeyEvent.KEYCODE_MEDIA_STOP)).build();

            stopForeground(true);
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }


        //  Notification notification = (mNotificationBuilder).build();



    }


    /**
     * Called when there's an error playing media. When this happens, the media player goes to
     * the Error state. We warn the user about the error and reset the media player.
     */
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // Toast.makeText(getApplicationContext(), "Media player error! Resetting.",
        //       Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));

        mState = State.Stopped;
        relaxResources(false);
        //giveUpAudioFocus();
        return true; // true indicates we handled the error
    }

    public void onGainedAudioFocus() {
        // Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
        mAudioFocus = AudioFocus.Focused;
        // restart media player with new focus settings
        if (mState == State.Playing)
            configAndStartMediaPlayer();
    }

    public void onLostAudioFocus(boolean canDuck) {
        try {

            // Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" :   "no duck"), Toast.LENGTH_SHORT).show();
            mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;
            // start/restart/pause media player with new focus settings
            if (mPlayer != null && mPlayer.isPlaying())
                configAndStartMediaPlayer();

        } catch (Exception e) {
            Log.d(TAG, "Error in lost Audio focus :" + e.toString());
        }

    }

    public void onMusicRetrieverPrepared() {
        // Done retrieving!
        mState = State.Stopped;
        // If the flag indicates we should start playing after retrieving, let's do that now.
        if (mStartPlayingAfterRetrieve) {
            tryToGetAudioFocus();
            playNextSong(mWhatToPlayAfterRetrieve == null ?
                    null : mWhatToPlayAfterRetrieve.toString());
        }
    }

    @Override
    public void onDestroy() {
        // Service is being killed, so make sure we release our resources
        //    Shaked_Device.shaked_device.stop_Detection();
        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
        isRunning = false;
        // Toast.makeText(getApplicationContext(),"Service DEstroyed",Toast.LENGTH_SHORT).show();
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


    // indicates the state our service:
    public enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    }

    enum PauseReason {
        UserRequest,  // paused by user request
        FocusLoss,    // paused because of audio focus loss
    }

    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            sendMessageToUI();

//            Log.d(TAG,"Sending message back with cu"+mPlayer.getCurrentPosition()+"/"+mPlayer.getDuration());
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_INT_VALUE: {
                    incrementby = msg.arg1;
                    if (incrementby == 0) {
                        Bundle bundle = msg.getData();
                        Name = bundle.getString("name");
                        id = bundle.getString("id");
                        Link = bundle.getString("source");
                        image = bundle.getString("image");

                        Intent i = new Intent();
            //            Toast.makeText(getApplicationContext(), "from incremt=0", Toast.LENGTH_SHORT).show();

                        i.putExtra("id from services", id);
                        i.putExtra("name from services", Name);
                        i.putExtra("source from services", Link);
                        i.putExtra("image from services", pm);

                        MediaButtonHelper.registerMediaButtonEventReceiverCompat(
                                mAudioManager, mMediaButtonReceiverComponent);
                        // Update the remote controls
                        if (mRemoteControlClientCompat == null) {
                            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                            intent.setComponent(mMediaButtonReceiverComponent);
                            mRemoteControlClientCompat = new RemoteControlClientCompat(
                                    PendingIntent.getBroadcast(getApplicationContext() /*context*/,
                                            0 /*requestCode, ignored*/, intent /*intent*/, 0 /*flags*/));
                            RemoteControlHelper.registerRemoteControlClient(mAudioManager,
                                    mRemoteControlClientCompat);
                        }
                        mRemoteControlClientCompat.setPlaybackState(
                                RemoteControlClient.PLAYSTATE_PLAYING);

                        mRemoteControlClientCompat.setTransportControlFlags(
                                RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                                        RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                                        RemoteControlClient.FLAG_KEY_MEDIA_STOP);

                        mRemoteControlClientCompat.editMetadata(true)
                                //   .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, Name)
                                .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, getHostName(Link))
                                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, Name)

                                // TODO: fetch real item artwork
                                .putBitmap(
                                        RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK,
                                        pm)
                                .apply();
                        processAddRequest(bundle);

                    } else if (incrementby == 1) {
                        sendMessageToUI();
                    } else if (incrementby == 2) {
                        Log.d(TAG, "playing or pausing");

                        processTogglePlaybackRequest();
                    } else if (incrementby == 3) {
                        processRewindRequest();
                    } else if (incrementby == 4)
                        processSkipRequest();


                }
                break;
                default:
                    super.handleMessage(msg);
            }
        }
    }



}