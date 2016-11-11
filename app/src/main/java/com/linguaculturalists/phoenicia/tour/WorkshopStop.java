package com.linguaculturalists.phoenicia.tour;

import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.locale.tour.Tour;
import com.linguaculturalists.phoenicia.models.DefaultTile;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.WordBuilder;
import com.linguaculturalists.phoenicia.models.WorkshopBuilder;
import com.linguaculturalists.phoenicia.ui.WordBuilderHUD;
import com.linguaculturalists.phoenicia.ui.WorkshopHUD;

import org.andengine.input.touch.TouchEvent;
import org.andengine.util.debug.Debug;

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
        Debug.d("WorkshopStop clicked");
        if (this.currentMessageIndex != MSG_MAPBLOCK && this.currentMessageIndex != MSG_SPELLING) {
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
        this.overlay.focusOn(this.getFocus(), new MapBlockSprite.OnClickListener() {
            @Override
            public void onClick(MapBlockSprite pPlacedBlockSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                next();
            }

            @Override
            public void onHold(MapBlockSprite pPlacedBlockSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {

            }
        });
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
        this.overlay.showGuide();
        this.overlay.showMessage(this.getMessage(MSG_MAPBLOCK), TourOverlay.MessageBox.Top);
    }

    private void showWorkshopHud() {
        this.overlay.clearFocus();
        Word word = this.tour.game.getCurrentLevel().words.get(this.tour.game.getCurrentLevel().words.size()-1);
        Debug.d("Adding chars for "+word.name);
        for (char c : word.chars) {
            Inventory.getInstance().add(String.valueOf(c), 1);
        }
        final WorkshopStop stop = this;
        WorkshopHUD hud = new WorkshopHUD(this.tour.game, this.tile) {
            @Override
            public WorkshopBuilder createWord(Word word, DefaultTile tile) {
                stop.next();
                return super.createWord(word, tile);
            }

            @Override
            public void finish() {
                return;
            }

            @Override
            public boolean onSceneTouchEvent(TouchEvent pSceneTouchEvent) {
                if (currentMessageIndex == MSG_HUD && pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
                    next();
                    return true;
                }
                return super.onSceneTouchEvent(pSceneTouchEvent);
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
        this.overlay.clearManagedHUD();
        this.overlay.showMessage(this.getMessage(MSG_WARN), TourOverlay.MessageBox.Bottom);
    }

    public void setFocus(DefaultTile tile) {
        this.tile = tile;
        this.setFocus(tile.sprite);
    }
}
