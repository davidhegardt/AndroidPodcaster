package dahe0070.androidpodcaster;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Dave on 2017-08-14.
 */

public interface PlayerClicks {

    public void radioPlay(String stationName);
    public void radioPause();
    public void radioStop();
    public boolean radioStatus();
    public void podClick(int podIndex);
    public void loadEpisodes(boolean xml);
    public void loadEpisodes(boolean xml, Podcast currpodcast);
    public void episodeClick(String epTitle, Context ctx, String mp3Link, int epIndex);
    public void startPodPlayer(String filePath,boolean downloaded);
    public void syncPodretrieval();
    public ArrayList<Podcast> syncEnglishRetrieval();
    public ArrayList<Podcast> syncSwedishRetrieval();
    public void syncEnglishDone();
    public void streamEpisode(String epTitle, Context ctx, String mp3Link, int epIndex);
    public void loadDownloadedEpisodes(ArrayList<PodEpisode> downloadList);
    public void showMediaController();
    public void switchMediaController(boolean status);
    public void setScrollingText(String currPod,String epTitle);
    public void navigateToPodlist();
    public void setupEnglishPodcasts();
    public void pauseAudio();
    public void setupLatest();
    public void itunesPodClick(Podcast newPod);
}
