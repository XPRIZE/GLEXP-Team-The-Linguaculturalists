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

    private static final int MSG_MAPBLOCK = 0;
    private static final int MSG_SELLING = 1;
    public InventoryStop(Tour tour) {
        super(tour);
    }

    public void start(TourOverlay overlay) {
        this.overlay = overlay;
    }

    public void show(int messageIndex) {
        Message msg = this.getMessage(messageIndex);
        switch (messageIndex) {
            case 0:
                this.showMapBlock();
                break;
            case 1:
                this.showSelling();
                break;
            default:
                this.close();
        }
    }

    private void showMapBlock() {
        this.overlay.focusOn(this.getFocus());
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_CENTER);
        this.overlay.showGuide();
        this.overlay.showMessage(this.getMessage(MSG_MAPBLOCK));
    }

    private void showSelling() {
        this.overlay.clearFocus();
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
        this.overlay.setManagedHUD(new InventoryHUD(this.tour.game));
        this.overlay.showMessage(this.getMessage(MSG_SELLING));
    }

    @Override
    public void onClicked() {
        if (this.currentMessageIndex != MSG_SELLING) {
            this.next();
        }
    }
}
