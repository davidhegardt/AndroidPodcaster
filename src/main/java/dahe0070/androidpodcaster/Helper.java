package dahe0070.androidpodcaster;

import android.text.format.DateUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Dave on 2018-02-09.
 */

public class Helper {

    public static boolean removeMP3(String localLink){
        File deleteFile = new File(localLink);
        if(deleteFile.exists()) {
            boolean deleted = deleteFile.delete();
            return deleted;
        } else return false;
    }

    public static ArrayList<PodEpisode> sortByDate(ArrayList<PodEpisode> episodes){
        Collections.sort(episodes, new Comparator<PodEpisode>() {
            @Override
            public int compare(PodEpisode o1, PodEpisode o2) {
                Date date1 = stringToDate(o1.getDate());
                Date date2 = stringToDate(o2.getDate());
                if(date1 == null){
                    return (date2 == null) ? 0 : -1;
                }
                if(date2 == null){
                    return 1;
                }
                //return stringToDate(o1.getDate()).compareTo(stringToDate(o2.getDate()));
                return date1.compareTo(date2);
            }
        });
        Collections.reverse(episodes);
        return episodes;
    }

    public static ArrayList<PodEpisode> sortByPodcast(ArrayList<PodEpisode> episodes){
        Collections.sort(episodes, new Comparator<PodEpisode>() {
            @Override
            public int compare(PodEpisode o1, PodEpisode o2) {
                return o1.getPodName().compareTo(o2.getPodName());
            }
        });
        return episodes;
    }

    public static String dateToString(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        //SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");


        Date date = null;
        String stringDate = "";
        try {
            date = sdf.parse(dateString);
            stringDate = date.toString();

            return stringDate;
        } catch (ParseException e) {
            e.printStackTrace();
        } return stringDate;

    }

    public static boolean URLworking(String mURL){
        try {
            URL testURL = new URL(mURL);

            HttpURLConnection.setFollowRedirects(false);

            HttpURLConnection httpURLConnection = (HttpURLConnection) testURL.openConnection();

            httpURLConnection.setRequestMethod("HEAD");

            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
            int responseCode = httpURLConnection.getResponseCode();

            return responseCode == HttpURLConnection.HTTP_OK;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


    }

    public static Date stringToDate(String dateString){
        //SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        //SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        //SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy", Locale.US);
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mma");
        //SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

        Date date = null;

        try {
            date = sdf.parse(dateString);

            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        } return null;
    }

    public static String dateToTimeago(String dateString){
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

        Date date = null;

        try {
            date = sdf.parse(dateString);
            SimpleDateFormat newFormat = new SimpleDateFormat("dd/MM/yyyy");
            //String formatedDate = newFormat.format(date);
            long dateTime = date.getTime();
            String timeago = (String) DateUtils.getRelativeTimeSpanString(dateTime);
            timeago = timeago.replaceAll("för","");
            timeago = timeago.replaceAll("sedan","sen");
            // Log.i("Time ago",timeago);
            return timeago;
        } catch (ParseException e) {
            Log.i("could not format date","now");
            return "unkown date";
            //e.printStackTrace();

        }
        //return null;
    }
}
