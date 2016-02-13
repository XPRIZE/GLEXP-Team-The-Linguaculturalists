package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.models.LetterBuilder;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.LetterTile;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.ease.EaseBackOut;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HUD for selecting \link Letter Letters \endlink to be placed as tiles onto the map.
 */
public class LetterPlacementHUD extends PhoeniciaHUD implements Inventory.InventoryUpdateListener {
    private Letter placeBlock = null;
    private Map<String, Text> inventoryCounts;
    private PhoeniciaGame game;

    private Rectangle whiteRect;
    private Scrollable blockPanel;

    public LetterPlacementHUD(final PhoeniciaGame game, final Level level) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.inventoryCounts = new HashMap<String, Text>();
        Inventory.getInstance().addUpdateListener(this);
        this.game = game;

        this.whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH/2, 64, 600, 96, PhoeniciaContext.vboManager);
        whiteRect .setColor(Color.WHITE);
        this.attachChild(whiteRect);

        this.blockPanel = new Scrollable(GameActivity.CAMERA_WIDTH/2, 64, 600, 96, Scrollable.SCROLL_HORIZONTAL);
        //this.blockPanel.setClip(false);

        this.registerTouchArea(blockPanel);
        this.registerTouchArea(blockPanel.contents);
        this.attachChild(blockPanel);

        final Font inventoryCountFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.RED_ARGB_PACKED_INT);
        inventoryCountFont.load();

        Debug.d("Loading letters for level: "+this.game.current_level);
        final List<Letter> letters = level.letters;
        final int tile_start = 130;
        final int startX = (int)(blockPanel.getWidth()/2);
        for (int i = 0; i < letters.size(); i++) {
            final Letter currentLetter = letters.get(i);
            Debug.d("Adding HUD letter: "+currentLetter.name+" (sprite: "+currentLetter.sprite+")");
            final int tile_id = currentLetter.sprite;
            ITiledTextureRegion blockRegion = new TiledTextureRegion(game.letterTextures.get(currentLetter),
                    game.letterTiles.get(currentLetter).getTextureRegion(0),
                    game.letterTiles.get(currentLetter).getTextureRegion(1),
                    game.letterTiles.get(currentLetter).getTextureRegion(2));
            final ButtonSprite block = new ButtonSprite((64 * ((i * 2)+1)), 48, blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    float[] cameraCenter = getCamera().getSceneCoordinatesFromCameraSceneCoordinates(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2);
                    TMXTile mapTile = game.getTileAt(cameraCenter[0], cameraCenter[1]);
                    addLetterTile(currentLetter, mapTile);
                }
            });
            this.registerTouchArea(block);
            blockPanel.attachChild(block);

            final Text inventoryCount = new Text((64 * ((i * 2)+1))+24, 20, inventoryCountFont, ""+game.inventory.getCount(currentLetter.name), 4, PhoeniciaContext.vboManager);
            blockPanel.attachChild(inventoryCount);
            this.inventoryCounts.put(currentLetter.name, inventoryCount);
        }
        Debug.d("Finished loading HUD letters");

        Debug.d("Finished instantiating LetterPlacementHUD");

        //this.clickDetector = new ClickDetector(this);
        //this.setOnSceneTouchListener(this);
    }

    /**
     * Animate the bottom panel sliding up into view.
     */
    @Override
    public void show() {
        whiteRect.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
        blockPanel.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
    }

    public void onInventoryUpdated(final InventoryItem[] items) {
        Debug.d("Updating BlockPlacementHUD inventory");
        for (int i = 0; i < items.length; i++) {
            Debug.d("Updating BlockPlacementHUD count for "+items[i].item_name.get());
            if (this.inventoryCounts.containsKey(items[i].item_name.get())) {
                Debug.d("New HUD count: "+items[i].quantity.get().toString());
                final Text countText = this.inventoryCounts.get(items[i].item_name.get());
                countText.setText(items[i].quantity.get().toString());
                //countText.setText("9");
            } else {
                Debug.e("[LetterPlacementHUD] No HUD item for "+items[i].item_name.get());
            }
        }
    }


    @Override
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        Debug.d("LetterPlacementHud touched at "+pSceneTouchEvent.getX()+"x"+pSceneTouchEvent.getY());

        boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        if (handled) return true;

        return false;
    }

    /**
     * Create a new LetterTile (with Sprite and Builder).
     * @param letter Letter to create the tile for
     * @param onTile Map tile to place the new tile on
     */
    private void addLetterTile(final Letter letter, final TMXTile onTile) {
        Debug.d("Placing letter "+letter.name+" at "+onTile.getTileColumn()+"x"+onTile.getTileRow());
        final LetterTile letterTile = new LetterTile(this.game, letter);

        letterTile.isoX.set(onTile.getTileColumn());
        letterTile.isoY.set(onTile.getTileRow());

        game.createLetterSprite(letterTile, new PhoeniciaGame.CreateLetterSpriteCallback() {
            @Override
            public void onLetterSpriteCreated(LetterTile tile) {
                LetterBuilder builder = new LetterBuilder(game.session, letterTile, letterTile.item_name.get(), letter.time);
                builder.start();
                builder.save(PhoeniciaContext.context);
                game.addBuilder(builder);

                letterTile.setBuilder(builder);
                letterTile.save(PhoeniciaContext.context);

            }

            @Override
            public void onLetterSpriteCreationFailed(LetterTile tile) {
                Debug.d("Failed to create letter sprite");
            }
        });

    }

}