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

    public static ArrayList<Country> addFirstCountries(){
        ArrayList<Country> countries = new ArrayList<>();
        countries.add(new Country("Sweden","SE"));
        countries.add(new Country("Denmark","DK"));
        countries.add(new Country("Norway","NO"));
        countries.add(new Country("Finland","FI"));
        countries.add(new Country("Great Britain","GB"));
        countries.add(new Country("USA","US"));
        countries.add(new Country("France","FR"));
        countries.add(new Country("Spain","ES"));
        countries.add(new Country("Germany","DE"));
        countries.add(new Country("Russia","RU"));
        countries.add(new Country("China","CN"));
        countries.add(new Country("Netherlands","NL"));
        countries.add(new Country("Hong Kong","HK"));

        return countries;

    }

    public static ArrayList<Country> getCountries(){
        ArrayList<Country> countries = new ArrayList<>();

        countries.add(new Country("United Arab Emirates","AE"));
        countries.add(new Country("Antigua and Barbuda","AG"));
        countries.add(new Country("Anguilla","AI"));
        countries.add(new Country("Albania","AL"));
        countries.add(new Country("Armenia","AM"));
        countries.add(new Country("Angola","AO"));
        countries.add(new Country("Argentina","AR"));
        countries.add(new Country("Austria","AT"));
        countries.add(new Country("Australia","AU"));
        countries.add(new Country("Azerbaijan","AZ"));
        countries.add(new Country("Barbados","BB"));
        countries.add(new Country("Belgium","BE"));
        countries.add(new Country("Burkina-Faso","BF"));
        countries.add(new Country("Bulgaria","BG"));
        countries.add(new Country("Bahrain","BH"));
        countries.add(new Country("Benin","BJ"));
        countries.add(new Country("Bermuda","BM"));
        countries.add(new Country("Brunei Darussalam","BN"));
        countries.add(new Country("Bolivia","BO"));
        countries.add(new Country("Brazil","BR"));
        countries.add(new Country("Bahamas","BS"));
        countries.add(new Country("Bhutan","BT"));
        countries.add(new Country("Botswana","BW"));
        countries.add(new Country("Belarus","BY"));
        countries.add(new Country("Belize","BZ"));
        countries.add(new Country("Canada","CA"));
        countries.add(new Country("DR Congo","CG"));
        countries.add(new Country("Switzerland","CH"));
        countries.add(new Country("Chile","CL"));
        countries.add(new Country("China","CN"));
        countries.add(new Country("Colombia","CO"));
        countries.add(new Country("Costa Rica","CR"));
        countries.add(new Country("Cape Verde","CV"));
        countries.add(new Country("Cyprus","CY"));
        countries.add(new Country("Czech Republic","CZ"));
        countries.add(new Country("Germany","DE"));
        countries.add(new Country("Denmark","DK"));
        countries.add(new Country("Dominica","DM"));
        countries.add(new Country("Dominican Republic","DO"));
        countries.add(new Country("Algeria","DZ"));
        countries.add(new Country("Ecuador","EC"));
        countries.add(new Country("Estonia","EE"));
        countries.add(new Country("Egypt","EG"));
        countries.add(new Country("Spain","ES"));
        countries.add(new Country("Finland","FI"));
        countries.add(new Country("Fiji","FJ"));
        countries.add(new Country("Micronesia","FM"));
        countries.add(new Country("France","FR"));
        countries.add(new Country("Great Britain","GB"));
        countries.add(new Country("Grenada","GD"));
        countries.add(new Country("Ghana","GH"));
        countries.add(new Country("Gambia","GM"));
        countries.add(new Country("Greece","GR"));
        countries.add(new Country("Guatemala","GT"));
        countries.add(new Country("Guinea Bissau","GW"));
        countries.add(new Country("Guyana","GY"));
        countries.add(new Country("Hong Kong","HK"));
        countries.add(new Country("Honduras","HN"));
        countries.add(new Country("Croatia","HR"));
        countries.add(new Country("Hungaria","HU"));
        countries.add(new Country("Indonesia","ID"));
        countries.add(new Country("Ireland","IE"));
        countries.add(new Country("Israel","IL"));
        countries.add(new Country("India","IN"));
        countries.add(new Country("Iceland","IS"));
        countries.add(new Country("Italy","IT"));
        countries.add(new Country("Jamaica","JM"));
        countries.add(new Country("Jordan","JO"));
        countries.add(new Country("Japan","JP"));
        countries.add(new Country("Kenya","KE"));
        countries.add(new Country("Krygyzstan","KG"));
        countries.add(new Country("Cambodia","KH"));
        countries.add(new Country("Saint Kitts and Nevis","KN"));
        countries.add(new Country("South Korea","KR"));
        countries.add(new Country("Kuwait","KW"));
        countries.add(new Country("Cayman Islands","KY"));
        countries.add(new Country("Kazakhstan","KZ"));
        countries.add(new Country("Laos","LA"));
        countries.add(new Country("Lebanon","LB"));
        countries.add(new Country("Saint Lucia","LC"));
        countries.add(new Country("Sri Lanka","LK"));
        countries.add(new Country("Liberia","LR"));
        countries.add(new Country("Lithuania","LT"));
        countries.add(new Country("Luxembourg","LU"));
        countries.add(new Country("Latvia","LV"));
        countries.add(new Country("Moldova","MD"));
        countries.add(new Country("Madagascar","MG"));
        countries.add(new Country("Macedonia","MK"));
        countries.add(new Country("Mali","ML"));
        countries.add(new Country("Mongolia","MN"));
        countries.add(new Country("Macau","MO"));
        countries.add(new Country("Mauritania","MR"));
        countries.add(new Country("Montserrat","MS"));
        countries.add(new Country("Malta","MT"));
        countries.add(new Country("Mauritius","MU"));
        countries.add(new Country("Malawi","MW"));
        countries.add(new Country("Mexico","MX"));
        countries.add(new Country("Malaysia","MY"));
        countries.add(new Country("Mozambique","MZ"));
        countries.add(new Country("Namibia","NA"));
        countries.add(new Country("Niger","NE"));
        countries.add(new Country("Nigeria","NG"));
        countries.add(new Country("Nicaragua","NI"));
        countries.add(new Country("Netherlands","NL"));
        countries.add(new Country("Nepal","NP"));
        countries.add(new Country("Norway","NO"));
        countries.add(new Country("New Zealand","NZ"));
        countries.add(new Country("Oman","OM"));
        countries.add(new Country("Panama","PA"));
        countries.add(new Country("Peru","PE"));
        countries.add(new Country("Papua New Guinea","PG"));
        countries.add(new Country("Philippines","PH"));
        countries.add(new Country("Pakistan","PK"));
        countries.add(new Country("Poland","PL"));
        countries.add(new Country("Portugal","PT"));
        countries.add(new Country("Palau","PW"));
        countries.add(new Country("Paraguay","PY"));
        countries.add(new Country("Qatar","QA"));
        countries.add(new Country("Romania","RO"));
        countries.add(new Country("Russia","RU"));
        countries.add(new Country("Saudi Arabia","SA"));
        countries.add(new Country("Soloman Islands","SB"));
        countries.add(new Country("Seychelles","SC"));
        countries.add(new Country("Sweden","SE"));
        countries.add(new Country("Singapore","SG"));
        countries.add(new Country("Slovenia","SI"));
        countries.add(new Country("Slovakia","SK"));
        countries.add(new Country("Sierra Leone","SL"));
        countries.add(new Country("Senegal","SN"));
        countries.add(new Country("Suriname","SR"));
        countries.add(new Country("Sao Tome e Principe","ST"));
        countries.add(new Country("El Salvador","SV"));
        countries.add(new Country("Swaziland","SZ"));
        countries.add(new Country("Turks and Caicos Islands","TC"));
        countries.add(new Country("Chad","TD"));
        countries.add(new Country("Thailand","TH"));
        countries.add(new Country("Tajikistan","TJ"));
        countries.add(new Country("Turkmenistan","TM"));
        countries.add(new Country("Tunisia","TN"));
        countries.add(new Country("Turkey","TR"));
        countries.add(new Country("Trinidad and Tobago","TT"));
        countries.add(new Country("Taiwan","TW"));
        countries.add(new Country("Tanzania","TZ"));
        countries.add(new Country("Ukraine","UA"));
        countries.add(new Country("Uganda","UG"));
        countries.add(new Country("United States","US"));
        countries.add(new Country("Uruguay","UY"));
        countries.add(new Country("Uzbekistan","UZ"));
        countries.add(new Country("Saint Vincent and the Grenadines","VC"));
        countries.add(new Country("Venezuela","VE"));
        countries.add(new Country("British Virgin Islands","VG"));
        countries.add(new Country("Vietnam","VN"));
        countries.add(new Country("Yemen","YE"));
        countries.add(new Country("South Africa","ZA"));
        countries.add(new Country("Zimbabwe","ZW"));







/*
        countries.add(new Country("Sweden","SE"));
        countries.add(new Country("Denmark","DK"));
        countries.add(new Country("Norway","NO"));
        countries.add(new Country("Finland","FI"));
        countries.add(new Country("Great Britain","GB"));
        countries.add(new Country("USA","US"));
        countries.add(new Country("France","FR"));
        countries.add(new Country("Spain","ES"));
        countries.add(new Country("Germany","DE"));
        countries.add(new Country("Russia","RU"));
        countries.add(new Country("China","CN"));
        countries.add(new Country("Netherlands","NL"));
        countries.add(new Country("Hong Kong","HK"));
        */

        Collections.sort(countries,new Comparator<Country>() {
            @Override
            public int compare(Country o1, Country o2) {
                return o1.getCountryName().compareTo(o2.getCountryName());
            }
        });
        return countries;

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
