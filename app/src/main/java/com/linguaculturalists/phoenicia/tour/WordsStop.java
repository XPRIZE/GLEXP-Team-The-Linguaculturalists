package com.linguaculturalists.phoenicia.tour;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.locale.tour.Tour;
import com.linguaculturalists.phoenicia.models.Builder;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.WordBuilder;
import com.linguaculturalists.phoenicia.models.WordTile;
import com.linguaculturalists.phoenicia.models.WordTileBuilder;
import com.linguaculturalists.phoenicia.ui.WordBuilderHUD;
import com.linguaculturalists.phoenicia.ui.WordPlacementHUD;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.sprite.TiledSprite;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 10/26/16.
 */
public class WordsStop extends Stop {

    public WordTile wordTile;
    private static final int MSG_LAUNCHER = 0;
    private static final int MSG_HUD = 1;
    private static final int MSG_PLACEMENT = 2;
    private static final int MSG_BUILDING = 3;
    private static final int MSG_SPELLING = 4;
    private static final int MSG_CREATING = 5;
    private static final int MSG_COLLECTION = 6;

    public WordsStop(Tour tour) {
        super(tour);
    }

    @Override
    public void onClicked() {
        if (this.currentMessageIndex == MSG_LAUNCHER ||
                this.currentMessageIndex != MSG_CREATING) {
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
            case MSG_LAUNCHER:
                this.showWordPlacementLauncher();
                break;
            case MSG_HUD:
                this.showWordPlacementHUD();
                break;
            case MSG_PLACEMENT:
                this.showWordPlacement();
                break;
            case MSG_BUILDING:
                this.showWordBuilding();
                break;
            case MSG_SPELLING:
                this.showWordSpelling();
                break;
            case MSG_CREATING:
                this.showWordCreation();
                break;
            case MSG_COLLECTION:
                this.showWordCollection();
                break;
            default:
                this.close();
        }
    }

    private void showWordPlacementLauncher() {
        this.overlay.showGuide();
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_BOTTOM_RIGHT);
        this.overlay.showMessage(this.getMessage(MSG_LAUNCHER), TourOverlay.MessageBox.Top);
    }

    private void showWordPlacementHUD() {
        final WordsStop stop = this;
        final WordPlacementHUD hud = new WordPlacementHUD(this.tour.game, this.tour.game.locale.level_map.get(this.tour.game.current_level)) {
            @Override
            protected void addWordTile(final Word word, final TMXTile onTile) {

                stop.wordTile = new WordTile(tour.game, word);

                stop.wordTile.isoX.set(onTile.getTileColumn());
                stop.wordTile.isoY.set(onTile.getTileRow());

                tour.game.createWordSprite(stop.wordTile, new PhoeniciaGame.CreateWordSpriteCallback() {
                    @Override
                    public void onWordSpriteCreated(WordTile tile) {
                        WordTileBuilder builder = new WordTileBuilder(tour.game.session, stop.wordTile, stop.wordTile.item_name.get(), word.construct);
                        builder.start();
                        builder.save(PhoeniciaContext.context);
                        tour.game.addBuilder(builder);

                        stop.wordTile.setBuilder(builder);
                        stop.wordTile.save(PhoeniciaContext.context);
                        stop.goTo(MSG_BUILDING);
                    }

                    @Override
                    public void onWordSpriteCreationFailed(WordTile tile) {
                        stop.goTo(MSG_HUD);
                    }
                });
                Debug.d("Going to next stop page");
                next();
            }
        };
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
        this.overlay.setManagedHUD(hud);
        this.overlay.showMessage(this.getMessage(MSG_HUD), TourOverlay.MessageBox.Top);
    }

    private void showWordPlacement() {
        this.overlay.clearManagedHUD();
        this.overlay.clearSpotlight();
        this.overlay.showMessage(this.getMessage(MSG_PLACEMENT), TourOverlay.MessageBox.Top);
    }

    private void showWordBuilding() {
        Builder wordTileBuilder = this.wordTile.getBuilder(PhoeniciaContext.context);
        wordTileBuilder.update(wordTileBuilder.time.get());
        this.overlay.focusOn(this.wordTile.sprite, new MapBlockSprite.OnClickListener() {
            @Override
            public void onClick(MapBlockSprite pPlacedBlockSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                next();
            }

            @Override
            public void onHold(MapBlockSprite pPlacedBlockSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {

            }
        });
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_CENTER);
        this.overlay.showMessage(this.getMessage(MSG_BUILDING), TourOverlay.MessageBox.Bottom);
    }

    private void showWordSpelling() {
        final WordsStop stop = this;
        this.overlay.clearFocus();
        // Make sure we have enough letters
        for (char c : this.wordTile.word.chars) {
            Inventory.getInstance().add(String.valueOf(c), 1);
        }
        final WordBuilderHUD hud = new WordBuilderHUD(this.tour.game, this.tour.game.getCurrentLevel(), this.wordTile) {
            @Override
            protected void createWord() {
                super.createWord();
                next();
            }
        };
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
        this.overlay.setManagedHUD(hud);
        this.overlay.showMessage(this.getMessage(MSG_SPELLING), TourOverlay.MessageBox.Top);
    }

    private void showWordCreation() {
        this.overlay.clearManagedHUD();
        for (WordBuilder b : this.wordTile.getQueue()) {
            if (b.status.get() != Builder.COMPLETE) {
                b.update(b.time.get());
            }
        }
        this.overlay.focusOn(this.wordTile.sprite, new MapBlockSprite.OnClickListener() {
            @Override
            public void onClick(MapBlockSprite pPlacedBlockSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                next();
            }

            @Override
            public void onHold(MapBlockSprite pPlacedBlockSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {

            }
        });
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_CENTER);
        this.overlay.showMessage(this.getMessage(MSG_CREATING), TourOverlay.MessageBox.Top);
    }

    private void showWordCollection() {
        final WordsStop stop = this;
        this.overlay.clearFocus();
        final WordBuilderHUD hud = new WordBuilderHUD(this.tour.game, this.tour.game.getCurrentLevel(), this.wordTile) {
            @Override
            protected void collectWord(final TiledSprite wordSprite, final WordBuilder builder) {
                super.collectWord(wordSprite, builder);
                next();
            }

            @Override
            public void putChar(Letter letter) {
                // Don't allow spelling
                return;
            }

            @Override
            public void finish() {
                overlay.clearManagedHUD();
            }
        };
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
        this.overlay.setManagedHUD(hud);
        this.overlay.showMessage(this.getMessage(MSG_COLLECTION), TourOverlay.MessageBox.Top);
    }
}
