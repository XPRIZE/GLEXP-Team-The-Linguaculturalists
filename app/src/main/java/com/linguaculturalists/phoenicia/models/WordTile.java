package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.components.PlacedBlockSprite;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.ui.SpriteMoveHUD;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;
import com.orm.androrm.migration.Migrator;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.FadeOutModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.modifier.ScaleAtModifier;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.text.Text;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseBackOut;
import org.andengine.util.modifier.ease.EaseLinear;

/**
 * Database model representing a Word tile that has been placed on the map.
 */
public class WordTile extends Model implements Builder.BuildStatusUpdateHandler, IOnAreaTouchListener, MapBlockSprite.OnClickListener {

    public ForeignKeyField<GameSession> game; /**< reference to the GameSession this tile is a part of */
    public ForeignKeyField<WordBuilder> builder; /**< reference to the WordBuilder used by this tile */
    public IntegerField isoX; /**< isometric X coordinate for this tile */
    public IntegerField isoY; /**< isometric Y coordinate for this tile */
    public CharField item_name; /**< name of the InventoryItem this tile produces */

    public PhoeniciaGame phoeniciaGame; /**< active game instance this tile is a part of */
    public Word word; /**< locale Word this tile represents */
    public PlacedBlockSprite sprite;  /**< sprite that has been placed on the map for this tile */

    private boolean isTouchDown = false;
    private WordTileListener eventListener;
    private boolean isCompleted = false;

    public WordTile() {
        super();
        this.game = new ForeignKeyField<GameSession>(GameSession.class);
        this.builder = new ForeignKeyField<WordBuilder>(WordBuilder.class);
        this.isoX = new IntegerField();
        this.isoY = new IntegerField();
        this.item_name = new CharField(32);
    }

    /**
     * Create a new tile for the given word that is a part of the given game
     * @param game active game instance this tile is a part of
     * @param word locale Letter this tile represents
     */
    public WordTile(PhoeniciaGame game, Word word) {
        this();
        this.phoeniciaGame = game;
        this.word = word;
        this.game.set(game.session);
        this.item_name.set(word.name);
    }

    public static final QuerySet<WordTile> objects(Context context) {
        return objects(context, WordTile.class);
    }

    /**
     * Get the Sprite that represents this tile on the map
     * @return sprite, or null if it does not have one
     */
    public PlacedBlockSprite getSprite() {

        return this.sprite;
    }

    /**
     * Attach a Sprite from the map to this tile
     * @param sprite sprite that represents this tile on the map
     */
    public void setSprite(PlacedBlockSprite sprite) {
        this.sprite = sprite;
    }

    /**
     * Get the builder instance that produces a Word from this tile
     * @param context ApplicationContext for use in database calls
     * @return the builder instance, or null if it does not have one
     */
    public WordBuilder getBuilder(Context context) {
        WordBuilder builder = this.builder.get(context);
        if (builder != null) {
            builder.setUpdateHandler(this);
            this.onProgressChanged(builder);
            phoeniciaGame.addBuilder(builder);
        }
        return builder;
    }

    /**
     * Attach a WordBuilder to this tile
     * @param builder used by this tile
     */
    public void setBuilder(WordBuilder builder) {
        builder.setUpdateHandler(this);
        this.builder.set(builder);
        this.onProgressChanged(builder);
    }

    public void onScheduled(Builder buildItem) { Debug.d("WordTile.onScheduled"); this.isCompleted = false; return; }
    public void onStarted(Builder buildItem) { Debug.d("WordTile.onStarted"); this.isCompleted = false; return; }
    public void onCompleted(Builder buildItem) {
        Debug.d("WordTile.onCompleted");
        this.isCompleted = true;
        if (this.eventListener != null) {
            this.eventListener.onWordTileBuildCompleted(this);
        }
        return;
    }

    /**
     * Called when the builder for this tile gets updated.
     * @param builtItem
     */
    public void onProgressChanged(Builder builtItem) {
        if (sprite != null) {
            sprite.setProgress(builtItem.progress.get(), word.construct);
        }
        if (builtItem.progress.get() >= word.construct) {
            builtItem.complete();
        }
        return;
    }

    /**
     * Restart the build progress for this tile
     * @param context ApplicationContext ApplicationContext for use in database calls
     */
    public void reset(Context context) {
        if (this.sprite != null) {
            this.sprite.setProgress(0, word.construct);
        }
        WordBuilder builder = this.getBuilder(context);
        if (builder != null) {
            Debug.d("Resetting WordTile builder");
            builder.progress.set(0);
            builder.start();
            builder.save(context);
            this.phoeniciaGame.addBuilder(builder);
        } else {
            Debug.e("Could not reset WordTile builder, because it was missing");
        }
    }

