package dahe0070.androidpodcaster;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;

/**
 * Created by Dave on 2018-03-04.
 */



public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

    final int PAGE_COUNT = 8;
    private Context context;
    private String tabTitles[];
    private String comedyID = "&genreId=1303";
    private String tvfilmdID = "&genreId=1309";
    private String scienceID = "&genreId=1315";
    private String techID = "&genreId=1318";
    private String businessID = "&genreId=1321";
    private String gamesID = "&genreId=1323";
    private String educationID = "&genreId=1304";
    private String policitsID = "&genredId=1311";
    private String societyID = "&genreId=1324";
    private ArrayList<Category> categoryList;
    private ArrayList<PodEpisode> epList;
    private Podcast currPod;
    private Fragment testFirstFragment;


    public FragmentPagerAdapter(FragmentManager fm, Context ctx) {
        super(fm);
        this.context = ctx;
        setupCategories();
    }

    public void setEpList(ArrayList<PodEpisode> incoming){
        this.epList = incoming;
    }

    public void setcurrPodcast(Podcast incomingPod){
        this.currPod = incomingPod;
    }

    @Override
    public int getItemPosition(Object object) {
        //return super.getItemPosition(object);
        if (object instanceof CategoryFragment){
            return POSITION_UNCHANGED;
        } else if (object instanceof NewSearchFragment){
            return POSITION_UNCHANGED;
        } else {
            return POSITION_NONE;
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0 : return NewSearchFragment.newInstance();

            case 1 : return CategoryFragment.newInstance(position + 1,categoryList.get(0));

            case 2 : return CategoryFragment.newInstance(position + 1,categoryList.get(1));

            case 3 : return CategoryFragment.newInstance(position + 1,categoryList.get(2));

            case 4 : return CategoryFragment.newInstance(position + 1, categoryList.get(3));

            case 5 : return CategoryFragment.newInstance(position + 1, categoryList.get(4));

            case 6 : return CategoryFragment.newInstance(position + 1,categoryList.get(5));

            case 7 : return CategoryFragment.newInstance(position + 1,categoryList.get(6));

            case 8 : return CategoryFragment.newInstance(position + 1, categoryList.get(7));

            case 10 :  testFirstFragment = EpisodesFragment.newInstance(epList,currPod,true); notifyDataSetChanged(); return testFirstFragment;
            // Instantiate adapterMode here

            default: return NewSearchFragment.newInstance();

        }
        /*
        if(position == 0){
            return NewSearchFragment.newInstance();
        }
        */

        //return CategoryFragment.newInstance(position + 1,"another tab");
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    private void setupCategories(){
        categoryList = new ArrayList<>();

        Category comedy = new Category(context.getString(R.string.Comedy_cat),comedyID,R.drawable.comedy_icon_white);
        Category film = new Category(context.getString(R.string.film_cat),tvfilmdID,R.drawable.cinema_icon_white);
        Category science = new Category(context.getString(R.string.science_cat),scienceID,R.drawable.science_icon_white);
        Category tech = new Category(context.getString(R.string.tech_cat),techID,R.drawable.tech_icon_white);
        Category business = new Category(context.getString(R.string.business_cat),businessID,R.drawable.business_icon_white);
        Category games = new Category(context.getString(R.string.games_cat),gamesID,R.drawable.games_icon_white);
        Category education = new Category(context.getString(R.string.education_cat),educationID,R.drawable.tech_icon_white);
        Category politics = new Category(context.getString(R.string.politcs_cat),policitsID,R.drawable.tech_icon_white);
        Category society = new Category(context.getString(R.string.society_cat),societyID,R.drawable.tech_icon_white);

        categoryList.add(comedy);
        categoryList.add(film);
        categoryList.add(science);
        categoryList.add(tech);
        categoryList.add(business);
        categoryList.add(games);
        categoryList.add(education);
        categoryList.add(politics);
        categoryList.add(society);

        tabTitles = new String[] { context.getString(R.string.search),comedy.title, film.title, science.title, tech.title, business.title, games.title, education.title, politics.title,society.title };

    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
