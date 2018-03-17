package dahe0070.androidpodcaster;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Dave on 2017-08-14.
 */

public class RadioPlayer {

    private MediaPlayer stationPlayer;
    private Context context;
    private String stationName;
    private int seekForwardTime = 10000;
    private int seekBackwardTime = 10000;

    public RadioPlayer(Context ctx) {
        this.context = ctx;
    }



    public void stopPlay() {
        if (stationPlayer.isPlaying()){
            stationPlayer.stop();
            stationPlayer.reset();
        }
    }

    public void releasePlayer(){
        stationPlayer.release();
    }

    public boolean isPlaying(){
        if (stationPlayer != null && stationPlayer.isPlaying()){
            return true;
        } else {
            return false;
        }
    }

    public void initPlayer(String link, String statName) {

        this.stationName = statName;

        if (stationPlayer != null && stationPlayer.isPlaying()){
            stopPlay();
        }


        stationPlayer = new MediaPlayer();
        try {
            stationPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            stationPlayer.setDataSource(link);

        } catch (IOException e) {
            e.printStackTrace();
        }

        stationPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                //Log.i("Buffering", "" + percent);
            }
        });
    }

    public void startPlay() {
        stationPlayer.prepareAsync();

        final ProgressDialog progressDialog = ProgressDialog.show(context,"Loading Station " + stationName,"Loading..");

        stationPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                stationPlayer.start();
            }
        });


    }

    public void forward() {
        if (isPlaying()) {
            int currentProgress = stationPlayer.getCurrentPosition();

            if (currentProgress + seekForwardTime <= stationPlayer.getDuration()){
                stationPlayer.seekTo(currentProgress + seekForwardTime);
            }

        }
    }

    public boolean PlayPause(){
        boolean play = false;

        if (isPlaying()){
            stationPlayer.pause();
            play = false;
            return play;
        } else if(stationPlayer != null) {
            stationPlayer.start();
            play = true;
            return play;
        }

        return play;
    }

    public String getTotalEpisodeLenght(){
        long totalDuration = stationPlayer.getDuration();
        //long currentDuration = stationPlayer.getCurrentPosition();

        String totalDurationString = milliSecondToTimer(totalDuration);

        return totalDurationString;
    }

    public String getCurrentDurationEpisode(){
        //long totalDuration = stationPlayer.getDuration();
        long currentDuration = stationPlayer.getCurrentPosition();

        String currDurationString = milliSecondToTimer(currentDuration);

        return currDurationString;
    }

    // Return time in progressbar time
    public int updateProgressBar(){
        long totalDuration = stationPlayer.getDuration();
        long currentDuration = stationPlayer.getCurrentPosition();

        int progress = (int) getPercentProgress(currentDuration,totalDuration);

        return progress;
    }

    public void backward() {
        if (isPlaying()) {
            int currentProgress = stationPlayer.getCurrentPosition();

            if (currentProgress - seekBackwardTime >= 0){
                stationPlayer.seekTo(currentProgress - seekBackwardTime);
            } else {
                stationPlayer.seekTo(0);
            }
        }
    }

    public int getDuration(){
        return stationPlayer.getDuration();
    }

    public void seekTo(int newTime){
        stationPlayer.seekTo(newTime);
    }

    // convert to timer time
    private String milliSecondToTimer(long milliSeconds){
        String finalTime = "";
        String secondString = "";

        int hours = (int)(milliSeconds / (1000*60*60));
        int minutes = (int)(milliSeconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliSeconds % (1000*60*60)) % (1000*60) / 1000);

        if(hours > 0){
            finalTime = hours + ":";
        }

        if(seconds < 10){
            secondString = "0" + seconds;
        } else {
            secondString = "" + seconds;
        }

        finalTime = finalTime + minutes + ":" + secondString;

        return finalTime;

    }


    public int progressToTimer(int progress, int totalDuration){
        int currentDuration = 0;
        totalDuration = (int) (totalDuration /1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        return currentDuration * 1000;
    }


    private int getPercentProgress(long curDuration,long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (curDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        percentage =(((double)currentSeconds)/totalSeconds)*100;

        return percentage.intValue();

    }


}
