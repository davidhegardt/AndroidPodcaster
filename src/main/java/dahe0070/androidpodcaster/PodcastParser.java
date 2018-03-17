package dahe0070.androidpodcaster;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

/**
 * Class used to create the actual podcasts.
 * Uses a separate thread to download images and parse
 * Podcast data.
 */

public class PodcastParser {

    private Context context;
    private ArrayList<Podcast> podList;                                     // Store the podcasts in this list
    private RSSparser rssParser;                                            // the parser to be used
    private DatabaseHandler podDatabase;                                    // Database to be used
    private PlayerClicks listener;
    private boolean english = false;                                        // if english list should be parsed
    private PodcastFactory factory;
    private ArrayList<Podcast> newList;



    public PodcastParser(Context ctx,boolean english){
        context = ctx;
        podList = new ArrayList<>();
        podDatabase = new DatabaseHandler(ctx);
        listener = (PlayerClicks) ctx;
        this.english = english;
        this.factory = new PodcastFactory(context);

    }

    public ArrayList<Podcast> getAllPodcasts(){
        return newList;
    }

    public void startParse(){                                                   // Called parsing should start
        boolean readFromDB = false;                                             // If the episodes are not parsed, they can be read from the database
        newList = new ArrayList<>();

        // Check Shared preferences here
        SharedPreferences dbPreference = PreferenceManager.getDefaultSharedPreferences(context);
        boolean dbcomplete = dbPreference.getBoolean(context.getString(R.string.data_ok),false);

        if(dbcomplete){
            readFromDb();
        } else if (podList.size() == podDatabase.typeCount(podList.get(0).getPodType())){
            readFromDb();
        } else {

            for (Podcast i : podList) {
                if (podDatabase.isExist(i.getPodName())) {                            // loop list of podcasts (containing name and feed only)
                    //i = podDatabase.getPodcast(i.getPodName());
                    try {
                        i = new DBRetriever().execute(i).get();
                        newList.add(i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    //Log.i("Retrieved from database",i.getPodImageUrl());
                } else {
                    i = factory.createPodcast(i);
                    newList.add(i);
                }

            }
            if (podList.get(0).getPodType().equals("english")){
                listener.syncEnglishDone();
            } else {
                listener.syncPodretrieval();
            }
        }
    }

    public void readFromDb(){
        /*
        Collections.sort(podList, new Comparator<Podcast>() {
            @Override
            public int compare(Podcast o1, Podcast o2) {
                return o1.getPodName().compareTo(o2.getPodName());                          // Sort the podcasts so they end up in alphabetical order
            }
        });
        */
        ArrayList<String> podNames = podDatabase.getPODCOLUMNData("podName",podList.get(0).getPodType());
        ArrayList<String> podDescs = podDatabase.getPODCOLUMNData("generalDesc",podList.get(0).getPodType());           // Get all the general descriptions from database
        ArrayList<String> podTypes = podDatabase.getPODCOLUMNData("podType",podList.get(0).getPodType());               // get the podtype
        ArrayList<String> feedLinks = podDatabase.getPODCOLUMNData("feedLink",podList.get(0).getPodType());             // and the link
        ArrayList<String> images = podDatabase.getPODCOLUMNData("podImage",podList.get(0).getPodType());                              // also get the Bitmaps stored for each podcast



        for (int i = 0; i < podNames.size();i++){                                            // Loop list of podcasts
            podList.get(i).setGeneralDesc(podDescs.get(i));                                 // complete them with description,feedlink and image
            podList.get(i).setFeedLink(feedLinks.get(i));
            podList.get(i).setPodImageUrl(images.get(i));
            podList.get(i).setPodType(podTypes.get(i));
            podList.get(i).setPodName(podNames.get(i));
            newList.add(podList.get(i));
        }

        Log.i("type",podList.get(0).getPodType());
        if (podList.get(0).getPodType().equals("swedish")) {
            listener.syncPodretrieval();
        } else if (podList.get(0).getPodType().equals("english")){
            listener.syncEnglishDone();
        }

    }



        private class DBRetriever extends AsyncTask<Podcast,Void,Podcast>{
            /**
             * Runs on the UI thread before {@link #doInBackground}.
             *
             * @see #onPostExecute
             * @see #doInBackground
             */

            private Podcast currPod;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                currPod = new Podcast();

            }

            @Override
            protected Podcast doInBackground(Podcast... params) {
                currPod = params[0];

                currPod = podDatabase.getPodcast(currPod.getPodName());

                return currPod;
            }

            /**
             * <p>Runs on the UI thread after {@link #doInBackground}. The
             * specified result is the value returned by {@link #doInBackground}.</p>
             * <p>
             * <p>This method won't be invoked if the task was cancelled.</p>
             *
             * @param aVoid The result of the operation computed by {@link #doInBackground}.
             * @see #onPreExecute
             * @see #doInBackground
             * @see #onCancelled(Object)
             */
            @Override
            protected void onPostExecute(Podcast aVoid) {
                super.onPostExecute(aVoid);


            }
        }


        /*
        if (readFromDB){                                                    // If podcasts are downloaded, then read from the database
            readFromDb();
            if (english){
                listener.syncEnglishDone();                                 // Once this is done, if english podcasts have been read from DB, call function to retrieve them
            } else {
                listener.syncPodretrieval();                                // Else, if swedish are read from the database, call function to retrieve swedish podcasts from database
            }
        }
        */







    /**
     * Function to read podcats from the database if the have been parsed
     */
    /*
    public void readFromDb(){
        Collections.sort(podList, new Comparator<Podcast>() {
            @Override
            public int compare(Podcast o1, Podcast o2) {
                return o1.getPodName().compareTo(o2.getPodName());                          // Sort the podcasts so they end up in alphabetical order
            }
        });

        ArrayList<String> podDescs = podDatabase.getPODCOLUMNData("generalDesc");           // Get all the general descriptions from database
        ArrayList<String> podTypes = podDatabase.getPODCOLUMNData("podType");               // get the podtype
        ArrayList<String> feedLinks = podDatabase.getPODCOLUMNData("feedLink");             // and the link
        ArrayList<Bitmap> bitmaps = podDatabase.getPodImage();                              // also get the Bitmaps stored for each podcast


        for (int i = 0; i < podList.size();i++){                                            // Loop list of podcasts
            podList.get(i).setGeneralDesc(podDescs.get(i));                                 // complete them with description,feedlink and image
            //Log.i("general",podDescs.get(i));
            podList.get(i).setFeedLink(feedLinks.get(i));
            podList.get(i).setPodImage(bitmaps.get(i));
        }

    }
*/
    /**
     * Function to create the first instance of podcast so this can be parsed from the link
     * @param podData data for name,feed and type
     */
    private void createPodcast(String[] podData) {

        if (podData.length > 1) {
            String name = podData[0];
            String feed = podData[1];
            String type = podData[2];

            Podcast newPod = new Podcast(name,feed,type);               // Create new (incomplete) podcast with name feed and type from file
            podList.add(newPod);
        }
    }

    /**
     * Function to split read podcast data from textfile
     * @param splitThis read string to split
     * @return array with data
     */
    public String[] stringSplitter(String splitThis) {
        String[] fkingSplit = splitThis.split(",");

        return fkingSplit;
    }

    /**
     * Function that reads file from assets
     * @param filename name of file
     * @return default string
     * @throws IOException
     */
    public String readFromAssets(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));  // Use streamreader and buffered reader and open file from assets-folder

