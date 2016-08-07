package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.components.PlacedBlockSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.ui.SpriteMoveHUD;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameTextures;
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
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.text.Text;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseBackOut;
import org.andengine.util.modifier.ease.EaseLinear;
import org.apache.commons.lang3.StringUtils;

/**
 * Database model representing a Letter tile that has been placed on the map.
 */
public class LetterTile extends Model implements Builder.BuildStatusUpdateHandler, IOnAreaTouchListener, MapBlockSprite.OnClickListener {

    public ForeignKeyField<GameSession> game; /**< reference to the GameSession this tile is a part of */
    public ForeignKeyField<LetterBuilder> builder; /**< reference to the LetterBuilder used by this tile */
    public IntegerField isoX; /**< isometric X coordinate for this tile */
    public IntegerField isoY; /**< isometric Y coordinate for this tile */
    public CharField item_name; /**< name of the InventoryItem this tile produces */

    public PhoeniciaGame phoeniciaGame; /**< active game instance this tile is a part of */
    public Letter letter; /**< locale Letter this tile represents */
    public PlacedBlockSprite sprite; /**< sprite that has been placed on the map for this tile */

    private boolean isTouchDown = false;
    private LetterTileListener eventListener;
    private boolean isCompleted = false;

    public LetterTile() {
        super();
        this.game = new ForeignKeyField<GameSession>(GameSession.class);
        this.builder = new ForeignKeyField<LetterBuilder>(LetterBuilder.class);
        this.isoX = new IntegerField();
        this.isoY = new IntegerField();
        this.item_name = new CharField(32);
    }

    /**
     * Create a new tile for the given letter that is a part of the given game
     * @param game active game instance this tile is a part of
     * @param letter locale Letter this tile represents
     */
    public LetterTile(PhoeniciaGame game, Letter letter) {
        this();
        this.phoeniciaGame = game;
        this.letter = letter;
        this.game.set(game.session);
        this.item_name.set(letter.name);
    }

    public static final QuerySet<LetterTile> objects(Context context) {
        return objects(context, LetterTile.class);
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
     * Get the builder instance that produces a Letter from this tile
     * @param context ApplicationContext for use in database calls
     * @return the builder instance, or null if it does not have one
     */
    public LetterBuilder getBuilder(Context context) {
        LetterBuilder builder = this.builder.get(context);
        if (builder != null) {
            builder.addUpdateHandler(this);
            this.onProgressChanged(builder);
            phoeniciaGame.addBuilder(builder);
        }
        return builder;
    }

    /**
     * Attach a LetterBuilder to this tile
     * @param builder used by this tile
     */
    public void setBuilder(LetterBuilder builder) {
        builder.addUpdateHandler(this);
        this.builder.set(builder);
        this.onProgressChanged(builder);
    }

    public void onScheduled(Builder buildItem) { Debug.d("Builder.onScheduled"); this.isCompleted = false; return; }
    public void onStarted(Builder buildItem) { Debug.d("Builder.onStarted"); this.isCompleted = false; return; }
    public void onCompleted(Builder buildItem) {
        Debug.d("Builder.onCompleted");
        this.isCompleted = true;
        if (this.eventListener != null) {
            this.eventListener.onLetterTileBuildCompleted(this);
        }
        Assets.getInsance().addLetterTile(this);
        // TODO: Find cause of redundant calls to this block and re-introduce phoneme playback
        //phoeniciaGame.playBlockSound(letter.phoneme);
        return;
    }

    /**
     * Called when the builder for this tile gets updated
     * @param builtItem
     */
    public void onProgressChanged(Builder builtItem) {
        //Debug.d("LetterTile builder progress changed: "+builtItem.progress.get());
        if (sprite != null) {
            sprite.setProgress(builtItem.progress.get(), letter.time);
        }
        if (builtItem.progress.get() >= letter.time && !this.isCompleted) {
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
            this.sprite.setProgress(0, letter.time);
        }
        LetterBuilder builder = this.getBuilder(context);
        if (builder != null) {
            //Debug.d("Resetting LetterTile builder");
            builder.progress.set(0);
            builder.start();
            builder.save(context);
            this.phoeniciaGame.addBuilder(builder);
        } else {
            Debug.e("Could not reset LetterTile builder, because it was missing");
        }
    }

    @Override
    protected void migrate(Context context) {
        Migrator<LetterTile> migrator = new Migrator<LetterTile>(LetterTile.class);

        migrator.addField("builder", new ForeignKeyField<LetterBuilder>(LetterBuilder.class));

        // roll out all migrations
        migrator.migrate(context);
        return;
    }

    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
        if (pSceneTouchEvent.isActionDown()) {
            this.isTouchDown = true;
        } else if (isTouchDown && pSceneTouchEvent.isActionUp()) {

            if (this.eventListener != null) {
                this.eventListener.onLetterTileClicked(this);
            }
            this.isTouchDown = false;
            return true;
        } else {
            this.isTouchDown = false;
        }
        return false;

    }

