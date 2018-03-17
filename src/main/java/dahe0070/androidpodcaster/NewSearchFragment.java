package dahe0070.androidpodcaster;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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
 * {@link NewSearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewSearchFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private SongInfo[] songs;
    private ArrayList<Podcast> results;
    ProgressDialog progressDialog;
    SearchViewAdapter searchViewAdapter;
    ImageButton searchBtn;
    EditText searchInput;
    private String baseURL = "https://itunes.apple.com/search?media=podcast&term=";

    public NewSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment NewSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewSearchFragment newInstance() {
        NewSearchFragment fragment = new NewSearchFragment();
        Bundle args = new Bundle();
      //  fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_search, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchBtn = (ImageButton) view.findViewById(R.id.btnSearch);
        searchInput = (EditText) view.findViewById(R.id.txtSearchPodcast);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = searchInput.getText().toString();
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

    private void setupAdapter(){
        final RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.listSearchResults);

        searchViewAdapter = new SearchViewAdapter(results,getActivity());
        recyclerView.setAdapter(searchViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);



        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    public void search(String urlString){
        //this.searchTerm = urlString;
        results = new ArrayList<>();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading results");
        progressDialog.setMessage("Setting up podcasts..");
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(R.drawable.radio_tower_large);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


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
                //showResults();
                setupAdapter();
                progressDialog.dismiss();
            }
        },1500);



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
