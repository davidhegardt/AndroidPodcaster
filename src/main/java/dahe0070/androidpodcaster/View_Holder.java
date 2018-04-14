package dahe0070.androidpodcaster;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;

import java.io.File;

/**
 * Created by Dave on 2017-08-19.
 */

public class View_Holder extends SwappingHolder implements View.OnClickListener,View.OnLongClickListener {

    public View_Holder(View itemView, MultiSelector multiSelector, ModalMultiSelectorCallback mCallback) {
        super(itemView, multiSelector);
        this.callBack = mCallback;
        this.multiSelector = multiSelector;

        epImage = (ImageButton) itemView.findViewById(R.id.imgPodcast);
        epTitle = (TextView) itemView.findViewById(R.id.txtEpisodeTitle);
        epDesc = (TextView) itemView.findViewById(R.id.txtEpisodeDesc);
        epDuration = (TextView) itemView.findViewById(R.id.txtEpisodeDuration);
        epDate = (TextView) itemView.findViewById(R.id.txtEpisodeDate);
        read = (ImageButton) itemView.findViewById(R.id.imgRead);
        epDownloaded = (TextView) itemView.findViewById(R.id.txtEpDownloaded);

        episodeProgress = (ProgressBar) itemView.findViewById(R.id.progressBarEpisode);

        btnPlay = (ImageButton) itemView.findViewById(R.id.btnPlayEpisode);
        btnDownload = (ImageButton) itemView.findViewById(R.id.btnDownloadEpisode);

        btnPlay.setOnClickListener(this);
        btnDownload.setOnClickListener(this);
        itemView.setLongClickable(true);
        itemView.setOnLongClickListener(this);
    }

    ImageButton epImage;
    TextView epTitle;
    TextView epDesc;
    TextView epDuration;
    TextView epDate;
    TextView epDownloaded;
    ImageButton read;
    ModalMultiSelectorCallback callBack;

    ProgressBar episodeProgress;

    Context context;
    int podIndex;
    PlayerClicks listener;
    String mp3Link;
    String currTitle;
    boolean isTextViewClicked = false;
    private MultiSelector multiSelector = new MultiSelector();

    ImageButton btnPlay;
    ImageButton btnDownload;


    View_Holder(View itemView){
        super(itemView);

        epImage = (ImageButton) itemView.findViewById(R.id.imgPodcast);
        epTitle = (TextView) itemView.findViewById(R.id.txtEpisodeTitle);
        epDesc = (TextView) itemView.findViewById(R.id.txtEpisodeDesc);
        epDuration = (TextView) itemView.findViewById(R.id.txtEpisodeDuration);
        epDate = (TextView) itemView.findViewById(R.id.txtEpisodeDate);
        read = (ImageButton) itemView.findViewById(R.id.imgRead);
        epDownloaded = (TextView) itemView.findViewById(R.id.txtEpDownloaded);

        episodeProgress = (ProgressBar) itemView.findViewById(R.id.progressBarEpisode);

        btnPlay = (ImageButton) itemView.findViewById(R.id.btnPlayEpisode);
        btnDownload = (ImageButton) itemView.findViewById(R.id.btnDownloadEpisode);

        btnPlay.setOnClickListener(this);
        btnDownload.setOnClickListener(this);
        itemView.setLongClickable(true);
        //epDesc.setMaxLines(3);
        //epDesc.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(!multiSelector.tapSelection(this)) {

            switch (v.getId()) {
                case R.id.btnPlayEpisode:
                    Toast.makeText(context, R.string.clicked_on + currTitle, Toast.LENGTH_SHORT).show();
                    File sdCard = new File(Environment.getExternalStorageDirectory(), "downloaded_episodes");
                    File file = new File(sdCard, currTitle + ".mp3");
                    if (file.exists() && !file.isDirectory()) {
                        listener.episodeClick(currTitle, context, this.mp3Link, podIndex);
                    } else {
                        listener.streamEpisode(currTitle, context, mp3Link, podIndex);
                        Toast.makeText(context, R.string.streamar_avsnitt, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnDownloadEpisode:
                    listener.episodeClick(currTitle, context, this.mp3Link, podIndex);
                    break;
            /*
            case R.id.txtEpisodeDesc :
                if(isTextViewClicked){
                    //This will shrink textview to 2 lines if it is expanded.
                    epDesc.setMaxLines(3);
                    isTextViewClicked = false;
                } else {
                    //This will expand the textview if it is of 2 lines
                    epDesc.setMaxLines(Integer.MAX_VALUE);
                    isTextViewClicked = true;
                }
                break;
                */


            }
        }
    }

    public void bind(Context context, int index,String title,String mp3Link){
        this.context = context;
        this.listener = (PlayerClicks) context;
        this.podIndex = index;
        this.mp3Link = mp3Link;
        this.currTitle = title;

    }

    private Dialog dialog;

    @Override
    public boolean onLongClick(View v) {
        if (!multiSelector.isSelectable()) { // (3)
            ((AppCompatActivity)v.getContext()).startSupportActionMode(callBack);
            multiSelector.setSelectable(true); // (4)
            multiSelector.setSelected(View_Holder.this,true); // (5)

            return true;
        }
        return false;
    }
}
