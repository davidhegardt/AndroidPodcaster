package dahe0070.androidpodcaster;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Dave on 2017-08-15.
 */

public class EpisodeParser {

    private RSSparser rssParser;
    private String[] descriptions;
    private String[] soundLinks;
    private String[] epTitles;
    private String[] epImages;
    private String[] durations;
    private String[] dates;
    private String podName;
    private Context ctx;
    private PlayerClicks listener;
    private ArrayList<PodEpisode> episodes;
    private String podType;
    private ProgressDialog progressDialog;



    public EpisodeParser(Context context){
        this.ctx = context;
        listener = (PlayerClicks) ctx;
    }

    public void parseEpisodes(Podcast currPod){
        podName = currPod.getPodName();
        podType = currPod.getPodType();
        new EpisodeDownloader().execute(currPod.getFeedLink());
    }

    public String[] splitInfo(String content){
        String delim = "<br>";
        String[] individual = content.split(delim);
        return individual;
    }

    public void createEpisodes(){
        episodes = new ArrayList<>();
        Log.i("epTitles","" + epTitles.length);
        Log.i("soundLinks","" + soundLinks.length);
        Log.i("descs","" + descriptions.length);
        Log.i("dates","" + dates.length);
        Log.i("images","" + epImages.length);

        if (descriptions.length == 0){
            descriptions = new String[epTitles.length];
            for (int i = 0; i < epTitles.length;i++){
                descriptions[i] = "Ingen beskrivning";
            }
        }




        if (epImages.length < 2 || podName.contains(ctx.getString(R.string.Under_Huden))){
            String titleImage = "";
            if (podName.contains(ctx.getString(R.string.Revisionist))){
                titleImage = rssParser.getImageRevisionist();
            } else if (podName.contains(ctx.getString(R.string.Under_Huden))){
                titleImage = rssParser.getImageUnderHuden();
            }
            else {
                titleImage = rssParser.getImageLink();
            }

            epImages = new String[epTitles.length];
            for (int i = 0; i < epTitles.length;i++){
                epImages[i] = titleImage;
            }
        }


        for (int i = 0; i < epTitles.length - 1;i++){
            PodEpisode podEpisode = new PodEpisode(podName,epTitles[i],soundLinks[i],descriptions[i],epImages[i],dates[i]);
            podEpisode.setDuration(durations[i]);
            episodes.add(podEpisode);
        }

        listener.loadEpisodes(false);
    }

    public ArrayList<PodEpisode> getEpisodes(){
        return episodes;
    }

    private class EpisodeDownloader extends AsyncTask<String,Integer,PodEpisode>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(ctx);
            progressDialog.setTitle(ctx.getString(R.string.loading_episodes));
            progressDialog.setMessage(ctx.getString(R.string.loading));
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected PodEpisode doInBackground(String... feedUrl) {
            try {
                URL url = new URL(feedUrl[0]);
                BufferedReader inReader = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuffer sb = new StringBuffer();
                int progress = 10;

                String line;
                while((line = inReader.readLine()) != null){
                    sb.append(line);
                }
                //Log.i("SB:",sb.toString());

                inReader.close();
                String content = sb.toString();
                rssParser = new RSSparser(content);
                String allDescriptions = "";

                if (podName.contains("Stuff You Should" ) || podName.contains("Revisionist")){
                    allDescriptions = rssParser.getDescriptionNoCDATA();
                } else {
                    allDescriptions = rssParser.getDescription();
                }


                String allSounds = "";

                if (podType.contains("acast")){
                    allSounds = rssParser.getSoundAcast();
                } else if (podType.contains("other")){
                    allSounds = rssParser.getSoundOther();
                }
                else {
                    allSounds = rssParser.getSoundModern();
                }


                String allTitles = rssParser.getTitles();
                String allImages = rssParser.getEpisodeImage();
                String allDurations = rssParser.getDurations();
                String allDates = rssParser.getDates();
                progress = progress + 10;
                publishProgress(progress);

                String[] tempDates = splitInfo(allDates);
                dates = new String[tempDates.length];

                for (int i = 0; i < tempDates.length;i++){
                    //dates[i] = rssParser.formatDate(tempDates[i]);
                    //dates[i] = Helper.dateToString(tempDates[i]);
                    dates[i] = tempDates[i];
                }

                durations = splitInfo(allDurations);
                soundLinks = splitInfo(allSounds);
                descriptions = splitInfo(allDescriptions);
                epTitles = splitInfo(allTitles);
                epImages = splitInfo(allImages);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException io){
                io.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(PodEpisode podEpisode) {
            createEpisodes();
            progressDialog.dismiss();
            super.onPostExecute(podEpisode);


        }
    }
}
