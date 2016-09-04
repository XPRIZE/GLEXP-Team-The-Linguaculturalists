package com.linguaculturalists.phoenicia.models;

import android.content.Context;
import android.graphics.Bitmap;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.components.PlacedBlockSprite;
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
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseBackOut;
import org.andengine.util.modifier.ease.EaseLinear;

import java.util.ArrayList;
import java.util.List;

/**
 * Database model representing a Word tile that has been placed on the map.
 */
public class WordTile extends Model implements Builder.BuildStatusUpdateHandler, IOnAreaTouchListener, MapBlockSprite.OnClickListener {

    public ForeignKeyField<GameSession> game; /**< reference to the GameSession this tile is a part of */
    public ForeignKeyField<WordTileBuilder> builder; /**< reference to the WordBuilder used by this tile */
    public IntegerField isoX; /**< isometric X coordinate for this tile */
    public IntegerField isoY; /**< isometric Y coordinate for this tile */
    public CharField item_name; /**< name of the InventoryItem this tile produces */
    public IntegerField stock; /**< number of these words that have been built and are ready for collection */

    public PhoeniciaGame phoeniciaGame; /**< active game instance this tile is a part of */
    public Word word; /**< locale Word this tile represents */
    public PlacedBlockSprite sprite;  /**< sprite that has been placed on the map for this tile */

    private boolean isTouchDown = false;
    private WordTileListener eventListener;
    private boolean isCompleted = false;

    private int queue_size = 1;
    private boolean isActive = false;
    private List<WordBuilder> buildQueue;

    private boolean attention = false;

