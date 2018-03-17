package dahe0070.androidpodcaster;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Dave on 2017-08-14.
 */

public class RadioParser {

    private Context context;
    private ProgressDialog progressDialog;
    private ArrayList<Station> stationVec;

    public RadioParser(Context ctx,ArrayList<Station> inputStations)
    {
        this.context = ctx;
        this.stationVec = inputStations;
    }

    private String radioChoice(String stationName) {

        for (Station i : stationVec) {
            if (i.getTitle() == stationName){
                return i.getFeedLink();
            }
        }

        return "";
/*
        String M3ulink = null;

        switch (stationName){
            case "P1" : M3ulink = "http://sverigesradio.se/topsy/direkt/132-hi-mp3.m3u";
                break;
            case "P2" : M3ulink = "http://sverigesradio.se/topsy/direkt/2562-hi-mp3.m3u";
                break;
            case "P3" : M3ulink = "http://sverigesradio.se/topsy/direkt/164-hi-mp3.m3u";
                break;
            case "P4" : M3ulink = "http://sverigesradio.se/topsy/direkt/701-hi-mp3.m3u";
                break;
            case "Megaton Cafe Radio" : M3ulink = "https://www.internet-radio.com/servers/tools/playlistgenerator/?u=http://us2.internet-radio.com:8443/listen.pls&t=.m3u";
                break;
            case "Bandit Rock" : M3ulink = "http://stream-ice.mtgradio.com:8080/stat_bandit.m3u";
                break;
            case "Rix FM" : M3ulink = "http://stream-ice.mtgradio.com:8080/stat_rix_fm.m3u";
                break;
            default: return null;
        }

        return M3ulink;
*/
    }

    public String readM3U(String stationName) {

        String url = radioChoice(stationName);

        if (url == null) return null;

        try {
            String M3U = new DownloadURL().execute(url).get();

            return M3U;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;

    }

    private class DownloadURL extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(context.getString(R.string.prepairing_station));
            progressDialog.setMessage(context.getString(R.string.loading));
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... url) {

            try {
                ArrayList<String> urlArray = new ArrayList<String>();
                URL urls = new URL(url[0]);
                BufferedReader inReader = new BufferedReader(new InputStreamReader(urls.openStream()));
                String line;
                int progress = 10;
                String validURL = "";
                while ((line = inReader.readLine()) != null) {
                    if (line.contains("http")){
                        validURL = line;
                    }
                    publishProgress(progress);
                    progress = progress + 10;
                }
                inReader.close();

                return validURL;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();

            super.onPostExecute(s);
        }
    }
}
