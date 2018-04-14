package dahe0070.androidpodcaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TabSearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TabSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabSearchFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private OnFragmentInteractionListener mListener;
    private ViewPager mViewPager;

    public TabSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TabSearchFragment newInstance(String param1, String param2) {
        TabSearchFragment fragment = new TabSearchFragment();
        Bundle args = new Bundle();

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
        return inflater.inflate(R.layout.fragment_tab_search, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private Spinner languageSpinner;
    private ArrayList<Country> countryList;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(),getActivity()));

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                //super.onPageSelected(position);
                //mViewPager.getAdapter().notifyDataSetChanged();
            }
        });

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        languageSpinner = (Spinner) view.findViewById(R.id.langSpinner);

        //String[] lang = {"Swe","Eng","Russian"};
        countryList = Helper.addFirstCountries();
        countryList.addAll(Helper.getCountries());
        //countryList = Helper.getCountries();
        ArrayList<String> countryNames = new ArrayList<>();

        for (Country c : countryList){
            countryNames.add(c.getCountryName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,countryNames);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        languageSpinner.setAdapter(adapter);
        languageSpinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String choice = parent.getItemAtPosition(position).toString();

        SharedPreferences languagePref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultLang = "SE";
        SharedPreferences.Editor prefEdit = languagePref.edit();

        String codeCountry = countryList.get(position).getCountryCode();
        prefEdit.putString(getString(R.string.searchLanguage),codeCountry);
        prefEdit.commit(); mViewPager.getAdapter().notifyDataSetChanged();



/*
        switch (choice){
            case "Swe" : prefEdit.putString(getString(R.string.searchLanguage),"SE");
                         prefEdit.commit(); mViewPager.getAdapter().notifyDataSetChanged();
                break;
            case "Eng" : prefEdit.putString(getString(R.string.searchLanguage),"GB");
                prefEdit.commit(); mViewPager.getAdapter().notifyDataSetChanged();
                break;
            case "Russian" : prefEdit.putString(getString(R.string.searchLanguage),"RU");
                prefEdit.commit(); mViewPager.getAdapter().notifyDataSetChanged();
                break;
        }
        */
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void testFindFragmet(ArrayList<PodEpisode> mList, Podcast currPod){
        FragmentPagerAdapter fa = (FragmentPagerAdapter) mViewPager.getAdapter();
        fa.setcurrPodcast(currPod);
        fa.setEpList(mList);
        Fragment fragment = fa.getItem(10);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor prefEdit = sharedPreferences.edit();
        prefEdit.putBoolean("itunesSubscribe",true);
        android.support.v4.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.main_view,fragment).addToBackStack("itunesview").commit();
        mViewPager.getAdapter().notifyDataSetChanged();

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
}
