package dahe0070.androidpodcaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RadioFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RadioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RadioFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private PlayerClicks listener;

    private ArrayList<Station> stations;

    public RadioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RadioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RadioFragment newInstance(String param1, String param2) {
        RadioFragment fragment = new RadioFragment();
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
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        return inflater.inflate(R.layout.fragment_radio, container, false);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //String[] stations = {"P1","P2","P3","P4","Megaton Cafe Radio","Bandit Rock","Rix FM"};

        StationFactory test = new StationFactory();
        try {
            test.readFromAssets(getActivity(),"stations.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        stations = test.getStations();
        String[] stationNames = test.getStationNames();

        RadioViewAdapter radioViewAdapter = new RadioViewAdapter(getActivity(),stations,stationNames);

        ListView listView = (ListView) view.findViewById(R.id.listStations);
        listView.setAdapter(radioViewAdapter);

        ImageButton currPlay = (ImageButton) view.findViewById(R.id.btnCurrPlay);
        currPlay.setOnClickListener(this);

        listener.switchMediaController(false);


    }

    public ArrayList<Station> getStations(){
        return stations;
    }


    public void updateStationPlayer(String stationName,boolean play) {
        ImageButton playingButton = (ImageButton) getView().findViewById(R.id.btnCurrPlay);
        TextView currStation = (TextView) getView().findViewById(R.id.txtCurrStation);
        if (play) {
            ImageView animation = (ImageView) getView().findViewById(R.id.imgAudioAnim);
            ((AnimationDrawable) animation.getBackground()).start();
            currStation.setText(getResources().getString(R.string.now_playing) + stationName);
        } else {
            ImageView animation = (ImageView) getView().findViewById(R.id.imgAudioAnim);
            ((AnimationDrawable) animation.getBackground()).stop();
            currStation.setText(stationName);
        }



        if(play) {
            playingButton.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
        } else if (!play) {
            playingButton.setImageResource(R.drawable.ic_play_circle_filled_white_48dp);
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCurrPlay :
                if (listener.radioStatus()) {
                    listener.radioPause();
                } else {
                    SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                    String stationName = sharedPreferences.getString("savedStation","none");
                    listener.radioPlay(stationName);
                }

                break;
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
}