        String str;
        while ((str = reader.readLine()) != null ){
            String[] podData = stringSplitter(str);
            createPodcast(podData);                                     // Create the podcast with the read split strings
        }
        reader.close();

        return "";
    }

    /**
     * Function to download rss from the file and read iamge bitmap
     */
    private class DownloadRSS extends AsyncTask<Void,Integer,Podcast>{

        private Podcast currPod;                            // The podcast that should be parsed
        private String feedURL;                             // URL to feed link

        DownloadRSS(Podcast currPod){
            this.currPod = currPod;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            feedURL = currPod.getFeedLink();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Podcast doInBackground(Void... rssURL) {
            try {
                URL url = new URL(feedURL);
                BufferedReader inReader = new BufferedReader(new InputStreamReader(url.openStream()));              // Connect to the feed
                StringBuffer sb = new StringBuffer();

                String line;
                while((line = inReader.readLine()) != null){
                    sb.append(line);                                                                                // Read every line from the feed
                }
                //Log.i("SB:",sb.toString());

                inReader.close();
                String content = sb.toString();                                                             // Convert string buffer to string, contains the whole feed
                rssParser = new RSSparser(content);                                                         // Setup the parser
                String desc = rssParser.getGeneralDesc();                                                   // get the general description for this podcast

                if (currPod.getPodName().contains("Spar")){                                                 // Exception for this podcast - it has a faulty description
                    desc = rssParser.removeAfter(desc,"undre värld.");
                }

                currPod.setGeneralDesc(desc);                                                               // Set the description for this podcast
                String imageURL = "";
                imageURL = rssParser.XMLImageParser(feedURL);                                               // To retrieve the podcast image, use XML parsing
                Bitmap newBitmap = getBitmapFromURL(imageURL);                                              // Call function to connect, retrieve and convert the url to Bitmap
                //Bitmap resized = Bitmap.createScaledBitmap(newBitmap,100,100, true);
                if (newBitmap == null){
                   //Log.i("Bitmap null for",currPod.getPodName());
                } else {
                    currPod.setPodImage(newBitmap);                                                         // If successfull, apply this image to the podcast
                }

                return currPod;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException io){
                io.printStackTrace();
            }


            return null;
        }

        /**
         * Connects to the internet to retrieve image from link
         * @param imageURL  url to image to retrieve
         * @return  created bitmap from URL
         */
        public Bitmap getBitmapFromURL(String imageURL) {
            try {
                URL url = new URL(imageURL);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);                            // Convert stream into bitmap
                Bitmap scaledImage = scaleBitmap(myBitmap,210,210);                             // scale the image
                return scaledImage;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Function to scale an image to correct format
         * @param bitmap    to scale
         * @param wantedWidth   scale width
         * @param wantedHeight  scale height
         * @return  a new scaled bitmap
         */

        public Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
            Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            Matrix m = new Matrix();
            m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
            canvas.drawBitmap(bitmap, m, new Paint());

            return output;
        }

        @Override
        protected void onPostExecute(Podcast podcast) {
            if(english){
                listener.syncEnglishDone();                             // Once all is parsed, the podcasts are ready to be recieved
            } else {
                listener.syncPodretrieval();
            }
            super.onPostExecute(podcast);
        }
    }
}
