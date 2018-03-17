package dahe0070.androidpodcaster;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

/**
 * Created by Dave on 2017-08-28.
 */

public class SearchViewAdapter extends RecyclerView.Adapter<Search_View_Holder>  {

    List<Podcast> list = Collections.emptyList();
    Context context;

    public SearchViewAdapter(List<Podcast> list,Context ctx) {
        this.list = list;
        this.context = ctx;
    }

    @Override
    public Search_View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rowViev = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_each_result,parent,false);

        Search_View_Holder holder = new Search_View_Holder(rowViev);
        return holder;
    }


    @Override
    public void onBindViewHolder(Search_View_Holder holder, int position) {
        holder.bind(context);
        holder.podName.setText(list.get(position).getPodName());

        holder.feed.setText(list.get(position).getFeedLink());

        if(list.get(position).getFeedLink() == ""){
            holder.subscribe.setVisibility(View.INVISIBLE);
        }

        holder.backupImage = list.get(position).getPodImageUrl();

        Picasso.with(context).load(list.get(position).getPodImageUrl()).resize(210, 210).centerCrop().into(holder.image);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
