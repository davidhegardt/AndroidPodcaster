package dahe0070.androidpodcaster;

/**
 * Created by Dave on 2017-08-14.
 */

public class Station {

    private String title;
    private String feedLink;
    private String stationImage;


    public Station(String title) {
        this.title = title;
        this.feedLink = "";
        this.stationImage = "";
    }

    public Station(String title,String feedLink, String stationImage){
        this.title = title;
        this.feedLink = feedLink;
        this.stationImage = stationImage;
    }

    public String getTitle(){
        return this.title;
    }

    public String getFeedLink() {
        return this.feedLink;
    }

    public String getStationImage() {
        return this.stationImage;
    }

    public void setTitle(String mtitle){
        this.title = mtitle;
    }

    public void setFeedLink(String mfeedLink) {
        this.feedLink = mfeedLink;
    }

    public void setStationImage(String mstationImage) {
        this.stationImage = mstationImage;
    }
}
