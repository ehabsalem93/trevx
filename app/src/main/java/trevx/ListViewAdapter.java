package trevx;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import trevx.com.trevx.R;

/**
 * Created by ptk on 7/3/16.
 */
public class ListViewAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> values;

    public ListViewAdapter(Context context, List<String> objects) {
        super(context, R.layout.search_static_list_item, objects);
        this.context = context;
        this.values = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        View rowView = inflater.inflate(R.layout.search_static_list_item, parent, false);

        final String query = values.get(position);

        AppCompatImageButton auto = (AppCompatImageButton) rowView.findViewById(R.id.auto);
        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.searchViewLayout.setExpandedText(query + " ");

            }
        });


        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    MainActivity.doSearch(values.get(position));
                    SearchStaticListSupportFragment.rootView.setVisibility(View.GONE);
                    MainActivity.searchViewLayout.collapse();
                }catch(Exception e) {

                }
            }
        });
        TextView textView = (TextView) rowView.findViewById(R.id.card_details);
        textView.setText(values.get(position));


        return rowView;
    }
}