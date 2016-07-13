package trevx.Search;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.sa90.infiniterecyclerview.InfiniteRecyclerView;
import com.sa90.infiniterecyclerview.listener.OnLoadMoreListener;

import java.util.ArrayList;

import trevx.Musicplayer.MusicService;
import trevx.Song_Manager.Song;
import trevx.Song_Manager.get_Song_trevx;
import trevx.Song_Manager.trevx_api.get_song_trevx_fill;
import trevx.Song_Manager.trevx_api.trevx_api;
import trevx.com.trevx.R;
import trevx.util.Internet_connectivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Search_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Search_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Search_Fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static boolean stop=false;
    public static Search_FRagment_Adapter1 adapter;
    public final String TAG = "search";
    public static int Song_count;
    SwipeRefreshLayout swipeRefreshLayout;
    View view;
    TextView noresult;
    Context context;
    //song list variables
    private ArrayList<Song> songList;
    private ListView songView;
    InfiniteRecyclerView rv;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public Search_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Search_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Search_Fragment newInstance(String param1, String param2) {
        Search_Fragment fragment = new Search_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
        songList=trevx_api.song_list;
        songList=trevx_api.song_list;
        outState.putParcelableArrayList("trevx",songList);
        */
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        if(savedInstanceState != null ) {
            trevx_api.song_list = savedInstanceState.getParcelableArrayList("trevx");
         //   Log.d("dataaa",songList.toString());
            MainActivity.searchFragment.setadapter();

        }
        */

            if (getArguments() != null) {

            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        setRetainInstance(true);



    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search__fragments, container, false);
        context = view.getContext();



        pull_request();

            return view;

    }


    private OnLoadMoreListener mLoadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            Log.v("Main", "Load more fired");

            { rv.setShouldLoadMore(true);

                if(Song_count > trevx_api.song_list.size()) {
                    new get_song_trevx_fill().execute();
                    // adapter=new Search_FRagment_Adapter1(trevx_api.song_list,context);

                    rv.setShouldLoadMore(true);

                    rv.moreDataLoaded();

                }else
                {
                    rv.setShouldLoadMore(false);
                }
              // rv.moreDataLoaded();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {



                    }
                }, 2000);
           }

        }
    };

    Runnable x=new Runnable() {
        @Override
        public void run() {
            new get_song_trevx_fill().execute();
            rv.moreDataLoaded();
            rv.setShouldLoadMore(false);



        }
    };
    public void pull_request()
    {
        if(!Internet_connectivity.check_connection(context))
        {
            return;
        }
        rv = (InfiniteRecyclerView) view.findViewById(R.id.rv);
        WrapContentLinearLayoutManager llm = new WrapContentLinearLayoutManager(context);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        rv.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                rv.setShouldLoadMore(true);
                if(Song_count >trevx_api.song_list.size()) {
                    new get_song_trevx_fill().execute();
                    // adapter=new Search_FRagment_Adapter1(trevx_api.song_list,context);

                    rv.setShouldLoadMore(true);

                    rv.moreDataLoaded();

                }else
                {
                    rv.setShouldLoadMore(false);
                }
            }
        });
//        rv.setShouldLoadMore(true);

        swipeRefreshLayout= (SwipeRefreshLayout) view.findViewById(R.id.swipelayuout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
              //  setadapter1();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    public class WrapContentLinearLayoutManager extends LinearLayoutManager {

        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("probe", "meet a IOOBE in RecyclerView");
            }
        }
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


    }


    public void setadapter1(){

        noresult=(TextView) view.findViewById(R.id.noresult);

if(!Internet_connectivity.check_connection(context))
{
return;
}

        else

        if(trevx_api.song_list != null)
        {
            rv = (InfiniteRecyclerView) view.findViewById(R.id.rv);
            WrapContentLinearLayoutManager llm = new WrapContentLinearLayoutManager(context);
            rv.setLayoutManager(llm);
            rv.setHasFixedSize(true);
//          ;
            rv.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    rv.setShouldLoadMore(true);
                    if(Song_count >trevx_api.song_list.size()) {
                        new get_song_trevx_fill().execute();
                        // adapter=new Search_FRagment_Adapter1(trevx_api.song_list,context);

                        rv.setShouldLoadMore(true);

                        rv.moreDataLoaded();

                    }else
                    {
                        rv.setShouldLoadMore(false);
                    }
                }
            });
            swipeRefreshLayout.setRefreshing(true);


            noresult.setVisibility(View.GONE);
    if( trevx_api.song_list .size()>0) {
        adapter = new Search_FRagment_Adapter1(trevx_api.song_list , context);

        rv.setAdapter(adapter);
        rv.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                rv.setShouldLoadMore(true);
                if(Song_count >trevx_api.song_list.size()) {
                    new get_song_trevx_fill().execute();
                    // adapter=new Search_FRagment_Adapter1(trevx_api.song_list,context);

                    rv.setShouldLoadMore(true);

                    rv.moreDataLoaded();

                }else
                {
                    rv.setShouldLoadMore(false);
                }
            }
        });


    }else
    {

        new get_Song_trevx();
        if( trevx_api.song_list .size()>0) {

             adapter = new Search_FRagment_Adapter1(trevx_api.song_list , context);
            rv.setAdapter(adapter);
            rv.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    rv.setShouldLoadMore(true);
                    if(Song_count >trevx_api.song_list.size()) {
                        new get_song_trevx_fill().execute();
                        // adapter=new Search_FRagment_Adapter1(trevx_api.song_list,context);

                        rv.setShouldLoadMore(true);

                        rv.moreDataLoaded();

                    }else
                    {
                        rv.setShouldLoadMore(false);
                    }
                }
            });


        }
    }
            swipeRefreshLayout.setRefreshing(false);
        }else {
          //  Toast.makeText(context, "", Toast.LENGTH_LONG).show();
           // Snackbar.make(MainActivity.layout,"Ops, Error occured while retrieving data, try again ",Snackbar.LENGTH_SHORT).setActionTextColor(context.getResources().getColor(R.color.cardview_dark_background)).show();
            noresult.setVisibility(View.VISIBLE);
        }




    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        if(MusicService.mPlayer!=null)

        stop=true;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



}
