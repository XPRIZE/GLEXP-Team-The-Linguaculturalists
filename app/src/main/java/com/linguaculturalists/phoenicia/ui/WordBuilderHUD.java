package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Builder;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.WordBuilder;
import com.linguaculturalists.phoenicia.models.WordTile;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
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
public class WordBuilderHUD extends PhoeniciaHUD implements Inventory.InventoryUpdateListener {

    private PhoeniciaGame game;
    private WordTile tile;
    private Word buildWord;
    private char spelling[];
    private int charBlocksX[];
    private int charBlocksY[];
    private Sprite charSprites[];
    private Map<String, Integer> usedCounts;
    private Map<String, Text> inventoryCounts;

    private int cursorAt;
    private int readyToCollect;

    private Rectangle whiteRect;
    private Entity queuePane;
    private Entity buildPane;
    private Entity lettersPane;
    private ClickDetector clickDetector;

    private Builder.BuildStatusUpdateHandler queueUpdateHandler;
    private List<WordBuilder> buildQueue;
    /**
     * A HUD for allowing the player to combine letters from their inventory to build a word
     * @param game A reference to the current PhoeniciaGame the HUD is running in
     * @param level The current level, used to display only available letters
     * @param tile The word tile which the player clicked
     */
    public WordBuilderHUD(final PhoeniciaGame game, final Level level, final WordTile tile) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        Inventory.getInstance().addUpdateListener(this);
        this.game = game;
        this.tile = tile;
        this.buildWord = tile.word;

        Inventory.getInstance().addUpdateListener(this);

        this.spelling = new char[tile.word.chars.length];
        this.eraseSpelling();
        this.charBlocksX = new int[tile.word.chars.length];
        this.charBlocksY = new int[tile.word.chars.length];
        this.charSprites = new Sprite[tile.word.chars.length];
        this.inventoryCounts = new HashMap<String, Text>();
        this.usedCounts = new HashMap<String, Integer>();

