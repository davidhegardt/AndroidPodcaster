package dahe0070.androidpodcaster;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dave on 2017-08-13.
 */

public class PodcastViewAdapter extends ArrayAdapter<String> implements Filterable,View.OnCreateContextMenuListener {

    private final Context context;
    private final String[] titles;
    //private final String[] descriptions;
//    private final String[] categories;
    //private final Bitmap[] testImages;
    private PlayerClicks listener;
    private SearchFilter searchFilter;
    private List<Podcast> podcastList;
    private List<Podcast> filteredList;
    private boolean isTextViewClicked = false;
    private boolean clicked = false;



    public PodcastViewAdapter(Context ctx, List<Podcast> currPodList,String[] titles) {
        super(ctx,-1,titles);
        this.context = ctx;
        podcastList = currPodList;
        this.titles = titles;
        this.filteredList = podcastList;
        listener = (PlayerClicks) ctx;

    }

    public void swapData(List<Podcast> newPodList){
        podcastList.clear();
        List<Podcast> newList = new ArrayList<>();
        newList = newPodList;
        podcastList.addAll(newList);
        notifyDataSetChanged();
    }

    public void addData(Podcast newPodcast){
        podcastList.add(newPodcast);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //super.getView(position, convertView, parent);

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.listview_each_podcast,parent,false);
        Podcast pod = getFiltered(position);

        final TextView title = (TextView) rowView.findViewById(R.id.txtPodTitle);
        final TextView desc = (TextView) rowView.findViewById(R.id.txtPodDesc);
        final TextView category = (TextView) rowView.findViewById(R.id.txtPodCategory);
        ImageButton readMore = (ImageButton) rowView.findViewById(R.id.btnReadMore);

        title.setText(pod.getPodName());
        Log.i("English pod loading: ", "" + pod.getPodName());
        Log.i("Image URL;", "" + pod.getPodImageUrl());
        desc.setText(Html.fromHtml(pod.getGeneralDesc()));
       // category.setText(categories[position]);
        category.setText(R.string.Podcast);

        final ImageButton podImage = (ImageButton) rowView.findViewById(R.id.imgPodcast);

        String podURL = pod.getPodImageUrl();

        if (pod.getPodImageUrl() != null){
            try {
                Picasso.with(context).load(pod.getPodImageUrl()).resize(210, 210).centerCrop().into(podImage);
            } catch (IllegalArgumentException ie){
                ie.printStackTrace();
                Picasso.with(context).load(pod.getBackupImage()).resize(210,210).centerCrop().into(podImage);
                Log.i("POD CAUSING ISSUES","" + pod.getPodName());
            }
        }
/*

        Log.i("Position","" + position);
        if (position % 2 == 0){

            podImage.setImageResource(R.drawable.dummy_image_2);
        }
*/
        final int podChoice = position;

        final int index = podcastList.indexOf(pod);
        final String podname = pod.getPodName();

        Log.i("desc lenght","" + pod.getGeneralDesc().length() + "for" + pod.getPodName());
        if (pod.getGeneralDesc().length() < 141){
            readMore.setVisibility(View.INVISIBLE);
        }



        readMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTextViewClicked){
                    //This will shrink textview to 2 lines if it is expanded.
                    desc.setMaxLines(5);
                    isTextViewClicked = false;
                } else {
                    //This will expand the textview if it is of 2 lines
                    desc.setMaxLines(Integer.MAX_VALUE);
                    isTextViewClicked = true;
                }
            }
        });


        podImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clicked) {
                    clicked = true;
                    //Toast.makeText(context, "Clicked on " + podname, Toast.LENGTH_SHORT).show();
                    listener.podClick(index);
                } else {
                    Toast.makeText(context, "Loading..STOP CLICKING!",Toast.LENGTH_SHORT).show();
                }
            }
        });




        desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!clicked) {
                    clicked = true;
                    //Toast.makeText(context, "Clicked on" + titles[podChoice], Toast.LENGTH_SHORT).show();
                    listener.podClick(podChoice);
                } else {
                    Toast.makeText(context, "Loading..STOP CLIKING!!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        rowView.setOnCreateContextMenuListener(this);
        return rowView;

    }



    @Override
    public int getCount() {
        //return super.getCount();
        return filteredList.size();
    }

    public Podcast getFiltered(int i){
        return filteredList.get(i);
    }



    @Override
    public Filter getFilter() {
        //return super.getFilter();
        if (searchFilter == null){
            searchFilter = new SearchFilter();
        }

        return searchFilter;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //
    }

    private class SearchFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            //return null;
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0){
                ArrayList<Podcast> tempList = new ArrayList<Podcast>();

                for (Podcast podcast : podcastList){
                    if(podcast.getPodName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(podcast);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = podcastList.size();
                filterResults.values = podcastList;
            }

            return filterResults;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<Podcast>) results.values;
            //Log.i("filtered List",filteredList.get(0).getPodName());
            //podcastList = filteredList;
            notifyDataSetChanged();
        }
    }

}
