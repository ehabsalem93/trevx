package trevx;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import trevx.com.trevx.R;


public class SearchStaticListSupportFragment extends Fragment {
    public static ListView listView;
    public static ListViewAdapter adapter;
    public static Context context;
    static View rootView;
    public SearchStaticListSupportFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_search_static_list, container, false);
        listView = (ListView) rootView.findViewById(R.id.search_static_list);
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            strings.add("");
        }
        // adapter = new ListViewAdapter(getActivity(), strings);
        listView.setAdapter(adapter);
        context = getActivity();

        return rootView;
    }



}
