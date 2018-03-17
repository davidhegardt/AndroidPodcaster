package dahe0070.androidpodcaster;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
 * {@link CategoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<String> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters

    private OnFragmentInteractionListener mListener;
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    SearchViewAdapter searchViewAdapter;
    private ArrayList<Podcast> results;
    private String baseURLCat = "https://itunes.apple.com/search?term=podcast";
    private String country = "&country=";
    //private String codeSweden = "SE";
    private String codeSweden = "KZ";
    private String codeUK = "GB";
    private String limit = "&limit=30";
    private SongInfo[] songs;
    private Category category;
    public static final int OPERATION_LOAD_RESULTS = 12;

    public CategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param page Parameter 1
     * @return A new instance of fragment CategoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoryFragment newInstance(int page,Category currCategory) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE,page);
        CategoryFragment fragment = new CategoryFragment();
        fragment.setArguments(args);
        fragment.category = currCategory;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        TextView textView = (TextView) view.findViewById(R.id.test_tab_textview);
        textView.setText("Fragment #" + mPage);
        setupResults();
        //getActivity().getSupportLoaderManager().initLoader(OPERATION_LOAD_RESULTS,null,this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //setupAdapter();

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

    private void setupResults(){
        /*
        results = new ArrayList<>();
        for (int i = 0; i < 10; i ++) {
            Podcast testPod = new Podcast("Test podcast", "http://rss.test");
            testPod.setPodImageUrl("http://static.libsyn.com/p/assets/7/1/f/3/71f3014e14ef2722/JREiTunesImage2.jpg");
            results.add(testPod);
        }
        */
        results = new ArrayList<>();
        createQuery(category.searchID);
    }

    public void createQuery(String category){
        String search = baseURLCat + country + codeSweden + category + limit;
        search(search);
    }

    public void search(String urlString){
        //this.searchTerm = urlString;
        results = new ArrayList<>();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading" + category.title);
        progressDialog.setMessage("Setting up podcasts..");
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(R.drawable.radio_tower_large);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        //String url = baseURL + term;
        String url = urlString;

        Log.i("Full search term:", urlString);

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
                //showResults();
                setupAdapter();
                progressDialog.dismiss();
            }
        },1500);



    }

    private void setupAdapter(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(Podcast pod : results){
                    if(!Helper.URLworking(pod.getFeedLink())){
                        pod.setFeedLink("");
                    }
                }
            }
        });

        thread.start();

        final RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.listSearchResults);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.HORIZONTAL);

        recyclerView.addItemDecoration(dividerItemDecoration);

        searchViewAdapter = new SearchViewAdapter(results,getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(searchViewAdapter);




        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
    }

    ProgressDialog progressDialog;

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<String>(getActivity()) {

            @Override
            protected void onStartLoading() {
                //super.onStartLoading();
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Loading" + category.title);
                progressDialog.setMessage("Setting up podcasts..");
                progressDialog.setMax(100);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIcon(R.drawable.radio_tower_large);
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                setupResults();
                return null;
            }
        };
        //return null;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        setupAdapter();
        progressDialog.dismiss();
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

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
