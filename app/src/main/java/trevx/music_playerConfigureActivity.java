package trevx;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import trevx.Musicplayer.MusicService;
import trevx.com.trevx.R;

/**
 * The configuration screen for the {@link music_player music_player} AppWidget.
 */
public class music_playerConfigureActivity extends Activity {

    public static final String TAG = "widget music player";
    private static final String PREFS_NAME = "music_player";
    private static final String PREF_PREFIX_KEY = "trevx";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    EditText mAppWidgetText;


    public music_playerConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default1 from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.music_player_configure);
//        mAppWidgetText = (EditText) findViewById(R.id.appwidget_text);
        //findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an trevx widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        AppCompatImageButton play= (AppCompatImageButton) findViewById(R.id.pause);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent serviceIntent = new Intent(MusicService.ACTION_TOGGLE_PLAYBACK);
                serviceIntent.setPackage(getPackageName());
                startService(serviceIntent);
                if (MusicService.mPlayer.isPlaying())
                    MusicService.mState = MusicService.State.Playing;
            }
        });


//        mAppWidgetText.setText(loadTitlePref(music_playerConfigureActivity.this, mAppWidgetId));
    }
    public void init()
    {
        final AppCompatImageButton play = (AppCompatImageButton) findViewById(R.id.play);
        final AppCompatImageButton pause = (AppCompatImageButton) findViewById(R.id.pause);
        TextView song_title= (TextView) findViewById(R.id.song_name);
        song_title.setText("");
        
        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (null != MusicService.mPlayer) {


                    Log.d(TAG, "u clicked to pause the song");
                    Intent serviceIntent = new Intent(MusicService.ACTION_PLAY);
                    serviceIntent.setPackage(getPackageName());
                    startService(serviceIntent);
                    if (MusicService.mPlayer.isPlaying())
                        MusicService.mState = MusicService.State.Playing;
                }
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    }
}

