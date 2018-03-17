package dahe0070.androidpodcaster;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Dave on 2017-08-14.
 */

public class StationFactory {

    private ArrayList<Station> stationsVec;

    public StationFactory(){
        stationsVec = new ArrayList<Station>();
    }


    public ArrayList<Station> getStations() {

        return stationsVec;
    }

    public void createStation(String splitThis) {

            String[] stationInfo = stringSplitter(splitThis);
            if (stationInfo.length > 1) {

                String title = stationInfo[0];
                String feed = stationInfo[1];
                String image = stationInfo[2];

                Station station = new Station(title,feed,image);
                stationsVec.add(station);
            }
    }

    public String[] getStationNames(){

        String[]dsf = new String[stationsVec.size()];
        for (int i = 0; i < dsf.length; i++){
            dsf[i] = stationsVec.get(i).getTitle();
        }

        return dsf;
    }

    public String[] stringSplitter(String splitThis) {
        String[] fkingSplit = splitThis.split(",");

        return fkingSplit;
    }

    public String readFromAssets(Context context, String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));  // Use streamreader and buffered reader and open file from assets-folder

        String str;
        while ((str = reader.readLine()) != null ){
            createStation(str);
        }
        reader.close();

        return "";
    }
}
