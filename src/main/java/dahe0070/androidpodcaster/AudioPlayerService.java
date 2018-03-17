package dahe0070.androidpodcaster;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;



import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dave on 2017-08-22.
 */



public class AudioPlayerService extends Service implements MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,
    MediaPlayer.OnSeekCompleteListener,MediaPlayer.OnInfoListener,MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener{

    private final IBinder iBinder = new LocalBinder();
    private MediaPlayer audioPlayer;
    private String audioPath;
    private int resumePosition;
    private AudioManager audioManager;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private PodEpisode currPod;
    private DatabaseHandler podDatabase;
    int dr;
    private int seekForwardTime = 10000;
    private int seekBackwardTime = 10000;
    private PlayerClicks listener;

    private void initAudioPlayer(){
        audioPlayer = new MediaPlayer();
        audioPlayer.setOnCompletionListener(this);
        audioPlayer.setOnErrorListener(this);
        audioPlayer.setOnPreparedListener(this);
        audioPlayer.setOnBufferingUpdateListener(this);
        audioPlayer.setOnSeekCompleteListener(this);
        audioPlayer.setOnInfoListener(this);

        audioPlayer.reset();

        audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            audioPlayer.setDataSource(audioPath);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        audioPlayer.prepareAsync();
//        listener = (PlayerClicks) this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        podDatabase = new DatabaseHandler(this);

        // one-time setup procedure

        // Manage incoming calls
        // Pause mediaplayer on incoming call
        // Resume on hangup
        callStateListener();

        registerBecomingNoisyReciever();

        register_playNewAudio();

        register_pauseAudio();

        register_resumeAudio();

        register_forwardAudio();

        register_rewindAudio();

        register_restartAudio();

    }

    public boolean isPlaying(){
        if (audioPlayer.isPlaying()){
            return true;
        } else return false;
    }

    private void playMedia(){
        if (!audioPlayer.isPlaying()){
            audioPlayer.start();
            sendMessage();
        }
    }

    private void sendMessage(){
        Intent intent = new Intent(getString(R.string.finished_loading));

        //intent.putExtra("message","now done");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendOnComplete(){
        Intent intent = new Intent(getString(R.string.song_completed));

        //intent.putExtra("finished","song stopped");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void stopMedia(){
        if (audioPlayer == null) return;
        if (audioPlayer.isPlaying()){
            audioPlayer.stop();
        }
    }

    private void resumeMedia(){
        if (!audioPlayer.isPlaying()){
            audioPlayer.seekTo(resumePosition);
            audioPlayer.start();
        }
    }

    private void restartMedia(){
        if (audioPlayer == null) return;
        if (audioPlayer.isPlaying()){
            audioPlayer.stop();
        }
        currPod.setProgress(0);
        initAudioPlayer();

    }

    private void pauseMedia() {
        if (audioPlayer.isPlaying()) {
            audioPlayer.pause();
            resumePosition = audioPlayer.getCurrentPosition();
        }
    }

    private void continueEpisode(int progress){
        if (audioPlayer != null){
            int totalDuration = getDuration();
            int seekTo = progressToTimer(progress,totalDuration);
            audioPlayer.seekTo(seekTo);
            resumePosition = seekTo;
        }
    }

    public void seekTo(int newTime){
        audioPlayer.seekTo(newTime);
    }

    // Converts stored progress to Timer to seek to
    public int progressToTimer(int progress, int totalDuration){
        int currentDuration = 0;
        totalDuration = (int) (totalDuration /1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        return currentDuration * 1000;
    }

    public int getProgress(){
        long totalDuration = getDuration();
        long currentDuration = audioPlayer.getCurrentPosition();

        int progress = (int) getPercentProgress(currentDuration,totalDuration);

        return progress;
    }


    private int getPercentProgress(long curDuration,long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (curDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        percentage =(((double)currentSeconds)/totalSeconds)*100;

        return percentage.intValue();

    }

    public String getCurrentDurationEpisode(){
        //long totalDuration = stationPlayer.getDuration();
        long currentDuration = audioPlayer.getCurrentPosition();

        String currDurationString = milliSecondToTimer(currentDuration);

        return currDurationString;
    }

    public String getTotalEpisodeLenght(){
        long totalDuration = getDuration();
        //long currentDuration = stationPlayer.getCurrentPosition();

        String totalDurationString = milliSecondToTimer(totalDuration);

        return totalDurationString;
    }

    private String milliSecondToTimer(long milliSeconds){
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) % TimeUnit.MINUTES.toSeconds(1));

        return hms;

    }



    // Returns total duration of song
    public int getDuration(){
        return dr;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        // called when audio focus is updated, incoming calls etc
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN: // gained focus, start playing
                // resume playback
                if (audioPlayer == null) {
                    initAudioPlayer();
                    audioPlayer.setVolume(1f,1f);
                }
                else if (!audioPlayer.isPlaying()){
                    //audioPlayer.start();
                    //audioPlayer.setVolume(1.0f,1.0f);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS: // User starts playing audio on another device
                if (audioPlayer.isPlaying()){
                    audioPlayer.stop();
                    audioPlayer.release();
                    audioPlayer = null;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: // Focus lost for short time, pause player
                if (audioPlayer.isPlaying()){
                    audioPlayer.pause();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: // Lost focus for short time, probably notification, lower volume
                if (audioPlayer.isPlaying()){
                    audioPlayer.setVolume(0.1f,0.1f);
                }
                break;

        }

    }

    private boolean requestAudioFocus(){
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            // Gained focus
            return true;
        }

        return false; // not granted
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Indicating buffer status of media that is streamed
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // When media is completed
        if (podDatabase.isAddedEpisode(currPod)) {
            currPod.setRead(true);
            podDatabase.updateProgress(100,currPod.getEpTitle());
            podDatabase.setEpRead(currPod.getEpTitle());
        }
        stopMedia();
        // Stop service
        stopSelf();
        sendOnComplete();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // show error during asynchronous operations

        switch (what){
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.i(getString(R.string.audio_player_error),"MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.i(getString(R.string.audio_player_error),"MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.i(getString(R.string.audio_player_error),"MEDIA ERROR UKNOWN " + extra);
                break;
        }


        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        // Communicates information
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // When ready for playback
        playMedia();
        dr = audioPlayer.getDuration();
        checkShouldContinue();

    }

    private void checkShouldContinue(){
        if (currPod.getProgress() > 0){                 // Spola till progress
            continueEpisode(currPod.getProgress());

        } else {

        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        // when seek is completed
    }

    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }


    // Calls when activity requests service to be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            audioPath = intent.getExtras().getString(getString(R.string.media));      // Passing audio path here
            currPod = (PodEpisode) intent.getExtras().getSerializable(getString(R.string.playingPod));
        } catch (NullPointerException nullPoint){
            stopSelf();
        }

        if (requestAudioFocus() == false){
            stopSelf();
        }

        if (audioPath != null && audioPath != ""){
            initAudioPlayer();
            if(mediaSessionManager == null){
                initMediaSession();
            }
           // buildNotification(PlaybackStatus.PLAYING);

        }



//        handleIncomingActions(intent);
        buildNotification();

        return super.onStartCommand(intent, flags, startId);
    }

    public PodEpisode getCurrPod(){
        if (currPod != null){
            return currPod;
        } else return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null){
            if (currPod != null) {
                int saveProgress = getProgress();
                if (saveProgress != 0) {
                    if (podDatabase.isAddedEpisode(currPod)) {
                        podDatabase.updateProgress(saveProgress, currPod.getEpTitle());
                    }
                }
            }

            stopMedia();
            audioPlayer.release();
        }
        removeAudioFocus();

        if(phoneStateListener != null){
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        // Unregister BroadcastRecievers
        unregisterReceiver(becomingNoisy);
        unregisterReceiver(playNewAudio);
        unregisterReceiver(resumeAudio);
        unregisterReceiver(pauseAudio);
        unregisterReceiver(forwardAudio);
        unregisterReceiver(rewindAudio);
        unregisterReceiver(restartAudio);

    }

    private BroadcastReceiver becomingNoisy = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // pause audio when headphones disconnected
            pauseMedia();
            //buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReciever(){
        // register this after gaining audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisy,intentFilter);
    }


    //Handle incoming calls
    private void callStateListener(){
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                //super.onCallStateChanged(state, incomingNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (audioPlayer != null){
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (audioPlayer != null){
                            if (ongoingCall){
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };

        telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);

    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //ladda in ny fil som ska spelas upp
            // activeAudio laddas dynamiskt
            if(isPlaying()){
                int seekTo = resumePosition;
                pauseMedia();
                int currProgress = getProgress();
                if (podDatabase.isAddedEpisode(currPod)) {
                    podDatabase.updateProgress(currProgress,currPod.getEpTitle());
                }
            }

            audioPath = intent.getExtras().getString(getString(R.string.media));
            currPod = (PodEpisode) intent.getExtras().getSerializable(getString(R.string.playingPod));
            stopMedia();
            audioPlayer.reset();
            initAudioPlayer();
            //buildNotification(PlaybackStatus.PLAYING);
            updateNotification();

        }
    };

    private void register_playNewAudio(){
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio,filter);
    }

    private BroadcastReceiver pauseAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
            // Skriv till databas
            int seekTo = resumePosition;
            int currProgress = getProgress();
            if (podDatabase.isAddedEpisode(currPod)) {
                podDatabase.updateProgress(currProgress,currPod.getEpTitle());
            }
            //buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void register_pauseAudio(){
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PAUSE_AUDIO);
        registerReceiver(pauseAudio,filter);
    }

    private BroadcastReceiver resumeAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            resumeMedia();
        }
    };

    private void register_resumeAudio(){
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_RESUME_AUDIO);
        registerReceiver(resumeAudio,filter);
    }

    private BroadcastReceiver forwardAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (audioPlayer.isPlaying()){
                int currentProgress = audioPlayer.getCurrentPosition();

                if (currentProgress + seekForwardTime <= audioPlayer.getDuration()){
                    audioPlayer.seekTo(currentProgress + seekForwardTime);
                }
            }
        }
    };

    private void register_forwardAudio(){
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_FORWARD_AUDIO);
        registerReceiver(forwardAudio,filter);
    }

    private BroadcastReceiver rewindAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (audioPlayer.isPlaying()){
                int currentProgress = audioPlayer.getCurrentPosition();

                if (currentProgress - seekBackwardTime >= 0){
                    audioPlayer.seekTo(currentProgress - seekBackwardTime);
                } else {
                    audioPlayer.seekTo(0);
                }

            }
        }
    };

    private void register_rewindAudio(){
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_REWIND_AUDIO);
        registerReceiver(rewindAudio,filter);
    }

    private BroadcastReceiver restartAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            restartMedia();
        }
    };

    private void register_restartAudio(){
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_RESTART_AUDIO);
        registerReceiver(restartAudio,filter);
    }

    private static int NOTIFICATION_ID = 101;
