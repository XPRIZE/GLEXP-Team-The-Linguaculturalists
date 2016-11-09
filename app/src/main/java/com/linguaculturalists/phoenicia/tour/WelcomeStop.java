package com.linguaculturalists.phoenicia.tour;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.locale.tour.Tour;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.LetterBuilder;
import com.linguaculturalists.phoenicia.models.LetterTile;
import com.linguaculturalists.phoenicia.ui.DefaultHUD;
import com.linguaculturalists.phoenicia.ui.LetterPlacementHUD;
import com.linguaculturalists.phoenicia.ui.PhoeniciaHUD;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 9/8/16.
 */
public class WelcomeStop extends Stop {

    public LetterTile letterTile;
    private static final int MSG_WELCOME = 0;
    private static final int MSG_LAUNCHER = 1;
    private static final int MSG_HUD = 2;
    private static final int MSG_PLACEMENT = 3;
    private static final int MSG_COLLECTION = 4;
    private static final int MSG_REGROW = 5;

    public WelcomeStop(Tour tour) {
        super(tour);
    }

    public void start(TourOverlay overlay) {
        this.overlay = overlay;
        this.currentMessageIndex = -1;
    }

    public void show(int messageIndex) {
        switch (messageIndex) {
            case MSG_WELCOME:
                this.showWelcome();
                break;
            case MSG_LAUNCHER:
                this.showLetterPlacementLauncher();
                break;
            case MSG_HUD:
                this.showLetterPlacementHUD();
                break;
            case MSG_PLACEMENT:
                this.showLetterPlacement();
                break;
            case MSG_COLLECTION:
                this.showLetterCollection();
                break;
            case MSG_REGROW:
                this.showLetterRegrowing();
                break;
            default:
                this.close();
        }
    }

    private void showWelcome() {
        overlay.setBackgroundHUD(new DefaultHUD(this.tour.game));
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
        overlay.showGuide();
        overlay.showMessage(this.getMessage(MSG_WELCOME), TourOverlay.MessageBox.Bottom);
    }

    private void showLetterPlacementLauncher() {
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_BOTTOM_RIGHT);
        this.overlay.showMessage(this.getMessage(MSG_LAUNCHER), TourOverlay.MessageBox.Top);
    }

    private void showLetterPlacementHUD() {
        final WelcomeStop stop = this;
        final LetterPlacementHUD hud = new LetterPlacementHUD(this.tour.game, this.tour.game.locale.level_map.get(this.tour.game.current_level)) {
            @Override
            protected void addLetterTile(final Letter letter, final TMXTile onTile) {

                stop.letterTile = new LetterTile(tour.game, letter);

                stop.letterTile.isoX.set(onTile.getTileColumn());
                stop.letterTile.isoY.set(onTile.getTileRow());

                tour.game.createLetterSprite(stop.letterTile, new PhoeniciaGame.CreateLetterSpriteCallback() {
                    @Override
                    public void onLetterSpriteCreated(LetterTile tile) {
                        LetterBuilder builder = new LetterBuilder(tour.game.session, stop.letterTile, stop.letterTile.item_name.get(), letter.time);
                        builder.start();
                        builder.save(PhoeniciaContext.context);
                        tour.game.addBuilder(builder);

                        stop.letterTile.setBuilder(builder);
                        stop.letterTile.save(PhoeniciaContext.context);
                        stop.goTo(MSG_COLLECTION);
                    }

                    @Override
                    public void onLetterSpriteCreationFailed(LetterTile tile) {
                        stop.goTo(MSG_HUD);
                    }
                });
                Debug.d("Going to next stop page");
                next();
            }

            @Override
            public void finish() {
                //overlay.clearManagedHUD();
            }
        };
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
        this.overlay.setManagedHUD(hud);
        this.overlay.showMessage(this.getMessage(MSG_HUD), TourOverlay.MessageBox.Top);
    }

    private void showLetterPlacement() {
        Debug.d("Clearing managed hud and spotlight");
        this.overlay.clearManagedHUD();
        this.overlay.clearSpotlight();
        this.overlay.showMessage(this.getMessage(MSG_PLACEMENT), TourOverlay.MessageBox.Top);
    }

    private void showLetterCollection() {
        Debug.d("Focusing on newly placed block");
        LetterBuilder tileBuilder = this.letterTile.getBuilder(PhoeniciaContext.context);
        tileBuilder.update(tileBuilder.time.get());
        this.overlay.focusOn(this.letterTile.sprite, new MapBlockSprite.OnClickListener() {
            @Override
            public void onClick(MapBlockSprite pPlacedBlockSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                Debug.d("Focused sprite clicked");
                letterTile.collect();
                next();
            }

            @Override
            public void onHold(MapBlockSprite pPlacedBlockSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {

            }
        });
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_CENTER);
        this.overlay.showMessage(this.getMessage(MSG_COLLECTION), TourOverlay.MessageBox.Bottom);
    }

    private void showLetterRegrowing() {
        //this.overlay.focusOn(this.letterTile.sprite);
        //this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_CENTER);
        this.overlay.clearFocus();
        this.overlay.showMessage(this.getMessage(MSG_REGROW), TourOverlay.MessageBox.Bottom);
        this.letterTile.onClick(null, 0, 0);

    }

    @Override
    public void onClicked() {
        Debug.d("Welcome stop clicked");
        if (this.currentMessageIndex != MSG_HUD && this.currentMessageIndex != MSG_COLLECTION) {
            Debug.d("Welcome stop click triggered next message");
            this.next();
        }
    }
}
