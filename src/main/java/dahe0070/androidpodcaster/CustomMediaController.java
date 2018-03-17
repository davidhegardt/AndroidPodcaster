package dahe0070.androidpodcaster;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;

/**
 * Created by Dave on 2017-08-23.
 */

public class CustomMediaController extends MediaController {

    private Context ctx;
    private String displayText;
    private TextView newText;
    private ImageButton showPlayer;

    public CustomMediaController(Context context, String info) {
        super(context);
        ctx = context;
        displayText = info;
    }

    /**
     * Remove the controller from the screen.
     */
    @Override
    public void hide() {
        super.hide();
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     * When VideoView calls this method, it will use the VideoView's parent
     * as the anchor.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

        newText = new TextView(ctx);
        newText.setText(displayText);
        newText.setTextColor(Color.WHITE);
        newText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        newText.setSelected(true);
        newText.setSingleLine(true);
        newText.setMarqueeRepeatLimit(-1);
        newText.setFocusableInTouchMode(true);
        newText.setFocusable(true);
        newText.setWidth(300);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        addView(newText,params);
    }

    public void updateText(String podName,String epTitle){
        displayText = podName + " - " + epTitle;
        newText.setText(displayText);
    }
}
