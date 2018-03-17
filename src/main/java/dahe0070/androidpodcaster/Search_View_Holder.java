package dahe0070.androidpodcaster;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Dave on 2017-08-27.
 */

public class Search_View_Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView podName;
    TextView feed;
    Button subscribe;
    ImageView image;
    Context context;
    PodAdder listener;
    PlayerClicks listener_two;
    String backupImage;

    public Search_View_Holder(View itemView) {
        super(itemView);

        podName = (TextView) itemView.findViewById(R.id.txtResultPodName);
        feed = (TextView) itemView.findViewById(R.id.txtResultFeed);
        subscribe = (Button) itemView.findViewById(R.id.btnSubscribe);
        image = (ImageView) itemView.findViewById(R.id.resultImage);
        subscribe.setOnClickListener(this);
        image.setOnClickListener(this);


    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSubscribe : createPodcast();
                break;
            case R.id.resultImage : showPodInfo();
                break;
        }
    }



    public void createPodcast(){
        final String podname = podName.getText().toString();
        final String feedLink = feed.getText().toString();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (Helper.URLworking(feedLink)){
                    Podcast clickedPod = new Podcast(podname,feedLink,"universal");
                    clickedPod.setPodImageUrl(backupImage);
                    clickedPod.setBackupImage(backupImage);
                    PodcastFactory podFactory = new PodcastFactory(context);
                    Podcast newPodcast = podFactory.createPodcast(clickedPod);
                    listener.addPodcast(newPodcast);
                }
            }
        });

        thread.start();

    }

    public void showPodInfo(){
        final String podname = podName.getText().toString();
        final String feedLink = feed.getText().toString();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (Helper.URLworking(feedLink)){
                    Log.i("URK IS ","VALID!!!");
                    Podcast clickedPod = new Podcast(podname, feedLink, "universal");
                    clickedPod.setPodImageUrl(backupImage);
                    clickedPod.setBackupImage(backupImage);
                    PodcastFactory podFactory = new PodcastFactory(context);
                    Podcast newPodcast = podFactory.createPodcast(clickedPod);
                    listener_two.itunesPodClick(newPodcast);
                } else {

                }
            }
        });

        thread.start();
    }

    public void showError(){
        Toast.makeText(context, R.string.itunes_not_avalible,Toast.LENGTH_SHORT).show();
    }

    public void bind(Context context){
        this.context = context;
        this.listener = (PodAdder) context;
        this.listener_two = (PlayerClicks) context;
    }
}
