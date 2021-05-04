package com.example.vision01;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * @author Yasir.Ali
 *
 */
public class OpenRader extends Activity {

    public static RadarView mRadarView = null;
    public static TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rader);

        mRadarView = (RadarView) findViewById(R.id.radarView);
        mRadarView.setShowCircles(true);
        mRadarView.alpha=0;
        textView = (TextView) findViewById(R.id.textView4);

        mRadarView.target_alpha = 90;
        if (mRadarView != null) mRadarView.startAnimation();
    }

}