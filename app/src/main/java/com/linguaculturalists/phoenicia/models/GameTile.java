package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.components.PlacedBlockSprite;
import com.linguaculturalists.phoenicia.locale.Game;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.ui.SpriteMoveHUD;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameTextures;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Filter;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Database model representing a Word tile that has been placed on the map.
 */
public class GameTile extends Model implements Builder.BuildStatusUpdateHandler, IOnAreaTouchListener, MapBlockSprite.OnClickListener {

    public ForeignKeyField<GameSession> session; /**< reference to the GameSession this tile is a part of */
    public ForeignKeyField<GameTileBuilder> builder; /**< reference to the WordBuilder used by this tile */
    public IntegerField isoX; /**< isometric X coordinate for this tile */
    public IntegerField isoY; /**< isometric Y coordinate for this tile */
    public CharField item_name; /**< name of the InventoryItem this tile produces */

    public PhoeniciaGame phoeniciaGame; /**< active game instance this tile is a part of */
    public Game game; /**< locale Game this tile represents */
    public PlacedBlockSprite sprite;  /**< sprite that has been placed on the map for this tile */

    private boolean isTouchDown = false;
    private GameTileListener eventListener;
    private boolean isCompleted = false;

    public GameTile() {
        super();
        this.session = new ForeignKeyField<GameSession>(GameSession.class);
        this.builder = new ForeignKeyField<GameTileBuilder>(GameTileBuilder.class);
        this.isoX = new IntegerField();
        this.isoY = new IntegerField();
        this.item_name = new CharField(32);
    }

    /**
     * Create a new tile for the given game that is a part of the given game
     * @param phoeniciaGame active game instance this tile is a part of
     * @param game locale Letter this tile represents
     */
    public GameTile(PhoeniciaGame phoeniciaGame, Game game) {
        this();
        this.phoeniciaGame = phoeniciaGame;
        this.game = game;
        this.session.set(phoeniciaGame.session);
        this.item_name.set(game.name);
    }

    public static final QuerySet<GameTile> objects(Context context) {
        return objects(context, GameTile.class);
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
    public GameTileBuilder getBuilder(Context context) {
        GameTileBuilder builder = this.builder.get(context);
        if (builder != null) {
            builder.addUpdateHandler(this);
            this.onProgressChanged(builder);
            phoeniciaGame.addBuilder(builder);
        }
        return builder;
    }

    /**
     * Attach a WordBuilder to this tile
     * @param builder used by this tile
     */
    public void setBuilder(GameTileBuilder builder) {
        builder.addUpdateHandler(this);
        this.builder.set(builder);
        this.onProgressChanged(builder);
        if (builder.status.get() == Builder.COMPLETE) {
            this.isCompleted = true;
        }
    }

    public void restart(Context context) {
        Debug.d("Restarting game timout");
        // TODO: Restart builder
    }

    public void onScheduled(Builder buildItem) { Debug.d("WordTile.onScheduled"); this.isCompleted = false; return; }
    public void onStarted(Builder buildItem) { Debug.d("WordTile.onStarted"); this.isCompleted = false; return; }
    public void onCompleted(Builder buildItem) {
        Debug.d("WordTile.onCompleted");
        this.isCompleted = true;
        if (this.eventListener != null) {
            this.eventListener.onGameTileBuildCompleted(this);
        }
        return;
    }

    /**
     * Called when the builder for this tile gets updated.
     * @param builtItem
     */
    public void onProgressChanged(Builder builtItem) {
        if (sprite != null) {
            sprite.setProgress(builtItem.progress.get(), game.construct);
        }
        if (builtItem.progress.get() >= game.construct) {
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
            this.sprite.setProgress(0, game.construct);
        }
        GameTileBuilder builder = this.getBuilder(context);
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
        Migrator<GameTile> migrator = new Migrator<GameTile>(GameTile.class);

        // roll out all migrations
        migrator.migrate(context);
        return;
    }

    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
        if (pSceneTouchEvent.isActionDown()) {
            this.isTouchDown = true;
        } else if (isTouchDown && pSceneTouchEvent.isActionUp()) {

            if (this.eventListener != null) {
                this.eventListener.onGameTileClicked(this);
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
        Debug.d("Clicked block: "+String.valueOf(this.game.name));
        Builder builder = this.getBuilder(PhoeniciaContext.context);
        if (builder != null) {
            if (builder.status.get() == Builder.COMPLETE) {
                Debug.d("Clicked block was completed");
                phoeniciaGame.hudManager.showGame(phoeniciaGame.locale.level_map.get(phoeniciaGame.current_level), this);
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
                //phoeniciaGame.playBlockSound(this.game.sound);
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
        phoeniciaGame.hudManager.push(new SpriteMoveHUD(phoeniciaGame, tmxTile, sprite, game.columns, game.rows, this.game.restriction, new SpriteMoveHUD.SpriteMoveHandler() {
            @Override
            public void onSpriteMoveCanceled(MapBlockSprite sprite) {
                float[] oldPos = GameTextures.calculateTilePosition(tmxTile, sprite, game.columns, game.rows);
                sprite.setPosition(oldPos[0], oldPos[1]);
                sprite.setZIndex(tmxTile.getTileZ());
                phoeniciaGame.scene.sortChildren();
            }

            @Override
            public void onSpriteMoveFinished(MapBlockSprite sprite, TMXTile newlocation) {
                isoX.set(newlocation.getTileColumn());
                isoY.set(newlocation.getTileRow());
                // Unset previous sprite location
                for (int c = 0; c < game.columns; c++) {
                    for (int r = 0; r < game.rows; r++) {
                        phoeniciaGame.placedSprites[tmxTile.getTileColumn()-c][tmxTile.getTileRow()-r] = null;
                    }
                }
                // Set new sprite location
                for (int c = 0; c < game.columns; c++) {
                    for (int r = 0; r < game.rows; r++) {
                        phoeniciaGame.placedSprites[newlocation.getTileColumn()-c][newlocation.getTileRow()-r] = sprite;
                    }
                }
                sprite.setZIndex(newlocation.getTileZ());
                phoeniciaGame.scene.sortChildren();
                save(PhoeniciaContext.context);
            }
        }));

    }

    public void setListener(final GameTileListener listener) {
        this.eventListener = listener;
    }

    /**
     * Callback handler for listening to changes and events on this tile
     */
    public interface GameTileListener {
        public void onGameTileClicked(final GameTile wordTile);
        public void onGameTileBuildCompleted(final GameTile wordTile);
    }
}

