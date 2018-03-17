package dahe0070.androidpodcaster;


import android.text.format.DateUtils;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Class used to parse RSS data from
 * podcasts and episodes which uses an RSS feed to display data.
 * Parses all information about podcast : title,feedlink,and image
 * Parses all information about episode : title,image link,description,date.
 * Class is called in order to parse and create Episodes of a podcast
 */

public class RSSparser {

    private final String content;

    public RSSparser(String content) {
        this.content = content;
    }


    /*
        Returns title of the current podcast

     */
    public String getTitle() {
        String s = getTextBetween("<title>", "</title");

        s = s.replaceAll("&amp;", "&");
        return s;
    }

    /*
        Returns the description of the podcast

     */

    public String getGeneralDesc(){
        StringBuffer generalDescBuffer = new StringBuffer();
        // Every episode is in separate <item>-tags. Creating a
        // new substring of content with start at the first tag <item>
        String tmp = content.substring(content.indexOf("<description>"));

        String generalDesc = "";

        generalDesc = getTextBetween("<description>","</description>");
        generalDesc = removeCDATA(generalDesc);
        String remove = getTextBetween(generalDesc,"<",">");
        generalDesc = trimString(generalDesc,remove);
        generalDesc = trimString(generalDesc,"<");
        generalDesc = trimString(generalDesc,">");


        return generalDesc;
    }



    public String getImageRevisionist(){
        return "http://static.megaphone.fm/podcasts/1427a2f4-2674-11e6-a3d7-cf7ee2a2c03c/image/uploads_2F1482446939047-8xkz61rh3k6t5hke-1d3a74b59a8aa1c724e95f8e00fae249_2FRevisionistHistory_1400x1400.jpg";
    }

    public String getImageUnderHuden(){
        return "https://imagecdn.acast.com/image?h=1500&w=1500&source=https%3A%2F%2Facastprod.blob.core.windows.net%3A443%2Fmedia%2Fv1%2Fde150f55-7c1b-4668-80be-25c9353a266c%2Fkakanhermansson-j6gk7rga.jpg";
    }



    public String getImageLink(){
        String s = getTextBetween("<image>", "</image>");
        s = getTextBetween("<url>", "</url>");
        // Image is between url tags

        if (s.isEmpty()) {
            return "";
        } else return s;
    }

    /*
            Returns description for an episode if it contains CDATA tags

     */

    public String getDescription() {
        StringBuffer Description = new StringBuffer();
        // Every episode is in separate <item>-tags. Creating a
        // new substring of content with start at the first tag <item>
        String tmp = content.substring(content.indexOf("<item>"));

        String desc;

        while ((desc = getTextBetween(tmp, "<description><![CDATA[", "]]></description>")) != "") {
            // Description is between descriptionCDATA tags, if nothing is returned then there are no more items

            Description.append(removeCDATA(desc) + "<br>");

            int index = tmp.indexOf("</item>");

            tmp = tmp.substring(index + 1);
        }

        String formatDesc = Description.toString();

        formatDesc = formatDesc.replaceAll("<p>","");
        formatDesc = formatDesc.replaceAll("</p>","");
        formatDesc = formatDesc.replaceAll("'"," ");

        return formatDesc;

    }

    /*
        Returns description if not CDATA is present

     */

    public String getDescriptionNoCDATA(){
        StringBuffer Description = new StringBuffer();
        // Every episode is in separate <item>-tags. Creating a
        // new substring of content with start at the first tag <item>
        String tmp = content.substring(content.indexOf("<item>"));

        String desc;

        while ((desc = getTextBetween(tmp, "<description>", "</description>")) != "") {
            // Description is between description tags, if nothing is returned then there are no more items

            Description.append(removeCDATA(desc) + "<br>");

            int index = tmp.indexOf("</item>");

            tmp = tmp.substring(index + 1);
        }

        String formatDesc = Description.toString();

        formatDesc = formatDesc.replaceAll("<p>","");           // Removes html tag <p>
        formatDesc = formatDesc.replaceAll("</p>","");
        formatDesc = formatDesc.replaceAll("'"," ");            // removes ' so that it can be read to database

        return formatDesc;
    }

