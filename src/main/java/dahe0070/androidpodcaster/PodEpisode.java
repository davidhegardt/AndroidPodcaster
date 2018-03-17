package dahe0070.androidpodcaster;

import java.io.Serializable;

/**
 * Created by Dave on 2017-08-15.
 */

public class PodEpisode implements Serializable {

    private String podName;
    private String mp3Link;
    private String epTitle;
    private String description;
    private String songImage;
    private String duration;
    private String date;
    private String localLink;
    private String backupImage;
    private int progress;
    private boolean read;
    private boolean downloaded = false;

    public PodEpisode(){
        this.podName = "";
        this.mp3Link = "";
        this.epTitle = "";
        this.description = "";
        this.songImage = "";
        this.duration = "";
        this.read = false;
        this.progress = 0;
    }

    public PodEpisode(String mPodName){
        this.podName = mPodName;
        this.mp3Link = "";
        this.epTitle = "";
        this.description = "";
        this.songImage = "";
        this.duration = "";
        this.read = false;
        this.progress = 0;
    }

    public PodEpisode(String mPodName,String epTitle,String mp3Link,String mDesc,String imgLink){
        this.podName = mPodName;
        this.mp3Link = mp3Link;
        this.epTitle = epTitle;
        this.description = mDesc;
        this.songImage = imgLink;
        this.duration = "";
        this.date = "Idag";
        this.read = false;
        this.progress = 0;
    }

    public PodEpisode(String mPodName,String epTitle,String mp3Link,String mDesc,String imgLink,String date){
        this.podName = mPodName;
        this.mp3Link = mp3Link;
        this.epTitle = epTitle;
        this.description = mDesc;
        this.songImage = imgLink;
        this.duration = "";
        this.date = date;
        this.read = false;
        this.progress = 0;
    }

    public String getBackupImage(){
        return this.backupImage;
    }

    public void setBackupImage(String backupImage) {
        this.backupImage = backupImage;
    }

    public void setProgress(int progress){
        this.progress = progress;
    }

    public int getProgress(){
        return this.progress;
    }

    public void setDate(String date){
        this.date = date;
    }

    public void setDownloaded(boolean status){
        this.downloaded = status;
    }

    public boolean getDownloaded(){
        return this.downloaded;
    }

    public void setLocalLink(String path){
        this.localLink = path;
    }

    public String getLocalLink(){
        if (getDownloaded()) {
            return this.localLink;
        } else {
            return this.mp3Link;
        }
    }

    public void setPodName(String podName){
        this.podName = podName;
    }

    public String getPodName(){
        return this.podName;
    }


    public void setMp3Link(String mp3Link){
        this.mp3Link = mp3Link;
    }

    public String getMp3Link(){
        return this.mp3Link;
    }


    public void setEpTitle(String epTitle){
        this.epTitle = epTitle;
    }

    public String getEpTitle(){
        return this.epTitle;
    }


    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription(){
        return this.description;
    }

    public void setSongImage(String songImage){
        this.songImage = songImage;
    }

    public String getSongImage(){
        return this.songImage;
    }

    public void setDuration(String duration){
        this.duration = duration;
    }

    public String getDuration(){
        return this.duration;
    }

    public String getDate() {return this.date;}

    public void setRead(boolean read){
        this.read = read;
    }

    public boolean getRead(){
        return this.read;
    }


}
