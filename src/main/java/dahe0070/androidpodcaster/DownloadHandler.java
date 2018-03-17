package dahe0070.androidpodcaster;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Dave on 2017-08-16.
 */

public class DownloadHandler {

    private Context context;
    private ProgressDialog progressDialog;
    private String mp3Link;
    private String epTitle;
    private PlayerClicks listener;

    public DownloadHandler(Context ctx, String mp3Link,String epTitle){
        this.context = ctx;
        this.mp3Link = mp3Link;
        this.epTitle = epTitle;
        listener = (PlayerClicks) ctx;
    }

    public void startDownload(){
        new DownloadEpisode().execute(mp3Link);
    }

    private class DownloadEpisode extends AsyncTask<String,Integer,String>{

        private File file;

        DownloadEpisode(){

        }

        public void tempPlayer(String fileTemp){
            RadioPlayer radio = new RadioPlayer(context);
            radio.initPlayer(fileTemp,epTitle);
            radio.startPlay();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(context.getString(R.string.downloading_episode));
            progressDialog.setMessage(context.getString(R.string.downloading));
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... link) {
            try {
                URL url = new URL(link[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                long filesize = connection.getContentLength();
                long totalDataRead = 0;

                File sdCard = new File(Environment.getExternalStorageDirectory(),context.getString(R.string.downloaded_episodes));
                if(!sdCard.exists()){
                    sdCard.mkdirs();
                }
                String mp3title = epTitle.replaceAll(":","-");
                //file = new File(sdCard,epTitle + ".mp3");
                file = new File(sdCard,mp3title + ".mp3");

                BufferedInputStream in = new BufferedInputStream(connection.getInputStream());

                FileOutputStream fos = new FileOutputStream(file);

                BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
                byte[] data = new byte[1024];
                int i;
                while ((i = in.read(data,0,1024)) >= 0){
                    totalDataRead = totalDataRead + i;
                    bout.write(data,0,i);
                    long tmpPercent = (totalDataRead * 100) / filesize;
                    int percent = (int) tmpPercent;
                    if (percent > 100){
                        percent = 99;
                    }
                    publishProgress(percent);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException io){
                io.printStackTrace();
            }

            return file.getAbsolutePath();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String fileLocation) {
            //super.onPostExecute(aVoid);
            Log.i("file location:",fileLocation);
            progressDialog.dismiss();
            Toast.makeText(context,epTitle + context.getString(R.string.successfully_downloaded),Toast.LENGTH_SHORT).show();
            //tempPlayer(fileLocation);
            listener.startPodPlayer(fileLocation,true);
        }
    }

}