    /*
        Helper function, calls parent function
     */
    private String getTextBetween(String start, String end) {
        return getTextBetween(content, start, end);
    }

    /*
        Helper function to remove data after a certain substring
     */
    public String removeAfter(String input,String after){
        String trimmed = input.substring(0,input.indexOf(after));

        return trimmed;
    }

    /*
        Trims down string to remove a specific section
     */
    private String trimString(String untrimmed,String removeFirst){
        String trimmed = untrimmed.replace(removeFirst,"");
        return trimmed;
    }

    /*
        Removes CDATA tags from input
     */
    private String removeCDATA(String text) {
        String cdata = getTextBetween(text, "CDATA[", "]");
        return cdata.length() > 0 ? cdata : text;
    }

    /*
        Function to retrieve text between start and end
     */
    private String getTextBetween(String text, String start, String end) {
        int startindex = text.indexOf(start);
        int endindex = text.indexOf(end, startindex);

        if (startindex < 0 || endindex < 0 || endindex < startindex) {
            return "";
        }

        return text.substring(startindex + start.length(), endindex);
    }

    /*
        Function to extract titles of episodes
     */

    public String getTitles(){
        StringBuffer titleBuffer = new StringBuffer();

        // Every episode is in separate <item>-tags. Creating a
        // new substring of content with start at the first tag <item>
        String tmp = content.substring(content.indexOf("<item>"));

        String title;
        while ((title = getTextBetween(tmp, "<title>", "</title>")) != "") {
            // Title is between enclosure tags, if nothing is returned then there are no more items

            titleBuffer.append(removeCDATA(title) + "<br>");

            int index = tmp.indexOf("</item>");

            tmp = tmp.substring(index + 1);
        }
        String formatted = titleBuffer.toString();
        formatted = formatted.replaceAll("&quot;", "");             // Functions to replace HTML codes in titles
        formatted = formatted.replaceAll("&apos;", "");
        formatted = formatted.replaceAll("&amp;","&");
        formatted = formatted.replaceAll("'","");
        formatted = formatted.replaceAll("\"", "");
        formatted = formatted.replace("?", "");
        formatted = formatted.replaceAll(":"," ");
        return formatted;
    }

    /*
        Returns image of an episode

     */


    public String getEpisodeImage() {
        StringBuffer imageBuffer = new StringBuffer();
        // Every episode is in separate <item>-tags. Creating a
        // new substring of content with start at the first tag <item>
        String tmp = content.substring(content.indexOf("<item>"));

        String link;
        while ((link = getTextBetween(tmp, "<itunes:image href=\"", "\" />")) != "") {
            // Image file is between enclosure tags, if nothing is returned then there are no more items

            imageBuffer.append(link + "<br>");

            int index = tmp.indexOf("</item>");

            tmp = tmp.substring(index + 1);
        }

        return imageBuffer.toString();
    }

    /*
        Returns duration of an episode
     */

    public String getDurations(){
        StringBuffer durBuffer = new StringBuffer();
        // Every episode is in separate <item>-tags. Creating a
        // new substring of content with start at the first tag <item>
        String tmp = content.substring(content.indexOf("<item>"));

        String duration;
        while ((duration = getTextBetween(tmp,"<itunes:duration>","</itunes:duration>")) != ""){
            // Duration is between duration tags, if nothing is returned then there are no more items

            durBuffer.append(duration + "<br>");

            int index = tmp.indexOf("</item>");

            tmp = tmp.substring(index + 1);
        }

        return durBuffer.toString();
    }


