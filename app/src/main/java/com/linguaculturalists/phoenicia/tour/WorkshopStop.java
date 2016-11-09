package com.linguaculturalists.phoenicia.tour;

import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.locale.tour.Tour;
import com.linguaculturalists.phoenicia.models.DefaultTile;
import com.linguaculturalists.phoenicia.models.WorkshopBuilder;
import com.linguaculturalists.phoenicia.ui.WorkshopHUD;

/**
 * Created by mhall on 10/15/16.
 */
public class WorkshopStop extends Stop {

    private DefaultTile tile;
    private static final int MSG_MAPBLOCK = 0;
    private static final int MSG_HUD = 1;
    private static final int MSG_SPELLING = 2;
    private static final int MSG_WARN = 3;

    public WorkshopStop(Tour tour) { super(tour); }

    @Override
    public void onClicked() {
        if (this.currentMessageIndex != MSG_SPELLING) {
            this.next();
        }
    }

    @Override
    public void start(TourOverlay overlay) {
        this.overlay = overlay;
        this.currentMessageIndex = -1;
    }

    @Override
    public void show(int messageIndex) {
        switch (messageIndex) {
            case MSG_MAPBLOCK:
                this.showMapBlock();
                break;
            case MSG_HUD:
                this.showWorkshopHud();
                break;
            case MSG_SPELLING:
                this.showSpelling();
                break;
            case MSG_WARN:
                this.showWarning();
                break;
            default:
                this.close();
        }
    }

    private void showMapBlock() {
        this.overlay.focusOn(this.getFocus());
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_CENTER);
        this.overlay.showGuide();
        this.overlay.showMessage(this.getMessage(MSG_MAPBLOCK), TourOverlay.MessageBox.Bottom);
    }

    private void showWorkshopHud() {
        this.overlay.clearFocus();
        final WorkshopStop stop = this;
        WorkshopHUD hud = new WorkshopHUD(this.tour.game, this.tile) {
            @Override
            public WorkshopBuilder createWord(Word word, DefaultTile tile) {
                stop.next();
                return super.createWord(word, tile);
            }

            @Override
            public void finish() {
                overlay.clearManagedHUD();
            }
        };
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
        this.overlay.setManagedHUD(hud);
        this.overlay.showMessage(this.getMessage(MSG_HUD), TourOverlay.MessageBox.Bottom);
    }

    private void showSpelling() {
        this.overlay.showMessage(this.getMessage(MSG_SPELLING), TourOverlay.MessageBox.Bottom);
    }

    private void showWarning() {
        this.overlay.showMessage(this.getMessage(MSG_WARN), TourOverlay.MessageBox.Bottom);
    }

    public void setFocus(DefaultTile tile) {
        this.tile = tile;
        this.setFocus(tile.sprite);
    }
}
