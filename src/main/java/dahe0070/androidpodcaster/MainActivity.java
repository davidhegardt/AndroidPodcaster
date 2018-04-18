package dahe0070.androidpodcaster;

import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import dahe0070.androidpodcaster.dummy.DummyContent;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,PodcastFragment.OnFragmentInteractionListener,RadioFragment.OnFragmentInteractionListener,EpisodesFragment.OnFragmentInteractionListener,PodPlayerFragment.OnFragmentInteractionListener,
        DownloadedEpisodesFragment.OnFragmentInteractionListener,StartedEpisodesFragment.OnFragmentInteractionListener,MediaController.MediaPlayerControl,SearchFragment.OnFragmentInteractionListener,LatestEpisodesFragment.OnFragmentInteractionListener,TabSearchFragment.OnFragmentInteractionListener,CategoryFragment.OnFragmentInteractionListener,NewSearchFragment.OnFragmentInteractionListener,SettingsFragment.OnListFragmentInteractionListener,PlayerClicks,PodAdder, android.support.v4.app.LoaderManager.LoaderCallbacks<String> {

    private RadioParser radioParser;
    private RadioPlayer radioPlayer;
    private PodcastParser podParser;
    private ArrayList<Podcast> podcasts;
    private ArrayList<Podcast> enPodcasts;
    private ArrayList<PodEpisode> episodes;
    private EpisodeParser epParser;
    private XMLParser xmlParser;
    private DownloadHandler downloadHandler;
    private ProgressDialog mProgressDialog;
    private int currEpisodeIndex;
    DrawerLayout drawer;
    DatabaseHandler podDatabase;
    private int currPodIndex;
    private boolean  playerStarted = false;
    //private MediaController smallController;
    private CustomMediaController smallController;

    private AudioPlayerService servicePlayer;
    boolean serviceBound = false;

    private SensorManager sensorManager;



    //Add new Broadcasts here
    public static final String Broadcast_PLAY_NEW_AUDIO = "dahe0070.androidpodcaster.PlayNewAudio"; // Register more of these
    public static final String Broadcast_PAUSE_AUDIO = "dahe0070.androidpodcaster.PauseAudio";
    public static final String Broadcast_RESUME_AUDIO = "dahe0070.androidpodcaster.ResumeAudio";
    public static final String Broadcast_FORWARD_AUDIO = "dahe0070.androidpodcaster.ForwardAudio";
    public static final String Broadcast_REWIND_AUDIO = "dahe0070.androidpodcaster.RewindAudio";
    public static final String Broadcast_RESTART_AUDIO = "dahe0070.androidpodcaster.RestartAudio";

    private static final String THEME = "THEME_PREF";
    private static final String DEFAULT_COLOR = "GREEN";

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReciever,new IntentFilter("finished-loading"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCompletedReciever,new IntentFilter("song-completed"));
        if(sensorListener != null){
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private BroadcastReceiver mMessageReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //String message = intent.getStringExtra("message");
            //Log.i("Ready to update","message");
            updateProgressInFragment();
            showMediaController();
        }
    };

    private BroadcastReceiver mCompletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switchMediaController(false);
        }
    };

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReciever);
        super.onPause();
        if (sensorListener != null) {
            sensorManager.unregisterListener(sensorListener);
        }
    }

    //Binding this to AudioPlayer service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            servicePlayer = binder.getService();
            serviceBound = true;

            Toast.makeText(MainActivity.this, R.string.service_bound,Toast.LENGTH_SHORT).show();
            Log.i("service bound","now");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
    /*

            BROADCAST FUNCTIONS


     */



    public int getProgressPercent(){
        return servicePlayer.getProgress();
    }

    public boolean isServicePlaying(){
        return servicePlayer.isPlaying();
    }

    public String getCurrentDurationEpisode(){
        return servicePlayer.getCurrentDurationEpisode();
    }

    public String getTotalEpisodeLenght(){
        return servicePlayer.getTotalEpisodeLenght();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, R.string.landscape, Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, R.string.portrait, Toast.LENGTH_SHORT).show();
        }
    }


    /*
        * Small MediaController methods
        *
        *
        *
     */


    @Override
    public void start() {

        //Intent intent = new Intent(this, AudioPlayerService.class);
        //bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
        resumeAudio();
    }

    @Override
    public void pause() {
        pauseAudio();
    }

    public int getDuration(){
        return servicePlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return servicePlayer.getCurrentPos();
    }

    public int progressToTimer(int progress, int totalDuration){
        return servicePlayer.progressToTimer(progress,totalDuration);
    }

    public void seekTo(int newTime){
        servicePlayer.seekTo(newTime);
    }

    @Override
    public boolean isPlaying() {
        return isServicePlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }



    /**
     * Get the audio session id for the player used by this VideoView. This can be used to
     * apply audio effects to the audio track of a video.
     *
     * @return The audio session, or 0 if there was an error.
     */
    @Override
    public int getAudioSessionId() {
        return 0;
    }


    public void playAudio(String path,PodEpisode currPod){
        if (!serviceBound){
            Intent playerIntent = new Intent(this, AudioPlayerService.class);
            playerIntent.putExtra("media",path); // send media to play to player
            playerIntent.putExtra("playingPod",currPod);
            startService(playerIntent);
            bindService(playerIntent,serviceConnection,Context.BIND_AUTO_CREATE);
        } else {
            // service is active
            // send media with broadcaster
            // Store current progress HERE before starting new pod
            PodEpisode storeEpisode = servicePlayer.getCurrPod();
            if (storeEpisode != null) {
                int saveProgress = servicePlayer.getProgress();
                if (podDatabase.isAddedEpisode(storeEpisode)) {
                    podDatabase.updateProgress(saveProgress, storeEpisode.getEpTitle());
                    Log.i("Old episode saved","before starting new");
                }
            }




            // Service is active - called from the recyclerView
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            broadcastIntent.putExtra("media",path);
            broadcastIntent.putExtra("playingPod",currPod);
            sendBroadcast(broadcastIntent);


        }
    }

    public void updateProgressInFragment(){
        FragmentManager fm = getSupportFragmentManager();

        PodPlayerFragment podPlayerFragment = (PodPlayerFragment) fm.findFragmentByTag("PodPlayer");
        podPlayerFragment.updateProgressBar();
    }

    @Override
    public void pauseAudio(){
        Intent broadcastIntent = new Intent(Broadcast_PAUSE_AUDIO);
        sendBroadcast(broadcastIntent);
    }

    public void resumeAudio(){
        Intent broadcastIntent = new Intent(Broadcast_RESUME_AUDIO);
        sendBroadcast(broadcastIntent);
    }

    public void forwardAudio(){
        Intent broadcastIntent = new Intent(Broadcast_FORWARD_AUDIO);
        sendBroadcast(broadcastIntent);
    }

    public void rewindAudio(){
        Intent broadcastIntent = new Intent(Broadcast_REWIND_AUDIO);
        sendBroadcast(broadcastIntent);
    }

    public void restartAudio(){
        Intent broadcastIntent = new Intent(Broadcast_RESTART_AUDIO);
        sendBroadcast(broadcastIntent);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("ServiceState",serviceBound);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            servicePlayer.stopSelf();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        podDatabase = new DatabaseHandler(this);

        //podDatabase.getDownloadedEpisodes();
        //Log.i("DB count","" + count);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        radioPlayer = new RadioPlayer(this);
        boolean network = isNetworkAvailable();
        Log.i("Is connected ?","" + network);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            setupSensor();
        }

        if(podDatabase.getRowCount() < 1) {
            Toast.makeText(MainActivity.this, R.string.first_time_message,Toast.LENGTH_LONG).show();
            firstTime = true;
            //setupPodcasts();
            setDBComplete(false);
            //new PodcastLoader().execute();
           // setupPodcasts();
           getSupportLoaderManager().initLoader(OPERATION_LOAD_PODCASTS,null,this);


        }

        setLatestDuration(12);

        SharedPreferences dbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        String ThemeColor = dbPreference.getString(THEME,DEFAULT_COLOR);
        RelativeLayout root = (RelativeLayout) findViewById(R.id.main_view);
        Helper.ChangeTheme(root,ThemeColor);
    }



    ProgressDialog progressDialog;
    public static final int OPERATION_LOAD_PODCASTS = 22;


    @Override
    public android.support.v4.content.Loader<String> onCreateLoader(int id, Bundle args) {
        return new android.support.v4.content.AsyncTaskLoader<String>(this) {
            @Override
            public String loadInBackground() {
                setupPodcasts();
               // setupEnglishPodcasts();
                return null;
            }

            @Override
            protected void onStartLoading() {
                //super.onStartLoading();
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle(getString(R.string.first_time_setup));
                progressDialog.setMessage(getString(R.string.setting_up_podcast));
                progressDialog.setMax(100);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIcon(R.drawable.radio_tower_large);
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                forceLoad();
            }
        };

    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<String> loader, String data) {
        progressDialog.dismiss();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<String> loader) {

    }

    /* Change color of Theme */
    @Override
    public void onListFragmentInteraction(ColorTheme item) {
        //Log.i("TEST CALL MAIN", item.getColorName());
        Toast.makeText(this,getString(R.string.theme_set_text) + " " + item.getColorName(),Toast.LENGTH_SHORT).show();
        RelativeLayout root = (RelativeLayout) findViewById(R.id.main_view);
        Helper.ChangeTheme(root,item.getSwitchColorName());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEdit = sharedPreferences.edit();
        prefEdit.putString(THEME,item.getSwitchColorName());
        prefEdit.commit();
    }


    private class PodcastLoader extends AsyncTask<Void,Void,Void> {



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle(MainActivity.this.getString(R.string.downloading_episode));
            progressDialog.setMessage(MainActivity.this.getString(R.string.downloading));
            //progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            setupPodcasts();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*
        if(podDatabase.getRowCount() < 1) {
            Toast.makeText(MainActivity.this,"You are using this app for the first time - first time sync will take a while",Toast.LENGTH_LONG).show();
            firstTime = true;
            setupPodcasts();

        }
        */
    }

    private SensorEventListener sensorListener;
    private float accelVal;
    private float accelLast;
    private float shake;

    private void setupSensor(){
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        accelVal = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;
        final Toast shakeToast = new Toast(MainActivity.this);

        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                accelLast = accelVal;
                accelVal = (float) Math.sqrt((double) (x*x + y*y + z*z));
                float delta = accelVal - accelLast;
                shake = shake * 0.9f + delta;

                if(shake > 12) {

                    if(serviceBound){
                        if(isServicePlaying()){
                            pauseAudio();
                            if (shakeToast != null) {
                                shakeToast.cancel();
                            }
                            shakeToast.makeText(MainActivity.this, R.string.pausing_playback,Toast.LENGTH_SHORT).show();
                        } else {
                            resumeAudio();
                            if (shakeToast != null){
                                shakeToast.cancel();
                            }
                            shakeToast.makeText(MainActivity.this, R.string.resuming_playback,Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }


    public void closeDrawer(){
        drawer.closeDrawer(Gravity.LEFT);
    }


    @Override
    public void onBackPressed() {


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        //String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        //Log.i("Tag",tag);
        FragmentManager fm = getSupportFragmentManager();
        Fragment test = getSupportFragmentManager().findFragmentByTag("PodPlayer");
        if (test instanceof PodPlayerFragment && test.isVisible()) {
            drawer.openDrawer(Gravity.LEFT);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.search_podcast, menu);
/*
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem search = menu.findItem(R.id.searchPodcast);
        SearchView searchView = (SearchView) search.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener();
*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void offlineMessage() {
        Toast.makeText(MainActivity.this, R.string.network_offline_message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void navigateToPodlist() {
        setTitle(R.string.podcasts);
        if (!isNetworkAvailable()){
            offlineMessage();
        } else {
            FragmentManager fm = getSupportFragmentManager();
            PodcastFragment test = (PodcastFragment) getSupportFragmentManager().findFragmentByTag("PodFragment");


            if (test != null){
                android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();

                ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);

                ft.replace(R.id.main_view,test,"PodFragment").addToBackStack(null).commit();

            } else {
                setupPodcasts();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        FragmentManager fragmentManager = getSupportFragmentManager();

        int id = item.getItemId();

        if (id == R.id.nav_home) {

            navigateToPodlist();


        }
        if (id == R.id.nav_radio) {
            setTitle(getString(R.string.radio_stations_title));
            if (!isNetworkAvailable()){
                offlineMessage();
            } else {

                android.support.v4.app.FragmentTransaction ft = fragmentManager.beginTransaction();

                ft.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out);

                ft.replace(R.id.main_view,new RadioFragment(),"radio").addToBackStack(null).commit();

            }

        }
        if (id == R.id.nav_player){
            if (playerStarted) {
                setTitle("Player");
                FragmentManager fm = getSupportFragmentManager();
                PodPlayerFragment test = (PodPlayerFragment) getSupportFragmentManager().findFragmentByTag("PodPlayer");
                getIntent().putExtra("Resume", "Resume");

                fm.beginTransaction().replace(R.id.main_view, test, "PodPlayer").addToBackStack(null).commit();
            } else {
                Toast.makeText(MainActivity.this, R.string.no_pod_playing,Toast.LENGTH_SHORT).show();
            }
        }

        if (id == R.id.nav_downloaded){
            setTitle(getString(R.string.Downloaded_episodes_title));
            fragmentManager.beginTransaction().replace(R.id.main_view, new DownloadedEpisodesFragment(),"downloaded").addToBackStack(null).commit();
        }

        if (id == R.id.nav_progress){
            setTitle(getString(R.string.Continue_episodes_title));
            fragmentManager.beginTransaction().replace(R.id.main_view, new StartedEpisodesFragment(),"progress").addToBackStack(null).commit();
        }

        if (id == R.id.nav_settings){
            setTitle("Settings");
            fragmentManager.beginTransaction().replace(R.id.main_view, new SettingsFragment(),"settings").addToBackStack(null).commit();
        }

        if(id == R.id.nav_tab_search){
            setTitle(getString(R.string.discover_podcasts));
            fragmentManager.beginTransaction().replace(R.id.main_view, new TabSearchFragment(),"tabs").addToBackStack(null).commit();
            /* PROGRAMATICALLY CHANGE NAVBAR COLOR */
            /*
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            View header = navigationView.getHeaderView(0);
            LinearLayout sideNavLayout = (LinearLayout)header.findViewById(R.id.sideNavLayout);
            sideNavLayout.setBackgroundResource(R.drawable.side_nav_bar_red);
            */
        }

        if(id == R.id.nav_latest){

            if (!isNetworkAvailable()) {
                offlineMessage();
            } else {
                setTitle(getString(R.string.latest_episodes));
                if (!latestStared) {
                    startLatestEpisodes();
                } else {
                    FragmentManager fm = getSupportFragmentManager();
                    LatestEpisodesFragment test = (LatestEpisodesFragment) getSupportFragmentManager().findFragmentByTag("latest");
                    getIntent().putExtra("Resume", "Resume");

                    fm.beginTransaction().replace(R.id.main_view, test, "latest").addToBackStack(null).commit();
                }
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void replaceFragment(Fragment fragment){

    }

    private void setupPodcasts(){
        podParser = new PodcastParser(this,false);
        try {
            String language= Locale.getDefault().getDisplayLanguage();
            String podcastFile = "podcasts.txt";
            Log.i("Current language",language);
            if(language.equalsIgnoreCase("español")){
                Log.i("SPANISH","JAJAJAJA");
                podcastFile = "podcasts-es.txt";
            }

            if(language.equalsIgnoreCase("English")){
                podcastFile = "podcasts-en-start.txt";
            }

            if(language.equalsIgnoreCase("Deutsch")){
                podcastFile = "podcasts-de.txt";
            }

            if(language.equalsIgnoreCase("français")){
                podcastFile = "podcasts-fr.txt";
            }
            //if(language.equals())
            podParser.readFromAssets(podcastFile);
            podParser.startParse();
            //podcasts = podParser.getAllPodcasts();
            //writeToDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setupEnglishPodcasts(){
        podParser = new PodcastParser(this,true);
        try{
            podParser.readFromAssets("podcasts-en.txt");
            podParser.startParse();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    private void writeToDatabase(){
        for (Podcast c : podcasts){
            String desc = c.getGeneralDesc();
        }

        for (Podcast pod : podcasts){
            if (!podDatabase.isExist(pod.getPodName())){
                Log.i("now iserting",pod.getPodImageUrl() + " " + pod.getPodName());
                podDatabase.insertData(pod);
            }
        }
    }

    public void writeEnglishToDatabase(){


        for (Podcast pod : enPodcasts){
            if(!podDatabase.isExist(pod.getPodName())){
                podDatabase.insertData(pod);
            }
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void radioPlay(String stationName) {
        Log.i("Station name","" + stationName);
        if(serviceBound){
            pauseAudio();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        RadioFragment radioFragment = (RadioFragment) fragmentManager.findFragmentByTag("radio");
        radioFragment.updateStationPlayer(stationName,true);

        ArrayList<Station> stations = radioFragment.getStations();

        radioParser = new RadioParser(this,stations);

        String radioLink = radioParser.readM3U(stationName);
        Log.i("RadioLink",radioLink);
        radioPlayer.initPlayer(radioLink,stationName);
        SharedPreferences sharedPreferences = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedStation",stationName);
        editor.commit();
        radioPlayer.startPlay();
    }

    @Override
    public void radioPause() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        RadioFragment radioFragment = (RadioFragment) fragmentManager.findFragmentByTag("radio");
        radioFragment.updateStationPlayer("-",false);
        radioPlayer.stopPlay();
    }

    @Override
    public void radioStop() {
        if (radioPlayer != null) {
            radioPlayer.stopPlay();
            radioPlayer.releasePlayer();
        }
    }

    @Override
    public boolean radioStatus() {
        if (radioPlayer.isPlaying()){
            return true;
        } else return false;
    }

    String type;
    PodcastFactory factory;

    @Override
    public void podClick(int podIndex) {
        type = podcasts.get(podIndex).getPodType();
        currPodIndex = podIndex;

        factory = new PodcastFactory(MainActivity.this);
        factory.createEpisodes(podcasts.get(podIndex).getPodName(),podcasts.get(podIndex).getFeedLink());

    }

    @Override
    public void itunesPodClick(Podcast newPod) {
        factory = new PodcastFactory(MainActivity.this);
        factory.createEpisodes(newPod.getPodName(),newPod.getFeedLink(),true,newPod);
    }

    public void startLatestEpisodes(){
        // ANROPA NÄR MENYVAL GÖRS

        factory = new PodcastFactory(MainActivity.this);
        ArrayList<Podcast> allPodcasts;
        allPodcasts = podDatabase.getAllPodcasts();
        String[] feeds = new String[allPodcasts.size()];
        String[] names = new String[allPodcasts.size()];

        for(int i = 0; i < feeds.length;i++){
            feeds[i] = allPodcasts.get(i).getFeedLink();
            names[i] = allPodcasts.get(i).getPodName();
        }

        factory.createLatestEpisodes(feeds,names);
    }

    private boolean latestStared = false;

    @Override
    public void setupLatest() {

        episodes = factory.getEpisodes();

        // Starta nytt fragment med lista osv
        // Skicka med episodes som argument

        getIntent().putExtra("Episodes",episodes);

        getIntent().putExtra("Resume", "New");

        FragmentManager fragmentManager = getSupportFragmentManager();

        android.support.v4.app.FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        latestStared = true;
        ft.replace(R.id.main_view,new LatestEpisodesFragment(),"latest").addToBackStack(null).commit();

    }

    @Override
    public void loadEpisodes(boolean xml) {
    /*
        if(xml){
            episodes = xmlParser.getEpisodes();
        } else if(type.contains("universal")) {
            episodes = factory.getEpisodes();
        }
        else {
            episodes = epParser.getEpisodes();
        }
        */
        episodes = factory.getEpisodes();

        Log.i("Retreived episodes:",""+ episodes.size());

        getIntent().putExtra("Episodes",episodes);

        getIntent().putExtra("currPodcast",podcasts.get(currPodIndex));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEdit = sharedPreferences.edit();
        prefEdit.putBoolean("itunesSubscribe",false);

        FragmentManager fragmentManager = getSupportFragmentManager();

        android.support.v4.app.FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);

        ft.replace(R.id.main_view,new EpisodesFragment(),"EpisodeFragment").addToBackStack(null).commit();

        //fragmentManager.beginTransaction().replace(R.id.main_view, new EpisodesFragment(),"EpisodeFragment").addToBackStack(null).commit();
    }

    private void testGetFragment() {
        //TabSearchFragment test = (TabSearchFragment) getSupportFragmentManager().findFragmentByTag("tabs");
        //test.testFindFragmet();

    }
    @Override
    public void loadEpisodes(boolean xml, Podcast currpodcast) {
        episodes = factory.getEpisodes();

        Log.i("Retreived episodes:",""+ episodes.size());

        TabSearchFragment test = (TabSearchFragment) getSupportFragmentManager().findFragmentByTag("tabs");
        test.testFindFragmet(episodes,currpodcast);

/*
        getIntent().putExtra("Episodes",episodes);

        getIntent().putExtra("currPodcast",currpodcast);

        FragmentManager fragmentManager = getSupportFragmentManager();

        android.support.v4.app.FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);

        ft.replace(R.id.main_view,new EpisodesFragment(),"EpisodeFragment").addToBackStack(null).commit();

        */
    }

    @Override
    public void episodeClick(String epTitle, Context ctx, String mp3Link, int epIndex) {
        currEpisodeIndex = epIndex;

        File sdCard = new File(Environment.getExternalStorageDirectory(),"downloaded_episodes");
        File file = new File(sdCard,epTitle + ".mp3");
        if(file.exists() && !file.isDirectory()){
            startPodPlayer(file.getAbsolutePath(),true);
            Log.i("file exists",file.getAbsolutePath());
        } else {

            downloadHandler = new DownloadHandler(ctx, mp3Link, epTitle);
            downloadHandler.startDownload();
        }
    }

    @Override
    public void startPodPlayer(String filePath, boolean downloaded) {
        PodEpisode current = episodes.get(currEpisodeIndex);

        if (downloaded){
            current.setDownloaded(true);
            current.setLocalLink(filePath);
            if(!podDatabase.isAddedEpisode(current)){
                podDatabase.insertDataEpisode(current);
                Log.i("startPodPlayer", "progress" + current.getProgress());
            }
                Log.i("startPodPlayer", "progress" + current.getProgress());
        }        //lägg in file-länk till episod


        if(podDatabase.isAddedEpisode(current)){
            int newProgress = podDatabase.getEpisodeProgress(current);
            current.setProgress(newProgress);
            Log.i("before start player","progress" + newProgress);
        }

        getIntent().putExtra("CurrentEpisode",current);

        getIntent().putExtra("Filepath",filePath);

        getIntent().putExtra("Resume","newSession");
/*
        boolean isRunning = PlayerSingleton.checkInstance();
        if (isRunning){
            Log.i("Player Running","true");
            RadioPlayer tempPlayer = PlayerSingleton.getReference();
            PodEpisode epPlaying = tempPlayer.getCurrentEpisode();
            if (podDatabase.isAddedEpisode(epPlaying)){
                //podDatabase.updateProgress(tempPlayer.updateProgressBar(),epPlaying.getEpTitle());
                Log.i("Progress saved","" + epPlaying.getEpTitle() + " value " + tempPlayer.updateProgressBar());
            }
        } else {
            Log.i("Player Running","false");
        }
*/
        if (radioStatus()){
            radioStop();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        playerStarted = true;
        fragmentManager.beginTransaction().replace(R.id.main_view, new PodPlayerFragment(),"PodPlayer").addToBackStack(null).commit();

    }


    @Override
    public void syncPodretrieval() {
    //   FragmentManager fragmentManager = getSupportFragmentManager();

        podcasts = new ArrayList<>();

        podcasts = podParser.getAllPodcasts();


        writeToDatabase();                                                                                                          // SPLIT FUNCTION ON STARTUP

/*
        getIntent().putExtra("Podcasts",podcasts);

        android.support.v4.app.FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);

        ft.replace(R.id.main_view,new PodcastFragment(),"PodFragment").addToBackStack(null).commit();
  */
        if(!firstTime) {
            showPodList();
        }
        firstTime = false;

    //   fragmentManager.beginTransaction().replace(R.id.main_view, new PodcastFragment(),"PodFragment").addToBackStack(null).commit();
    }

    private boolean firstTime = false;

    private void showPodList(){
        FragmentManager fragmentManager = getSupportFragmentManager();

        getIntent().putExtra("Podcasts",podcasts);

        android.support.v4.app.FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        firstTime = false;

        ft.replace(R.id.main_view,new PodcastFragment(),"PodFragment").addToBackStack(null).commit();
    }

    @Override
    public ArrayList<Podcast> syncEnglishRetrieval() {

        enPodcasts = new ArrayList<>();

        enPodcasts = podParser.getAllPodcasts();
        writeEnglishToDatabase();
        //writeToDatabase();

        return enPodcasts;
    }

    @Override
    public ArrayList<Podcast> syncSwedishRetrieval() {
        if (podcasts != null){
            String language= Locale.getDefault().getDisplayLanguage();
            Log.i("Current language",language);
            if(language.equalsIgnoreCase("English")){
                SharedPreferences dbPreference = PreferenceManager.getDefaultSharedPreferences(this);
                boolean defValue = false;
                boolean DBOK = dbPreference.getBoolean(getString(R.string.data_ok),defValue);
                if(!DBOK){
                    setDBComplete(true);
                }
            }
            return podcasts;
        } else return null;
    }

    @Override
    public void syncEnglishDone(){
        FragmentManager fm = getSupportFragmentManager();
        PodcastFragment test = (PodcastFragment) getSupportFragmentManager().findFragmentByTag("PodFragment");

        test.syncNewList();

        // Set shared preference here
        SharedPreferences dbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean defValue = false;
        boolean DBOK = dbPreference.getBoolean(getString(R.string.data_ok),defValue);
        if(!DBOK){
            setDBComplete(true);
        }
    }

    private void setDBComplete(boolean currentStatus){
        SharedPreferences dbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEditor = dbPreference.edit();
        prefEditor.putBoolean(getString(R.string.data_ok),currentStatus);
        prefEditor.commit();
    }

    private void setLatestDuration(int newDuration){
        SharedPreferences dbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEditor = dbPreference.edit();
        prefEditor.putInt(getString(R.string.latest_duration),newDuration);
        prefEditor.commit();
    }

    @Override
    public void streamEpisode(String epTitle, Context ctx, String mp3Link, int epIndex) {
        currEpisodeIndex = epIndex;
        startPodPlayer(mp3Link,false);
    }

    @Override
    public void loadDownloadedEpisodes(ArrayList<PodEpisode> downloadList) {
        episodes = downloadList;
    }

    @Override
    public void showMediaController() {
        if(serviceBound) {
            smallController = new CustomMediaController(this,"Dummy Text");
            smallController.setAnchorView(findViewById(R.id.main_view));
            smallController.setMediaPlayer(this);
            smallController.setEnabled(true);


        }

    }



    @Override
    public void switchMediaController(boolean status) {
        if (smallController != null){
            if(status){
                smallController.show(0);

            } else if (!status){
                smallController.show(1);

            }
        }
    }

    @Override
    public void setScrollingText(String currPod, String epTitle) {
        if (smallController != null){
            smallController.updateText(currPod,epTitle);

        }
    }




    @Override
    public void addPodcast(Podcast newPod) {
        final FragmentManager fm = getSupportFragmentManager();
        final PodcastFragment test = (PodcastFragment) getSupportFragmentManager().findFragmentByTag("PodFragment");

        /**
         * ADD TO DATABASE HERE
         * GET PODCAST BY TYPE UNIVERSAL
         *
         */
        //writeToDatabase();

        if (!podDatabase.isExist(newPod.getPodName())){
            podDatabase.insertData(newPod);
        }

        if (test != null) {
            AlertDialog.Builder showDialog = new AlertDialog.Builder(MainActivity.this);
            showDialog.setTitle(R.string.podcast_subscribed);
            showDialog.setMessage(R.string.show_ep_now_question);

            showDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (test != null) {

                        test.readFromDb(0);

                        fm.beginTransaction().replace(R.id.main_view, test, "PodFragment").addToBackStack(null).commit();
                    }
                }
            });

            showDialog.setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Toast.makeText(MainActivity.this, R.string.podcast_added_message, Toast.LENGTH_LONG).show();
                }
            });

            AlertDialog dialog = showDialog.create();
            dialog.show();
        } else {
            Toast.makeText(MainActivity.this, R.string.podcast_added_message, Toast.LENGTH_LONG).show();
        }


    }


}