    /*
        Returns the date of an episode
     */
    public String getDates(){
        StringBuffer durBuffer = new StringBuffer();
        // Every episode is in separate <item>-tags. Creating a
        // new substring of content with start at the first tag <item>
        String tmp = content.substring(content.indexOf("<item>"));

        String duration;
        while ((duration = getTextBetween(tmp,"<pubDate>","</pubDate>")) != ""){
            // Date is between pubDate tags, if nothing is returned then there are no more items

            durBuffer.append(duration + "<br>");

            int index = tmp.indexOf("</item>");

            tmp = tmp.substring(index + 1);
        }

        String dateString = durBuffer.toString();



        return dateString;

    }

    /*
        Function to format the date of an episode, to present time ago in swedish
     */


    public String formatDate(String dateString){
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

        Date date = null;

        try {
            date = sdf.parse(dateString);
            SimpleDateFormat newFormat = new SimpleDateFormat("dd/MM/yyyy");
            long dateTime = date.getTime();
            String timeago = (String) DateUtils.getRelativeTimeSpanString(dateTime);
            timeago = timeago.replaceAll("för","");
            timeago = timeago.replaceAll("sedan","sen");

            return timeago;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
        Function to retrieve an mp3 link to the current episode
     */

    public String getSoundModern() {
        StringBuffer sound = new StringBuffer();

        // Every episode is in separate <item>-tags. Creating a
        // new substring of content with start at the first tag <item>
        String tmp = content.substring(content.indexOf("<item>"));

        String link;
        while ((link = getTextBetween(tmp, "url=\"", "?")) != "") {
            // Sound file is between url tags, if nothing is returned then there are no more items

            if (link.contains(".mp3")) {
                sound.append(removeCDATA(link) + "<br>");
            }

            // Finds out where item ends
            int index = tmp.indexOf("</item>");

            // Creates a substring of rest of content. Adds one to ignore item so that this
            // does not get used in final string
            tmp = tmp.substring(index + 1);
        }

        return sound.toString();
    }

    /*
        Another function to retrieve mp3 link to current episode
     */

    public String getSoundOther() {
        StringBuffer sound = new StringBuffer();

        // Every episode is in separate <item>-tags. Creating a
        // new substring of content with start at the first tag <item>
        String tmp = content.substring(content.indexOf("<item>"));

        String link;
        while ((link = getTextBetween(tmp, "<link>", "</link>")) != "") {
            // Sound file is between link tags, if nothing is returned then there are no more items

            if (link.contains(".mp3")) {
                sound.append(removeCDATA(link) + "<br>");
            }

            // Finds out where item ends
            int index = tmp.indexOf("</item>");

            // Creates a substring of rest of content. Adds one to ignore item so that this
            // does not get used in final string
            tmp = tmp.substring(index + 1);
        }

        return sound.toString();
    }

    /*
        Function to retrieve mp3 link of acast podcasts
     */

    public String getSoundAcast() {
        StringBuffer sound = new StringBuffer();

        // Every episode is in separate <item>-tags. Creating a
        // new substring of content with start at the first tag <item>
        String tmp = content.substring(content.indexOf("<item>"));

        String link;
        while ((link = getTextBetween(tmp, "<enclosure url=\"", "\" length")) != "") {
            // Sound file is between enclosure tags, if nothing is returned then there are no more items

            if (link.contains(".mp3")) {
                sound.append((link) + "<br>");
            }

            // Finds out where item ends
            int index = tmp.indexOf("</item>");

            // Creates a substring of rest of content. Adds one to ignore item so that this
            // does not get used in final string
            tmp = tmp.substring(index + 1);
        }


        return sound.toString();
    }

    /*
        Function to retrieve image for a podcast, uses XML parser
     */

    public String XMLImageParser(String urlString) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = null;

        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(urlString).openStream());

            NodeList imgNodes = doc.getElementsByTagName("itunes:image");
            for (int x = 0; x < imgNodes.getLength(); x++) {
                Node imgNode = imgNodes.item(x);
                Element eElement = (Element) imgNode;
                String tempOut = eElement.getAttribute("href");

                return tempOut;                 // Returns the first image in file, this is the podcast image
            }

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    }
