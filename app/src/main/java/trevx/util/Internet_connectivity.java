package trevx.util;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.support.design.widget.Snackbar;
import android.view.View;

import trevx.MainActivity;

/**
 * Created by ptk on 6/22/16.
 */
public class Internet_connectivity {

   public static  Snackbar snackbar;
    public static boolean isNetworkAvailable(final Context context) {

        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        boolean con= connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();

        if(con)
        { if(snackbar!= null)
            snackbar.dismiss();}
        return con;
    }

    public static boolean check_connection(final Context context)
    {
        if (!Internet_connectivity.isNetworkAvailable(context)) {
             snackbar = Snackbar
                    .make(MainActivity.layout, "No internet connection!", Snackbar.LENGTH_INDEFINITE
                    )
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // doSearch(queryStr);
                            check_connection(context);
                        }
                    });
            // Changing message text color
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();

            return  false;
        }
        return true;
    }
}