    /**
     * Called when the Sprite for the tile has been clicked by the player
     * @param buttonSprite
     * @param v
     * @param v2
     */
    public void onClick(MapBlockSprite buttonSprite, float v, float v2) {
        Debug.d("Clicked block: "+String.valueOf(this.letter.chars));
        final LetterBuilder builder = this.getBuilder(PhoeniciaContext.context);
        if (builder != null) {
            if (builder.status.get() == LetterBuilder.COMPLETE) {
                Debug.d("Clicked block was completed");
                phoeniciaGame.playBlockSound(this.letter.sound);
                Inventory.getInstance().add(this.letter.name);
                phoeniciaGame.session.addExperience(this.letter.points);
                this.reset(PhoeniciaContext.context);
            } else {
                Debug.d("Clicked block was NOT completed");
                // Don't run another modifier animation if one is still running
                if (sprite.getEntityModifierCount() <= 0) {
                    sprite.registerEntityModifier(new ScaleAtModifier(0.5f, sprite.getScaleX(), sprite.getScaleX(), sprite.getScaleY() * 0.7f, sprite.getScaleY(), sprite.getScaleCenterX(), 0, EaseBackOut.getInstance()));
                    int time_left = builder.time.get() - builder.progress.get();
                    String time_display = String.valueOf(time_left) + "s";
                    if (time_left > (60*60)) {
                        time_left = time_left / (60*60);
                        time_display = String.valueOf(time_left) + "h";
                    } else if (time_left > 60) {
                        time_left = time_left / 60;
                        time_display = String.valueOf(time_left) + "m";
                    }
                    final Text progressText = new Text(sprite.getWidth()/2, 16, GameFonts.progressText(), time_display, time_display.length(), PhoeniciaContext.vboManager);
                    sprite.attachChild(progressText);
                    progressText.registerEntityModifier(new ParallelEntityModifier(
                            new ScaleModifier(0.4f, 0.3f, 0.8f, EaseLinear.getInstance()),
                            new FadeOutModifier(3.0f, new IEntityModifier.IEntityModifierListener() {
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
                if (phoeniciaGame.locale.level_map.get(phoeniciaGame.current_level).help_letters.contains(this.letter)) {
                    phoeniciaGame.playBlockSound(this.letter.phoneme);
                }
            }
        } else {
            Debug.e("Clicked block has no builder");
        }
    }

    public void onHold(MapBlockSprite buttonSprite, float v, float v2) {
        final TMXTile tmxTile = phoeniciaGame.getTileAtIso(this.isoX.get(), this.isoY.get());
        if (tmxTile == null) {
            Debug.d("No tile at "+this.isoX.get()+"x"+this.isoY.get());
            return;
        }
        phoeniciaGame.hudManager.push(new SpriteMoveHUD(phoeniciaGame, tmxTile, sprite, letter.columns, letter.rows, this.letter.restriction, new SpriteMoveHUD.SpriteMoveHandler() {
            @Override
            public void onSpriteMoveCanceled(MapBlockSprite sprite) {
                float[] oldPos = GameTextures.calculateTilePosition(tmxTile, sprite, letter.columns, letter.rows);
                sprite.setPosition(oldPos[0], oldPos[1]);
                sprite.setZIndex(tmxTile.getTileZ());
                phoeniciaGame.scene.sortChildren();
            }

            @Override
            public void onSpriteMoveFinished(MapBlockSprite sprite, TMXTile newlocation) {
                isoX.set(newlocation.getTileColumn());
                isoY.set(newlocation.getTileRow());
                // Unset previous sprite location
                for (int c = 0; c < letter.columns; c++) {
                    for (int r = 0; r < letter.rows; r++) {
                        phoeniciaGame.placedSprites[tmxTile.getTileColumn()-c][tmxTile.getTileRow()-r] = null;
                    }
                }
                // Set new sprite location
                for (int c = 0; c < letter.columns; c++) {
                    for (int r = 0; r < letter.rows; r++) {
                        phoeniciaGame.placedSprites[newlocation.getTileColumn()-c][newlocation.getTileRow()-r] = sprite;
                    }
                }
                sprite.setZIndex(newlocation.getTileZ());
                phoeniciaGame.scene.sortChildren();
                save(PhoeniciaContext.context);
            }
        }));

    }
    public void setListener(final LetterTileListener listener) {
        this.eventListener = listener;
    }

    /**
     * Callback handler for listening to changes and events on this tile
     */
    public interface LetterTileListener {
        public void onLetterTileClicked(final LetterTile letterTile);
        public void onLetterTileBuildCompleted(final LetterTile letterTile);
    }
}

