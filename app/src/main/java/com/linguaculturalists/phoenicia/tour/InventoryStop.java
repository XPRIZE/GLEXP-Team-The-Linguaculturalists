package com.linguaculturalists.phoenicia.tour;

import com.linguaculturalists.phoenicia.locale.tour.Message;
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

    public void start(TourOverlay overlay) {
        this.overlay = overlay;
        this.currentMessageIndex = -1;
    }

    public void show(int messageIndex) {
        Message msg = this.getMessage(messageIndex);
        switch (messageIndex) {
            case 0:
                this.overlay.setBackgroundHUD(new DefaultHUD(this.tour.game));
                this.overlay.focusOn(this.getFocus());
                this.overlay.showGuide();
                this.overlay.showMessage(msg);
                break;
            case 1:
                this.overlay.clearFocus();
                this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
                this.overlay.attachChild(new InventoryHUD(this.tour.game));
                this.overlay.showMessage(msg);
                break;
            default:
                this.close();
        }
    }

    public void close() {
        this.tour.game.hudManager.clear();
    }
}
