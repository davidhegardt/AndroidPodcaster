package dahe0070.androidpodcaster;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Dave on 2017-08-15.
 */

public class EpisodeViewAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] titles;
    private final String[] imageLinks;
    private final String[] descs;
    private final String[] durations;
    private final String[] dates;
    private final boolean[] read;
    private final String[] mp3Links;
    private PlayerClicks listener;
    private Bitmap episodeImage;
    List<Podcast> podList = Collections.emptyList();


    public EpisodeViewAdapter(Context ctx,String[] titles,String[] imageLinks,String[] descriptions,String[] durations, String[] dates,boolean[] read,String[] mp3Links){
        super(ctx,-1,titles);
        this.context = ctx;
        this.titles = titles;
        this.imageLinks = imageLinks;
        this.descs = descriptions;
        this.durations = durations;
        this.dates = dates;
        this.read = read;
        this.mp3Links = mp3Links;
        listener = (PlayerClicks) ctx;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);



        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.listview_each_episode,parent,false);

        final ImageButton epImage = (ImageButton) rowView.findViewById(R.id.imgPodcast);
        final TextView epTitle = (TextView) rowView.findViewById(R.id.txtEpisodeTitle);
        final TextView epDesc = (TextView) rowView.findViewById(R.id.txtEpisodeDesc);
        final TextView epDuration = (TextView) rowView.findViewById(R.id.txtEpisodeDuration);
        final TextView epDate = (TextView) rowView.findViewById(R.id.txtEpisodeDate);

        if (!read[position]){
            final ImageButton read = (ImageButton) rowView.findViewById(R.id.imgRead);
            read.setVisibility(View.GONE);
        }


        ImageButton btnPlay = (ImageButton) rowView.findViewById(R.id.btnPlayEpisode);
        ImageButton btnDownload = (ImageButton) rowView.findViewById(R.id.btnDownloadEpisode);


        if (imageLinks[position] != null) {
            try {
                episodeImage = new ImageDownloader().execute(imageLinks[position]).get();
                if (episodeImage != null) {
                    epImage.setImageBitmap(episodeImage);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

        epTitle.setText(titles[position]);
        epDesc.setText(descs[position]);
        epDuration.setText(durations[position]);
        epDate.setText(dates[position]);
        //epDate.setText(Helper.dateToTimeago(dates));

        final int podChoice = position;

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,context.getString(R.string.clicked_on) + titles[podChoice],Toast.LENGTH_SHORT).show();
                //try to stream for now
                File file = new File(context.getExternalCacheDir(),titles[podChoice] + ".mp3");
                if(file.exists() && !file.isDirectory()) {
                    listener.episodeClick(titles[podChoice], context, mp3Links[podChoice], podChoice);
                } else {
                    listener.streamEpisode(titles[podChoice],context,mp3Links[podChoice],podChoice);
                }


            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, R.string.clicked_on_download,Toast.LENGTH_SHORT).show();
                listener.episodeClick(titles[podChoice],context,mp3Links[podChoice],podChoice);

            }
        });



        return rowView;
    }

    public void displayMessage(final int podIndex){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.stream_episode);
        builder.setMessage("Episode has not been downloadeed, download now or stream?");

        builder.setPositiveButton("Stream now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.streamEpisode(titles[podIndex],context,mp3Links[podIndex],podIndex);
            }
        });

        builder.setNegativeButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.episodeClick(titles[podIndex],context,mp3Links[podIndex],podIndex);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class ImageDownloader extends AsyncTask<String,Integer,Bitmap>{
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
}
