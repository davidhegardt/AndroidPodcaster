package dahe0070.androidpodcaster;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LatestEpisodesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LatestEpisodesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LatestEpisodesFragment extends Fragment implements SearchView.OnQueryTextListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<PodEpisode> epList;
    private SearchView searchView;
    Recycler_View_Adapter recyclerViewAdapter;
    private PlayerClicks listener;
    private static String TYPE = "EPISODES";
    private MenuItem search;

    private OnFragmentInteractionListener mListener;

    public LatestEpisodesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LatestEpisodesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LatestEpisodesFragment newInstance(String param1, String param2) {
        LatestEpisodesFragment fragment = new LatestEpisodesFragment();
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
        setHasOptionsMenu(true);
    }

    private String message = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Intent i = getActivity().getIntent();

        message = i.getStringExtra("Resume");

        if(!message.contains("Resume")) {

            epList = (ArrayList<PodEpisode>) i.getSerializableExtra("Episodes");
            epList = Helper.sortByDate(epList);

        }

        View view = inflater.inflate(R.layout.fragment_latest_episodes,container,false);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.downloaded_menu,menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        search = menu.findItem(R.id.searchPodcast);

        searchView = (SearchView) MenuItemCompat.getActionView((menu.findItem(R.id.searchPodcast)));

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.sort_by_date :
                Toast.makeText(getContext(),"Episodes sorted by date",Toast.LENGTH_SHORT).show();
                if(!epList.isEmpty()){
                    epList = Helper.sortByDate(epList);
                    recyclerViewAdapter.notifyDataSetChanged();
                }
                return true;
            case R.id.sort_by_pod :
                Toast.makeText(getContext(),"Episodes sorted by podcast",Toast.LENGTH_SHORT).show();
                if(!epList.isEmpty()){
                    epList = Helper.sortByPodcast(epList);
                    recyclerViewAdapter.notifyDataSetChanged();
                }
                return true;
        }

        return false;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.listLatest);

        recyclerViewAdapter = new Recycler_View_Adapter(epList,getActivity(),this.TYPE);
        //recyclerViewAdapter = new Recycler_View_Adapter(epList,getActivity());

        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
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
