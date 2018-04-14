package dahe0070.androidpodcaster;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Dave on 2017-08-29.
 */

public class PodcastFactory {

    private Podcast newPod;
    private ArrayList<PodEpisode> episodes;
    private String[] podNames;
    private Context ctx;
    private PlayerClicks listener;
    private String backupImage;
    private ProgressDialog progressDialog;

    public PodcastFactory(Context context){

        this.newPod = new Podcast();
        this.ctx = context;
        this.listener = (PlayerClicks) context;
    }

    public Podcast createPodcast(Podcast basicPod){
        newPod = basicPod;
        backupImage = basicPod.getPodImageUrl();

        try {
            newPod = new PodCreator().execute(newPod.getFeedLink()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return newPod;
    }

    public ArrayList<PodEpisode> createEpisodes(String podName,String feed){
        new XMLretriever().execute(feed);

        return null;
    }

    public ArrayList<PodEpisode> createEpisodes(String podName,String feed,boolean itunes,Podcast currPod){
        this.itunesMode = true;
        this.itunesPod = currPod;
        new XMLretriever().execute(feed);

        return null;
    }

    public boolean itunesMode = false;
    public Podcast itunesPod;

    public void createLatestEpisodes(String[] feedLinkz,String[] podNamez){
        podNames = podNamez;
        new LatestEpRetriever().execute(feedLinkz);
    }


    private class XMLretriever extends AsyncTask<String,Integer,Void>{
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */

        XMLUniversal newParser;

        @Override
        protected Void doInBackground(String... params) {
            String feed = params[0];
            newParser = new XMLUniversal(ctx);

            boolean success = newParser.parseXMLSR(feed);
            if(!success){
                Log.i("READ ERROR","called now");
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.i("Cancelling ","now");
            Toast.makeText(ctx, R.string.invalid_podcast,Toast.LENGTH_SHORT).show();
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            episodes = newParser.loopAll();
            if(itunesMode){
                listener.loadEpisodes(false,itunesPod);
            } else {
                listener.loadEpisodes(false);
            }
        }
    }

    public ArrayList<PodEpisode> getEpisodes(){
        return episodes;
    }

    private class LatestEpRetriever extends AsyncTask<String,Integer,Void> {

        XMLUniversal parser;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(ctx);
            progressDialog.setTitle(ctx.getString(R.string.Loading_episodes_title));
            progressDialog.setMessage(ctx.getString(R.string.updating));
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            String[] feedLinks = params;
            parser = new XMLUniversal(ctx);

            for(int i = 0; i < feedLinks.length; i++){
                parser.parseXMLLatest(feedLinks[i]);
                publishProgress(i);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int currIndex = values[0];
            progressDialog.setMessage("Updating pod " + podNames[currIndex]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            episodes = parser.getLatestEpisodes();
            progressDialog.dismiss();
            listener.setupLatest();
        }
    }


    private class PodCreator extends AsyncTask<String,Void,Podcast>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
/*
            progressDialog = new ProgressDialog(ctx);
            progressDialog.setTitle("Setting up first time use");
            progressDialog.setMessage("Updating podcasts..");
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
*/
        }

        @Override
        protected Podcast doInBackground(String... params) {
            String feed = params[0];

            XMLUniversal newParser = new XMLUniversal(ctx);

            String title = newParser.parsePodcastName(feed);
            String desc = newParser.parsePodcastDesc(feed);
            String image = newParser.parsePodcastImage(feed);


            newPod.setPodName(title);
            newPod.setGeneralDesc(desc);
            if(newPod.getPodImageUrl() == null) {
                newPod.setPodImageUrl(image);
            }


            return newPod;
        }

        @Override
        protected void onPostExecute(Podcast podcast) {
           // progressDialog.dismiss();
            super.onPostExecute(podcast);
        }
    }


    public Bitmap getBitmapFromURL(String imageURL) {
        try {
            URL url = new URL(imageURL);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            if (myBitmap != null) {
                Bitmap scaledImage = scaleBitmap(myBitmap, 210, 210);
                return scaledImage;
            } else return null;


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());

        return output;
    }
}
