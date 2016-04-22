package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

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
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HUD for constructing a new Word out of \link Letter Letters \endlink in the player's Inventory..
 */
public class WordBuilderHUD extends PhoeniciaHUD implements Inventory.InventoryUpdateListener, IOnSceneTouchListener {

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

    /**
     * A HUD for allowing the player to combine letters from their inventory to build a word
     * @param game A reference to the current PhoeniciaGame the HUD is running in
     * @param level The current level, used to display only available letters
     * @param word The word which the player is attempting to build
     */
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

        this.whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 400, 400, PhoeniciaContext.vboManager);
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);

        ITextureRegion wordSpriteRegion = game.wordTiles.get(word).getTextureRegion(0);
        ButtonSprite wordSprite = new ButtonSprite((whiteRect.getWidth()/2), whiteRect.getHeight()-50, wordSpriteRegion, PhoeniciaContext.vboManager);
        wordSprite.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                Debug.d("Activated block: " + buildWord.name);
                game.playBlockSound(buildWord.sound);
            }
        });
        this.registerTouchArea(wordSprite);
        whiteRect.attachChild(wordSprite);

        int startX = (int)(whiteRect.getWidth()/2) - (word.chars.length * 35) + 35;
        for (int i = 0; i < word.chars.length; i++) {
            this.charBlocksX[i] = startX+(64*i);
            this.charBlocksY[i] = (int)whiteRect.getHeight()/2+50;
            Rectangle borderRect = new Rectangle(startX+(64*i), whiteRect.getHeight()/2+50, 60, 100, PhoeniciaContext.vboManager);
            borderRect.setColor(Color.RED);
            whiteRect.attachChild(borderRect);

            Rectangle innerRect = new Rectangle(startX+(64*i), whiteRect.getHeight()/2+50, 55, 95, PhoeniciaContext.vboManager);
            innerRect.setColor(Color.WHITE);
            whiteRect.attachChild(innerRect);

        }

        final Font inventoryCountFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.RED_ARGB_PACKED_INT);
        inventoryCountFont.load();

        final int columns = 4;
        startX = (int)(whiteRect.getWidth()/2) - (columns * 32) - 16;

        int offsetX = 0;
        int offsetY = (int) whiteRect.getHeight()/2-50;

        for (int i = 0; i < level.letters.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 80;
                offsetX = 0;
            }
            final Letter currentLetter = level.letters.get(i);
            Debug.d("Adding Builder letter: "+currentLetter.name+" (tile: "+currentLetter.tile+")");
            final int tile_id = currentLetter.sprite;
            final ITextureRegion blockRegion = new TiledTextureRegion(game.letterTextures.get(currentLetter),
                    game.letterTiles.get(currentLetter).getTextureRegion(0),
                    game.letterTiles.get(currentLetter).getTextureRegion(1),
                    game.letterTiles.get(currentLetter).getTextureRegion(2));
            final ButtonSprite block = new ButtonSprite(startX + (96 * offsetX), offsetY, blockRegion, PhoeniciaContext.vboManager);
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
            final Text inventoryCount = new Text(startX + (96 * offsetX) + 24, offsetY-40, inventoryCountFont, ""+this.game.inventory.getCount(currentLetter.name), 4, PhoeniciaContext.vboManager);
            whiteRect.attachChild(inventoryCount);
            this.inventoryCounts.put(currentLetter.name, inventoryCount);
            this.usedCounts.put(currentLetter.name, 0);

            offsetX++;
        }

    }

    /**
     * Clear the letters that were used to try and build a word
     */
    private void eraseSpelling() {
        for (int i = 0; i < this.buildWord.chars.length; i++) {
            this.spelling[i] = ' ';
        }
    }

    /**
     * When closed, stop listening for inventory changes
     */
    public void close() {
        Inventory.getInstance().removeUpdateListener(this);
    }

    /**
     * Add the given letter to the next space in the spelling sequence for the word
     * @param letter Letter to add to the proposed spelling
     */
    public void putChar(Letter letter) {
        if (this.cursorAt >= this.buildWord.chars.length) {
            Debug.d("Too many characters!");
            return;
        }
        final int startX = 200 - (this.buildWord.chars.length * 35) + 35; // TODO: replace magic numbers
        final ITextureRegion blockRegion = game.letterTiles.get(letter).getTextureRegion(0);
        final Sprite character = new Sprite(this.charBlocksX[cursorAt], this.charBlocksY[cursorAt], blockRegion, PhoeniciaContext.vboManager);
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
                        game.activity.runOnUpdateThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    for (int i = 0; i < that.spelling.length; i++) {
                                        final String letter = new String(spelling, i, 1);
                                        usedCounts.put(letter, usedCounts.get(letter)-1);
                                        Inventory.getInstance().subtract(letter);
                                    }
                                    that.game.hudManager.pop();
                                    Inventory.getInstance().add(that.buildWord.name);
                                } catch (Exception e) {
                                    Debug.e("Error subtracting letter: "+e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        try {Thread.sleep(500);} catch (InterruptedException e) {}
                        game.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                that.clear();
                            }
                        });
                    }
                }
            };
            checker.start();
        }
    }

    /**
     * Aport spelling attempt and return used letters to the player's Inventory
     */
    public synchronized void clear() {
        Debug.d("Clearing");
        PhoeniciaContext.activity.runOnUpdateThread(new Runnable() {
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

    /**
     * Handle changes in the player's inventory by resetting the counter under each letter
     * @param items The items which have changed
     */
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

    /**
     * Capture scene touch events without passing them through
     * @param pScene
     * @param pSceneTouchEvent
     * @return
     */
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        // TODO: instead of ignoring touch events outside the HUD, make them trigger closing the HUD
        return true;
    }
}
