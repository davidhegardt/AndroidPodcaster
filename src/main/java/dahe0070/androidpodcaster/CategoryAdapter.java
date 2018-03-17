package dahe0070.androidpodcaster;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Dave on 2017-09-03.
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context context;
    private List<Category> catList;
    private String baseURLCat = "https://itunes.apple.com/search?term=podcast";
    private String country = "&country=";
    private String codeSweden = "SE";
    private String codeUK = "GB";
    private String limit = "&limit=30";

    public CategoryAdapter(Context ctx,List<Category> list){
        this.catList = list;
        this.context = ctx;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_card,parent,false);


        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Category currCategory = catList.get(position);
        holder.title.setText(currCategory.title);
        holder.thumb.setImageResource(currCategory.image);

        holder.thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = catList.get(position).searchID;
                createQuery(category);
            }
        });

    }

    public void createQuery(String category){
        String search = baseURLCat + country + codeSweden + category + limit;

        FragmentManager fm = ((MainActivity)context).getSupportFragmentManager();

        SearchFragment searchFragment = (SearchFragment) fm.findFragmentByTag("search");
        searchFragment.search(search);
    }

    @Override
    public int getItemCount() {
        return catList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public ImageButton thumb;

        public ViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.txtTitleCategory);
            thumb = (ImageButton) itemView.findViewById(R.id.btnImageCategory);


        }
    }

}
