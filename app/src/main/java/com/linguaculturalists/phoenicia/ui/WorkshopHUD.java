package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Builder;
import com.linguaculturalists.phoenicia.models.DefaultTile;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.WordBuilder;
import com.linguaculturalists.phoenicia.models.WorkshopBuilder;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameTextures;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.util.HashMap;
import java.util.Map;

/**
 * HUD for constructing a new Word out of \link Letter Letters \endlink in the player's Inventory..
 */
public class WorkshopHUD extends PhoeniciaHUD implements Inventory.InventoryUpdateListener {
    private static final int MAX_WORD_SIZE = 8;
    private PhoeniciaGame game;
    private DefaultTile tile;
    private Level level;
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
    private Scrollable lettersPane;
    private ClickDetector clickDetector;

    private Builder.BuildStatusUpdateHandler buildUpdateHandler;
    private WorkshopBuilder builder;
    private TiledSprite buildSprite;
    private Text buildProgress;
    private ButtonSprite tryButton;

    /**
     * A HUD for allowing the player to combine letters from their inventory to build a word
     * @param game A reference to the current PhoeniciaGame the HUD is running in
     * @param tile The word tile which the player clicked
     */
    public WorkshopHUD(final PhoeniciaGame game, final Level level, final DefaultTile tile) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        Inventory.getInstance().addUpdateListener(this);
        this.game = game;
        this.tile = tile;
        this.level = level;

        Inventory.getInstance().addUpdateListener(this);

        this.spelling = new char[MAX_WORD_SIZE];
        this.eraseSpelling();
        this.charBlocksX = new int[MAX_WORD_SIZE];
        this.charBlocksY = new int[MAX_WORD_SIZE];
        this.charSprites = new Sprite[MAX_WORD_SIZE];
        this.inventoryCounts = new HashMap<String, Text>();
        this.usedCounts = new HashMap<String, Integer>();

