package trevx.home;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import trevx.MainActivity;
import trevx.Song_Manager.Song;
import trevx.com.trevx.R;
import trevx.trevx_home_api.home_api;
import trevx.trevx_home_api.home_artist;
import trevx.trevx_home_api.home_category;
import trevx.trevx_home_api.home_song;
import trevx.trevx_home_api.home_word_discovery;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link home.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class home extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    View view;
    SectionedRecyclerViewAdapter sectionAdapter;
    Context context;
    MainActivity activity;
    private SliderLayout mDemoSlider;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public home() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment home.
     */
    // TODO: Rename and change types and number of parameters
    public static home newInstance(String param1, String param2) {
        home fragment = new home();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        Toast.makeText(getActivity(), slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Log.d("Slider Demo", "Page Changed: " + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
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
        view = inflater.inflate(R.layout.fragment_home2, container, false);
        //setApater();
        activity = (MainActivity) MainActivity.context;
        mDemoSlider = (SliderLayout) view.findViewById(R.id.slider);

        if (null == home_category.categories)
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        for (int x = 0; x < home_category.categories.size(); x++) {
            TextSliderView textSliderView = new TextSliderView(getActivity());
            // initialize a SliderLayout
            final int finalX = x;
            textSliderView
                    .description(home_category.categories.get(x).getQtitle())
                    .image(home_category.categories.get(x).getImagelink())
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                        @Override
                        public void onSliderClick(BaseSliderView slider) {
                            try {
                                activity.doSearch(home_category.categories.get(finalX).getSearchq());
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    });

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra", home_category.categories.get(x).getQtitle());

            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Stack);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(2000);

        mDemoSlider.addOnPageChangeListener(this);

        sectionAdapter = new SectionedRecyclerViewAdapter();
        sectionAdapter.addSection(new SongSection(getString(R.string.most_popular_Songs_topic), home_song.home_song_list));
        sectionAdapter.addSection(new ArtistSection(getString(R.string.top_rated_artist), home_artist.getHome_atists()));
        sectionAdapter.addSection(new ArtistSection(getString(R.string.top_rated_discovery), home_word_discovery.getHome_word_discovery_list()));

        //   sectionAdapter.addSection(new ArtistSection(getString(R.string.most_popular_Songs_topic), getMostPopularMoviesList()));


        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        GridLayoutManager glm = new GridLayoutManager(getContext(), 2);

        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (sectionAdapter.getSectionItemViewType(position)) {
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
                        return 2;
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_FOOTER:
                        return 2;
                    default:
                        return 1;
                }
            }
        });

        recyclerView.setLayoutManager(glm);
        recyclerView.setAdapter(sectionAdapter);

        return view;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class ArtistSection extends StatelessSection {

        String title;
        List<home_api> list;

        public ArtistSection(String title, List<home_api> list) {
            super(R.layout.section_4, R.layout.section_4_item);

            this.title = title;
            this.list = list;
        }

        @Override
        public int getContentItemsTotal() {
            return list.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            String name = list.get(position).getQtitle();
   

            itemHolder.tvItem.setText(name);

            //  itemHolder.imgItem.setImageResource(R.drawable.trevx);
            Picasso.with(getActivity()).load(list.get(position).getImagelink()).fit().into(itemHolder.imgItem);
            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), String.format("Clicked on position #%s of Section %s",
                            sectionAdapter.getSectionPosition(itemHolder.getAdapterPosition()), title),
                            Toast.LENGTH_SHORT).show();

                    try {
                        activity.doSearch(list.get(position).getSearchq());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.tvTitle.setText(title);

            headerHolder.btnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), String.format("Clicked on more button from the header of Section %s",
                            title),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getFooterViewHolder(View view) {
            return super.getFooterViewHolder(view);
        }

        @Override
        public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
            super.onBindFooterViewHolder(holder);
//            FooterViewHolder footer= (FooterViewHolder) holder;
         /*   ((FooterViewHolder) holder).btnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(),"More clicked",Toast.LENGTH_LONG).show();
                }
            });*/

        }
    }

    class SongSection extends StatelessSection {

        String title;
        LinkedList<Song> list;


        public SongSection(String title, LinkedList<Song> list) {
            //  super(R.layout.section_4, R.layout.section_4_footer, R.layout.section_4_song);
            super(R.layout.section_4, R.layout.section_4_song);

            this.title = title;
            this.list = list;
        }


        @Override
        public int getContentItemsTotal() {

            return list.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            {
                String name = list.get(position).getTitle();


                itemHolder.tvItem.setText(name);
//if(list.get(position).getImage()!=null)
                try {
                    Picasso.with(getActivity()).load(list.get(position).getImage()).error(R.drawable.trevx).fit().into(itemHolder.imgItem);
                } catch (Exception e) {
                    Picasso.with(getActivity()).load(R.drawable.brand).fit().into(itemHolder.imgItem);
                }
                itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
          /*  Toast.makeText(getContext(), String.format("Clicked on position #%s of Section %s",
                    sectionAdapter.getSectionPosition(itemHolder.getAdapterPosition()), title),
                    Toast.LENGTH_SHORT).show();*/
                        activity.configure_small_player(list.get(position).getId(), list.get(position).getTitle(), list.get(position).getLink(), list.get(position).getImage(), list);

                    }
                });

            }
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.tvTitle.setText(title);

            headerHolder.btnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), String.format("Clicked on more button from the header of Section %s",
                            title),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getFooterViewHolder(View view) {
            return super.getFooterViewHolder(view);
        }

        @Override
        public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
            super.onBindFooterViewHolder(holder);
//            FooterViewHolder footer= (FooterViewHolder) holder;
         /*   ((FooterViewHolder) holder).btnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(),"More clicked",Toast.LENGTH_LONG).show();
                }
            });*/

        }
    }


    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;
        private final Button btnMore;

        public HeaderViewHolder(View view) {
            super(view);

            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            btnMore = (Button) view.findViewById(R.id.btnMore);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        private final Button btnMore;

        public FooterViewHolder(View view) {
            super(view);

            btnMore = (Button) view.findViewById(R.id.btnMore);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final ImageView imgItem;
        private final ImageView playitem;
        private final TextView tvItem;


        public ItemViewHolder(View view) {
            super(view);

            rootView = view;
            imgItem = (ImageView) view.findViewById(R.id.imgItem);
            tvItem = (TextView) view.findViewById(R.id.tvItem);
            playitem = (ImageView) view.findViewById(R.id.ic_play);

        }
    }



}
