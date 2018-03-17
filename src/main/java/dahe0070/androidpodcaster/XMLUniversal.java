package dahe0070.androidpodcaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Dave on 2017-08-27.
 */

public class XMLUniversal {

    private ArrayList<String> dates;
    private ArrayList<String> titleList;
    private ArrayList<String> descList;
    private ArrayList<String> imageList;
    private ArrayList<String> mp3Correct;
    private ArrayList<PodEpisode> latestEpisodes;
    private String podCastTitle;
    private String podCastDesc;
    private String podcastImage;
    private boolean readError = false;
    private Context ctx;

    public XMLUniversal(Context context) {
        latestEpisodes = new ArrayList<>();
        this.ctx = context;
    }

    public String getPodCastTitle(){
        return this.podCastTitle;
    }

    public String getPodCastDesc(){
        return this.podCastDesc;
    }

    public String getPodcastImage(){
        return this.podcastImage;
    }

    public String parsePodcastImage(String urlString) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = null;

        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(urlString).openStream());

            NodeList imgNodes = doc.getElementsByTagName("itunes:image");

            if(imgNodes.getLength() == 0) {
                Uri uri = Uri.parse("android.resource://dahe0070.androidpodcaster/drawable/default_image.png");
                String imageResource = uri.toString();
                return imageResource;
            }

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

    public String parsePodcastName(String urlString){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = null;

        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(urlString).openStream());

            NodeList titleNodes = doc.getElementsByTagName("title");
            for (int temp = 0; temp < titleNodes.getLength(); temp++) {
                Node nNode = titleNodes.item(temp);
                String tempOut = nNode.getTextContent();
                if (tempOut.contains("\"")) {
                    tempOut = tempOut.replace("\"", "");

                }
                tempOut = tempOut.replace("'","");
                return tempOut;                                 // First title == Podcast name
            }

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    public String parsePodcastDesc(String urlString){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = null;

        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(urlString).openStream());
            NodeList descNodes = doc.getElementsByTagName("description");

            if (descNodes.getLength() == 0){
                return "No description for podcast";
            }

            for (int z = 0; z < descNodes.getLength(); z++) {
                Node descNode = descNodes.item(z);
                String tempOut = descNode.getTextContent();
                tempOut = tempOut.replaceAll("'","");
                return tempOut;
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

    public void parseXMLLatest(String urlString){
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder documentBuilder = null;

        try {
            documentBuilder = builderFactory.newDocumentBuilder();

                Document doc = documentBuilder.parse(new URL(urlString).openStream());

                NodeList dateNodes = doc.getElementsByTagName("pubDate");

                dates = new ArrayList<>();

                for (int i = 1; i < dateNodes.getLength(); i++){
                    Node dateNode = dateNodes.item(i);
                    String tempOut = dateNode.getTextContent();
                    if(checkDate(tempOut)){
                        //String corrDate = formatDate(tempOut);
                        //String corrDate = Helper.dateToString(tempOut);
                        String corrDate = tempOut;
                        dates.add(corrDate);
                    } else break;
                }

                int maxLenght = dates.size();

            if(maxLenght == 0){
                return;
            }

            NodeList titleNodes = doc.getElementsByTagName("title");
            NodeList linkNodes = doc.getElementsByTagName("enclosure");
            NodeList imgNodes = doc.getElementsByTagName("itunes:image");
            NodeList descNodes = doc.getElementsByTagName("description");
            NodeList items = doc.getElementsByTagName("item");

            NodeList channel = doc.getElementsByTagName("channel");

            NodeList tags = null;
            for (int i = 0;i < channel.getLength();i++){
                tags = channel.item(i).getChildNodes();

            }

            for (int u = 0; u < tags.getLength();u++){
                String tagnames = tags.item(u).getNodeName();
                if (tagnames.equals("title")){
                    String title = tags.item(u).getTextContent();
                    Log.i("title",title);
                    podCastTitle = title;
                }

            }

            if(titleNodes.getLength() == 0){
                readError = true;
                return;
            }

            if(linkNodes.getLength() == 0){
                readError = true;
                return;
            }
            titleList = new ArrayList<String>();

            nodeExistsTitle(items, maxLenght);              // KOLLA VILKEN TITEL SOM ÄR PÅ SENASTE AVSNITTET
/*
            Log.i("titlenodes","" + titleNodes.getLength());
            Log.i("linkNodes","" + linkNodes.getLength());
            Log.i("descNodes","" + descNodes.getLength());
            Log.i("datenodes","" + dateNodes.getLength());
            Log.i("imgNodes","" + imgNodes.getLength());
*/
            imageList = new ArrayList<String>();

            if(imgNodes.getLength() == 0){
                Uri uri = Uri.parse("android.resource://dahe0070.jsonparsing/drawable/default_image.png");
                String imageResource = uri.toString();
                for (int i = 0; i < maxLenght;i++){
                    imageList.add(imageResource);
                    podcastImage = imageResource;
                }
            } else {

                if(imgNodes.getLength() > 0) {
                    for (int x = 0; x < maxLenght; x++) {
                        Node imgNode = imgNodes.item(x);
                        Element eElement = (Element) imgNode;
                        if (eElement != null) {
                            String tempOut = eElement.getAttribute("href");
                            //              tempOut = getTextBetween(tempOut);
                            //System.out.println(tempOut);
                            imageList.add(tempOut);
                        }

                    }
                    podcastImage = imageList.get(0); // första bilden
                    imageList.remove(0); // ta bort första bilden

                }

                if(imageList.size() != titleList.size()){
                    imageList = new ArrayList<>();
                    nodeExistsImage(items,maxLenght);
                }

            }

            descList = new ArrayList<String>();
            if (descNodes.getLength() < 2){
                nodeExistsSummary(items,maxLenght);
            } else {
                nodeExistsDescription(items, maxLenght);
            }

            String[] mp3Array = new String[maxLenght];

            for (int index = 0; index < maxLenght; index++) {
                Node linkNode = linkNodes.item(index);
                Element eElement = (Element) linkNode;
                String tempOut = eElement.getAttribute("url");
                mp3Array[index] = tempOut;
            }

            mp3Correct = new ArrayList<String>();

            for (int i = 0; i < mp3Array.length; i++) {
                String newLink = mp3Array[i];
                //System.out.println(i + " " + newLink);
                mp3Correct.add(newLink);
            }

            if (titleList.size() != mp3Correct.size()){
                mp3Correct = new ArrayList<>();
                nodeExistsLink(items, maxLenght);
            }

            for (int i = 0; i < maxLenght;i++){
                PodEpisode created = new PodEpisode(podCastTitle,titleList.get(i),mp3Correct.get(i),descList.get(i),imageList.get(i),dates.get(i));
                latestEpisodes.add(created);
                Log.i("Episodes lenght","" + latestEpisodes.size());
                Log.i("Latest ep ADDED","" + created.getPodName() + created.getEpTitle());
            }


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public boolean parseXMLSR(String urlString){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(urlString).openStream());


            NodeList titleNodes = doc.getElementsByTagName("title");
            NodeList linkNodes = doc.getElementsByTagName("enclosure");
            NodeList imgNodes = doc.getElementsByTagName("itunes:image");
            NodeList descNodes = doc.getElementsByTagName("description");
            NodeList dateNodes = doc.getElementsByTagName("pubDate");
            NodeList items = doc.getElementsByTagName("item");

            NodeList channel = doc.getElementsByTagName("channel");

            NodeList tags = null;
            for (int i = 0;i < channel.getLength();i++){
                tags = channel.item(i).getChildNodes();

            }

            for (int u = 0; u < tags.getLength();u++){
                String tagnames = tags.item(u).getNodeName();
                if (tagnames.equals("title")){
                    String title = tags.item(u).getTextContent();
                    Log.i("title",title);
                    podCastTitle = title;
                }

            }




            if(titleNodes.getLength() == 0){
                readError = true;
                return false;
            }

            if(linkNodes.getLength() == 0){
                readError = true;
                return false;
            }
            titleList = new ArrayList<String>();

            nodeExistsTitle(items);

            Log.i("titlenodes","" + titleNodes.getLength());
            Log.i("linkNodes","" + linkNodes.getLength());
            Log.i("descNodes","" + descNodes.getLength());
            Log.i("datenodes","" + dateNodes.getLength());
            Log.i("imgNodes","" + imgNodes.getLength());

            imageList = new ArrayList<String>();



            dates = new ArrayList<>();

            for (int y = 1; y < dateNodes.getLength(); y++){
                Node dateNode = dateNodes.item(y);
                String tempOut = dateNode.getTextContent();
                //String corrDate = formatDate(tempOut);
                //String corrDate = Helper.dateToString(tempOut);
                String corrDate = tempOut;
                dates.add(corrDate);
            }


/*
            for (int temp = 0; temp < titleNodes.getLength(); temp++) {
                Node nNode = titleNodes.item(temp);

                String tempOut = nNode.getTextContent();
                if (tempOut.contains("\"")) {
                    tempOut = tempOut.replace("\"", "");

                }
                tempOut = tempOut.replace("'","");
                titleList.add(tempOut);
            }
            podCastTitle = titleList.get(0); // Första titeln
            Log.i("title of pod","" + podCastTitle);
            Log.i("first entry","" + titleList.get(1));

            titleList.remove(0); // always remove first
            if (podCastTitle.contains(titleList.get(0))) {
                titleList.remove(0);
                Log.i("duplicate","removed");
            }
*/
            if(imgNodes.getLength() == 0){
                Uri uri = Uri.parse("android.resource://dahe0070.jsonparsing/drawable/default_image.png");
                String imageResource = uri.toString();
                for (int i = 0; i < linkNodes.getLength();i++){
                    imageList.add(imageResource);
                    podcastImage = imageResource;
                }
            } else {

                for (int x = 0; x < imgNodes.getLength(); x++) {
                    Node imgNode = imgNodes.item(x);
                    Element eElement = (Element) imgNode;
                    String tempOut = eElement.getAttribute("href");
      //              tempOut = getTextBetween(tempOut);
                    //System.out.println(tempOut);
                    imageList.add(tempOut);
                }
                podcastImage = imageList.get(0); // första bilden
                imageList.remove(0); // ta bort första bilden
/*
                if (imageList.size() < 2) {
                    imageList = new ArrayList<>();
                    for (int i = 0; i < titleList.size(); i++) {
                        imageList.add(podcastImage);
                    }
                }
*/

                if(imageList.size() != titleList.size()){
                    imageList = new ArrayList<>();
                    nodeExistsImage(items);
                }

            }



            descList = new ArrayList<String>();
            if (descNodes.getLength() < 2){
                nodeExistsSummary(items);
            } else {
                nodeExistsDescription(items);
            }
/*
            for (int z = 0; z < descNodes.getLength(); z++) {
                Node descNode = descNodes.item(z);
                //System.out.println(descNode.getTextContent());
                //System.out.println("\n");
                String tempOut = descNode.getTextContent();
                descList.add(tempOut);
            }
            podCastDesc = descList.get(0); // beskrivning av podden

            //nodeExists(items);
            descList.remove(0);
*/


            //imageList.remove(0);

            String[] mp3Array = new String[linkNodes.getLength()];

            for (int index = 0; index < linkNodes.getLength(); index++) {
                Node linkNode = linkNodes.item(index);
                Element eElement = (Element) linkNode;
                String tempOut = eElement.getAttribute("url");
                mp3Array[index] = tempOut;
            }

            mp3Correct = new ArrayList<String>();

            for (int i = 0; i < mp3Array.length; i++) {
                String newLink = mp3Array[i];
                //System.out.println(i + " " + newLink);
                mp3Correct.add(newLink);
            }

            if (titleList.size() != mp3Correct.size()){
                mp3Correct = new ArrayList<>();
                nodeExistsLink(items);
            }



        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return false;
        } catch (MalformedURLException url) {
            url.printStackTrace();
            return false;
        } catch (IOException io){
            io.printStackTrace();
            return false;
        } catch (SAXException sax){
            sax.printStackTrace();
            return false;
        }

        return true;

    }

    public ArrayList<PodEpisode> getLatestEpisodes(){
        return latestEpisodes;
    }

    public ArrayList<PodEpisode> loopAll(){

        if(!readError) {


            //dates;
            Log.i("dates size", "" + dates.size());

            //titleList;
            Log.i("titleList size", "" + titleList.size());

            //descList;
            Log.i("desclist size", "" + descList.size());

            //imageList;
            Log.i("imagelist size", "" + imageList.size());
            for (String i : imageList){
                Log.i("Ep IMG URL",i);
            }

            //mp3Correct;
            Log.i("mp3links size", "" + mp3Correct.size());

            //podCastTitle;
            Log.i("podcast title", "" + podCastTitle);
            //podCastDesc;
            Log.i("podcast desc", "" + podCastDesc);
/*
            for (String desc : descList) {
                Log.i("Description:", desc);
            }
*/
            for (String link : mp3Correct) {
                Log.i("mp3 link", link);
            }

            //podcastImage;
            Log.i("podcast image", "" + podcastImage);
            Log.i("podcast desc","" + podCastDesc);



        } else {
            Log.i("Read error","aborting");
        }

        if (titleList.size() != mp3Correct.size()){
            Log.i("MISSMATCH!","titles:" + titleList.size() + " mp3s " + mp3Correct.size());
        }

        ArrayList<PodEpisode> episodes = new ArrayList<>();

        ArrayList diffSizes = new ArrayList<>();

        int sOne = titleList.size();    int sThree = descList.size();
        int sTwo = mp3Correct.size();   int sFour = imageList.size();
        int sFive = dates.size();

        diffSizes.add(sOne);diffSizes.add(sTwo); diffSizes.add(sThree); diffSizes.add(sFour); diffSizes.add(sFive);

        Object safeMax = Collections.min(diffSizes);

        Log.i("safeMax size", "" + safeMax);

        int maxIt = (int) safeMax;


        for (int i = 0; i < maxIt;i++){
            PodEpisode created = new PodEpisode(podCastTitle,titleList.get(i),mp3Correct.get(i),descList.get(i),imageList.get(i),dates.get(i));
            episodes.add(created);
        }

        return episodes;

    }

    public void nodeExistsDescription(NodeList nodeList){
        for (int i = 0; i < nodeList.getLength();i++){
            NodeList childList = nodeList.item(i).getChildNodes();          //children is TITLE,LINK,ENCLOSURE ETC For every item
            innerLoopDescription(childList);
        }
        Log.i("Number of episodes","" + nodeList.getLength());
    }

    public void nodeExistsDescription(NodeList nodeList, int maxLenght){
        for (int i = 0; i < nodeList.getLength();i++){
            NodeList childList = nodeList.item(i).getChildNodes();          //children is TITLE,LINK,ENCLOSURE ETC For every item
            innerLoopDescription(childList);
        }
        Log.i("Number of episodes","" + nodeList.getLength());
    }

    public void nodeExistsSummary(NodeList nodeList){
        for (int i = 0; i < nodeList.getLength();i++){
            NodeList childList = nodeList.item(i).getChildNodes();
            innerLoopSummary(childList);
        }
    }

    public void nodeExistsSummary(NodeList nodeList, int maxLenght){
        for (int i = 0; i < maxLenght;i++){
            NodeList childList = nodeList.item(i).getChildNodes();
            innerLoopSummary(childList);
        }
    }

    private void innerLoopSummary(NodeList childList){
        String description = "No description";
        for (int i = 0; i < childList.getLength();i++){
            String child = childList.item(i).getNodeName();
            if(child.equals("itunes:summary")){
                description = childList.item(i).getTextContent();
                description = description.replaceAll("'","");
            }
        }
        descList.add(description);

    }

    private void innerLoopDescription(NodeList childList){
        String description = "No description";
        for (int i = 0; i < childList.getLength();i++){
            String child = childList.item(i).getNodeName();
            if(child.equals("description")){
                description = childList.item(i).getTextContent();
                description = description.replaceAll("'","");
            }
        }
        descList.add(description);
    }


    public void nodeExistsTitle(NodeList nodeList){                         //nodelist is ITEM
        for (int i = 0; i < nodeList.getLength();i++){
            NodeList childList = nodeList.item(i).getChildNodes();          //children is TITLE,LINK,ENCLOSURE ETC For every item
            innerLoopTitle(childList);
        }
        Log.i("Number of episodes","" + nodeList.getLength());
    }

    public void nodeExistsTitle(NodeList nodeList, int maxLenght){                         //nodelist is ITEM
        for (int i = 0; i < maxLenght;i++){
            NodeList childList = nodeList.item(i).getChildNodes();          //children is TITLE,LINK,ENCLOSURE ETC For every item
            innerLoopTitle(childList);
        }
        Log.i("Number of episodes","" + nodeList.getLength());
    }

    private void innerLoopTitle(NodeList childList){
        for (int i = 0; i < childList.getLength();i++){
            Node childNode = childList.item(i);
            if(childNode.getNodeName().equals("title")){
                Log.i("Episode ONLY:",childNode.getTextContent());
                String title = childNode.getTextContent();
                title = title.replaceAll("'","");
                titleList.add(title);
            }
        }
    }


    public boolean nodeExistsLink(NodeList nodeList){
        for (int i = 0; i < nodeList.getLength();i++){
            NodeList childList = nodeList.item(i).getChildNodes();
            innerLoopLink(childList);
        }
        return true;
    }

    public boolean nodeExistsLink(NodeList nodeList, int maxLenght){
        for (int i = 0; i < maxLenght;i++){
            NodeList childList = nodeList.item(i).getChildNodes();
            innerLoopLink(childList);
        }
        return true;
    }

    private void innerLoopLink(NodeList childList){
        int hitCount = 0;
        String description = "none";
        for (int i = 0; i < childList.getLength();i++){
            String child = childList.item(i).getNodeName();
            if(child.equals("enclosure")){
                Node testnode = childList.item(i);
                Element nodeElement = (Element) testnode;
                description = nodeElement.getAttribute("url");
            }
        }
        mp3Correct.add(description);
    }

    public boolean nodeExistsImage(NodeList nodeList){
        for (int i = 0; i < nodeList.getLength();i++){
            NodeList childList = nodeList.item(i).getChildNodes();
            innerLoopImage(childList);
        }
        return true;
    }

    public boolean nodeExistsImage(NodeList nodeList, int maxLenght){
        for (int i = 0; i < maxLenght;i++){
            NodeList childList = nodeList.item(i).getChildNodes();
            innerLoopImage(childList);
        }
        return true;
    }

    private void innerLoopImage(NodeList childList){
        int hitCount = 0;
        String description = podcastImage;
        for (int i = 0; i < childList.getLength();i++){
            String child = childList.item(i).getNodeName();
            if(child.equals("itunes:image")){
                Node testnode = childList.item(i);
                Element nodeElement = (Element) testnode;
                description = nodeElement.getAttribute("href");
            }
        }
        imageList.add(description);
    }


    private String getTextBetween(String text) {
        if (text.contains("?")) {
            String result = text.substring(0, text.indexOf("?"));

            return result;
        } else return text;
    }

    public boolean checkDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

        Date date = null;

        try {
            date = sdf.parse(dateString);
            Date today = new Date();
            //String formatedDate = newFormat.format(date);
            long diff = today.getTime() - date.getTime();
            float daysBetween = (diff / (1000*60*60*24));

            SharedPreferences dbPreference = PreferenceManager.getDefaultSharedPreferences(this.ctx);
            int defValue = 10;
            int dayDuration = dbPreference.getInt(ctx.getString(R.string.latest_duration),defValue);

            if (daysBetween < dayDuration){                                                  // ANTAL DAGAR SOM ÄR LATEST
                return true;
            } else return false;

        } catch (ParseException e) {
            Log.i("could not format date","now");
            return false;
            //e.printStackTrace();

        }
    }

    public String formatDate(String dateString){
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

    private String removeCDATA(String text) {
        String cdata = getTextBetween(text, "CDATA[", "]");
        return cdata.length() > 0 ? cdata : text;
    }





    private String getTextBetween(String text, String start, String end) {
        int startindex = text.indexOf(start);
        int endindex = text.indexOf(end, startindex);

        if (startindex < 0 || endindex < 0 || endindex < startindex) {
            return "";
        }

        return text.substring(startindex + start.length(), endindex);
    }

}