/*
    private void buildNotification(PlaybackStatus playbackStatus){
        int notificationAction = android.R.drawable.ic_menu_view;

        PendingIntent play_pauseAction = null;

        if(playbackStatus == PlaybackStatus.PLAYING) {

            notificationAction = android.R.drawable.ic_media_pause;
            play_pauseAction = playbackAction(1);
        } else if(playbackStatus == PlaybackStatus.PAUSED){
            notificationAction = android.R.drawable.ic_media_play;

            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = null;
        try {
            largeIcon = new ImageDownloader().execute(currPod.getSongImage()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_rss_feed_black_48dp);
        } catch (ExecutionException e) {
            e.printStackTrace();
            largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_rss_feed_black_48dp);
        }

        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setShowWhen(false)
                // Set notif style
                .setStyle(new NotificationCompat.MediaStyle()

                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0))

                .setColor(getResources().getColor(R.color.colorCenterGreen))

                .setLargeIcon(largeIcon)

                .setSmallIcon(android.R.drawable.stat_sys_headset)

                .setContentText(currPod.getPodName())

                .setContentTitle(currPod.getEpTitle())

                .setContentInfo(currPod.getDescription())

                .addAction(notificationAction,"pause",play_pauseAction);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());

    }
*/
/*
    @Override
    public boolean onUnbind(Intent intent) {
        mediaSession.release();
        return super.onUnbind(intent);
    }
*/
    public enum PlaybackStatus {
        PLAYING,
        PAUSED
    }

    public static final String ACTION_PLAY = "dahe0070.androidpodcaster.play";
    public static final String ACTION_PAUSE = "dahe0070.androidpodcaster.pause";
    public static final String ACTION_REWIND = "dahe0070.androidpodcaster.action_rewind";
    public static final String ACTION_FAST_FORWARD = "dahe0070.androidpodcaster.action_fast_foward";

    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaController mediaController;

    private MediaControllerCompat.TransportControls transportControls;

    private void initMediaSession() {
        if (mediaSessionManager != null) return;

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);

        mediaSession = new MediaSessionCompat(getApplicationContext(),"AudioPlayerService");

        transportControls = mediaSession.getController().getTransportControls();

        mediaSession.setActive(true);

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                if (requestAudioFocus()) {
                    resumeMedia();
                    mediaSession.setActive(true);
                    //resumeMedia();
                    //mediaSession.setActive(true);
                }
                Log.i("ONPLAY","RUNNING");
                //buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                if (requestAudioFocus()) {
                    //pauseMedia();
                    audioPlayer.pause();
                    mediaSession.setActive(false);
                }
                Log.i("ONPAUSE","RUNNING");
                //buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onFastForward() {
                super.onFastForward();
            }

            @Override
            public void onRewind() {
                super.onRewind();
            }

            @Override
            public void onStop() {
                super.onStop();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }
        });
    }

    private void updateMetaData(){

        Bitmap largeIcon = null;
        try {
            largeIcon = new ImageDownloader().execute(currPod.getSongImage()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_rss_feed_black_48dp);
        } catch (ExecutionException e) {
            e.printStackTrace();
            largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_rss_feed_black_48dp);
        }

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, largeIcon)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,currPod.getPodName())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,currPod.getDescription())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,currPod.getEpTitle())
                .build());

    }

    private PendingIntent playbackAction(int actionNumber){
        Intent playbackAction = new Intent(this,AudioPlayerService.class);
        switch (actionNumber){
            case 0:
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this,actionNumber,playbackAction,0);
            case 1:
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction){
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if(actionString.equalsIgnoreCase(ACTION_PLAY)) {
            // play media
            //resumeMedia();
            transportControls.play();
            Log.i("Resuming","now");

        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            // pause media
            //pauseMedia();
            transportControls.pause();
            Log.i("Pausing","now");
        }
    }

    private NotificationCompat.Builder notificationBuilder = null;


    private void buildNotification(){

        //Intent notIntent = new Intent(this,MainActivity.class);
        //notIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.setAction(Intent.ACTION_MAIN);
        notIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        currPod.getSongImage();




        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap largeIcon = null;
        try {
            largeIcon = new ImageDownloader().execute(currPod.getSongImage()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_rss_feed_black_48dp);
        } catch (ExecutionException e) {
            e.printStackTrace();
            largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_rss_feed_black_48dp);
        }


        //Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.dummy_image);

        //Notification.Action test = new Notification.Action.Builder(R.drawable.circled_forward_filled,,pendingIntent).build();

        //NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.circled_forward_filled,"Play",pendingIntent).build();

