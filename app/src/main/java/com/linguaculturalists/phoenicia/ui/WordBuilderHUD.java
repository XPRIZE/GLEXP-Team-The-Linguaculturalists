package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.PlacedBlock;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mhall on 8/26/15.
 */
public class WordBuilderHUD extends CameraScene implements Inventory.InventoryUpdateListener, IOnSceneTouchListener, Closeable {

    private PhoeniciaGame game;
    private Word buildWord;
    private char spelling[];
    private int charBlocksX[];
    private int charBlocksY[];
    private Sprite charSprites[];
    private Map<String, Integer> usedCounts;
    private Map<String, Text> inventoryCounts;

    private int cursorAt;

    private Rectangle whiteRect;

    public WordBuilderHUD(final PhoeniciaGame game, final Level level, final Word word) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        Inventory.getInstance().addUpdateListener(this);
        this.game = game;
        this.buildWord = word;
        this.setOnSceneTouchListener(this);
        Inventory.getInstance().addUpdateListener(this);

        this.spelling = new char[word.chars.length];
        this.eraseSpelling();
        this.charBlocksX = new int[word.chars.length];
        this.charBlocksY = new int[word.chars.length];
        this.charSprites = new Sprite[word.chars.length];
        this.inventoryCounts = new HashMap<String, Text>();
        this.usedCounts = new HashMap<String, Integer>();

        this.cursorAt = 0;

