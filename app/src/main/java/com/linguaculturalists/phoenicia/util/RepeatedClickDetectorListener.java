package com.linguaculturalists.phoenicia.util;

import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 6/13/16.
 */
public abstract class RepeatedClickDetectorListener implements ClickDetector.IClickDetectorListener {
    private int clicksToTrigger;
    private long timeToTrigger;

    private int clickCount;
    private long firstClick;

    public RepeatedClickDetectorListener(int clicksToTrigger, long timeToTrigger) {
        this.clicksToTrigger = clicksToTrigger;
        this.timeToTrigger = timeToTrigger;

        this.clickCount = 0;
        this.firstClick = 0;
    }
    @Override
    public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
        long newTime = System.currentTimeMillis();
        if (newTime - firstClick > timeToTrigger) {
            clickCount = 0;
            firstClick = newTime;
        }
        clickCount++;

        Debug.d("Click count: "+clickCount);
        if (clickCount >= clicksToTrigger) {
            this.onRepeatedClick(clickDetector, i, v, v1);
            clickCount = 0;
        }
    }

    public abstract void onRepeatedClick(ClickDetector clickDetector, int i, float v, float v1);
}