/*
        Notification.Builder notificationBuilder = new Notification.Builder(this)

                        .setContentIntent(pendingIntent)

                         .setLargeIcon(largeIcon)

                        .setSmallIcon(R.drawable.dummy_image_episode)

                        .setOngoing(true)

                        .setContentText(currPod.getPodName())

                        .setContentTitle(currPod.getEpTitle())

                        .setContentInfo(currPod.getDescription());

*/

        notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setShowWhen(false)
                // Set notif style
                .setStyle(new NotificationCompat.MediaStyle()

                        //.setMediaSession(mediaSession.getSessionToken())
                        //.setShowActionsInCompactView(0))
                )
               // .setStyle(new NotificationCompat.BigTextStyle().bigText(currPod.getDescription()))

                .setColor(getResources().getColor(R.color.colorCenterGreen))

                .setContentIntent(pendingIntent)

                .setLargeIcon(largeIcon)

                .setSmallIcon(android.R.drawable.stat_sys_headset)

                .setContentText(currPod.getPodName())

                .setContentTitle(currPod.getEpTitle());

                //.setContentInfo(currPod.getDescription());



                //.addAction(notificationAction,"pause",play_pauseAction);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());

        Notification not = notificationBuilder.build();
        not.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;


        startForeground(NOTIFICATION_ID,not);


    }

    private void updateNotification(){
        Bitmap largeIcon = null;
        try {
            largeIcon = new ImageDownloader().execute(currPod.getSongImage()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_rss_feed_black_48dp);
        } catch (ExecutionException e) {
            e.printStackTrace();
            largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_rss_feed_black_48dp);
        }

        notificationBuilder.setLargeIcon(largeIcon);
        notificationBuilder.setContentText(currPod.getPodName());
        notificationBuilder.setContentTitle(currPod.getEpTitle());
        //notificationBuilder.setContentInfo(currPod.getDescription());

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());

    }

    private class ImageDownloader extends AsyncTask<String,Integer,Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap epImage = getBitmapFromURL(params[0]);

            return epImage;
        }
    }


    public Bitmap getBitmapFromURL(String imageURL) {
        try {
            URL url = new URL(imageURL);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            if (myBitmap != null) {
                Bitmap scaledImage = scaleBitmap(myBitmap, 210, 210);
                return scaledImage;
            } else return null;


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());

        return output;
    }


    public int getCurrentPos(){
        return audioPlayer.getCurrentPosition();
    }

}