        this.cursorAt = 0;

        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                game.hudManager.pop();
            }
        });

        this.whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 600, 350, PhoeniciaContext.vboManager) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                //Debug.d("Word builder dialog touched");
                super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
                return true;
            }
        };
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);
        this.registerTouchArea(whiteRect);

        /**
         * Start build queue area
         */
        this.queuePane = new Entity(whiteRect.getWidth()/2, whiteRect.getHeight()-50, whiteRect.getWidth(), 100);
        whiteRect.attachChild(queuePane);

        final Map<Builder, TiledSprite> queueSpriteMap = new HashMap<Builder, TiledSprite>();
        final Map<Builder, Text> queueProgressMap = new HashMap<Builder, Text>();
        this.buildQueue = tile.getQueue();
        this.queueUpdateHandler = new Builder.BuildStatusUpdateHandler() {
            @Override
            public void onScheduled(Builder buildItem) {
                if (queueSpriteMap.containsKey(buildItem)) {
                    queueSpriteMap.get(buildItem).setCurrentTileIndex(2);
                }
                queueProgressMap.get(buildItem).setVisible(false);
            }

            @Override
            public void onStarted(Builder buildItem) {
                if (queueSpriteMap.containsKey(buildItem)) {
                    queueSpriteMap.get(buildItem).setCurrentTileIndex(0);
                }
                queueProgressMap.get(buildItem).setVisible(true);
            }

            @Override
            public void onCompleted(Builder buildItem) {
                if (queueSpriteMap.containsKey(buildItem)) {
                    queueSpriteMap.get(buildItem).setCurrentTileIndex(1);
                    buildItem.removeUpdateHandler(this);
                } else {
                    Debug.d("No sprite found for build item: "+buildItem);
                }
                if (queueProgressMap.containsKey(buildItem)) {
                    queueProgressMap.get(buildItem).setVisible(false);
                }
            }

            @Override
            public void onProgressChanged(Builder builtItem) {
                int remaining = builtItem.time.get() - builtItem.progress.get();
                if (remaining > 60) {
                    remaining = (remaining / 60);
                    queueProgressMap.get(builtItem).setText("" + remaining + "m");
                } else if (remaining < 1) {
                    queueProgressMap.get(builtItem).setVisible(false);
                } else {
                    queueProgressMap.get(builtItem).setText(""+remaining+"s");
                }
            }
        };

        int startX = 50;
        for(final WordBuilder builder : this.buildQueue) {
            final ITiledTextureRegion wordSpriteRegion = game.wordSprites.get(tile.word);
            TiledSprite wordSprite = new TiledSprite(startX, this.queuePane.getHeight()-48, wordSpriteRegion, PhoeniciaContext.vboManager);
            queueSpriteMap.put(builder, wordSprite);
            /*
            wordSprite.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    Debug.d("Activated block: " + builder.getId());
                    game.playBlockSound(buildWord.sound);
                }
            });
            this.registerTouchArea(wordSprite);
            */
            this.queuePane.attachChild(wordSprite);

            final Text builderProgress = new Text(startX, this.queuePane.getHeight()-90, GameFonts.inventoryCount(), "", 5, PhoeniciaContext.vboManager);
            this.queuePane.attachChild(builderProgress);

            queueProgressMap.put(builder, builderProgress);
            builder.addUpdateHandler(this.queueUpdateHandler);

            switch (builder.status.get()) {
                case Builder.SCHEDULED:
                    this.queueUpdateHandler.onScheduled(builder);
                    break;
                case Builder.BUILDING:
                    this.queueUpdateHandler.onStarted(builder);
                    break;
                case Builder.COMPLETE:
                    this.queueUpdateHandler.onCompleted(builder);
                    break;
                default:
                    wordSprite.setCurrentTileIndex(3);
            }
            this.queueUpdateHandler.onProgressChanged(builder);
            startX += 64;
        }

        /**
         * Start word building area
         */
        this.buildPane = new Entity(whiteRect.getWidth()/2, whiteRect.getHeight()-175, whiteRect.getWidth(), 100);
        whiteRect.attachChild(buildPane);

        ITextureRegion wordSpriteRegion = game.wordSprites.get(tile.word).getTextureRegion(0);
        ButtonSprite wordSprite = new ButtonSprite(50, this.buildPane.getHeight()-50, wordSpriteRegion, PhoeniciaContext.vboManager);
        wordSprite.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                Debug.d("Activated block: " + buildWord.name);
                game.playBlockSound(buildWord.sound);
            }
        });
        this.registerTouchArea(wordSprite);
        this.buildPane.attachChild(wordSprite);

        startX = (int)(wordSprite.getX() + 64);
        for (int i = 0; i < tile.word.chars.length; i++) {
            this.charBlocksX[i] = startX+(64*i);
            this.charBlocksY[i] = (int)wordSprite.getY();
            Rectangle borderRect = new Rectangle(startX+(64*i), this.buildPane.getHeight()-50, 60, 100, PhoeniciaContext.vboManager);
            borderRect.setColor(Color.RED);
            this.buildPane.attachChild(borderRect);

            Rectangle innerRect = new Rectangle(startX+(64*i), this.buildPane.getHeight()-50, 55, 95, PhoeniciaContext.vboManager);
            innerRect.setColor(Color.WHITE);
            this.buildPane.attachChild(innerRect);

        }

        /**
         * Start available letters area
         */
        this.lettersPane = new Entity(whiteRect.getWidth()/2, whiteRect.getHeight()-225, whiteRect.getWidth(), 150);
        whiteRect.attachChild(this.lettersPane);
        final int columns = 6;
        startX = 50;

        int offsetX = 0;
        int offsetY = (int) this.lettersPane.getHeight()/2-50;

        for (int i = 0; i < level.letters.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 80;
                offsetX = 0;
            }
            final Letter currentLetter = level.letters.get(i);
            Debug.d("Adding Builder letter: "+currentLetter.name+" (tile: "+currentLetter.tile+")");
            final int tile_id = currentLetter.sprite;
            final ITextureRegion blockRegion = new TiledTextureRegion(game.letterTextures.get(currentLetter),
                    game.letterSprites.get(currentLetter).getTextureRegion(0),
                    game.letterSprites.get(currentLetter).getTextureRegion(1),
                    game.letterSprites.get(currentLetter).getTextureRegion(2));
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
            this.lettersPane.attachChild(block);

            Debug.d("Checking inventory for "+currentLetter.name);
            Debug.d("Inventory says: "+this.game.inventory.getCount(currentLetter.name));
            final Text inventoryCount = new Text(startX + (96 * offsetX) + 24, offsetY-40, GameFonts.inventoryCount(), ""+this.game.inventory.getCount(currentLetter.name), 4, PhoeniciaContext.vboManager);
            this.lettersPane.attachChild(inventoryCount);
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
        for (WordBuilder builder : this.buildQueue) {
            builder.removeUpdateHandler(this.queueUpdateHandler);
        }
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
        final ITextureRegion blockRegion = game.letterSprites.get(letter).getTextureRegion(0);
        final Sprite character = new Sprite(this.charBlocksX[cursorAt], this.charBlocksY[cursorAt], blockRegion, PhoeniciaContext.vboManager);
        this.buildPane.attachChild(character);
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
                                Debug.d("Preparing to build word: "+tile.word.name);
                                try {
                                    for (int i = 0; i < that.spelling.length; i++) {
                                        final String letter = new String(spelling, i, 1);
                                        usedCounts.put(letter, usedCounts.get(letter)-1);
                                        Inventory.getInstance().subtract(letter);
                                    }
                                    Debug.d("Creating new WordBuilder for " + tile.word.name);
                                    tile.createWord();
                                    that.game.hudManager.pop();
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
                        buildPane.detachChild(charSprites[i]);
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
        Debug.d("Updating WordBuilderHUD inventory");
        for (int i = 0; i < items.length; i++) {
            Debug.d("Updating WordBuilderHUD count for "+items[i].item_name.get());
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
     * @param pSceneTouchEvent
     * @return
     */
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        // Block touch events
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        //Debug.d("Inventory HUD touched, handled? "+handled);
        if (handled) return true;
        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
    }
}