    public WordTile() {
        super();
        this.game = new ForeignKeyField<GameSession>(GameSession.class);
        this.builder = new ForeignKeyField<WordTileBuilder>(WordTileBuilder.class);
        this.isoX = new IntegerField();
        this.isoY = new IntegerField();
        this.item_name = new CharField(32);
        this.stock = new IntegerField();
        this.buildQueue = new ArrayList<WordBuilder>();
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

    public List<WordBuilder> getQueue() {
        return this.buildQueue;
    }

    public WordBuilder createWord() {
        WordBuilder newBuilder = new WordBuilder(this.phoeniciaGame.session, this, this.word.name, this.word.time);
        newBuilder.schedule();
        newBuilder.save(PhoeniciaContext.context);
        this.buildQueue.add(newBuilder);
        next();
        return newBuilder;
    }

    /**
     * Add a given number of this word to this tile's stock
     * @param amount to add
     */
    public void addToStock(int amount) {
        this.stock.set(this.stock.get() + amount);
        this.save(PhoeniciaContext.context);
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
    public WordTileBuilder getBuilder(Context context) {
        WordTileBuilder builder = this.builder.get(context);
        if (builder != null) {
            builder.addUpdateHandler(this);
            this.onProgressChanged(builder);
            phoeniciaGame.addBuilder(builder);
        }
        return builder;
    }

    public void setAttention(boolean attention) {
        if (this.attention == attention) return;
        Debug.d("Setting attention for word tile "+this.word.name+" to "+attention);
        this.attention = attention;
        if (this.attention) {
            final ITiledTextureRegion wordSpriteRegion = phoeniciaGame.wordSprites.get(this.word);
            final Sprite wordSprite = new Sprite(0, 0, wordSpriteRegion.getTextureRegion(1), PhoeniciaContext.vboManager);
            this.sprite.setEmblem(wordSprite);
        } else {
            this.sprite.clearEmblem();
        }
    }

    public void checkAttention() {
        for (Builder b : this.getQueue()) {
            if (b.status.get() == Builder.COMPLETE) {
                this.setAttention(true);
                return;
            }
        }
        this.setAttention(false);
    }
    public void next() {
        Debug.d("Checking for next word builder for "+this.word.name);
        if (this.isActive) return;
        Debug.d("Word Tile is not active, checking the build queue");
        List<WordBuilder> queue = this.getQueue();
        for (WordBuilder builder : queue) {
            if (builder.status.get() == Builder.SCHEDULED) {
                this.setActiveBuilder(builder);
                Debug.d("Starting word builder: " + builder.getId());
                break;
            }
        }
    }

    public void restart(Context context) {
        Debug.d("Loading word builder queue");
        Filter queueBuilders = new Filter();
        queueBuilders.is("tile", this);
        this.buildQueue = WordBuilder.objects(PhoeniciaContext.context).filter(queueBuilders).toList();

        Debug.d("Restarting active word builder for tile: "+this.word.name);
        WordBuilder activeBuilder = this.getActiveBuilder(context);
        if (activeBuilder != null) {
            Debug.d("Restarting active word builder: " + activeBuilder.getId());
            this.setActiveBuilder(activeBuilder);
        } else {
            Debug.d("No active builder found, moving on to the next one");
            this.isActive = false;
            this.next();
        }
        this.checkAttention();
    }

    public boolean isActive() {
        return isActive;
    }

    public int getQueueSize() {
        return queue_size;
    }

    public void setActiveBuilder(final WordBuilder builder) {
        this.isActive = true;
        final WordTile tile = this;
        builder.addUpdateHandler(new Builder.BuildStatusUpdateHandler() {
            @Override
            public void onCompleted(Builder buildItem) {
                Debug.d("WordBuilder for " + buildItem.item_name.get() + " has completed");
                phoeniciaGame.playBlockSound(word.sound);
                isActive = false;
                builder.removeUpdateHandler(tile);
                phoeniciaGame.removeBuilder(builder);
                setAttention(true);
                next();
            }

            @Override
            public void onProgressChanged(Builder buildItem) {
                //Debug.d("WordBuilder updated " + buildItem.item_name.get() + " is at: " + buildItem.progress.get());
            }

            @Override
            public void onScheduled(Builder buildItem) {

            }

            @Override
            public void onStarted(Builder buildItem) {
                Debug.d("WordBuilder for " + buildItem.item_name.get() + " has been started");
                phoeniciaGame.addBuilder(builder);
            }
        });
        builder.start();
        builder.save(PhoeniciaContext.context);
    }

    public WordBuilder getActiveBuilder(Context context) {
        for (WordBuilder builder : this.buildQueue) {
            if (builder.status.get() == Builder.BUILDING){
                return builder;
            }
        }
        return null;
    }

    /**
     * Attach a WordBuilder to this tile
     * @param builder used by this tile
     */
    public void setBuilder(WordTileBuilder builder) {
        builder.addUpdateHandler(this);
        this.builder.set(builder);
        this.onProgressChanged(builder);
        if (builder.status.get() == Builder.COMPLETE) {
            this.isCompleted = true;
        }
    }

    public void onScheduled(Builder buildItem) { Debug.d("WordTile.onScheduled"); this.isCompleted = false; return; }
    public void onStarted(Builder buildItem) { Debug.d("WordTile.onStarted"); this.isCompleted = false; return; }
    public void onCompleted(Builder buildItem) {
        Debug.d("WordTile.onCompleted");
        this.isCompleted = true;
        if (this.eventListener != null) {
            this.eventListener.onWordTileBuildCompleted(this);
        }
        Assets.getInsance().addWordTile(this);
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
        WordTileBuilder builder = this.getBuilder(context);
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

        migrator.addField("stock", new IntegerField());

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
     * @param px
     * @param py
     */
    public void onClick(MapBlockSprite buttonSprite, float px, float py) {
        Builder builder = this.getBuilder(PhoeniciaContext.context);
        if (builder != null) {
            if (builder.status.get() == Builder.COMPLETE) {
                Debug.d("Clicked block was completed");
                phoeniciaGame.hudManager.showWordBuilder(phoeniciaGame.locale.level_map.get(phoeniciaGame.current_level), this);
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
                float[] oldPos = GameTextures.calculateTilePosition(tmxTile, sprite, word.columns, word.rows);
                sprite.setPosition(oldPos[0], oldPos[1]);
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

