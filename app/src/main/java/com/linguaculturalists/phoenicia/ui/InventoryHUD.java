package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.LetterSprite;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.components.WordSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.util.List;

/**
 * Display the \link InventoryItem InventoryItems \endlink with a positive balance and allow selling them.
 */
public class InventoryHUD extends PhoeniciaHUD {
    private PhoeniciaGame game;
    private Rectangle whiteRect;
    private ClickDetector clickDetector;

    /**
     * Display the \link InventoryItem InventoryItems \endlink with a positive balance and allow selling them.
     * @param game Reference to the PhoeniciaGame this HUD is running in
     */
    public InventoryHUD(final PhoeniciaGame game) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        this.game = game;
        // Close the HUD if the user clicks outside the whiteRect
        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                game.hudManager.pop();
            }
        });

        this.whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 400, 400, PhoeniciaContext.vboManager) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
                return true;
            }
        };
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);
        this.registerTouchArea(whiteRect);

        Scrollable itemsPane = new Scrollable(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 400, 400, Scrollable.SCROLL_VERTICAL);
        itemsPane.setPadding(32);
        this.attachChild(itemsPane);
        this.registerTouchArea(itemsPane);

        final int columns = 4;
        int startX = (int) (itemsPane.getWidth() / 2) - (columns * 32) - 16;
        int startY = (int) itemsPane.getHeight() - 50;

        int offsetX = 0;
        int offsetY = startY;

        List<InventoryItem> items = Inventory.getInstance().items();
        for (int i = 0; i < items.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 96;
                offsetX = 0;
            }
            final InventoryItem item = items.get(i);
            final Letter currentLetter = game.locale.letter_map.get(item.item_name.get());
            if (currentLetter == null) {
                Debug.d("Inventory Word: "+item.item_name.get());
                continue;
            }
            Debug.d("Adding Builder letter: " + currentLetter.name + " (tile: " + currentLetter.tile + ")");
            final int tile_id = currentLetter.sprite;
            final ITiledTextureRegion blockRegion = game.letterSprites.get(currentLetter);
            final LetterSprite block = new LetterSprite(startX + (96 * offsetX), offsetY, currentLetter, item.quantity.get(), blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    Debug.d("Inventory Item " + currentLetter.name + " clicked");
                    try {
                        Inventory.getInstance().subtract(currentLetter.name);
                        Bank.getInstance().credit(currentLetter.sell);
                        final int newCount = Inventory.getInstance().getCount(currentLetter.name);
                        block.setCount(newCount);
                        if (newCount < 1) {
                            block.setEnabled(false);
                        }
                    } catch (Exception e) {
                        Debug.d("Could not sell "+currentLetter.name, e);

                    }
                }
            });
            this.registerTouchArea(block);
            itemsPane.attachChild(block);
            offsetX++;

        }
        // Do the same for words
        for (int i = 0; i < items.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 80;
                offsetX = 0;
            }
            final InventoryItem item = items.get(i);
            final Word currentWord = game.locale.word_map.get(item.item_name.get());
            if (currentWord == null) {
                Debug.d("Inventory letter: "+item.item_name.get());
                continue;
            }
            Debug.d("Adding Builder word: " + currentWord.name + " (tile: " + currentWord.tile + ")");
            final int tile_id = currentWord.sprite;
            final ITiledTextureRegion blockRegion = game.wordSprites.get(currentWord);
            final WordSprite block = new WordSprite(startX + (96 * offsetX), offsetY, currentWord, item.quantity.get(), blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    Debug.d("Inventory Item " + currentWord.name + " clicked");
                    try {
                        Inventory.getInstance().subtract(currentWord.name);
                        Bank.getInstance().credit(currentWord.sell);
                        final int newCount = Inventory.getInstance().getCount(currentWord.name);
                        block.setCount(newCount);
                        if (newCount < 1) {
                            block.setEnabled(false);
                        }
                    } catch (Exception e) {
                        Debug.d("Could not sell "+currentWord.name, e);

                    }
                }
            });
            this.registerTouchArea(block);
            itemsPane.attachChild(block);
            offsetX++;

        }
    }

    /**
     * Capture scene touch events and look for click events
     * @param pSceneTouchEvent
     * @return
     */
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        // Block touch events
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        if (handled) return true;
        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
        // TODO: Fix inventory selling
    }
}
