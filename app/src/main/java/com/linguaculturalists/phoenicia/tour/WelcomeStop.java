package com.linguaculturalists.phoenicia.tour;

import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.locale.tour.Tour;
import com.linguaculturalists.phoenicia.ui.DefaultHUD;
import com.linguaculturalists.phoenicia.ui.LetterPlacementHUD;

import org.andengine.entity.modifier.MoveXModifier;

/**
 * Created by mhall on 9/8/16.
 */
public class WelcomeStop extends Stop {

    public WelcomeStop(Tour tour) {
        super(tour);
    }

    public void start(TourOverlay overlay) {
        this.overlay = overlay;
        this.currentMessageIndex = -1;
    }

    public void show(int messageIndex) {
        switch (messageIndex) {
            case 0:
                overlay.setBackgroundHUD(new DefaultHUD(this.tour.game));
                this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
                overlay.showGuide();
                overlay.showMessage(this.getMessage(messageIndex), TourOverlay.MessageBox.Bottom);
                break;
            case 1:
                this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_BOTTOM_RIGHT);
                this.overlay.showMessage(this.getMessage(messageIndex), TourOverlay.MessageBox.Top);
                break;
            case 2:
                this.overlay.attachChild(new LetterPlacementHUD(this.tour.game, this.tour.game.locale.level_map.get(this.tour.game.current_level)));
                this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
                this.overlay.showMessage(this.getMessage(messageIndex), TourOverlay.MessageBox.Top);
                break;
            default:
                this.close();


        }
    }
    public void close() {
        this.tour.game.hudManager.clear();
    }
}
