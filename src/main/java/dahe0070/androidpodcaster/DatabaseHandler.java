package dahe0070.androidpodcaster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.util.ArrayList;

/**
 * Created by Dave on 2017-08-17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pod_archive.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PODCAST = "podcasts";
    private static final String TABLE_EPISODES = "episodes";

    private static final String EPCOLUMN_ID = "_id";
    private static final String EPCOLUMN_1 = "podName text";
    private static final String EPCOLUMN_2 = "mp3Link text";
    private static final String EPCOLUMN_3 = "epTitle text";
    private static final String EPCOLUMN_4 = "description text";
    private static final String EPCOLUMN_5 = "songImage text";
    private static final String EPCOLUMN_6 = "duration text";
    private static final String EPCOLUMN_7 = "date text";
    private static final String EPCOLUMN_8 = "localLink text";
    private static final String EPCOLUMN_9 = "read integer";
    private static final String EPCOLUMN_10 = "downloaded integer";
    private static final String EPCOLUMN_11 = "progress integer";


    private static final String PODCOLUMN_ID = "_id";
    private static final String PODCOLUMN_1 = "podName text";
    private static final String PODCOLUMN_2 = "generalDesc text";
    private static final String PODCOLUMN_3 = "podImage text";
    private static final String PODCOLUMN_4 = "podType text";
    private static final String PODCOLUMN_5 = "feedLink text";

    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_PODCAST + "(" + PODCOLUMN_ID +
            " integer primary key autoincrement," + PODCOLUMN_1 + "," + PODCOLUMN_2 + "," + PODCOLUMN_3 + "," + PODCOLUMN_4 + "," + PODCOLUMN_5 + ");";

    private static final String DATABASE_CREATE_2 = "CREATE TABLE " + TABLE_EPISODES + "(" + EPCOLUMN_ID +
            " integer primary key autoincrement," + EPCOLUMN_1 + "," + EPCOLUMN_2 + "," + EPCOLUMN_3 + "," + EPCOLUMN_4 + "," + EPCOLUMN_5 + "," + EPCOLUMN_6 +
            "," + EPCOLUMN_7 + "," + EPCOLUMN_8 + "," + EPCOLUMN_9 + "," +EPCOLUMN_10 + "," + EPCOLUMN_11 + ");";

    SQLiteDatabase podDb;

    public DatabaseHandler(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        //deleteDatabase(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        db.execSQL(DATABASE_CREATE_2);
        Log.i("Database created","true");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists" + TABLE_PODCAST);                              // Function called if database needs to be updated
        db.execSQL("drop table if extists" + TABLE_EPISODES);
        onCreate(db);
    }

    public void insertData(Podcast thisPod){
        podDb = getWritableDatabase();

            String podName = thisPod.getPodName();
            String generalDesc = thisPod.getGeneralDesc();
            String podImage = thisPod.getPodImageUrl();
            String podType = thisPod.getPodType();
            String feedLink = thisPod.getFeedLink();
            //byte[] convImage = getBytes(podImage);

        /**
         * ÄNDRA FUNKTION - LÄGG TILL CONTENT VALUES ISTÄLLET
         */



        //podDb.execSQL("INSERT INTO " + TABLE_PODCAST + " (podName,generalDesc,podImage,podType,feedLink) values('"+ podName +"','"+ generalDesc +"','" + convImage + "','" + podType + "','" + feedLink + "');");
            //podDb.execSQL("INSERT INTO " + TABLE_PODCAST + " (podName,generalDesc,podType,feedLink,podImage) values('" + podName + "','" + generalDesc + "','" + podType + "','" + feedLink +  "','" + convImage + "');");
            String testQuery = "INSERT INTO " + TABLE_PODCAST + " (podName,generalDesc,podType,feedLink,podImage) values('" + podName + "','" + generalDesc + "','" + podType + "','" + feedLink +  "','" + podImage + "');";
            SQLiteStatement statement = podDb.compileStatement(testQuery);
            statement.executeInsert();
            //addImageToDb(convImage);

        podDb.close();
            //Log.i("Data insert correctly", "true");


    }

    public void insertDataEpisode(PodEpisode thisEpisode){
        podDb = getReadableDatabase();

        String podName = thisEpisode.getPodName();
        String mp3Link = thisEpisode.getMp3Link();
        String epTitle = thisEpisode.getEpTitle();
        String desc = thisEpisode.getDescription();
        String songImage = thisEpisode.getSongImage();
        String duration = thisEpisode.getDuration();
        String date = thisEpisode.getDate();
        String localLink = thisEpisode.getLocalLink();
        int read = (thisEpisode.getRead())? 1:0;
        int downloaded = (thisEpisode.getDownloaded())? 1:0;
        int progress = thisEpisode.getProgress();
        //Log.i("insert data ep","progress :" + progress);

        podDb.execSQL("INSERT INTO " + TABLE_EPISODES + " (podName,mp3Link,epTitle,description,songImage,duration,date,localLink,read,downloaded,progress) values('" + podName + "','" + mp3Link + "','" + epTitle +
                "','" + desc + "','" + songImage + "','" + duration + "','" + date + "','" + localLink + "','" + read + "','" + downloaded + "','" + progress + "');");

        Log.i("Episode data added",epTitle);
        podDb.close();

    }

    public ArrayList<PodEpisode> getDownloadedEpisodes(){
        this.podDb = getReadableDatabase();
        ArrayList<PodEpisode> episodesArray = new ArrayList<>();
        Cursor cur = podDb.rawQuery("SELECT * FROM " + TABLE_EPISODES + " WHERE downloaded = 1",null);
        if(cur.getCount() > 0 ){
            while (cur.moveToNext()){
                PodEpisode newEp = new PodEpisode(cur.getString(1),cur.getString(3),cur.getString(2),cur.getString(4),cur.getString(5));
                String duration = cur.getString(6);
                String date = cur.getString(7);
                String localLink = cur.getString(8);
                boolean read = (cur.getInt(9) == 1)? true: false;
                boolean downloaded = (cur.getInt(10) == 1)? true: false;
                int progress = cur.getInt(11);
                newEp.setDuration(duration);
                newEp.setDate(date);
                newEp.setLocalLink(localLink);
                newEp.setRead(read);
                newEp.setDownloaded(downloaded);
                newEp.setProgress(progress);
                episodesArray.add(newEp);
            }
        }

        cur.close();
        podDb.close();

        return episodesArray;
    }

    public boolean removeDownload(String epTitle){
        this.podDb = getReadableDatabase();
        podDb.execSQL("UPDATE " + TABLE_EPISODES + " SET downloaded = 0 WHERE epTitle = '" + epTitle + "'");
        podDb.close();
        return true;

    }

    public ArrayList<PodEpisode> getDownloadedEpisodes(String currPodname){
        this.podDb = getReadableDatabase();
        ArrayList<PodEpisode> episodesArray = new ArrayList<>();
        Cursor cur = podDb.rawQuery("SELECT * FROM " + TABLE_EPISODES + " WHERE downloaded = 1" + " AND " + "podName = '" + currPodname + "'",null);
        if(cur.getCount() > 0 ){
            while (cur.moveToNext()){
                PodEpisode newEp = new PodEpisode(cur.getString(1),cur.getString(3),cur.getString(2),cur.getString(4),cur.getString(5));
                String duration = cur.getString(6);
                String date = cur.getString(7);
                String localLink = cur.getString(8);
                boolean read = (cur.getInt(9) == 1)? true: false;
                boolean downloaded = (cur.getInt(10) == 1)? true: false;
                int progress = cur.getInt(11);
                newEp.setDuration(duration);
                newEp.setDate(date);
                newEp.setLocalLink(localLink);
                newEp.setRead(read);
                newEp.setDownloaded(downloaded);
                newEp.setProgress(progress);
                episodesArray.add(newEp);
            }
        }

        cur.close();
        podDb.close();

        return episodesArray;
    }

    public ArrayList<PodEpisode> getStartedEpisodes(){
        this.podDb = getReadableDatabase();
        ArrayList<PodEpisode> episodesArray = new ArrayList<>();
        Cursor cur = podDb.rawQuery("SELECT * FROM " + TABLE_EPISODES + " WHERE progress > 0",null);
        if(cur.getCount() > 0 ){
            while (cur.moveToNext()){
                PodEpisode newEp = new PodEpisode(cur.getString(1),cur.getString(3),cur.getString(2),cur.getString(4),cur.getString(5));
                String duration = cur.getString(6);
                String date = cur.getString(7);
                String localLink = cur.getString(8);
                boolean read = (cur.getInt(9) == 1)? true: false;
                boolean downloaded = (cur.getInt(10) == 1)? true: false;
                int progress = cur.getInt(11);


                newEp.setDuration(duration);
                newEp.setDate(date);
                newEp.setLocalLink(localLink);
                newEp.setRead(read);
                newEp.setDownloaded(downloaded);
                newEp.setProgress(progress);
                episodesArray.add(newEp);
            }
        }

        cur.close();
        podDb.close();

        return episodesArray;
    }


    public int getEpisodeProgress(PodEpisode currEpisode){
        this.podDb = getReadableDatabase();
        String epTitle = currEpisode.getEpTitle();
        int progress = 2;
        Cursor cur = podDb.rawQuery("SELECT progress FROM " + TABLE_EPISODES + " WHERE epTitle = '" + epTitle + "'", null);
        if (cur.getCount() > 0){
            while (cur.moveToNext()){
                progress = cur.getInt(0);

            }
        }
        cur.close();
        podDb.close();
        return progress;
    }



    public boolean isAddedEpisode(PodEpisode currEpisode){
        podDb = this.getReadableDatabase();
        String epTitle = currEpisode.getEpTitle();
        Cursor cur = podDb.rawQuery("SELECT * FROM " + TABLE_EPISODES + " WHERE epTitle = '" + epTitle + "'", null);
        boolean exist = (cur.getCount() > 0);
        cur.close();
        podDb.close();
        return exist;
    }

    public Podcast getPodcast(String currPodName){
        podDb = this.getReadableDatabase();
        Cursor cur = podDb.rawQuery("SELECT * FROM " + TABLE_PODCAST + " WHERE podName = '" + currPodName + "'", null);
        if (cur.getCount() > 0){
            while (cur.moveToNext()){
                String podName = cur.getString(1);
                String desc = cur.getString(2);
                String imageUrl = cur.getString(3);
                //Log.i("Image url from DB",imageUrl);
                String podType = cur.getString(4);
                String feedLink = cur.getString(5);

                Podcast newPod = new Podcast(podName,feedLink,podType);
                newPod.setPodImageUrl(imageUrl);
                newPod.setGeneralDesc(desc);
                return newPod;

            }
        }
        return null;

    }

    public boolean deletePodcast(String currPodName){
        podDb = this.getReadableDatabase();
        podDb.execSQL("DELETE FROM " + TABLE_PODCAST + " WHERE podName = '" + currPodName + "'");
        podDb.execSQL("DELETE FROM " + TABLE_EPISODES + " Where podName = '" + currPodName + "'");

        podDb.close();
        return true;
    }

    public boolean deleteEpisode(String epTitle){
        podDb = this.getReadableDatabase();
        podDb.execSQL("DELETE FROM " + TABLE_EPISODES + " Where epTitle = '" + epTitle + "'");

        podDb.close();
        return true;
    }

    public ArrayList<Podcast> getAllPodcasts(){
        podDb = this.getReadableDatabase();
        Cursor cur = podDb.rawQuery("SELECT * FROM " + TABLE_PODCAST + " ORDER BY podName",null);
        ArrayList<Podcast> podcastsArray = new ArrayList<>();
        if (cur.getCount() > 0) {
            while (cur.moveToNext()){
                String podName = cur.getString(1);
                String desc = cur.getString(2);
                String imageUrl = cur.getString(3);
                //Log.i("Image url from DB",imageUrl);
                String podType = cur.getString(4);
                String feedLink = cur.getString(5);

                Podcast newPod = new Podcast(podName,feedLink,podType);
                newPod.setPodImageUrl(imageUrl);
                newPod.setGeneralDesc(desc);
                podcastsArray.add(newPod);
            }

        }
        cur.close();
        podDb.close();
        return  podcastsArray;
    }

    public void updateProgress(int currProgress,String currTitle){
        podDb = this.getReadableDatabase();
        podDb.execSQL("UPDATE " + TABLE_EPISODES + " SET progress = '" + currProgress + "'" + " WHERE epTitle = '" + currTitle + "'");
        podDb.close();
    }

    public void setEpRead(String currTitle){
        int read = 1;
        podDb = this.getReadableDatabase();
        podDb.execSQL("UPDATE " + TABLE_EPISODES + " SET read = '" + read + "'" + " WHERE epTitle = '" + currTitle + "'");
        podDb.close();
    }


    public void addImageToDb(byte[] image){
        podDb = getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("podImage",image);
        podDb.insert(TABLE_PODCAST,null,cv);
        podDb.close();
    }

    public ArrayList<Bitmap> getPodImage(){
        podDb = getReadableDatabase();

        ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();

        Cursor cr = podDb.rawQuery("SELECT * FROM " + TABLE_PODCAST + " ORDER BY podName",null);

        while (cr.moveToNext()){
            byte[] image = cr.getBlob(3);
            if (image != null) {
                int test = image.length;

                Bitmap podImage = getImage(image);
                bitmapArrayList.add(podImage);
            }
        }

        podDb.close();
        return bitmapArrayList;

    }

    public Bitmap getCurrPodImage(String podName){
        podDb = getReadableDatabase();
        Bitmap podImage = null;
        Cursor cur = podDb.rawQuery("SELECT * FROM " + TABLE_PODCAST + " WHERE podName = '" + podName + "'", null);
        if (cur.getCount() > 0){
            while (cur.moveToNext()){
                byte[] image = cur.getBlob(3);
                if (podImage != null) {
                    podImage = getImage(image);
                    //Log.i("getEpisodeProgress","progress" + progress);
                }
            }
        }
        cur.close();
        podDb.close();
        return podImage;
    }


    private void deleteDatabase(Context context){
        context.deleteDatabase(DATABASE_NAME);
        Log.i("Database:","Deleted");
    }



    public boolean isExist(String currPodName){
        podDb = this.getReadableDatabase();
        Cursor cur = podDb.rawQuery("SELECT * FROM " + TABLE_PODCAST + " WHERE podName = '" + currPodName + "'", null);
        boolean exist = (cur.getCount() > 0);
        cur.close();
        podDb.close();
        return exist;
    }

    public int typeCount(String currType){
        podDb = this.getReadableDatabase();
        Cursor cur = podDb.rawQuery("SELECT * FROM " + TABLE_PODCAST + " WHERE podType = '" + currType + "'", null);
        //boolean exist = (cur.getCount() > 0);
        int count = cur.getCount();
        cur.close();
        podDb.close();
        return count;
    }




    public ArrayList<String> getPODCOLUMNData(String PODCOLUMNName,String currType){

        ArrayList<String> PODCOLUMNList = new ArrayList<>();

        podDb = getReadableDatabase();

        //Cursor cr = podDb.rawQuery("SELECT * FROM " + TABLE_PODCAST,null);
        Cursor cur = podDb.rawQuery("SELECT * FROM " + TABLE_PODCAST + " WHERE podType = '" + currType + "'", null);
        int PODCOLUMNId = 0;

        switch (PODCOLUMNName){
            case "podName" : PODCOLUMNId = 1;
                break;
            case "generalDesc" : PODCOLUMNId = 2;
                break;
            case "podImage" : PODCOLUMNId = 3;
                break;
            case "podType" : PODCOLUMNId = 4;
                break;
            case "feedLink" : PODCOLUMNId = 5;
                break;
        }

        while (cur.moveToNext()){
            String s1 = cur.getString(PODCOLUMNId);
            if (s1 != null) {
                PODCOLUMNList.add(s1);
            }
        }

        return PODCOLUMNList;
    }

    public static byte[] getBytes(Bitmap bitmap){

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,0,stream);
        return stream.toByteArray();

    }

    public static Bitmap getImage(byte[] image){

        return BitmapFactory.decodeByteArray(image,0,image.length);

    }

    public int getRowCount(){
        podDb = getReadableDatabase();
        int count = (int) DatabaseUtils.queryNumEntries(podDb,TABLE_PODCAST);
        return count;
    }
}
