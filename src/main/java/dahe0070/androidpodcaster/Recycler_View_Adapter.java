package dahe0070.androidpodcaster;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.datatype.Duration;

/**
 * Created by Dave on 2017-08-19.
 */

public class Recycler_View_Adapter extends RecyclerView.Adapter<Recycler_View_Adapter.MyViewHolder> implements Filterable {

    List<PodEpisode> list = Collections.emptyList();
    Context context;
    private Bitmap episodeImage;
    PlayerClicks listener;
    private SearchFilter searchFilter;
    private List<PodEpisode> filteredList;
    private DatabaseHandler podDatabase;
    private boolean isTextViewClicked = false;
    private Picasso.Builder builder;
    private Picasso.Listener picassoListener;
    PodEpisode podEpisode;
    //private View_Holder tempHolder;
    private MyViewHolder tempHolder;
    public boolean scrolling = false;
    Picasso singlePicasso;
    private String TYPE = "";


    public Recycler_View_Adapter(List<PodEpisode> mList, Context ctx,String mType){
        this.list = mList;
        this.context = ctx;
        this.filteredList = mList;
        listener = (PlayerClicks) ctx;
        podDatabase = new DatabaseHandler(ctx);
        builder = new Picasso.Builder(ctx);
        this.TYPE = mType;
        Log.i("Current type : ","" + this.TYPE);
        setListener();
    }



