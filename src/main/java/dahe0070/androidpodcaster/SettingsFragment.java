package dahe0070.androidpodcaster;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dahe0070.androidpodcaster.dummy.DummyContent;
import dahe0070.androidpodcaster.dummy.DummyContent.DummyItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SettingsFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SettingsFragment newInstance(int columnCount) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_colortheme_list, container, false);

        List<ColorTheme> colorList = new ArrayList<ColorTheme>();

        ColorTheme orangeColor = new ColorTheme("Orange Theme","ORANGE");
        ColorTheme yellowColor = new ColorTheme("Yellow Theme","YELLOW");
        ColorTheme pinkColor = new ColorTheme("Pink Theme","PINK");
        ColorTheme redColor = new ColorTheme("Red Theme","RED");
        ColorTheme greenColor = new ColorTheme("Green Theme","GREEN");
        ColorTheme blueColor = new ColorTheme("Blue Theme","BLUE");
        ColorTheme brownColor = new ColorTheme("Brown Theme","BROWN");
        int yellowId = this.getResources().getIdentifier("side_nav_bar_yellow","drawable", getActivity().getPackageName());
        int orangeId = this.getResources().getIdentifier("side_nav_bar_orange","drawable", getActivity().getPackageName());
        int pinkID = this.getResources().getIdentifier("side_nav_bar_pink","drawable", getActivity().getPackageName());
        int redID = this.getResources().getIdentifier("side_nav_bar_red","drawable", getActivity().getPackageName());
        int greenID = this.getResources().getIdentifier("side_nav_bar_green","drawable", getActivity().getPackageName());
        int blueID = this.getResources().getIdentifier("side_nav_bar_blue","drawable", getActivity().getPackageName());
        int brownID = this.getResources().getIdentifier("side_nav_bar_brown","drawable", getActivity().getPackageName());

        orangeColor.setColorID(orangeId);
        yellowColor.setColorID(yellowId);
        pinkColor.setColorID(pinkID);
        redColor.setColorID(redID);
        greenColor.setColorID(greenID);
        blueColor.setColorID(blueID);
        brownColor.setColorID(brownID);
        colorList.add(orangeColor);
        colorList.add(yellowColor);
        colorList.add(pinkColor);
        colorList.add(redColor);
        colorList.add(greenColor);
        colorList.add(blueColor);
        colorList.add(brownColor);


        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            recyclerView.setAdapter(new MyColorThemeRecyclerViewAdapter(colorList, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(ColorTheme item);
    }
}