        this.cursorAt = 0;

        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                game.hudManager.pop();
            }
        });

        this.whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 800, 500, PhoeniciaContext.vboManager) {
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
        this.builder = tile.getBuilder(PhoeniciaContext.context);
        this.buildUpdateHandler = new Builder.BuildStatusUpdateHandler() {
            @Override
            public void onScheduled(Builder buildItem) {
                if (buildSprite != null) {
                    buildSprite.setCurrentTileIndex(2);
                }
                buildProgress.setVisible(false);
            }

            @Override
            public void onStarted(Builder buildItem) {
                if (buildSprite != null) {
                    buildSprite.setCurrentTileIndex(0);
                }
                buildProgress.setVisible(true);
            }

            @Override
            public void onCompleted(Builder buildItem) {
                if (buildSprite != null) {
                    buildSprite.setCurrentTileIndex(1);
                } else {
                    Debug.d("No sprite found for build item: "+buildItem);
                }
                if (buildProgress != null) {
                    buildProgress.setVisible(false);
                }
            }

            @Override
            public void onProgressChanged(Builder builtItem) {
                int remaining = builtItem.time.get() - builtItem.progress.get();
                if (remaining > 60) {
                    remaining = (remaining / 60);
                    buildProgress.setText("" + remaining + "m");
                } else if (remaining < 1) {
                    buildProgress.setVisible(false);
                } else {
                    buildProgress.setText("" + remaining + "s");
                }
            }
        };

        if(this.builder != null && this.builder.status.get() != Builder.NONE) {
            Debug.d("Preparing to display active builder for: "+this.builder.item_name.get());
            final Word buildWord = game.locale.word_map.get(this.builder.item_name.get());
            final ITiledTextureRegion wordSpriteRegion = game.wordSprites.get(buildWord);
            this.buildSprite = new TiledSprite(whiteRect.getWidth()/2, this.queuePane.getHeight()-48, wordSpriteRegion, PhoeniciaContext.vboManager);
            this.queuePane.attachChild(this.buildSprite);
            final ClickDetector builderClickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
                @Override
                public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                    Debug.d("Activated block: " + builder.getId());
                    if (builder.status.get() == Builder.COMPLETE) {
                        game.playBlockSound(buildWord.sound);
                        Inventory.getInstance().add(builder.item_name.get());
                        builder.item_name.set("");
                        builder.status.set(Builder.NONE);
                        builder.save(PhoeniciaContext.context);
                        buildSprite.setVisible(false);
                        tryButton.setVisible(true);
                    }
                }
            });
            Entity clickArea = new Entity(buildSprite.getX(), buildSprite.getY(), buildSprite.getWidth(), buildSprite.getHeight()) {
                @Override
                public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    return builderClickDetector.onManagedTouchEvent(pSceneTouchEvent);
                }
            };
            this.queuePane.attachChild(clickArea);
            this.registerTouchArea(clickArea);


            this.buildProgress = new Text(whiteRect.getWidth()/2, this.queuePane.getHeight()-90, GameFonts.inventoryCount(), "", 5, PhoeniciaContext.vboManager);
            this.queuePane.attachChild(buildProgress);

            builder.addUpdateHandler(this.buildUpdateHandler);

            switch (builder.status.get()) {
                case Builder.SCHEDULED:
                    this.buildUpdateHandler.onScheduled(builder);
                    break;
                case Builder.BUILDING:
                    this.buildUpdateHandler.onStarted(builder);
                    break;
                case Builder.COMPLETE:
                    this.buildUpdateHandler.onCompleted(builder);
                    break;
                default:
                    buildSprite.setCurrentTileIndex(3);
            }
            this.buildUpdateHandler.onProgressChanged(builder);
        }

        /**
         * Start word building area
         */
        this.buildPane = new Entity(whiteRect.getWidth()/2, whiteRect.getHeight()-175, whiteRect.getWidth(), 100);
        whiteRect.attachChild(buildPane);

        int startX = 96;
        for (int i = 0; i < MAX_WORD_SIZE; i++) {
            this.charBlocksX[i] = startX+(64*i);
            this.charBlocksY[i] = (int)this.buildPane.getHeight()-50;
            Rectangle borderRect = new Rectangle(startX+(64*i), this.buildPane.getHeight()-50, 60, 100, PhoeniciaContext.vboManager);
            borderRect.setColor(Color.RED);
            this.buildPane.attachChild(borderRect);

            Rectangle innerRect = new Rectangle(startX+(64*i), this.buildPane.getHeight()-50, 55, 95, PhoeniciaContext.vboManager);
            innerRect.setColor(Color.WHITE);
            this.buildPane.attachChild(innerRect);

        }
        ITextureRegion tryRegion = game.shellTiles.getTextureRegion(GameTextures.OK);
        this.tryButton = new ButtonSprite(startX+(64*MAX_WORD_SIZE), this.buildPane.getHeight()-50, tryRegion, PhoeniciaContext.vboManager);
        tryButton.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                checkSpelling();
            }
        });
        if (this.builder != null && this.builder.status.get() != Builder.NONE) {
            tryButton.setVisible(false);
        }
        this.registerTouchArea(tryButton);
        this.buildPane.attachChild(tryButton);

        ITextureRegion abortRegion = game.shellTiles.getTextureRegion(GameTextures.CANCEL);
        ButtonSprite abortButton = new ButtonSprite(startX+(64*(MAX_WORD_SIZE+1)), this.buildPane.getHeight()-50, abortRegion, PhoeniciaContext.vboManager);
        abortButton.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                clear();
            }
        });
        this.registerTouchArea(abortButton);
        this.buildPane.attachChild(abortButton);

        /**
         * Start available letters area
         */
        this.lettersPane = new Scrollable(this.getWidth()/2, (this.getHeight()/2)-125, whiteRect.getWidth(), 250, Scrollable.SCROLL_VERTICAL);
        //this.lettersPane.setClip(false);
        final int columns = 8;
        startX = 50;

        int offsetX = 0;
        int offsetY = (int) this.lettersPane.getHeight()-32;

        for (int i = 0; i < game.locale.letters.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 80;
                offsetX = 0;
            }
            final Letter currentLetter = game.locale.letters.get(i);
            Debug.d("Adding Builder letter: "+currentLetter.name+" (column: "+offsetX+")");
            final int tile_id = currentLetter.sprite;
            final ITiledTextureRegion blockRegion = new TiledTextureRegion(game.letterTextures.get(currentLetter),
                    game.letterSprites.get(currentLetter).getTextureRegion(0),
                    game.letterSprites.get(currentLetter).getTextureRegion(1),
                    game.letterSprites.get(currentLetter).getTextureRegion(2));
            final ButtonSprite block = new ButtonSprite(startX + (96 * offsetX), offsetY, blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    Debug.d("Activated block: " + currentLetter.name);
                    if (cursorAt >= MAX_WORD_SIZE) {
                        Debug.d("Can't put anymore characters");
                    } else if (Inventory.getInstance().getCount(currentLetter.name) > usedCounts.get(currentLetter.name)) {
                        try {
                            usedCounts.put(currentLetter.name, usedCounts.get(currentLetter.name) + 1);
                            putChar(currentLetter);
                            final Text countText = inventoryCounts.get(currentLetter.name);
                            final int newCount = (Inventory.getInstance().getCount(currentLetter.name) - usedCounts.get(currentLetter.name));
                            countText.setText("" + newCount);
                        } catch (Exception e) {
                            Debug.e("Failed to update inventory");
                        }
                    }
                }
            });
            this.registerTouchArea(block);
            this.lettersPane.attachChild(block);

            if (level.letters.contains(currentLetter)) {
                Debug.d("Checking inventory for "+currentLetter.name);
                Debug.d("Inventory says: "+this.game.inventory.getCount(currentLetter.name));
                final Text inventoryCount = new Text(startX + (96 * offsetX) + 24, offsetY-40, GameFonts.inventoryCount(), ""+this.game.inventory.getCount(currentLetter.name), 4, PhoeniciaContext.vboManager);
                this.lettersPane.attachChild(inventoryCount);
                this.inventoryCounts.put(currentLetter.name, inventoryCount);
                this.usedCounts.put(currentLetter.name, 0);
            } else {
                block.setEnabled(false);
            }

            offsetX++;
        }
        Debug.d("whiteRect width: " + whiteRect.getWidth() + ", lettersPage width: " + this.lettersPane.getWidth() + ", childRect width: " + this.lettersPane.childRect.getWidth());
        this.attachChild(this.lettersPane);
        this.registerTouchArea(this.lettersPane);
    }

    /**
     * Clear the letters that were used to try and build a word
     */
    private void eraseSpelling() {
        for (int i = 0; i < this.spelling.length; i++) {
            this.spelling[i] = ' ';
        }
    }

    /**
     * When closed, stop listening for inventory changes
     */
    public void close() {
        Inventory.getInstance().removeUpdateListener(this);
        if (this.builder != null) {
            this.builder.removeUpdateHandler(this.buildUpdateHandler);
        }
    }

    /**
     * Add the given letter to the next space in the spelling sequence for the word
     * @param letter Letter to add to the proposed spelling
     */
    public void putChar(Letter letter) {
        if (this.cursorAt >= MAX_WORD_SIZE) {
            Debug.d("Too many characters!");
            return;
        }
        final int startX = 200 - (this.spelling.length * 35) + 35; // TODO: replace magic numbers
        final ITextureRegion blockRegion = game.letterSprites.get(letter).getTextureRegion(0);
        final Sprite character = new Sprite(this.charBlocksX[cursorAt], this.charBlocksY[cursorAt], blockRegion, PhoeniciaContext.vboManager);
        this.buildPane.attachChild(character);
        this.charSprites[this.cursorAt] = character;
        this.spelling[cursorAt] = letter.chars[0];
        this.cursorAt++;

        this.game.playBlockSound(letter.phoneme);
    }

    public void checkSpelling() {
        final WorkshopHUD that = this;
        final String trySpelling = String.valueOf(spelling).trim();
        Debug.d("Checking word attempt: '"+trySpelling+"'");
        Thread checker = new Thread() {
            public void run() {
                final Word tryWord = game.locale.word_map.get(trySpelling);
                // Sound out the spelling
                for (int i = 0; i < trySpelling.length(); i++) {
                    final Letter letter = game.locale.letter_map.get(trySpelling.substring(i, i+1));
                    game.playBlockSound(letter.phoneme);
                    try {Thread.sleep(200);} catch (InterruptedException e) {}
                }
                if (tryWord != null) {
                    try {Thread.sleep(700);} catch (InterruptedException e) {}
                    Debug.d("You spelled it!");
                    that.game.playBlockSound(tryWord.sound);
                    try {Thread.sleep(500);} catch (InterruptedException e) {}
                    game.activity.runOnUpdateThread(new Runnable() {
                        @Override
                        public void run() {
                            Debug.d("Preparing to build word: "+tryWord.name);
                            try {
                                for (int i = 0; i < trySpelling.length(); i++) {
                                    final String letter = new String(spelling, i, 1);
                                    usedCounts.put(letter, usedCounts.get(letter)-1);
                                    Inventory.getInstance().subtract(letter);
                                }
                                Debug.d("Creating new WordBuilder for " + tryWord.name);
                                that.createWord(tryWord, that.tile);
                                that.game.hudManager.pop();
                            } catch (Exception e) {
                                Debug.e("Error subtracting letter: "+e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Debug.d("Locale has no word definition for "+trySpelling);
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

    public WorkshopBuilder createWord(final Word word, DefaultTile tile) {
        WorkshopBuilder builder = tile.getBuilder(PhoeniciaContext.context);
        if (builder == null) {
            builder = new WorkshopBuilder(game.session, tile);
        }
        builder.item_name.set(word.name);
        builder.time.set(word.time);
        builder.progress.set(0);
        builder.status.set(Builder.SCHEDULED);
        builder.save(PhoeniciaContext.context);
        builder.addUpdateHandler(new Builder.BuildStatusUpdateHandler() {
            @Override
            public void onCompleted(Builder buildItem) {
                Debug.d("WordBuilder for " + buildItem.item_name.get() + " has completed");
                game.playBlockSound(word.sound);
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
            }
        });
        game.addBuilder(builder);
        builder.start();
        tile.setBuilder(builder);
        return builder;
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
                for (int i = 0; i < spelling.length; i++) {
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
        Debug.d("Updating WorkshopHUD inventory");
        for (int i = 0; i < items.length; i++) {
            Debug.d("Updating WorkshopHUD count for "+items[i].item_name.get());
            if (this.inventoryCounts.containsKey(items[i].item_name.get())) {
                Debug.d("New HUD count: "+items[i].quantity.get().toString());
                final Text countText = this.inventoryCounts.get(items[i].item_name.get());
                final int newCount =(items[i].quantity.get()-this.usedCounts.get(items[i].item_name.get()));
                countText.setText(""+newCount);
                //countText.setText("9");
            } else {
                Debug.e("[Workshop] No HUD item for "+items[i].item_name.get());
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
