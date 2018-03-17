package dahe0070.androidpodcaster;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Dave on 2017-08-16.
 */

public class XMLParser {
    private Context context;
    private URL url;
    private ArrayList<String> titleList;
    private ArrayList<String> descList;
    private ArrayList<String> imageList;
    private ArrayList<String> mp3Correct;
    private ArrayList<PodEpisode> episodes;
    private ArrayList<String> dates;
    private String podCastTitle;
    private PlayerClicks listener;
    private String podCastDesc;
    private String podcastImage;
    private String title;
    private boolean SR = false;
    private boolean readError = false;


    public XMLParser(Context ctx,String feedLink,String podName){
        this.context = ctx;
        this.title = podName;
        try {
            this.url = new URL(feedLink);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        listener = (PlayerClicks) ctx;

    }

    public void startParsing(String type){
        if(type.equals("xmlsr")){
            SR = true;
        }
        new XMLDownloader().execute(url);
    }

    public void createEpisodes(){
        episodes = new ArrayList<>();
        Log.i("titles","" +titleList.size());
        Log.i("mp3s","" + mp3Correct.size());
        Log.i("descs","" + descList.size());
        Log.i("images","" + imageList.size());
        Log.i("dates","" + dates.size());
        for (int i = 0; i < titleList.size() - 1;i++){
            PodEpisode newEpisode = new PodEpisode(title,titleList.get(i),mp3Correct.get(i),descList.get(i),imageList.get(i),dates.get(i));
            episodes.add(newEpisode);
        }
    }

    public ArrayList<PodEpisode> getEpisodes(){
        return episodes;
    }

    private class XMLDownloader extends AsyncTask<URL,Integer,Void> {
        @Override
        protected Void doInBackground(URL... params) {
            String urlString = params[0].toString();
            parseXMLSR(urlString);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            createEpisodes();

            listener.loadEpisodes(true);
        }
    }

    /* PROBABLY THIS IS USED TO PARSE ALL................. */
    /******************************************************/


    public void parseXMLSR(String urlString){
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

            if(titleNodes.getLength() == 0){
                readError = true;
                return;
            }

            if(linkNodes.getLength() == 0){
                readError = true;
                return;
            }

            Log.i("titlenodes","" + titleNodes.getLength());
            Log.i("linkNodes","" + linkNodes.getLength());
            Log.i("descNodes","" + descNodes.getLength());
            Log.i("datenodes","" + dateNodes.getLength());
            Log.i("imgNodes","" + imgNodes.getLength());

            imageList = new ArrayList<String>();



            dates = new ArrayList<>();

            for (int y = 0; y < dateNodes.getLength(); y++){
                Node dateNode = dateNodes.item(y);
                String tempOut = dateNode.getTextContent();
                //String corrDate = formatDate(tempOut);
                //String corrDate = Helper.dateToString(tempOut);
                String corrDate = tempOut;
                dates.add(corrDate);
            }

            titleList = new ArrayList<String>();

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

            if(imgNodes.getLength() == 0){
                Uri uri = Uri.parse("android.resource://dahe0070.androidpodcaster/drawable/default_image.png");
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
                    tempOut = getTextBetween(tempOut);
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
        } catch (MalformedURLException url) {
            url.printStackTrace();
        } catch (IOException io){
            io.printStackTrace();
        } catch (SAXException sax){
            sax.printStackTrace();
        }


    }

    public void loopAll(){
        if(!readError) {

            //dates;
            Log.i("dates size", "" + dates.size());
            //titleList;
            Log.i("titleList size", "" + titleList.size());
            //descList;
            Log.i("desclist size", "" + descList.size());
            //imageList;
            Log.i("imagelist size", "" + imageList.size());
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
            Log.i("MISSMATCH!","titles:" + titleList.size() + "mp3s" + mp3Correct.size());
        }


    }

    public boolean nodeExistsLink(NodeList nodeList){
        for (int i = 0; i < nodeList.getLength();i++){
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




/*
    public void parseXML(String urlString){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(urlString).openStream());
            doc.getDocumentElement().normalize();

            NodeList titleNodes = doc.getElementsByTagName("title");
            NodeList linkNodes = doc.getElementsByTagName("link");
            NodeList imgNodes = doc.getElementsByTagName("itunes:image");
            NodeList descNodes = doc.getElementsByTagName("itunes:summary");
            NodeList dateNodes = doc.getElementsByTagName("pubDate");

            titleList = new ArrayList<>();

            for (int temp = 0; temp < titleNodes.getLength(); temp++) {
                Node nNode = titleNodes.item(temp);
                //System.out.println(nNode.getNodeName());
                //System.out.println(nNode.getTextContent());
                Log.i("textContent",nNode.getTextContent());
                String tempOut = nNode.getTextContent();
                if (tempOut.contains("\"")) {
                    tempOut = tempOut.replace("\"", "");
                }
                tempOut = tempOut.replace("'","");
                titleList.add(tempOut);
            }

            title = titleList.get(0);
            titleList.remove(0);

            dates = new ArrayList<>();

            for (int y = 0; y < dateNodes.getLength(); y++){
                Node dateNode = dateNodes.item(y);
                String tempOut = dateNode.getTextContent();
                String corrDate = formatDate(tempOut);
                dates.add(corrDate);
            }

            descList = new ArrayList<String>();

            for (int z = 0; z < descNodes.getLength(); z++) {
                Node descNode = descNodes.item(z);
                //System.out.println(descNode.getTextContent());
                //System.out.println("\n");
                Log.i("descContent",descNode.getTextContent());
                String tempOut = descNode.getTextContent();
                descList.add(tempOut);
            }
            welcomeText = descList.get(0);
            descList.remove(0);

            imgArray = new ArrayList<String>();

            for (int x = 0; x < imgNodes.getLength(); x++) {
                Node imgNode = imgNodes.item(x);
                Element eElement = (Element) imgNode;
                //System.out.println(eElement.getAttribute("href"));
                String tempOut = eElement.getAttribute("href");
                imgArray.add(tempOut);
            }
            startImg = imgArray.get(0);
            imgArray.remove(0);

            String[] mp3Array = new String[linkNodes.getLength()];

            for (int index = 0; index < linkNodes.getLength(); index++) {
                Node linkNode = linkNodes.item(index);

                //System.out.println(index + " " + linkNode.getTextContent());
                String tempOut = linkNode.getTextContent();
                //String result = getTextBetween(tempOut);
                mp3Array[index] = tempOut;
                //System.out.println(tempOut);
            }

            mp3Correct = new ArrayList<String>();

            for (int i = 0; i < mp3Array.length; i++) {
                //String newLink = getTextBetween(mp3Array[i]);
                String newLink = mp3Array[i];
                //System.out.println(i + " " + newLink);
                Log.i("correct MP3",newLink);
                mp3Correct.add(newLink);
            }

            mp3Correct.remove(0);


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (MalformedURLException url){
            url.printStackTrace();
        } catch (IOException io){
            io.printStackTrace();
        } catch (SAXException sax){
            sax.printStackTrace();
        }
    }

    public void parseXMLSR(String urlString){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(urlString).openStream());

            NodeList titleNodes = doc.getElementsByTagName("title");
            NodeList linkNodes = doc.getElementsByTagName("enclosure");
            NodeList imgNodes = doc.getElementsByTagName("itunes:image");
            NodeList descNodes = doc.getElementsByTagName("itunes:summary");
            NodeList dateNodes = doc.getElementsByTagName("pubDate");
            NodeList items = doc.getElementsByTagName("item");

            dates = new ArrayList<>();

            for (int y = 0; y < dateNodes.getLength(); y++){
                Node dateNode = dateNodes.item(y);
                String tempOut = dateNode.getTextContent();
                String corrDate = formatDate(tempOut);
                dates.add(corrDate);
            }

            titleList = new ArrayList<String>();

            for (int temp = 0; temp < titleNodes.getLength(); temp++) {
                Node nNode = titleNodes.item(temp);

                String tempOut = nNode.getTextContent();
                if (tempOut.contains("\"")) {
                    tempOut = tempOut.replace("\"", "");

                }
                tempOut = tempOut.replace("'","");
                titleList.add(tempOut);
            }
            title = titleList.get(0);
            titleList.remove(0);
            titleList.remove(1);

            descList = new ArrayList<String>();
/*
            for (int z = 0; z < descNodes.getLength(); z++) {
                Node descNode = descNodes.item(z);
                //System.out.println(descNode.getTextContent());
                //System.out.println("\n");
                String tempOut = descNode.getTextContent();
                descList.add(tempOut);
            }
            welcomeText = descList.get(0);

            nodeExists(items);
            //descList.remove(0);

            imgArray = new ArrayList<String>();

            for (int x = 0; x < imgNodes.getLength(); x++) {
                Node imgNode = imgNodes.item(x);
                Element eElement = (Element) imgNode;
                String tempOut = eElement.getAttribute("href");
                tempOut = getTextBetween(tempOut);
                System.out.println(tempOut);
                imgArray.add(tempOut);
            }
            startImg = imgArray.get(0);
            imgArray.remove(0);

            String[] mp3Array = new String[linkNodes.getLength()];

            for (int index = 0; index < linkNodes.getLength(); index++) {
                Node linkNode = linkNodes.item(index);
                Element eElement = (Element) linkNode;
                String tempOut = eElement.getAttribute("url");
                //String result = getTextBetween(tempOut);
                mp3Array[index] = tempOut;
            }

            mp3Correct = new ArrayList<String>();

            for (int i = 0; i < mp3Array.length; i++) {
                String newLink = mp3Array[i];
                System.out.println(i + " " + newLink);
                mp3Correct.add(newLink);
            }


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (MalformedURLException url) {
            url.printStackTrace();
        } catch (IOException io){
            io.printStackTrace();
        } catch (SAXException sax){
            sax.printStackTrace();
        }

    }

    public boolean nodeExists(NodeList nodeList){
        for (int i = 0; i < nodeList.getLength();i++){
            NodeList childList = nodeList.item(i).getChildNodes();
            innerLoop(childList);
        }
        return true;
    }

    private void innerLoop(NodeList childList){
        int hitCount = 0;
        String description = "Ingen Beskrivning";
        for (int i = 0; i < childList.getLength();i++){
            String child = childList.item(i).getNodeName();
            if(child.equals("itunes:summary")){
                description = childList.item(i).getTextContent();
            }
        }
       descList.add(description);
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
            e.printStackTrace();
        }
        return null;
    }

    private void correctImages(){
        String[] fullImageArray = new String[titleList.size()];

        for (int i = 0; i < fullImageArray.length;i++){
            fullImageArray[i] = startImg;
        }

        for (int i = 0; i < imgArray.size(); i++){
            fullImageArray[i] = imgArray.get(i);
        }

        imgArray = new ArrayList<>(Arrays.asList(fullImageArray));
    }



    private String getTextBetween(String text) {
        if (text.contains("?")) {
            String result = text.substring(0, text.indexOf("?"));

            return result;
        } else return text;
    }
    */

}
