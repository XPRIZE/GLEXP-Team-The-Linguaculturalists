package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.components.PlacedBlockSprite;
import com.linguaculturalists.phoenicia.ui.SpriteMoveHUD;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameTextures;
import com.linguaculturalists.phoenicia.util.GameUI;
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
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseBackOut;
import org.andengine.util.modifier.ease.EaseLinear;

/**
 * Created by mhall on 2/16/16.
 */
public class DefaultTile extends Model implements MapBlockSprite.OnClickListener {

    public ForeignKeyField<GameSession> game; /**< reference to the GameSession this tile is a part of */
    public IntegerField isoX; /**< isometric X coordinate for this tile */
    public IntegerField isoY; /**< isometric Y coordinate for this tile */
    public CharField item_type; /**< type of default tile (inventory, market) */
    public WorkshopBuilder builder; /**< builder for this tile's activity */

    public PhoeniciaGame phoeniciaGame; /**< active game instance this tile is a part of */
    public MapBlockSprite sprite; /**< sprite that has been placed on the map for this tile */

    private boolean attention = false;

    public DefaultTile() {
        super();
        this.game = new ForeignKeyField<GameSession>(GameSession.class);
        this.isoX = new IntegerField();
        this.isoY = new IntegerField();
        this.item_type = new CharField(32);
    }

    public static final QuerySet<DefaultTile> objects(Context context) {
        return objects(context, DefaultTile.class);
    }

    /**
     * Get the Sprite that represents this tile on the map
     * @return sprite, or null if it does not have one
     */
    public MapBlockSprite getSprite() {

        return this.sprite;
    }

    public WorkshopBuilder getBuilder(Context context) {
        return this.builder;
    }

    public void setBuilder(WorkshopBuilder builder) {
        this.builder = builder;
    }

    /**
     * Attach a Sprite from the map to this tile
     * @param sprite sprite that represents this tile on the map
     */
    public void setSprite(MapBlockSprite sprite) {
        this.sprite = sprite;
    }

    public void setAttention(boolean attention) {
        if (this.attention == attention) return;
        Debug.d("Setting attention for workshop tile "+builder.item_name.get()+" to "+attention);
        this.attention = attention;
        if (this.attention && phoeniciaGame.locale.word_map.containsKey(builder.item_name.get())) {
            final ITiledTextureRegion wordSpriteRegion = phoeniciaGame.wordSprites.get(phoeniciaGame.locale.word_map.get(builder.item_name.get()));
            if (wordSpriteRegion == null) return;
            final Sprite wordSprite = new Sprite(0, 0, wordSpriteRegion.getTextureRegion(1), PhoeniciaContext.vboManager);
            this.sprite.setEmblem(wordSprite);
        } else {
            this.sprite.clearEmblem();
        }
    }

    @Override
    protected void migrate(Context context) {
        Migrator<DefaultTile> migrator = new Migrator<DefaultTile>(DefaultTile.class);

        // roll out all migrations
        migrator.migrate(context);
        return;
    }

    /**
     * Called when the Sprite for the tile has been clicked by the player
     * @param buttonSprite
     * @param v
     * @param v2
     */
    public void onClick(MapBlockSprite buttonSprite, float v, float v2) {
        if (this.item_type.get().equals("inventory")) {
            if (phoeniciaGame.locale.isLevelReached(phoeniciaGame.locale.inventoryBlock.level, phoeniciaGame.getCurrentLevel())) {
                phoeniciaGame.hudManager.showInventory();
            } else {
                this.showLevelLock(phoeniciaGame.locale.inventoryBlock.level);
            }
        } else if (this.item_type.get().equals("market")) {
            if (phoeniciaGame.locale.isLevelReached(phoeniciaGame.locale.marketBlock.level, phoeniciaGame.getCurrentLevel())) {
                phoeniciaGame.hudManager.showMarket();
            } else {
                this.showLevelLock(phoeniciaGame.locale.marketBlock.level);
            }
        } else if (this.item_type.get().equals("workshop")) {
            if (phoeniciaGame.locale.isLevelReached(phoeniciaGame.locale.workshopBlock.level, phoeniciaGame.getCurrentLevel())) {
                phoeniciaGame.hudManager.showWorkshop(this);
            } else {
                this.showLevelLock(phoeniciaGame.locale.workshopBlock.level);
            }
        } else {
            Debug.e("Unknown default block: "+this.item_type.get());
        }
    }

