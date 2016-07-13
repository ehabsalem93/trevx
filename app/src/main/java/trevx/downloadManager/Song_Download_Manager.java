package trevx.downloadManager;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

/**
 * Created by ptk on 6/2/16.
 */
public class Song_Download_Manager {
    public Song_Download_Manager() {
    }

    public void download(String fileName,String fileurl,Context context)
    {
        String servicestring = Context.DOWNLOAD_SERVICE;
        DownloadManager downloadmanager;

        Uri uri = Uri
                .parse(fileurl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setMimeType("audio/MP3");
        request.setTitle("trevx_"+fileName+".mp3");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "trevx_"+fileName+".mp3");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }


        downloadmanager = (DownloadManager) context.getSystemService(servicestring);

        Long reference = downloadmanager.enqueue(request);
    }
}
