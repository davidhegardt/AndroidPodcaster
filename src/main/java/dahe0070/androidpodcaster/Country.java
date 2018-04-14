package dahe0070.androidpodcaster;

/**
 * Created by Dave on 2018-03-17.
 */

public class Country {

    private String countryName;
    private String countryCode;

    public Country(){
        countryName = "";
        countryCode = "";
    }

    public Country(String mName){
        this.countryName = mName;
    }

    public Country(String mName,String mCode){
        this.countryName = mName;
        this.countryCode = mCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
