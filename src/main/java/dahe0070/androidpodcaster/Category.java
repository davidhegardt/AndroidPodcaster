package dahe0070.androidpodcaster;

/**
 * Created by Dave on 2017-09-03.
 */

public class Category {

    String title;
    String searchID;
    int image;

    public Category(String title,String searchID,int resId){
        this.title = title;
        this.searchID = searchID;
        this.image = resId;
    }

}
