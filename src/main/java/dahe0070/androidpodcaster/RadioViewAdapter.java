package dahe0070.androidpodcaster;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Dave on 2017-08-14.
 */

public class RadioViewAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] stationNames;
    private final ArrayList<Station> stationsVec;
    private PlayerClicks listener;


    public RadioViewAdapter(Context ctx, ArrayList<Station> stations, String[] stationNames) {
        super(ctx,-1,stationNames);
        this.context = ctx;
        this.stationsVec = stations;
        this.stationNames = stationNames;
        listener = (PlayerClicks) ctx;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.listview_each_station,parent,false);

        final TextView stationName = (TextView) rowView.findViewById(R.id.txtStationName);

        stationName.setText(stationNames[position]);

        final ImageButton stationImage = (ImageButton) rowView.findViewById(R.id.imgStation);
        stationImage.setImageResource(radioImage(position));

        ImageButton btnPlay = (ImageButton) rowView.findViewById(R.id.btnPlayStation);
        final int radioChoice = position;

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               listener.radioPlay(stationNames[radioChoice]);
            }
        });

        return rowView;
    }

    public int radioImage(int index){
        int resID = 0;
        resID = context.getResources().getIdentifier(stationsVec.get(index).getStationImage(), "drawable",context.getPackageName());

        return resID;
    }
}
