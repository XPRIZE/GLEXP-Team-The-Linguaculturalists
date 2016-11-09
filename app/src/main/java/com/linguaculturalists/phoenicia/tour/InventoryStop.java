package com.linguaculturalists.phoenicia.tour;

import com.linguaculturalists.phoenicia.components.LetterSprite;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.components.WordSprite;
import com.linguaculturalists.phoenicia.locale.tour.Message;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.locale.tour.Tour;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.ui.DebugHUD;
import com.linguaculturalists.phoenicia.ui.DefaultHUD;
import com.linguaculturalists.phoenicia.ui.InventoryHUD;
import com.linguaculturalists.phoenicia.ui.PhoeniciaHUD;

/**
 * Created by mhall on 9/8/16.
 */
public class InventoryStop extends Stop {

    private static final int MSG_MAPBLOCK = 0;
    private static final int MSG_HUD = 1;
    public InventoryStop(Tour tour) {
        super(tour);
    }

    public void start(TourOverlay overlay) {
        this.currentMessageIndex = -1;
        this.overlay = overlay;
    }

    public void show(int messageIndex) {
        switch (messageIndex) {
            case MSG_MAPBLOCK:
                this.showMapBlock();
                break;
            case MSG_HUD:
                this.showHud();
                break;
            default:
                this.close();
        }
    }

    private void showMapBlock() {
        this.overlay.focusOn(this.getFocus(), new MapBlockSprite.OnClickListener() {
            @Override
            public void onClick(MapBlockSprite pPlacedBlockSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                next();
            }

            @Override
            public void onHold(MapBlockSprite pPlacedBlockSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {

            }
        });
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_CENTER);
        this.overlay.showGuide();
        this.overlay.showMessage(this.getMessage(MSG_MAPBLOCK));
    }

    private void showHud() {
        this.overlay.clearFocus();
        final InventoryStop stop = this;
        if (Inventory.getInstance().items().size() < 1) {
            Inventory.getInstance().add(this.tour.game.getCurrentLevel().letters.get(0).name, 1);
        }
        PhoeniciaHUD hud = new InventoryHUD(this.tour.game) {
            @Override
            protected void sellWord(WordSprite block) {
                super.sellWord(block);
                stop.next();
            }

            @Override
            protected void sellLetter(LetterSprite block) {
                super.sellLetter(block);
                stop.next();
            }

            @Override
            public void finish() {
                return;
            }
        };
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
        this.overlay.setManagedHUD(hud);
        this.overlay.showMessage(this.getMessage(MSG_HUD));
    }

    @Override
    public void onClicked() {
        return;
    }
}
