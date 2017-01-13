package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.BorderRectangle;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.components.WordSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Builder;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.WordBuilder;
import com.linguaculturalists.phoenicia.models.WordTile;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameSounds;
import com.linguaculturalists.phoenicia.util.GameUI;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.FadeOutModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.IModifier;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HUD for constructing a new Word out of \link Letter Letters \endlink in the player's Inventory..
 */
public class WordBuilderHUD extends PhoeniciaHUD implements Inventory.InventoryUpdateListener {

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

    private BorderRectangle whiteRect;
    private Entity buildPane;
    private Entity lettersPane;
    private ClickDetector clickDetector;

    private Builder.BuildStatusUpdateHandler queueUpdateHandler;
    private List<WordBuilder> buildQueue;

    private ButtonSprite wordSprite;
    private Map<Builder, TiledSprite> queueSpriteMap;
    private Map<Builder, Text> queueProgressMap;
    private Map<Builder, ITouchArea> queueTouchAreaMap;

    private static final int maxQueueSize = 5;

    /**
     * A HUD for allowing the player to combine letters from their inventory to build a word
     * @param game A reference to the current PhoeniciaGame the HUD is running in
     * @param level The current level, used to display only available letters
     * @param tile The word tile which the player clicked
     */
    public WordBuilderHUD(final PhoeniciaGame game, final Level level, final WordTile tile) {
        super(game);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        Inventory.getInstance().addUpdateListener(this);
        this.game = game;
        this.tile = tile;
        this.buildWord = tile.word;

        Inventory.getInstance().addUpdateListener(this);

        this.spelling = new char[tile.word.chars.length];
        this.charBlocksX = new int[tile.word.chars.length];
        this.charBlocksY = new int[tile.word.chars.length];
        this.charSprites = new Sprite[tile.word.chars.length];
        this.inventoryCounts = new HashMap<String, Text>();
        this.usedCounts = new HashMap<String, Integer>();
        this.cursorAt = 0;
        this.eraseSpelling();

        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                finish();
            }
        });

        Rectangle border = new Rectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 604, 454, PhoeniciaContext.vboManager);
        this.attachChild(border);
        this.whiteRect = new BorderRectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 600, 450, PhoeniciaContext.vboManager) {
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

        ITextureRegion bannerRegion = GameUI.getInstance().getBlueBanner();
        Sprite banner = new Sprite(whiteRect.getWidth()/2, whiteRect.getHeight()+8, bannerRegion, PhoeniciaContext.vboManager);
        banner.setScaleX(whiteRect.getWidth() / (bannerRegion.getWidth() * 0.6f));
        whiteRect.attachChild(banner);

        /**
         * Start build queue area
         */
        float buildQueueStartX = this.whiteRect.getWidth()/2 - (32*maxQueueSize) + 32;
        for (int i = 0; i < this.maxQueueSize; i++) {
            Rectangle queueSpace = new Rectangle(buildQueueStartX+(64*i), this.whiteRect.getHeight()-80, 60, 2, PhoeniciaContext.vboManager);
            queueSpace.setColor(0.5f, 0.5f, 0.5f);
            this.whiteRect.attachChild(queueSpace);

        }
        queueSpriteMap = new HashMap<Builder, TiledSprite>();
        queueProgressMap = new HashMap<Builder, Text>();
        queueTouchAreaMap = new HashMap<Builder, ITouchArea>();
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

        int builderIndex = 0;
        Debug.d("Adding "+this.buildQueue.size()+" existing builders");
        for(final WordBuilder builder : this.buildQueue) {
            Debug.d("Adding queue builder "+builderIndex);
            builderIndex++;
            this.addWordToQueue(builder, false);
        }

        /**
         * Start word building area
         */
        this.buildPane = new Entity(whiteRect.getWidth()/2, whiteRect.getHeight()-175, whiteRect.getWidth(), 100);
        whiteRect.attachChild(buildPane);

        ITextureRegion wordSpriteRegion = game.wordSprites.get(tile.word).getTextureRegion(0);
        wordSprite = new ButtonSprite(50, this.buildPane.getHeight()-50, wordSpriteRegion, PhoeniciaContext.vboManager);
        wordSprite.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                Debug.d("Activated spelling sprite: " + buildWord.name);
                game.playBlockSound(buildWord.sound);
            }
        });
        this.registerTouchArea(wordSprite);
        this.buildPane.attachChild(wordSprite);

        int startX = (int)(wordSprite.getX() + 64);
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
        this.lettersPane = new Scrollable(GameActivity.CAMERA_WIDTH/2, (GameActivity.CAMERA_HEIGHT/2) - (whiteRect.getHeight()/2)+100, whiteRect.getWidth(), 200, Scrollable.SCROLL_VERTICAL);
        this.attachChild(this.lettersPane);
        this.registerTouchArea(this.lettersPane);
        final int columns = 6;
        startX = 50;

        int offsetX = 0;
        int offsetY = (int) this.lettersPane.getHeight()-50;

        for (int i = 0; i < level.letters.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 80;
                offsetX = 0;
            }
            final Letter currentLetter = level.letters.get(i);
            Debug.d("Adding Builder letter: "+currentLetter.name+" (tile: "+currentLetter.tile+")");
            final int tile_id = currentLetter.sprite;
            final ITiledTextureRegion blockRegion = game.letterSprites.get(currentLetter);
            final ButtonSprite block = new ButtonSprite(startX + (96 * offsetX), offsetY, blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    Debug.d("Activated letter sprite: " + currentLetter.name);
                    if (Inventory.getInstance().getCount(currentLetter.name) > usedCounts.get(currentLetter.name)) {
                        try {
                            putChar(currentLetter);
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
            if (charSprites[i] != null) {
                buildPane.detachChild(charSprites[i]);
                charSprites[i] = null;
            }
            this.spelling[i] = ' ';
        }
        cursorAt = 0;
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
        usedCounts.put(letter.name, usedCounts.get(letter.name)+1);
        final Text countText = inventoryCounts.get(letter.name);
        final int newCount =(Inventory.getInstance().getCount(letter.name)-usedCounts.get(letter.name));
        countText.setText("" + newCount);

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
                        if (buildQueue.size() < maxQueueSize) {
                            game.activity.runOnUpdateThread(new Runnable() {
                                @Override
                                public void run() {
                                    that.createWord();
                                    that.eraseSpelling();
                                }
                            });
                        } else {
                            game.activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    that.clear();
                                }
                            });
                        }
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

    protected void createWord() {
        Debug.d("Preparing to build word: " + tile.word.name);
        try {
            for (int i = 0; i < spelling.length; i++) {
                final String letter = new String(spelling, i, 1);
                usedCounts.put(letter, usedCounts.get(letter)-1);
                Inventory.getInstance().subtract(letter);
            }
            Debug.d("Creating new WordBuilder for " + tile.word.name);
            WordBuilder builder = tile.createWord();
            this.addWordToQueue(builder, true);
            GameSounds.play(GameSounds.COMPLETE);
        } catch (Exception e) {
            Debug.e("Error subtracting letter: "+e.getMessage());
            e.printStackTrace();
        }
    }

    protected void addWordToQueue(final WordBuilder builder, final boolean animate) {
        float buildQueueStartX = this.whiteRect.getWidth()/2 - (32*maxQueueSize) + 32;
        float startX = buildQueueStartX + (64 * this.queueSpriteMap.size());
        final ITiledTextureRegion wordSpriteRegion = game.wordSprites.get(tile.word);
        final TiledSprite wordSprite = new TiledSprite(startX, this.whiteRect.getHeight()-48, wordSpriteRegion, PhoeniciaContext.vboManager);
        this.whiteRect.attachChild(wordSprite);
        queueSpriteMap.put(builder, wordSprite);
        final ClickDetector builderClickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                if (builder.status.get() == Builder.COMPLETE) {
                    collectWord(wordSprite, builder);
                }
            }
        });
        Entity clickArea = new Entity(wordSprite.getWidth()/2, wordSprite.getHeight()/2, wordSprite.getWidth(), wordSprite.getHeight()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                return builderClickDetector.onManagedTouchEvent(pSceneTouchEvent);
            }
        };
        queueTouchAreaMap.put(builder, clickArea);
        wordSprite.attachChild(clickArea);
        this.registerTouchArea(clickArea);
        if (animate) {
            wordSprite.registerEntityModifier(new MoveYModifier(0.5f, this.buildPane.getY(), this.whiteRect.getHeight()-48));
            wordSprite.registerEntityModifier(new MoveXModifier(0.5f, this.wordSprite.getX(), startX));
        }


        final Text builderProgress = new Text(startX, this.whiteRect.getHeight()-96, GameFonts.inventoryCount(), "", 5, PhoeniciaContext.vboManager);
        this.whiteRect.attachChild(builderProgress);

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

    }

    protected void collectWord(final TiledSprite wordSprite, final WordBuilder builder) {
        game.playBlockSound(buildWord.sound);
        Inventory.getInstance().add(builder.item_name.get());
        game.session.addExperience(buildWord.points);
        int builderIndex = this.buildQueue.indexOf(builder);
        Debug.d("Removing queue item at " + builderIndex);
        for (int i = builderIndex+1; i >= 0 && i < this.buildQueue.size(); i++) {
            Debug.d("Moving queue item at "+i);
            WordBuilder nextInQueue = this.buildQueue.get(i);
            TiledSprite nextSprite = this.queueSpriteMap.get(nextInQueue);
            nextSprite.registerEntityModifier(new MoveXModifier(0.5f, nextSprite.getX(), nextSprite.getX()-64));
            Text nextProgress = this.queueProgressMap.get(nextInQueue);
            nextProgress.registerEntityModifier(new MoveXModifier(0.5f, nextProgress.getX(), nextProgress.getX()-64));
        }
        this.queueSpriteMap.remove(builder);
        wordSprite.registerEntityModifier(new ParallelEntityModifier(
                new MoveYModifier(0.5f, wordSprite.getY(), wordSprite.getY() + 128),
                new FadeOutModifier(0.5f)
        ));

        this.unregisterTouchArea(this.queueTouchAreaMap.get(builder));
        this.queueTouchAreaMap.remove(builder);

        this.buildQueue.remove(builder);
        tile.checkAttention();
        builder.delete(PhoeniciaContext.context);
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

    @Override
    public void finish() {
        game.hudManager.clear();
    }


}