    @Override
    protected void migrate(Context context) {
        Migrator<WordTile> migrator = new Migrator<WordTile>(WordTile.class);

        // roll out all migrations
        migrator.migrate(context);
        return;
    }

    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
        if (pSceneTouchEvent.isActionDown()) {
            this.isTouchDown = true;
        } else if (isTouchDown && pSceneTouchEvent.isActionUp()) {

            if (this.eventListener != null) {
                this.eventListener.onWordTileClicked(this);
            }
            this.isTouchDown = false;
            return true;
        } else {
            this.isTouchDown = false;
        }
        return false;

    }

    /**
     * Called when the Sprite for this tile is clicked by the player
     * @param buttonSprite
     * @param v
     * @param v2
     */
    public void onClick(MapBlockSprite buttonSprite, float v, float v2) {
        Debug.d("Clicked block: "+String.valueOf(this.word.chars));
        WordBuilder builder = this.getBuilder(PhoeniciaContext.context);
        if (builder != null) {
            if (builder.status.get() == LetterBuilder.COMPLETE) {
                Debug.d("Clicked block was completed");
                phoeniciaGame.hudManager.showWordBuilder(phoeniciaGame.locale.level_map.get(phoeniciaGame.current_level), this.word);
            } else {
                Debug.d("Clicked block was NOT completed");
                // Don't run another modifier animation if one is still running
                if (sprite.getEntityModifierCount() <= 0) {
                    sprite.registerEntityModifier(new ScaleAtModifier(0.5f, sprite.getScaleX(), sprite.getScaleX(), sprite.getScaleY() * 0.7f, sprite.getScaleY(), sprite.getScaleCenterX(), 0, EaseBackOut.getInstance()));

                    final Text progressText = new Text(32, 32, GameFonts.inventoryCount(), String.valueOf(100 * builder.progress.get() / builder.time.get()) + "%", 4, PhoeniciaContext.vboManager);
                    sprite.attachChild(progressText);
                    progressText.registerEntityModifier(new ParallelEntityModifier(
                            new MoveYModifier(0.8f, 32, 64, EaseLinear.getInstance()),
                            new FadeOutModifier(1.0f, new IEntityModifier.IEntityModifierListener() {
                                @Override
                                public void onModifierStarted(IModifier<IEntity> iModifier, IEntity iEntity) {
                                }

                                @Override
                                public void onModifierFinished(IModifier<IEntity> iModifier, IEntity iEntity) {
                                    phoeniciaGame.activity.runOnUpdateThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            sprite.detachChild(progressText);
                                        }
                                    });
                                }
                            }, EaseLinear.getInstance())
                    ));
                }
                //phoeniciaGame.playBlockSound(this.word.sound);
            }
        } else {
            Debug.e("Clicked block has no builder");
        }
    }

    @Override
    public void onHold(MapBlockSprite buttonSprite, float v, float v2) {
        final TMXTile tmxTile = phoeniciaGame.getTileAtIso(this.isoX.get(), this.isoY.get());
        if (tmxTile == null) {
            Debug.d("No tile at "+this.isoX.get()+"x"+this.isoY.get());
            return;
        }
        phoeniciaGame.hudManager.push(new SpriteMoveHUD(phoeniciaGame, tmxTile, sprite, word.columns, word.rows, this.word.restriction, new SpriteMoveHUD.SpriteMoveHandler() {
            @Override
            public void onSpriteMoveCanceled(MapBlockSprite sprite) {
                sprite.setPosition(tmxTile.getTileX()+32, tmxTile.getTileY()+32);
                sprite.setZIndex(tmxTile.getTileZ());
                phoeniciaGame.scene.sortChildren();
            }

            @Override
            public void onSpriteMoveFinished(MapBlockSprite sprite, TMXTile newlocation) {
                isoX.set(newlocation.getTileColumn());
                isoY.set(newlocation.getTileRow());
                // Unset previous sprite location
                for (int c = 0; c < word.columns; c++) {
                    for (int r = 0; r < word.rows; r++) {
                        phoeniciaGame.placedSprites[tmxTile.getTileColumn()-c][tmxTile.getTileRow()-r] = null;
                    }
                }
                // Set new sprite location
                for (int c = 0; c < word.columns; c++) {
                    for (int r = 0; r < word.rows; r++) {
                        phoeniciaGame.placedSprites[newlocation.getTileColumn()-c][newlocation.getTileRow()-r] = sprite;
                    }
                }
                sprite.setZIndex(newlocation.getTileZ());
                phoeniciaGame.scene.sortChildren();
                save(PhoeniciaContext.context);
            }
        }));

    }

    public void setListener(final WordTileListener listener) {
        this.eventListener = listener;
    }

    /**
     * Callback handler for listening to changes and events on this tile
     */
    public interface WordTileListener {
        public void onWordTileClicked(final WordTile wordTile);
        public void onWordTileBuildCompleted(final WordTile wordTile);
    }
}

