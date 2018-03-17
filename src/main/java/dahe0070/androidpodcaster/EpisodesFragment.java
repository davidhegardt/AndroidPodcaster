package dahe0070.androidpodcaster;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EpisodesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EpisodesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EpisodesFragment extends Fragment implements SearchView.OnQueryTextListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<PodEpisode> epList;
    private Podcast currPodcast;
    private SearchView searchView;
    private MenuItem search;
    private ListView listView;
    Recycler_View_Adapter recyclerViewAdapter;
    private DatabaseHandler podDatabase;
    private PlayerClicks listener;
    private PodAdder listener_two;
    private static String TYPE = "EPISODES";
    private boolean adapterMode = false;

    private OnFragmentInteractionListener mListener;

    public EpisodesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
   //  * @param param1 Parameter 1.
   //  * @param param2 Parameter 2.
     * @return A new instance of fragment EpisodesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EpisodesFragment newInstance(ArrayList<PodEpisode> mList, Podcast currPodcast,boolean adapter) {
        EpisodesFragment fragment = new EpisodesFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);
        fragment.epList = mList;
        fragment.currPodcast = currPodcast;
        fragment.adapterMode = adapter;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        podDatabase = new DatabaseHandler(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_podcast,menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        search = menu.findItem(R.id.searchPodcast);

        searchView = (SearchView) MenuItemCompat.getActionView((menu.findItem(R.id.searchPodcast)));

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Intent i = getActivity().getIntent();
        if(!this.adapterMode) {
            epList = (ArrayList<PodEpisode>) i.getSerializableExtra("Episodes");

            currPodcast = (Podcast) i.getSerializableExtra("currPodcast");
        }

        View view = inflater.inflate(R.layout.fragment_episodes, container, false);

        swipeBack();

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return viewGesture.onTouchEvent(event);
            }
        });



        return view;
    }

    GestureDetector viewGesture;

    private void swipeBack(){
        viewGesture = new GestureDetector(getActivity(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int SWIPE_MIN_DISTANCE = 120;
                final int SWIPE_MAX_OFF_PATH = 250;
                final int SWIPE_THRESHOLD_VELOCITY = 200;
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        listener.navigateToPodlist();
                }

                return true;
            }
        });
    }

    private boolean isTextViewClicked = false;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (PodEpisode i : epList){
            i.setBackupImage(currPodcast.getBackupImage());
        }


        ImageButton podImage = (ImageButton) view.findViewById(R.id.imgCurrPod);
        //podImage.setImageBitmap(currPodcast.getPodImage());
        Picasso.with(getActivity()).load(currPodcast.getPodImageUrl()).resize(240,240).centerCrop().into(podImage);

        TextView podName = (TextView) view.findViewById(R.id.txtHeaderPodName);
        podName.setText(currPodcast.getPodName());

        final TextView podDesc = (TextView) view.findViewById(R.id.txtPodHeaderDesc);
        podDesc.setText(Html.fromHtml(currPodcast.getGeneralDesc()));


        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.listEpisodes);
        if(adapterMode){
            this.TYPE = "ITUNES";
            Button subscribe = (Button) view.findViewById(R.id.btnSubscribe);
            subscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createPodcast();
                }
            });
        } else {
            Button subscribe = (Button) view.findViewById(R.id.btnSubscribe);
            subscribe.setVisibility(View.INVISIBLE);
        }

        recyclerViewAdapter = new Recycler_View_Adapter(epList,getActivity(),this.TYPE);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            /**
             * Callback method to be invoked when RecyclerView's scroll state changes.
             *
             * @param recyclerView The RecyclerView whose scroll state has changed.
             * @param newState     The updated scroll state. One of {@link #},
             *                     {@link } or {@link #}.
             */
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    recyclerViewAdapter.scrolling = true;
                } else if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    recyclerViewAdapter.scrolling = false;
                }
            }
        });

        podDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTextViewClicked){
                    //This will shrink textview to 2 lines if it is expanded.
                    podDesc.setMaxLines(2);
                    isTextViewClicked = false;
                } else {
                    //This will expand the textview if it is of 2 lines
                    podDesc.setMaxLines(Integer.MAX_VALUE);
                    isTextViewClicked = true;
                }
            }
        });



    }

    public void createPodcast(){
        listener_two.addPodcast(currPodcast);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        listener = (PlayerClicks) context;
        listener_two = (PodAdder) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        recyclerViewAdapter.getFilter().filter(newText);
        listener.switchMediaController(false);
        return true;
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
}
