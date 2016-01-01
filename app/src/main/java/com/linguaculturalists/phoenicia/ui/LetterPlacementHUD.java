package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.PlacedBlockSprite;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.models.BuildQueue;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.LetterTile;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mhall on 6/19/15.
 */
public class LetterPlacementHUD extends CameraScene implements Inventory.InventoryUpdateListener, IOnSceneTouchListener {
    private Letter placeBlock = null;
    private Map<String, Text> inventoryCounts;
    private PhoeniciaGame game;
    private boolean scenePressed = false;
    private Letter activeLetter;

    public LetterPlacementHUD(final PhoeniciaGame game, final Level level) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.inventoryCounts = new HashMap<String, Text>();
        Inventory.getInstance().addUpdateListener(this);
        this.game = game;

        Rectangle whiteRect = new Rectangle(game.activity.CAMERA_WIDTH/2, 64, 400, 96, game.activity.getVertexBufferObjectManager());
        whiteRect .setColor(Color.WHITE);
        this.attachChild(whiteRect);

        Scrollable blockPanel = new Scrollable(game.activity.CAMERA_WIDTH/2, 64, 400, 96, Scrollable.SCROLL_HORIZONTAL);
        Rectangle redRect = new Rectangle(game.activity.CAMERA_WIDTH/2, 64, 512, 128, game.activity.getVertexBufferObjectManager());
        redRect.setColor(Color.RED);
        //blockPanel.attachChild(redRect);


        this.registerTouchArea(blockPanel);
        this.registerTouchArea(blockPanel.contents);
        this.attachChild(blockPanel);

        final Font inventoryCountFont = FontFactory.create(game.activity.getFontManager(), game.activity.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.RED_ARGB_PACKED_INT);
        inventoryCountFont.load();
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
            final ButtonSprite block = new ButtonSprite((64 * ((i * 2)+1))-startX, 48, blockRegion, game.activity.getVertexBufferObjectManager());
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    if (activeLetter != currentLetter) {
                        Debug.d("Activated block: " + currentLetter.name);
                        activeLetter = currentLetter;
                    } else {
                        Debug.d("Deactivated block: " + activeLetter.name);
                        activeLetter = null;
                    }
                }
            });
            this.registerTouchArea(block);
            blockPanel.attachChild(block);

            final Text inventoryCount = new Text((64 * ((i * 2)+1))-startX+24, 20, inventoryCountFont, ""+game.inventory.getCount(currentLetter.name), 4, game.activity.getVertexBufferObjectManager());
            blockPanel.attachChild(inventoryCount);
            this.inventoryCounts.put(currentLetter.name, inventoryCount);
        }
        Debug.d("Finished loading HUD letters");

        Debug.d("Finished instantiating LetterPlacementHUD");

        //this.setOnSceneTouchListener(this);
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
    public boolean onSceneTouchEvent(Scene scene, TouchEvent touchEvent) {
        return false;
    }

    @Override
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        Debug.d("LetterPlacementHud touched at "+pSceneTouchEvent.getX()+"x"+pSceneTouchEvent.getY());

        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        if (handled) return true;

        final float[] gameSceneCoordinates = this.getCamera().getCameraSceneCoordinatesFromSceneCoordinates(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
        Debug.d("gameSceneCoordinates as "+gameSceneCoordinates[0]+"x"+gameSceneCoordinates[1]);
        switch (pSceneTouchEvent.getAction()) {
            case TouchEvent.ACTION_DOWN:
                Debug.d("LetterPlacementHud scene touch ACTION_DOWN");
                this.scenePressed = true;
                return handled;
            case TouchEvent.ACTION_UP:
                Debug.d("LetterPlacementHud scene touch ACTION_UP");
                if (this.scenePressed) {
                    TMXTile mapTile = game.getTileAt(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
                    Debug.d("LetterPlacementHud scene touch tile: "+mapTile);
                    Debug.d("LetterPlacementHud scene touch active: "+this.activeLetter);
                    if (mapTile != null && this.activeLetter != null) {
                        this.addLetterTile(activeLetter, mapTile);
                        return true;
                    }
                    this.scenePressed = false;
                }
            default:
                Debug.d("LetterPlacementHud scene touch "+pSceneTouchEvent.getAction());
                this.scenePressed = false;

        }
        return handled;
    }

    private void addLetterTile(Letter letter, TMXTile onTile) {
        Debug.d("Placing letter "+letter.name+" at "+onTile.getTileColumn()+"x"+onTile.getTileRow());
        final LetterTile letterTile = new LetterTile(this.game, letter);

        letterTile.isoX.set(onTile.getTileColumn());
        letterTile.isoY.set(onTile.getTileRow());
        game.createLetterSprite(letterTile);

        BuildQueue builder = new BuildQueue(game.session, letterTile, letterTile.item_name.get(), letter.time);
        builder.start();
        builder.save(game.activity.getApplicationContext());
        game.addBuilder(builder);

        letterTile.setBuilder(builder);
        letterTile.save(game.activity.getApplicationContext());
    }

}