    private void showLevelLock(String level_name) {
        ITextureRegion levelRegion = GameUI.getInstance().getLevelButton();
        final Sprite levelIcon = new Sprite((this.sprite.getWidth()/2), levelRegion.getHeight()/2, levelRegion, PhoeniciaContext.vboManager);
        final Text levelName = new Text(110, levelIcon.getHeight()/2, GameFonts.defaultHUDDisplay(), level_name, level_name.length(), PhoeniciaContext.vboManager);
        levelIcon.attachChild(levelName);
        this.sprite.attachChild(levelIcon);
        levelIcon.registerEntityModifier(new ParallelEntityModifier(
                new ScaleModifier(0.4f, 0.2f, 0.5f, EaseLinear.getInstance()),
                new FadeOutModifier(2.0f, new IEntityModifier.IEntityModifierListener() {
                    @Override
                    public void onModifierStarted(IModifier<IEntity> iModifier, IEntity iEntity) {
                    }

                    @Override
                    public void onModifierFinished(IModifier<IEntity> iModifier, IEntity iEntity) {
                        phoeniciaGame.activity.runOnUpdateThread(new Runnable() {
                            @Override
                            public void run() {
                                sprite.detachChild(levelIcon);
                            }
                        });

                    }
                }, EaseLinear.getInstance())
        ));
    }
    public void onHold(MapBlockSprite buttonSprite, float v, float v2) {
        final TMXTile tmxTile = phoeniciaGame.getTileAtIso(this.isoX.get(), this.isoY.get());
        if (tmxTile == null) {
            Debug.d("No tile at "+this.isoX.get()+"x"+this.isoY.get());
            return;
        }
        int cols = 1;
        int rows = 1;
        if (this.item_type.get().equals("inventory")) {
            cols = phoeniciaGame.locale.inventoryBlock.columns;
            rows = phoeniciaGame.locale.inventoryBlock.rows;
        } else if (this.item_type.get().equals("market")) {
            cols = phoeniciaGame.locale.marketBlock.columns;
            rows = phoeniciaGame.locale.marketBlock.rows;
        } else if (this.item_type.get().equals("workshop")) {
            cols = phoeniciaGame.locale.workshopBlock.columns;
            rows = phoeniciaGame.locale.workshopBlock.rows;
        } else {
            Debug.e("Unknown default block: "+this.item_type.get());
        }
        final int spriteColumns = cols;
        phoeniciaGame.hudManager.push(new SpriteMoveHUD(phoeniciaGame, tmxTile, buttonSprite, cols, rows, null, new SpriteMoveHUD.SpriteMoveHandler() {
            @Override
            public void onSpriteMoveCanceled(MapBlockSprite sprite) {
                float newX = tmxTile.getTileX() + (sprite.getWidth()/2) - ((GameTextures.BASE_TILE_WIDTH/2)*(spriteColumns-1));// anchor sprite center to tile center
                float newY = tmxTile.getTileY() + (sprite.getHeight()/2);// anchor sprite center half the sprite's height higher than the tile bottom
                sprite.setPosition(newX, newY);
                sprite.setZIndex(tmxTile.getTileZ());
                phoeniciaGame.scene.sortChildren();
            }

            @Override
            public void onSpriteMoveFinished(MapBlockSprite sprite, TMXTile newlocation) {
                isoX.set(newlocation.getTileColumn());
                isoY.set(newlocation.getTileRow());
                int cols = 1;
                int rows = 1;
                if (item_type.get().equals("inventory")) {
                    cols = phoeniciaGame.locale.inventoryBlock.columns;
                    rows = phoeniciaGame.locale.inventoryBlock.rows;
                } else if (item_type.get().equals("market")) {
                    cols = phoeniciaGame.locale.marketBlock.columns;
                    rows = phoeniciaGame.locale.marketBlock.rows;
                } else if (item_type.get().equals("workshop")) {
                    cols = phoeniciaGame.locale.workshopBlock.columns;
                    rows = phoeniciaGame.locale.workshopBlock.rows;
                } else {
                    Debug.e("Unknown default block: " + item_type.get());
                }
                for (int c = 0; c < cols; c++) {
                    for (int r = 0; r < rows; r++) {
                        phoeniciaGame.placedSprites[tmxTile.getTileColumn()-c][tmxTile.getTileRow()-r] = null;
                    }
                }
                for (int c = 0; c < cols; c++) {
                    for (int r = 0; r < rows; r++) {
                        phoeniciaGame.placedSprites[newlocation.getTileColumn()-c][newlocation.getTileRow()-r] = sprite;
                    }
                }
                sprite.setZIndex(newlocation.getTileZ());
                phoeniciaGame.scene.sortChildren();
                save(PhoeniciaContext.context);

            }
        }));

    }

}
