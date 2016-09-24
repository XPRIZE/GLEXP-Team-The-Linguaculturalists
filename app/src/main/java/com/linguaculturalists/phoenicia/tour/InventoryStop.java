package com.linguaculturalists.phoenicia.tour;

import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.locale.tour.Tour;
import com.linguaculturalists.phoenicia.ui.DebugHUD;
import com.linguaculturalists.phoenicia.ui.DefaultHUD;
import com.linguaculturalists.phoenicia.ui.InventoryHUD;

/**
 * Created by mhall on 9/8/16.
 */
public class InventoryStop extends Stop {

    public InventoryStop(Tour tour) {
        super(tour);
    }

    public void run(TourOverlay overlay) {
        this.overlay = overlay;
        this.overlay.setBackgroundHUD(new DefaultHUD(this.tour.game));

        this.moveCameraTo(this.getFocus());

        this.overlay.showGuide();
        this.overlay.nextMessage();
    }

    public void next() {
        this.overlay.setBackgroundHUD(new InventoryHUD(this.tour.game));
        this.overlay.nextMessage();
    }

    public void close() {
        this.tour.game.hudManager.clear();
    }
}
