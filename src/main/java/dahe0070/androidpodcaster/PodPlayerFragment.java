package dahe0070.androidpodcaster;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.xml.sax.XMLReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PodPlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PodPlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PodPlayerFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private PodEpisode currEpisode;
    private String filePath;

    private OnFragmentInteractionListener mListener;

    private Handler eventHandler = new Handler();

    private ImageButton imgPodEpisode;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private ImageButton btnPlayPause;
    private ImageButton btnBackward;
    private ImageButton btnForward;
    private ImageView streaming;
    private SeekBar seekBar;
    //private RadioPlayer radioPlayer;

    private TextView podName;
    private TextView episodeName;
    private TextView dateText;
    private TextView fileSize;
    private TextView description;
    private TextView currTime;
    private TextView totalTime;
    private String messge;
    private String sizeMessage = "";
    private boolean downloaded;
    private DatabaseHandler podDatabase;
    private PlayerClicks listener;


    public PodPlayerFragment() {
        // Required empty public constructor
    }

    private Animator mCurrentAnimator;

    private int mShortAnimationDuration;
    View thumb1View;
    Bitmap episodeImage;
    private GestureDetector gestureDetector;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PodPlayerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PodPlayerFragment newInstance(String param1, String param2) {
        PodPlayerFragment fragment = new PodPlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        podDatabase = new DatabaseHandler(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent i = getActivity().getIntent();

        currEpisode = (PodEpisode) i.getSerializableExtra("CurrentEpisode");
        downloaded = currEpisode.getDownloaded();


        //filePath = i.getStringExtra("Filepath");
        filePath = currEpisode.getLocalLink();

        if (downloaded) {
            File tempFile = new File(filePath);
            int size = (int) tempFile.length();
            //stringSize(size);
            sizeMessage = Formatter.formatFileSize(getActivity(),size);
        }

        messge = i.getStringExtra("Resume");

        //radioPlayer = PlayerSingleton.getInstance(getActivity());

        if (messge.contains("Resume")){

        } else {
            //radioPlayer.initPlayer(filePath,currEpisode.getEpTitle());
            //radioPlayer.startPlay();
            ((MainActivity)getActivity()).playAudio(filePath,currEpisode); // startar och spelar upp direkt
        }

        if(!podDatabase.isAddedEpisode(currEpisode)){
            podDatabase.insertDataEpisode(currEpisode);
            //Log.i("Current progress player","" + currEpisode.getProgress());
        }



        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pod_player, container, false);
        viewDetector();
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return viewGesture.onTouchEvent(event);
            }
        });
        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        gestureDetector = new GestureDetector(getActivity(),new DoubleTapImage());
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        return view;
    }

    GestureDetector viewGesture;

    private void viewDetector(){
        viewGesture = new GestureDetector(getActivity(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.i("On fling called","now");
                final int SWIPE_MIN_DISTANCE = 180;
                final int SWIPE_MAX_OFF_PATH = 250;
                final int SWIPE_THRESHOLD_VELOCITY = 200;
               // if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
               //     return false;
                if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    Log.i("motionEvent", "Down to Up");
                    listener.navigateToPodlist();
                } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    Log.i("motionEvent", "Up to Down");
                }

                return true;
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }




    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            description.setMaxLines(5);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

            description.setMaxLines(20);
        }
    }

    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        listener.switchMediaController(false);
        if (messge.contains("Resume")){
            updateProgressBar();
        } else {

        }
        /*
        boolean isPlayer = radioPlayer.isPlaying();
        if (isPlayer){
            Log.i("Mediaplayer","playing");
        } else {
            Log.i("Mediaplayer","NOT PLAYING");
        }
        */
    }

    @Override
    public void onPause() {
        super.onPause();

        listener.setScrollingText(currEpisode.getPodName(),currEpisode.getEpTitle());
        //eventHandler.removeCallbacks(updateEvents);
    }

    private static final String THEME = "THEME_PREF";
    private static final String DEFAULT_COLOR = "GREEN";

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);






        //radioPlayer = new RadioPlayer(getActivity());



        imgPodEpisode = (ImageButton) view.findViewById(R.id.imgPodEpisode);
        btnPrev = (ImageButton) view.findViewById(R.id.btnPrev);
        btnNext = (ImageButton) view.findViewById(R.id.btnNext);
        btnPlayPause = (ImageButton) view.findViewById(R.id.btnPlayPause);
        btnBackward = (ImageButton) view.findViewById(R.id.btnBackward);
        btnForward = (ImageButton) view.findViewById(R.id.btnForward);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        fileSize = (TextView) view.findViewById(R.id.txtFileSize);
        streaming = (ImageView) view.findViewById(R.id.imgStreaming);

        if (downloaded){
            fileSize.setText(sizeMessage);
            streaming.setVisibility(View.GONE);
        } else {
            fileSize.setText(R.string.streaming);

        }


        btnForward.setOnClickListener(this);
        btnBackward.setOnClickListener(this);
        btnPlayPause.setOnClickListener(this);
        btnPrev.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(this);



        if (currEpisode.getSongImage() != null) {
            //Bitmap episodeImage = null;
            if(!isNetworkAvailable()){
                imgPodEpisode.setImageResource(R.drawable.ic_rss_feed_black_48dp);
            } else {

                try {
                    episodeImage = new ImageDownloader().execute(currEpisode.getSongImage()).get();
                    if (episodeImage != null) {
                        imgPodEpisode.setImageBitmap(episodeImage);
                    } else {
                        Picasso.with(getActivity()).load(currEpisode.getBackupImage()).resize(210, 210).centerCrop().into(imgPodEpisode);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }

        }


        podName = (TextView) view.findViewById(R.id.txtPodName);
        podName.setText(currEpisode.getPodName());

        SharedPreferences dbPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String ThemeColor = dbPreference.getString(THEME,DEFAULT_COLOR);
        changeTextColor(ThemeColor);

        episodeName = (TextView) view.findViewById(R.id.txtEpisodeName);
        episodeName.setText(currEpisode.getEpTitle());

        dateText = (TextView) view.findViewById(R.id.txtDate);
        //dateText.setText(currEpisode.getDate());
        dateText.setText(Helper.dateToTimeago(currEpisode.getDate()));

        fileSize = (TextView) view.findViewById(R.id.txtFileSize);
        description = (TextView) view.findViewById(R.id.txtEpisodeDescription);
        description.setText(Html.fromHtml(currEpisode.getDescription(),null,new UTTagHandler()));
        description.setMovementMethod(new ScrollingMovementMethod());

        thumb1View = getView().findViewById(R.id.imgPodEpisode);
        //thumb1View.setOnClickListener(this);


        thumb1View.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //return false;
                return gestureDetector.onTouchEvent(event);
            }
        });



        currTime = (TextView) view.findViewById(R.id.txtCurrTime);
        totalTime = (TextView) view.findViewById(R.id.txtTotalTime);

        seekBar.setProgress(0);
        seekBar.setMax(100);

        if (messge.contains("Resume")){
        } else {

            if(podDatabase.isAddedEpisode(currEpisode)){
                    int currProgress = podDatabase.getEpisodeProgress(currEpisode); // OBS läser progress från databasen

                    seekBar.setProgress(currProgress);

            }
        }
        listener.switchMediaController(false);

        //((MainActivity)getActivity()).showMediaController();
        //updateProgressBar();


    }

    private void changeTextColor(String theme){

        switch (theme){
            case "RED" : podName.setTextColor(Color.parseColor("#f44336"));
                break;
            case "PINK" : podName.setTextColor(Color.parseColor("#ec407a"));
                break;
            case "YELLOW" : podName.setTextColor(Color.parseColor("#ffca28"));
                break;
            case "ORANGE" : podName.setTextColor(Color.parseColor("#ff5722"));
                break;
            case "GREEN" : podName.setTextColor(Color.parseColor("#1b5e20"));
                break;
            case "BLUE" : podName.setTextColor(Color.parseColor("#0d47a1"));
                break;
            case "BROWN" : podName.setTextColor(Color.parseColor("#795548"));
                break;
        }
    }


    private class DoubleTapImage extends GestureDetector.SimpleOnGestureListener {

        public DoubleTapImage() {
            super();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            zoomImageFromThumb(thumb1View,largeImage);
            return super.onDoubleTap(e);
        }
    }

    private class UTTagHandler implements Html.TagHandler{

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            if(tag.equals("ul") && !opening) output.append("\n");
            if(tag.equals("li") && opening) output.append("\n\t•");
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        listener = (PlayerClicks) context;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        MainActivity main = ((MainActivity)getActivity());
        switch (v.getId()){
            case R.id.btnPlayPause :
                boolean play = main.isServicePlaying();
                if(!play){
                    btnPlayPause.setImageResource(R.drawable.circled_pause_large);
                    if(listener.radioStatus()){
                        listener.radioStop();
                    }

                    main.resumeAudio();
                    //updateEpisodeData();
                    updateProgressBar();
                } else {
                    btnPlayPause.setImageResource(R.drawable.circled_play_filled);
                    if(listener.radioStatus()){
                        listener.radioStop();
                    }
                    main.pauseAudio();
                    updateProgressBar();
                }
                break;
            case R.id.btnForward:
                main.forwardAudio();
                break;
            case R.id.btnBackward:
                main.rewindAudio();
                break;
            case R.id.btnPrev:
                main.restartAudio();
                break;
            case R.id.imgPodEpisode:
                if (largeImage != null) {

                    zoomImageFromThumb(thumb1View,largeImage);

                }
                break;
        }
    }
