package trevx;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import trevx.Musicplayer.MusicService;
import trevx.com.trevx.R;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link music_playerConfigureActivity music_playerConfigureActivity}
 */public class music_player extends AppWidgetProvider {

    private final String ACTION_WIDGET_PLAY = "PlaySong";
    private final String ACTION_WIDGET_PAUSE = "PauseSong";
    private final String ACTION_WIDGET_STOP = "StopSong";
    private final int INTENT_FLAGS = 0;
    private final int REQUEST_CODE = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        RemoteViews controlButtons = new RemoteViews(context.getPackageName(),
                R.layout.music_player);

        Intent actionTogglePlayBack = new Intent(MusicService.ACTION_TOGGLE_PLAYBACK);
        actionTogglePlayBack.setPackage(context.getPackageName());
        PendingIntent pausePendingIntent = PendingIntent.getService(context.getApplicationContext(), 50, actionTogglePlayBack, 0);

        Intent pauseIntent = new Intent(context, MusicService.class);

        Intent stopIntent = new Intent(context, MusicService.class);



      //  PendingIntent pausePendingIntent = PendingIntent.getService(       context, REQUEST_CODE, pauseIntent, INTENT_FLAGS);
      //  PendingIntent stopPendingIntent = PendingIntent.getService(
       //         context, REQUEST_CODE, stopIntent, INTENT_FLAGS);

     //   controlButtons.setOnClickPendingIntent(          R.id.pause, pausePendingIntent);
      //  controlButtons.setOnClickPendingIntent(       R.id.btnPause, pausePendingIntent);
    //    controlButtons.setOnClickPendingIntent(          R.id.btnStop, stopPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, controlButtons);
    }
}