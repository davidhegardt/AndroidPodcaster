package dahe0070.androidpodcaster;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PodcastFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PodcastFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PodcastFragment extends Fragment implements SearchView.OnQueryTextListener,AdapterView.OnItemSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ArrayList<Podcast> podcastList;
    private ArrayList<Podcast> swePodListTest;
    private static ArrayList<Podcast> swePodcasts;
    private ArrayList<Podcast> enPodcastList;
    private ArrayList<Podcast> personalList;
    private PodcastViewAdapter podcastViewAdapter;
    private SearchView searchView;
    private MenuItem search;
    private ListView listView;
    private PlayerClicks listener;
    private Spinner spinner;
    private DatabaseHandler dbhandler;




    private OnFragmentInteractionListener mListener;

    public PodcastFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PodcastFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PodcastFragment newInstance(String param1, String param2) {
        PodcastFragment fragment = new PodcastFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Log.i("In fragment:",podcastList.get(0).getPodName() + podcastList.get(0).getGeneralDesc());
        Log.i("Nr of pods","" + podcastList.size());
        final int podCounter = podcastList.size();

/*
        String[] titles = {podcastList.get(0).getPodName(),podcastList.get(1).getPodName(),podcastList.get(2).getPodName(),podcastList.get(3).getPodName(),podcastList.get(4).getPodName(),podcastList.get(5).getPodName(),podcastList.get(6).getPodName()};
        String[] descs = {podcastList.get(0).getGeneralDesc(),podcastList.get(1).getGeneralDesc(),podcastList.get(2).getGeneralDesc(),podcastList.get(3).getGeneralDesc(),podcastList.get(4).getGeneralDesc(),podcastList.get(5).getGeneralDesc(),podcastList.get(6).getGeneralDesc()};
        String[] categories = {"Comedy","Comedy","Comedy","Documentary","Food","Comedy","Other"};
        Bitmap[] images = {podcastList.get(0).getPodImage(),podcastList.get(1).getPodImage(),podcastList.get(2).getPodImage(),podcastList.get(3).getPodImage(),podcastList.get(4).getPodImage(),podcastList.get(5).getPodImage(),podcastList.get(6).getPodImage()};
*/
        String[] titles = new String[podCounter];
        String[] descs = new String[podCounter];
        //String[] categories = new String[podCounter];
       // String[] categories = {"Comedy","Comedy","Comedy","Documentary","Food","Comedy","Other","Fact","None","Other","Documentary","Games","None"};
        Bitmap[] images = new Bitmap[podCounter];

        for (int i = 0; i < podCounter; i++){
            titles[i] = podcastList.get(i).getPodName();
            descs[i] = podcastList.get(i).getGeneralDesc();
            images[i] = podcastList.get(i).getPodImage();
        }

        podcastViewAdapter = new PodcastViewAdapter(getActivity(),podcastList,titles);
        swePodcasts = podcastList;

        listView = (ListView) view.findViewById(R.id.listPodcasts);
        listView.setAdapter(podcastViewAdapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        registerForContextMenu(listView);

        listener.switchMediaController(true);
        Log.i("podcastlist ","size" + podcastList.size());

        spinner = (Spinner) getView().findViewById(R.id.podSpinner);
        String language= Locale.getDefault().getDisplayLanguage();
        Log.i("Current language",language);



        String[] choices = {getString(R.string.select_list),getString(R.string.svenska_poddar),getString(R.string.engelska_poddar),getString(R.string.egen_lista)};

        if(language.equalsIgnoreCase("English")){
            choices = new String[]{getString(R.string.select_list), getString(R.string.svenska_poddar), getString(R.string.egen_lista)};
        }
        /**
         * Lägg till val egen lista
         */

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,choices);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

    }

    public void addPodcast(Podcast newPod){
        //podcastViewAdapter.addData(newPod);
        personalList.add(newPod);

        //podcastViewAdapter.notifyDataSetChanged();
        podcastViewAdapter.swapData(personalList);
    }

    private void swapPersonal(){

        podcastViewAdapter.swapData(personalList);
    }

    private void swapSwedish(){
        //podcastViewAdapter.swapData(podcastList);
        podcastViewAdapter.swapData(swePodListTest);
    }


    public void startSwedishList(){
       listener.navigateToPodlist();
       podcastViewAdapter.swapData(podcastList);
    }

    public void startEnglishList(){
        if (enPodcastList == null) {
            listener.setupEnglishPodcasts();
        } else {
            podcastViewAdapter.swapData(enPodcastList);
        }
    }


    public void syncNewList(){
        enPodcastList = new ArrayList<>();
        enPodcastList = listener.syncEnglishRetrieval();
        podcastViewAdapter.swapData(enPodcastList);
    }

    public boolean deletemode = false;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        if(deletemode) {
            inflater.inflate(R.menu.delete_menu,menu);
        } else {
            inflater.inflate(R.menu.search_podcast, menu);

            SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            search = menu.findItem(R.id.searchPodcast);
            //searchView = (SearchView) search.getActionView();
            searchView = (SearchView) MenuItemCompat.getActionView((menu.findItem(R.id.searchPodcast)));

            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setSubmitButtonEnabled(true);
            searchView.setOnQueryTextListener(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setRetainInstance(true);
        setHasOptionsMenu(true);
        personalList = new ArrayList<>();
        swePodListTest = new ArrayList<>();
        dbhandler = new DatabaseHandler(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Intent i = getActivity().getIntent();

       // setupPodcasts();
        podcastList = (ArrayList<Podcast>) i.getSerializableExtra("Podcasts");
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        return inflater.inflate(R.layout.fragment_podcast, container, false);
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
        podcastViewAdapter.getFilter().filter(newText);
        //listView.setAdapter(podcastViewAdapter);
        listener.switchMediaController(false);
         return true;
    }

    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     * <p>
     * Impelmenters can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String choice = parent.getItemAtPosition(position).toString();

        String language= Locale.getDefault().getDisplayLanguage();
        if (language.equalsIgnoreCase("English")){
            switch (position){
                case 1 : Log.i("Svenska poddar","Nu");
                    //startSwedishList();
                    readFromDb(1);
                    break;
                case 2 :
                    readFromDb(0);
                    break;
                /**
                 * LÄGG TILL FUNKTION EGEN LISTA
                 */
            }
        } else {

            switch (position) {
                case 1:
                    Log.i("Svenska poddar", "Nu");
                    //startSwedishList();
                    readFromDb(1);
                    break;
                case 2:
                    startEnglishList();
                    break;
                case 3:
                    readFromDb(0);
                    break;
                /**
                 * LÄGG TILL FUNKTION EGEN LISTA
                 */
            }
        }
    }

    int podPosition;
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        podPosition = info.position;
        //Log.i("position","" + pos);
        //deletemode = true;
        //getActivity().invalidateOptionsMenu();
        menu.setHeaderTitle(podcastList.get(podPosition).getPodName());
        menu.setHeaderIcon(R.drawable.trash);
        getActivity().getMenuInflater().inflate(R.menu.delete_menu,menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //return super.onContextItemSelected(item);
        item.getItemId();
        item.getTitle();
        Log.i("Selection : ", "" + item.getItemId() + item.getTitle());
        int id = item.getItemId();
        if(id == R.id.delete_pod) {
            removeDownloaded(podcastList.get(podPosition).getPodName());
            dbhandler.deletePodcast(podcastList.get(podPosition).getPodName());
            podcastList.remove(podPosition);
            podcastViewAdapter.notifyDataSetChanged();


        }
        return true;
    }

    private void removeDownloaded(String podName){
        ArrayList<PodEpisode> eps = dbhandler.getDownloadedEpisodes(podName);
        for(PodEpisode ep : eps){
            Log.i("Test function ep name","" + ep.getPodName());
            Log.i("Test for ep title","" + ep.getEpTitle());
            Log.i("file location","" + ep.getLocalLink());
            Helper.removeMP3(ep.getLocalLink());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        podcastViewAdapter.notifyDataSetChanged();
    }

    private static final String customType = "universal";
    private static final String sweType = "swedish";

    public void readFromDb(int typeInt){
        String type = "";
        if(typeInt == 0){
            type = customType;
        } else if(typeInt == 1){
            type = sweType;
        }

        //podcastList.clear();
        swePodListTest.clear();
        personalList.clear();

        ArrayList<String> podNames = dbhandler.getPODCOLUMNData("podName",type);
        ArrayList<String> podDescs = dbhandler.getPODCOLUMNData("generalDesc",type);           // Get all the general descriptions from database
        ArrayList<String> podTypes = dbhandler.getPODCOLUMNData("podType",type);               // get the podtype
        ArrayList<String> feedLinks = dbhandler.getPODCOLUMNData("feedLink",type);             // and the link
        ArrayList<String> images = dbhandler.getPODCOLUMNData("podImage",type);                              // also get the Bitmaps stored for each podcast



        for (int i = 0; i < podNames.size();i++){                                            // Loop list of podcasts
            Podcast newPod = new Podcast(podNames.get(i),feedLinks.get(i));
            newPod.setPodType(podTypes.get(i));
            newPod.setPodImageUrl(images.get(i));
            newPod.setGeneralDesc(podDescs.get(i));
            if (typeInt == 0) {
                personalList.add(newPod);
            } else {
                //podcastList.add(newPod);
                swePodListTest.add(newPod);
            }
        }
        if(typeInt == 0) {
            swapPersonal();
        } else {
            swapSwedish();
        }

    }


    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
