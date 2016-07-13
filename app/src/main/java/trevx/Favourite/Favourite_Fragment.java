package trevx.Favourite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;

import trevx.Song_Manager.Song;
import trevx.com.trevx.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Favourite_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Favourite_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Favourite_Fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static TextView noresult;
    public static  boolean stop=false;
    public static Favourite_Fragment_Adapter adapter;
    public static String TAG="Favourite_Fragment";
    View view;
    Context context;
    SwipeRefreshLayout swipe;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private BroadcastReceiver mMessageReceiverq = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            String ids = intent.getStringExtra("id");


            if (ids != null) {
                if(Favourite_Main_API.check_exist(ids)) {
                    Favourite_Main_API.removewithid(ids);
                    setAdapterf(view);
                }


            }


        }
    };
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String Name = intent.getStringExtra("name");
            String ids = intent.getStringExtra("id");
            String Link = intent.getStringExtra("source");
            String imgs = intent.getStringExtra("image");


            if (Link != null) {
                if(!Favourite_Main_API.check_exist(ids)) {
                    Favourite_Main_API.addtolist(new Song(Name, ids, imgs, Link));
//                adapter.notifyDataSetChanged();
                    setAdapterf(view);
                }


            }


        }
    };

    // public static Favourite_Fragment_Adapter adapter;
    public Favourite_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Favourite_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Favourite_Fragment newInstance(String param1, String param2) {
        Favourite_Fragment fragment = new Favourite_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_favourite, container, false);
         context = view.getContext();

        swipe= (SwipeRefreshLayout) view.findViewById(R.id.main_content);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setAdapterf(view);
                swipe.setRefreshing(false);
            }
        });

setAdapterf(view);

        LocalBroadcastManager.getInstance(context).registerReceiver(
                mMessageReceiver, new IntentFilter("AddtoFavourite"));

        LocalBroadcastManager.getInstance(context).registerReceiver(
                mMessageReceiverq, new IntentFilter("removefromfavourite"));
        return view;
    }

    public   void setAdapterf(View view)
    {

        noresult= (TextView) view.findViewById(R.id.noresult);
        if(Favourite_Main_API.favouriteList != null)
        {
            if( Favourite_Main_API.favouriteList.size()>0) {
                noresult.setVisibility(View.GONE);
                RecyclerView rv = (RecyclerView) view.findViewById(R.id.rv);
                rv.setHasFixedSize(true);
                LinearLayoutManager llm = new LinearLayoutManager(context);
                rv.setLayoutManager(llm);
                adapter= new Favourite_Fragment_Adapter(Favourite_Main_API.favouriteList, context);

                ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        //Remove swiped item from list and notify the RecyclerView
                        // Toast.makeText(context,+"",Toast.LENGTH_LONG).show();


                        Favourite_Main_API.removesong(viewHolder.getPosition());
                        adapter.notifyItemRemoved(swipeDir);
                        //Toast.makeText(context,"Swiped number"+ swipeDir,Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        if(adapter.getItemCount()==0)
                        {noresult.setVisibility(View.VISIBLE);}else
                            noresult.setVisibility(View.GONE);

                    }
                };

                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

                itemTouchHelper.attachToRecyclerView(rv);



                //   rv.addOnItemTouchListener(swipeTouchListener);
                rv.setAdapter(adapter);
            }

        }else
        {//Toast.makeText(context, "Ops, Error occured while retrieving data, please try again", Toast.LENGTH_LONG).show();
            noresult.setVisibility(View.VISIBLE);
        }



    }

    public void setadapter1(){


        if(Favourite_Main_API.favouriteList != null)
        {
            if( Favourite_Main_API.favouriteList.size()>0) {

                RecyclerView rv = (RecyclerView) view.findViewById(R.id.rv);
                rv.setHasFixedSize(true);
                LinearLayoutManager llm = new LinearLayoutManager(context);
                rv.setLayoutManager(llm);
                final Favourite_Fragment_Adapter adapter = new Favourite_Fragment_Adapter(Favourite_Main_API.favouriteList, context);


                rv.setNestedScrollingEnabled(false);
                SwipeableRecyclerViewTouchListener swipeTouchListener =
                        new SwipeableRecyclerViewTouchListener(rv,
                                new SwipeableRecyclerViewTouchListener.SwipeListener() {
                                    @Override
                                    public boolean canSwipeLeft(int position) {
                                        return true;
                                    }

                                    @Override
                                    public boolean canSwipeRight(int position) {
                                        return true;
                                    }

                                    @Override
                                    public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                        for (int position : reverseSortedPositions) {
                                            // mItems.remove(position);
                                            adapter.notifyItemRemoved(position);
                                        }
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                        for (int position : reverseSortedPositions) {
                                            //   mItems.remove(position);
                                            adapter.notifyItemRemoved(position);
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                });

                rv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        return false;
                    }
                });
                rv.addOnItemTouchListener(swipeTouchListener);
                rv.setAdapter(adapter);
            }
            else {
                noresult.setVisibility(View.VISIBLE);
        }

        }else
            noresult.setVisibility(View.VISIBLE);




    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