    private void setListener(){

        picassoListener = new Picasso.Listener() {

            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                singlePicasso.with(context).load(podEpisode.getBackupImage()).resize(210, 210).centerCrop().into(tempHolder.epImage);

            }


        };
        //builder.listener(picassoListener);
    }

    MultiSelector mSelector = new MultiSelector();

    ModalMultiSelectorCallback multiSelectorCallback = new ModalMultiSelectorCallback(mSelector) {

        /* HERE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
        /*************************************************************************/
        /************************************************************************/

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            if(!TYPE.equals("EPISODES")) {
                MenuInflater test = new MenuInflater(context);
                test.inflate(R.menu.list_context_menu, menu);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            if(item.getItemId() == R.id.delete){
                if(TYPE.equals("DOWNLOAD")) {
                    for (int i = list.size(); i >= 0; i--) {
                        if (mSelector.isSelected(i, 0)) {
                            Log.i("REMOVAL of EP", "" + list.get(i).getEpTitle());
                            Helper.removeMP3(list.get(i).getLocalLink());
                            podDatabase.removeDownload(list.get(i).getEpTitle());
                            Toast.makeText(context, "Episode removed : " + list.get(i).getEpTitle(), Toast.LENGTH_SHORT).show();
                            list.remove(i);
                            notifyDataSetChanged();
                            mode.finish();
                        }
                    }
                } else if (TYPE.equals("CONTINUE")){
                    for (int i = list.size(); i >= 0; i--) {
                        if (mSelector.isSelected(i, 0)) {
                            Log.i("REMOVAL of EP", "" + list.get(i).getEpTitle());
                            Helper.removeMP3(list.get(i).getLocalLink());
                            podDatabase.deleteEpisode(list.get(i).getEpTitle());
                            Toast.makeText(context, "Episode removed : " + list.get(i).getEpTitle(), Toast.LENGTH_SHORT).show();
                            list.remove(i);
                            notifyDataSetChanged();
                            mode.finish();
                        }
                    }
                }

                else {
                    Toast.makeText(context,"CANNOT REMOVE THIS EPISODE" + TYPE,Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        }
    };
/*
    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rowViev = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_each_episode,parent,false);


        View_Holder holder = new View_Holder(rowViev,mSelector,multiSelectorCallback);



        return holder;
    }
*/


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //View rowViev = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_each_episode,parent,false);
        View rowViev = null;
        if(TYPE.equals("EPISODES")){
            rowViev = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_each_episode_latest,parent,false);
        } else {
            rowViev = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_each_episode,parent,false);
        }

        MyViewHolder holder = new MyViewHolder(rowViev,mSelector,multiSelectorCallback);

        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        tempHolder = holder;
        podEpisode = getFiltered(position);
        if (!scrolling) {
            if (podDatabase.isAddedEpisode(podEpisode)) {
                int progress = podDatabase.getEpisodeProgress(podEpisode);
                holder.episodeProgress.setProgress(progress);
                if (progress > 98) {
                    podEpisode.setRead(true);
                }
            }
        }
        //}


        int index = list.indexOf(podEpisode);

        final MyViewHolder newHolder = holder;

        holder.bind(context,index,podEpisode.getEpTitle(),podEpisode.getMp3Link());
        holder.epTitle.setText(podEpisode.getEpTitle());
        holder.epDesc.setText(Html.fromHtml(podEpisode.getDescription()));
        if(TYPE.equals("EPISODES")){
            holder.epPodcastTitle.setText(podEpisode.getPodName());
        }

        holder.epDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newHolder.epDesc.setMaxLines(50);
            }
        });

        if (podEpisode.getMp3Link().equals("none")){
            holder.btnDownload.setVisibility(View.GONE);
            holder.btnPlay.setVisibility(View.GONE);
        }

        if(TYPE.equals("ITUNES")){
            holder.btnDownload.setVisibility(View.GONE);
            holder.btnPlay.setVisibility(View.GONE);
        }

        holder.epDuration.setText(podEpisode.getDuration());
        //holder.epDate.setText(podEpisode.getDate());
        holder.epDate.setText(Helper.dateToTimeago(podEpisode.getDate()));

        if(!podEpisode.getRead()){
            holder.read.setVisibility(View.GONE);
        }
        File sdCard = new File(Environment.getExternalStorageDirectory(),"downloaded_episodes");
        File file = new File(sdCard,podEpisode.getEpTitle() + ".mp3");
        if(file.exists() && !file.isDirectory()) {
            holder.btnDownload.setEnabled(false);
            holder.btnDownload.setColorFilter(Color.GRAY);
        } else {
            holder.epDownloaded.setVisibility(View.GONE);
        }


        if(list.get(position).getSongImage() != null){
            if(!isNetworkAvailable()){
                holder.epImage.setImageResource(R.drawable.ic_rss_feed_black_24dp);
            } else {
                //Picasso.with(context).load(podEpisode.getSongImage()).placeholder(R.drawable.dummy_image_episode).resize(210, 210).centerCrop().into(holder.epImage);
                Glide.with(context).load(podEpisode.getSongImage()).diskCacheStrategy(DiskCacheStrategy.ALL).override(240,240).centerCrop().into(holder.epImage);
                //Picasso.with(context).setLoggingEnabled(true);
                //Picasso.with(context).load(podEpisode.getSongImage()).into(holder.epImage);

                //builder.listener(picassoListener);
                //builder.build().load(podEpisode.getSongImage()).resize(210,210).centerCrop().into(holder.epImage);

            }

        }

        holder.epDesc.setMaxLines(2);

        holder.epDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTextViewClicked){
                    //This will shrink textview to 2 lines if it is expanded.
                    newHolder.epDesc.setMaxLines(2);
                    isTextViewClicked = false;
                } else {
                    //This will expand the textview if it is of 2 lines
                    newHolder.epDesc.setMaxLines(Integer.MAX_VALUE);
                    isTextViewClicked = true;
                }
            }
        });
    }

    public class MyViewHolder extends SwappingHolder implements View.OnClickListener,View.OnLongClickListener {

        ImageButton epImage;
        TextView epTitle;
        TextView epDesc;
        TextView epDuration;
        TextView epDate;
        TextView epDownloaded;
        ImageButton read;
        Button subscribe;
        //ModalMultiSelectorCallback callBack;

        ProgressBar episodeProgress;

        Context context;
        int podIndex;
        PlayerClicks listener;
        String mp3Link;
        String currTitle;
        boolean isTextViewClicked = false;
        //private MultiSelector multiSelector = new MultiSelector();

        ImageButton btnPlay;
        ImageButton btnDownload;
        TextView epPodcastTitle;

        public MyViewHolder(View itemView, MultiSelector multiSelector, ModalMultiSelectorCallback mCallback) {
            super(itemView, multiSelector);
            //this.callBack = mCallback;
            //this.multiSelector = multiSelector;
            subscribe = (Button) itemView.findViewById(R.id.btnSubscribe);
            epImage = (ImageButton) itemView.findViewById(R.id.imgPodcast);
            epTitle = (TextView) itemView.findViewById(R.id.txtEpisodeTitle);
            epDesc = (TextView) itemView.findViewById(R.id.txtEpisodeDesc);
            epDuration = (TextView) itemView.findViewById(R.id.txtEpisodeDuration);
            epDate = (TextView) itemView.findViewById(R.id.txtEpisodeDate);
            read = (ImageButton) itemView.findViewById(R.id.imgRead);
            epDownloaded = (TextView) itemView.findViewById(R.id.txtEpDownloaded);

            episodeProgress = (ProgressBar) itemView.findViewById(R.id.progressBarEpisode);
            if(TYPE.equals("EPISODES")){
                epPodcastTitle = (TextView) itemView.findViewById(R.id.txtEpPodcastTitle);
            }

            btnPlay = (ImageButton) itemView.findViewById(R.id.btnPlayEpisode);
            btnDownload = (ImageButton) itemView.findViewById(R.id.btnDownloadEpisode);

            btnPlay.setOnClickListener(this);
            btnDownload.setOnClickListener(this);
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View v) {
            Log.i("on click", "" + v.getId());
            if(!mSelector.tapSelection(this)) {

                switch (v.getId()) {
                    case R.id.btnPlayEpisode:
                        Toast.makeText(context, "Klickade på" + currTitle, Toast.LENGTH_SHORT).show();
                        File sdCard = new File(Environment.getExternalStorageDirectory(), "downloaded_episodes");
                        File file = new File(sdCard, currTitle + ".mp3");
                        if (file.exists() && !file.isDirectory()) {
                            listener.episodeClick(currTitle, context, this.mp3Link, podIndex);
                        } else {
                            listener.streamEpisode(currTitle, context, mp3Link, podIndex);
                            Toast.makeText(context, "Streamar avsnitt..", Toast.LENGTH_SHORT).show();
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

        @Override
        public boolean onLongClick(View v) {
            if (!mSelector.isSelectable()) { // (3)
                if(!TYPE.equals("EPISODES")) {
                    ((AppCompatActivity) v.getContext()).startSupportActionMode(multiSelectorCallback);
                    mSelector.setSelectable(true); // (4)
                    mSelector.setSelected(MyViewHolder.this, true); // (5)
                    int id = v.getId();
                    Log.i("Id for view", "" + id);
                    return true;
                }
            }
            return false;
        }

    }

    public void DBchecker(){
        if (podDatabase.isAddedEpisode(podEpisode)){
            int progress = podDatabase.getEpisodeProgress(podEpisode);
            tempHolder.episodeProgress.setProgress(progress);
            if (progress > 98){
                podEpisode.setRead(true);
            }
        }
    }
/*
    @Override
    public void onBindViewHolder(final View_Holder holder, int position) {
        tempHolder = holder;
        podEpisode = getFiltered(position);
        if (!scrolling) {
            if (podDatabase.isAddedEpisode(podEpisode)) {
                int progress = podDatabase.getEpisodeProgress(podEpisode);
                holder.episodeProgress.setProgress(progress);
                if (progress > 98) {
                    podEpisode.setRead(true);
                }
            }
        }
        //}


        int index = list.indexOf(podEpisode);

        final View_Holder newHolder = holder;

        holder.bind(context,index,podEpisode.getEpTitle(),podEpisode.getMp3Link());
        holder.epTitle.setText(podEpisode.getEpTitle());
        holder.epDesc.setText(Html.fromHtml(podEpisode.getDescription()));
        holder.epDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newHolder.epDesc.setMaxLines(50);
            }
        });

        if (podEpisode.getMp3Link().equals("none")){
            holder.btnDownload.setVisibility(View.GONE);
            holder.btnPlay.setVisibility(View.GONE);
        }

        holder.epDuration.setText(podEpisode.getDuration());
        holder.epDate.setText(podEpisode.getDate());

        if(!podEpisode.getRead()){
            holder.read.setVisibility(View.GONE);
        }
        File sdCard = new File(Environment.getExternalStorageDirectory(),"downloaded_episodes");
        File file = new File(sdCard,podEpisode.getEpTitle() + ".mp3");
        if(file.exists() && !file.isDirectory()) {
            holder.btnDownload.setEnabled(false);
            holder.btnDownload.setColorFilter(Color.GRAY);
        } else {
            holder.epDownloaded.setVisibility(View.GONE);
        }


        if(list.get(position).getSongImage() != null){
            if(!isNetworkAvailable()){
                holder.epImage.setImageResource(R.drawable.ic_rss_feed_black_24dp);
            } else {
                Picasso.with(context).load(podEpisode.getSongImage()).resize(210, 210).centerCrop().into(holder.epImage);

                //builder.listener(picassoListener);
                //builder.build().load(podEpisode.getSongImage()).resize(210,210).centerCrop().into(holder.epImage);

            }

        }

        holder.epDesc.setMaxLines(2);

        holder.epDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTextViewClicked){
                    //This will shrink textview to 2 lines if it is expanded.
                    holder.epDesc.setMaxLines(2);
                    isTextViewClicked = false;
                } else {
                    //This will expand the textview if it is of 2 lines
                    holder.epDesc.setMaxLines(Integer.MAX_VALUE);
                    isTextViewClicked = true;
                }
            }
        });



    }
*/
    private void loadOriginal(){

    }

    private void loadBackup(){

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    public int getItemCount() {
        return filteredList.size();
    }


    public PodEpisode getFiltered(int i){
        return filteredList.get(i);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void insert(int position, PodEpisode podEpisode){
        list.add(position,podEpisode);
        notifyItemInserted(position);
    }

    public void remove(PodEpisode podEpisode){
        int position = list.indexOf(podEpisode);
        list.remove(position);
        notifyItemRemoved(position);
    }



    @Override
    public Filter getFilter() {
        if (searchFilter == null){
            searchFilter = new SearchFilter();
        }

        return searchFilter;
    }

    private class SearchFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                ArrayList<PodEpisode> tempList = new ArrayList<PodEpisode>();

                for (PodEpisode ep : list){
                    if (ep.getEpTitle().toLowerCase().contains(constraint.toString().toLowerCase()) || ep.getDescription().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(ep);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;

            } else {
                filterResults.count = list.size();
                filterResults.values = list;

            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<PodEpisode>) results.values;

            notifyDataSetChanged();
        }
    }


/*
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
    */
}
