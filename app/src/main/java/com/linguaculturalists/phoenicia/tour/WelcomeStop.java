package com.linguaculturalists.phoenicia.tour;

import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.locale.tour.Tour;
import com.linguaculturalists.phoenicia.ui.DefaultHUD;

import org.andengine.entity.modifier.MoveXModifier;

/**
 * Created by mhall on 9/8/16.
 */
public class WelcomeStop extends Stop {

    public WelcomeStop(Tour tour) {
        super(tour);
    }

    public void run(TourOverlay overlay) {
        this.overlay = overlay;
        overlay.showGuide();
        overlay.nextMessage();
    }

    public void next() {
        this.overlay.nextMessage();
    }
    public void close() {

    }
}
