package trevx.Search;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.riontech.staggeredtextgridview.StaggeredTextGridView;

import it.gmariotti.recyclerview.itemanimator.SlideInOutRightItemAnimator;
import trevx.Musicplayer.MusicService;
import trevx.Song_Manager.trevx_api.get_search_tag;
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
    public static Search_FRagment_Adapter adapter;
    public static int Song_count;
    public static View view;
    static RecyclerView rv;
    public final String TAG = "search";
    Context context;
    //song list variables
    //private ArrayList<Song> songList;
    private ListView songView;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


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

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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

        new get_search_tag().execute();
        pull_request();







       /*
*/
            return view;

    }





    public void pull_request()
    {
        if(!Internet_connectivity.check_connection(context))
        {
            return;
        }
        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        //WrapContentLinearLayoutManager llm = new WrapContentLinearLayoutManager(context);
        RecyclerView.LayoutManager llm = new LinearLayoutManager(context);
        rv.setLayoutManager(llm);

        //   adapter=new Search_FRagment_Adapter(null,context);


//        adapter.setOnLoadMoreListener(mLoadMoreListener2);
//        rv.setShouldLoadMore(true);


    }





    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


    }


    public void setadapter2(boolean new_swearch) {


        if (new_swearch) {
            Glide.get(getActivity()).clearMemory();
        }


        if (!Internet_connectivity.check_connection(context)) {
            return;
        }

        else

        if(trevx_api.song_list != null)
        {
            if (trevx_api.song_list.size() > 0) {

                adapter = new Search_FRagment_Adapter(trevx_api.song_list, context);

                rv.setAdapter(adapter);
                SlideInOutRightItemAnimator slideInOutRightItemAnimator = new SlideInOutRightItemAnimator(rv);
                slideInOutRightItemAnimator.setAddDuration(5000);
                rv.setItemAnimator(slideInOutRightItemAnimator);
                adapter.setOnLoadMoreListener(new OnLoadMoreListnert() {
                    @Override
                    public void onLoadMore() {
                        if (adapter.song_list.size() < Song_count) {
                            int index = trevx_api.song_list.size();
                            new get_song_trevx_fill().execute();
                            for (int x = index; x < (trevx_api.song_list.size()); x++) {
                                adapter.song_list.add(trevx_api.song_list.get(x));

                                adapter.notifyItemInserted(adapter.song_list.size());
                            }

                            //adapter.notifyDataSetChanged();

                        } else {
                            StaggeredTextGridView staggeredTextGridView = (StaggeredTextGridView) Search_Fragment.view.findViewById(R.id.staggeredTextView);
                            staggeredTextGridView.setVisibility(View.VISIBLE);

                        }
                    }
                });


            } else {

                try {
                    new get_song_trevx_fill().execute();
                    //new getSong().get_more_song();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "Error in filling more song: " + e.toString());
                }

                if (trevx_api.song_list.size() > 0) {

                    adapter = new Search_FRagment_Adapter(trevx_api.song_list, context);
                    rv.setAdapter(adapter);
                    SlideInOutRightItemAnimator slideInOutRightItemAnimator = new SlideInOutRightItemAnimator(rv);
                    slideInOutRightItemAnimator.setAddDuration(5000);
                    rv.setItemAnimator(slideInOutRightItemAnimator);
                    rv.setHasFixedSize(true);
                    ;
                    adapter.setOnLoadMoreListener(new OnLoadMoreListnert() {
                        @Override
                        public void onLoadMore() {
                            if (adapter.song_list.size() < Song_count) {
                                int index = trevx_api.song_list.size();
                                try {
                                    new get_song_trevx_fill().execute();
                                    // new getSong().get_more_song();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "Error in filling more song: " + e.toString());
                                }

                                for (int x = index; x < (trevx_api.song_list.size()); x++) {
                                    adapter.song_list.add(trevx_api.song_list.get(x));


                                }

                                adapter.notifyDataSetChanged();

                            } else {
                                StaggeredTextGridView staggeredTextGridView = (StaggeredTextGridView) Search_Fragment.view.findViewById(R.id.staggeredTextView);
                                staggeredTextGridView.setVisibility(View.VISIBLE);
                            }
                        }
                    });


                }
            }
        }else {
            //  Toast.makeText(context, "", Toast.LENGTH_LONG).show();
            // Snackbar.make(MainActivity.layout,"Ops, Error occured while retrieving data, try again ",Snackbar.LENGTH_SHORT).setActionTextColor(context.getResources().getColor(R.color.cardview_dark_background)).show();
            StaggeredTextGridView staggeredTextGridView = (StaggeredTextGridView) Search_Fragment.view.findViewById(R.id.staggeredTextView);
            staggeredTextGridView.setVisibility(View.VISIBLE);
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
