package dahe0070.androidpodcaster;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    SearchViewAdapter searchViewAdapter;

    private OnFragmentInteractionListener mListener;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.search_menu, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    private String baseURL = "https://itunes.apple.com/search?media=podcast&term=";
    private String baseURLCat = "https://itunes.apple.com/search?term=podcast";
    private String country = "&country=";
    private String codeSweden = "SE";
    private String codeUK = "GB";
    private String limit = "&limit=30";
    private String category = "https://itunes.apple.com/search?term=podcast&genreId=1402&limit=20";
    private String comedyID = "&genreId=1303";
    private String tvfilmdID = "&genreId=1309";
    private String scienceID = "&genreId=1315";
    private String techID = "&genreId=1318";
    private String businessID = "&genreId=1321";
    private String gamesID = "&genreId=1323";

    private String swedish = "https://itunes.apple.com/search?term=podcast&country=SE&limit=20";

    private RecyclerView categoryView;
    private CategoryAdapter categoryAdapter;
    private ArrayList<Category> categoryArrayList;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView searchinput = (TextView) view.findViewById(R.id.txtSearchPodcast);
        ImageButton searchbutton = (ImageButton) view.findViewById(R.id.btnSearch);

        categoryView = (RecyclerView) view.findViewById(R.id.category_view);

        categoryArrayList = new ArrayList<>();

        Category newCat = new Category("Comedy",comedyID,R.drawable.comedy_icon_white);
        Category film = new Category("Film/Movies",tvfilmdID,R.drawable.cinema_icon_white);
        Category science = new Category("Science",scienceID,R.drawable.science_icon_white);
        Category tech = new Category("Technology",techID,R.drawable.tech_icon_white);
        Category business = new Category("Business",businessID,R.drawable.business_icon_white);
        Category games = new Category("Games",gamesID,R.drawable.games_icon_white);
        categoryArrayList.add(newCat);
        categoryArrayList.add(film);
        categoryArrayList.add(science);
        categoryArrayList.add(tech);
        categoryArrayList.add(business);
        categoryArrayList.add(games);

        categoryAdapter = new CategoryAdapter(getActivity(),categoryArrayList);

        RecyclerView.LayoutManager gridManager = new GridLayoutManager(getActivity(),4);
        categoryView.setLayoutManager(gridManager);
        categoryView.setItemAnimator(new DefaultItemAnimator());
        categoryView.setAdapter(categoryAdapter);


        //Button comedy = (Button) view.findViewById(R.id.btnComedy);
        //Button movies = (Button) view.findViewById(R.id.btnFilm);

        /*
        comedy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comedy = baseURLCat + country + codeSweden + comedyID + limit;
                search(comedy);
            }
        });

        movies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String film = baseURLCat + country + codeUK + comedyID + limit;
                search(film);
            }
        });

*/



        searchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = searchinput.getText().toString();
                String theSearch = searchTerm.replaceAll("\\s+","");
                search(baseURL + theSearch);
            }
        });


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


    private String searchTerm = "";
    private ArrayList<String> names;
    private ArrayList<String> feedUrls;
    private SongInfo[] songs;
    private ArrayList<Podcast> results;

    public void showResults(){
        if (results.isEmpty()){
            Toast.makeText(getActivity(),"No results for search",Toast.LENGTH_SHORT).show();
        } else {
            // starta adapter
            setupAdapter();
        }
    }

    private void setupAdapter(){
        final RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.listSearchResults);

        searchViewAdapter = new SearchViewAdapter(results,getActivity());
        recyclerView.setAdapter(searchViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
    }


    public void search(String urlString){
        this.searchTerm = urlString;
        results = new ArrayList<>();
        feedUrls = new ArrayList<>();

        //String url = baseURL + term;
        String url = urlString;


        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    int resultCount = response.optInt("resultCount");
                    if (resultCount > 0) {
                        Gson gson = new Gson();
                        JSONArray jsonArray = response.optJSONArray("results");
                        if (jsonArray != null) {
                            songs = gson.fromJson(jsonArray.toString(), SongInfo[].class);
                            if (songs != null && songs.length > 0) {
                                for (SongInfo song : songs) {
                                    /*
                                    Log.i("Feed url", song.feedUrl);
                                    Log.i("Artist name",song.artistName);
                                    Log.i("collection name",song.collectionName);
                                    Log.i("artworkurl60",song.artworkUrl100);
                                    */
                                    //results.add(song);
                                    Podcast newPod = new Podcast(song.collectionName,song.feedUrl);
                                    newPod.setPodImageUrl(song.artworkUrl100);
                                    results.add(newPod);
                                }
                            }
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            /**
             * Callback method that an error has been occurred with the
             * provided error code and optional user-readable message.
             *
             * @param error
             */
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Log",error.toString());
            }
        });
        requestQueue.add(jsonObjectRequest);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showResults();
            }
        },1500);


    }
}
