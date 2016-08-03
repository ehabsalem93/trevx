package trevx.Search;

/**
 * Created by ptk on 7/21/16.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.riontech.staggeredtextgridview.utils.ColorGenerator;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import trevx.MainActivity;
import trevx.com.trevx.R;


public class WordsAdapter extends BaseAdapter {

    private Context context;
    private JSONArray jArray;
    private String TAG = "Search TAG Adapter";

    public WordsAdapter(Context context, JSONArray jArray) {
        this.context = context;
        this.jArray = jArray;
    }

    @Override
    public int getCount() {
        if (jArray == null)
            return 0;
        return jArray.length();
    }

    @Override
    public String getItem(int position) {
        try {
            return jArray.getString(position);
        } catch (Exception e) {
            return "Sample";
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) LayoutInflater.from(context).inflate(R.layout.row_item_word, null);

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.selector_item);

        drawable.setColorFilter(ColorGenerator.DEFAULT.getRandomColor(), PorterDuff.Mode.SRC_ATOP);
        view.setHeight(110);
        view.setBackground(drawable);


        try {
            view.setText(jArray.getString(position));
            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Toast.makeText(context, ((TextView) v).getText().toString(), Toast.LENGTH_LONG).show();
                    try {
                        MainActivity.doSearch(((TextView) v).getText().toString());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error: " + e.toString());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error: " + e.toString());
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }
}