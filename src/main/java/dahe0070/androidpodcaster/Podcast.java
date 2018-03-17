package dahe0070.androidpodcaster;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Dave on 2017-08-15.
 */

public class Podcast implements Serializable {

    private String feedLink;
    private String podName;
    private String generalDesc;
    private Bitmap podImage;
    private String podType;
    private String podImageUrl;
    private String backupImage;

    public Podcast(){
        this.feedLink = "";
        this.podName = "";
    }

    public void setBackupImage(String backupImage) {
        this.backupImage = backupImage;
    }

    public String getBackupImage() {
        return backupImage;
    }

    public Podcast(String mPodName, String mFeedLink){
        this.feedLink = mFeedLink;
        this.podName = mPodName;
    }

    public Podcast(String mPodName, String mFeedLink,String type){
        this.podName = mPodName;
        this.feedLink = mFeedLink;
        this.podType = type;
    }

    public void setPodType(String type){
        this.podType = type;
    }

    public void setGeneralDesc(String desc){
        this.generalDesc = desc;
    }

    public String getGeneralDesc(){
        return this.generalDesc;
    }

    public void setFeedLink(String newFeed){
        this.feedLink = newFeed;
    }

    public void setPodName(String newPodName){
        this.podName = newPodName;
    }

    public String getFeedLink(){
        return this.feedLink;
    }

    public String getPodName() {
        return this.podName;
    }

    public Bitmap getPodImage(){
        return this.podImage;
    }

    public String getPodType(){
        return this.podType;
    }

    public void setPodImage(Bitmap currBitmap){
        this.podImage = currBitmap;
    }

    public void setPodImageUrl(String newUrl){
        this.podImageUrl = newUrl;
    }

    public String getPodImageUrl(){
        return this.podImageUrl;
    }

}