        this.whiteRect = new Rectangle(game.activity.CAMERA_WIDTH / 2, game.activity.CAMERA_HEIGHT / 2, 400, 400, game.activity.getVertexBufferObjectManager());
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);

        ITextureRegion wordSpriteRegion = game.wordTiles.getTextureRegion(this.buildWord.sprite);
        ButtonSprite wordSprite = new ButtonSprite((whiteRect.getWidth()/2), (game.activity.CAMERA_HEIGHT/2)+100, wordSpriteRegion, game.activity.getVertexBufferObjectManager());
        wordSprite.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                Debug.d("Activated block: "+buildWord.name);
                game.playBlockSound(buildWord.sound);
            }
        });
        this.registerTouchArea(wordSprite);
        whiteRect.attachChild(wordSprite);

        final int startX = (int)(whiteRect.getWidth()/2) - (word.chars.length * 35) + 35;
        for (int i = 0; i < word.chars.length; i++) {
            this.charBlocksX[i] = startX+(64*i);
            this.charBlocksY[i] = (int)whiteRect.getHeight()/2+50;
            Rectangle borderRect = new Rectangle(startX+(64*i), whiteRect.getHeight()/2+50, 60, 100, game.activity.getVertexBufferObjectManager());
            borderRect.setColor(Color.RED);
            whiteRect.attachChild(borderRect);

            Rectangle innerRect = new Rectangle(startX+(64*i), whiteRect.getHeight()/2+50, 55, 95, game.activity.getVertexBufferObjectManager());
            innerRect.setColor(Color.WHITE);
            whiteRect.attachChild(innerRect);

        }

        final Font inventoryCountFont = FontFactory.create(game.activity.getFontManager(), game.activity.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.RED_ARGB_PACKED_INT);
        inventoryCountFont.load();

        for (int i = 0; i < level.letters.size(); i++) {
            final Letter currentLetter = level.letters.get(i);
            Debug.d("Adding Builder letter: "+currentLetter.name+" (tile: "+currentLetter.tile+")");
            final int tile_id = currentLetter.sprite;
            final ITextureRegion blockRegion = game.letterTiles.getTextureRegion(tile_id);
            final ButtonSprite block = new ButtonSprite(startX+(64*i), whiteRect.getHeight()/2-50, blockRegion, game.activity.getVertexBufferObjectManager());
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    Debug.d("Activated block: " + currentLetter.name);
                    if (Inventory.getInstance().getCount(currentLetter.name) > usedCounts.get(currentLetter.name)) {
                        try {
                            usedCounts.put(currentLetter.name, usedCounts.get(currentLetter.name)+1);
                            putChar(currentLetter);
                            final Text countText = inventoryCounts.get(currentLetter.name);
                            final int newCount =(Inventory.getInstance().getCount(currentLetter.name)-usedCounts.get(currentLetter.name));
                            countText.setText("" + newCount);
                        } catch (Exception e) {
                            Debug.e("Failed to update inventory");
                        }
                    }
                }
            });
            this.registerTouchArea(block);
            whiteRect.attachChild(block);

            Debug.d("Checking inventory for "+currentLetter.name);
            Debug.d("Inventory says: "+this.game.inventory.getCount(currentLetter.name));
            final Text inventoryCount = new Text(startX+(64*i)+24, whiteRect.getHeight()/2-80, inventoryCountFont, ""+this.game.inventory.getCount(currentLetter.name), 4, game.activity.getVertexBufferObjectManager());
            whiteRect.attachChild(inventoryCount);
            this.inventoryCounts.put(currentLetter.name, inventoryCount);
            this.usedCounts.put(currentLetter.name, 0);
        }

    }

    private void eraseSpelling() {
        for (int i = 0; i < this.buildWord.chars.length; i++) {
            this.spelling[i] = ' ';
        }
    }
    public void close() {
        Inventory.getInstance().removeUpdateListener(this);
    }

    public void putChar(Letter letter) {
        if (this.cursorAt >= this.buildWord.chars.length) {
            Debug.d("Too many characters!");
            return;
        }
        final int startX = 200 - (this.buildWord.chars.length * 35) + 35; // TODO: replace magic numbers
        final ITextureRegion blockRegion = game.letterTiles.getTextureRegion(letter.sprite);
        final Sprite character = new Sprite(this.charBlocksX[cursorAt], this.charBlocksY[cursorAt], blockRegion, game.activity.getVertexBufferObjectManager());
        this.whiteRect.attachChild(character);
        this.charSprites[this.cursorAt] = character;
        this.spelling[cursorAt] = letter.chars[0];
        this.cursorAt++;

        this.game.playBlockSound(letter.phoneme);
        if (this.cursorAt == this.buildWord.chars.length) {
            final WordBuilderHUD that = this;
            Thread checker = new Thread() {
                public void run() {
                    if (String.valueOf(that.spelling).equals(String.valueOf(that.buildWord.chars))) {
                        try {Thread.sleep(700);} catch (InterruptedException e) {}
                        Debug.d("You spelled it!");
                        that.game.playBlockSound(that.buildWord.sound);
                        try {Thread.sleep(500);} catch (InterruptedException e) {}
                        try {
                            for (int i = 0; i < that.spelling.length; i++) {
                                final String letter = new String(spelling, i, 1);
                                usedCounts.put(letter, usedCounts.get(letter)-1);
                                Inventory.getInstance().subtract(letter);
                            }
                            that.game.hudManager.pop();
                            Inventory.getInstance().add(that.buildWord.name);
                        } catch (Exception e) {

                        }
                    } else {
                        try {Thread.sleep(500);} catch (InterruptedException e) {}
                        that.clear();
                    }
                }
            };
            checker.start();
        }
    }

    public synchronized void clear() {
        Debug.d("Clearing");
        this.game.activity.runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                cursorAt = 0;
                for (int i = 0; i < buildWord.chars.length; i++) {
                    if (charSprites[i] != null) {
                        whiteRect.detachChild(charSprites[i]);
                        charSprites[i] = null;
                    }
                    if (spelling[i] != ' ') {
                        usedCounts.put(new String(spelling, i, 1), 0);
                        InventoryItem[] items = {Inventory.getInstance().get(new String(spelling, i, 1))};
                        onInventoryUpdated(items);
                    }
                    spelling[i] = ' ';

                }
            }
        });
    }

    public void onInventoryUpdated(final InventoryItem[] items) {
        Debug.d("Updating BlockPlacementHUD inventory");
        for (int i = 0; i < items.length; i++) {
            Debug.d("Updating BlockPlacementHUD count for "+items[i].item_name.get());
            if (this.inventoryCounts.containsKey(items[i].item_name.get())) {
                Debug.d("New HUD count: "+items[i].quantity.get().toString());
                final Text countText = this.inventoryCounts.get(items[i].item_name.get());
                final int newCount =(items[i].quantity.get()-this.usedCounts.get(items[i].item_name.get()));
                countText.setText(""+newCount);
                //countText.setText("9");
            } else {
                Debug.e("[WordBuilderHUD] No HUD item for "+items[i].item_name.get());
            }
        }
    }

    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        // Block touch events
        return true;
    }
}
