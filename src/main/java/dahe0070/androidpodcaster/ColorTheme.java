package dahe0070.androidpodcaster;

/**
 * Created by Dave on 2018-04-15.
 */

public class ColorTheme {

    private String colorName;
    private int colorID;
    private String switchColorName;

    public ColorTheme(String colorName, String switchColorName) {
        this.colorName = colorName;
        this.switchColorName = switchColorName;
    }

    public ColorTheme(String colorName) {
        this.colorName = colorName;
    }

    public ColorTheme(String colorName, int colorID) {
        this.colorName = colorName;
        this.colorID = colorID;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public int getColorID() {
        return colorID;
    }

    public void setColorID(int colorID) {
        this.colorID = colorID;
    }

    public String getSwitchColorName() {
        return switchColorName;
    }

    public void setSwitchColorName(String switchColorName) {
        this.switchColorName = switchColorName;
    }
}