/*
    public void updateEpisodeData(){
        if (podDatabase.isAddedEpisode(currEpisode)){
            podDatabase.updateProgress(radioPlayer.updateProgressBar(),currEpisode.getEpTitle());
            Log.i("updateEpisodeData","progress" + "progress" + radioPlayer.updateProgressBar());
        }
    }
*/



    private void zoomImageFromThumb(final View thumbView, Bitmap imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) getView().findViewById(
                R.id.expanded_image);
        expandedImageView.setImageBitmap(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        getView().findViewById(R.id.parentRelativePodPlayer)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }









    public void updateProgressBar(){
        eventHandler.postDelayed(updateEvents,100);
    }

    private Runnable updateEvents = new Runnable() {
        @Override
        public void run() {
            MainActivity main = ((MainActivity)getActivity());
            // update everything

            currTime.setText(main.getCurrentDurationEpisode());

            totalTime.setText(main.getTotalEpisodeLenght());

            int progress = main.getProgressPercent();
            seekBar.setProgress(progress);
            eventHandler.postDelayed(this,100);
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        eventHandler.removeCallbacks(updateEvents);

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        eventHandler.removeCallbacks(updateEvents);

        MainActivity main = ((MainActivity)getActivity());

        int totalDuration = main.getDuration();
        int currentPos = main.progressToTimer(seekBar.getProgress(),totalDuration);

        main.seekTo(currentPos);

        updateProgressBar();
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class ImageDownloader extends AsyncTask<String,Integer,Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap epImage = getBitmapFromURL(params[0]);

            return epImage;
        }
    }

    private Bitmap largeImage;

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
                largeImage = scaleBitmap(myBitmap,520,520);
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
}
