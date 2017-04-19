package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.BorderRectangle;
import com.linguaculturalists.phoenicia.components.LetterSprite;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.components.WordSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.Market;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameSounds;
import com.linguaculturalists.phoenicia.util.GameUI;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.util.List;

/**
 * Display the \link InventoryItem InventoryItems \endlink with a positive balance and allow selling them.
 */
public class InventoryHUD extends PhoeniciaHUD {
    private Rectangle whiteRect;
    private ClickDetector clickDetector;

    /**
     * Display the \link InventoryItem InventoryItems \endlink with a positive balance and allow selling them.
     * @param game Reference to the PhoeniciaGame this HUD is running in
     */
    public InventoryHUD(final PhoeniciaGame game) {
        super(game);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        this.game = game;
        // Close the HUD if the user clicks outside the whiteRect
        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                finish();
            }
        });

        this.whiteRect = new BorderRectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 400, 500, PhoeniciaContext.vboManager) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
                return true;
            }
        };
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);
        this.registerTouchArea(whiteRect);

        int giftIconHeight = 0;
        if (Market.getInstance().filledCount() >= game.locale.marketBlock.gifts_after) {
            giftIconHeight = 120;
        }
        Scrollable itemsPane = new Scrollable(GameActivity.CAMERA_WIDTH / 2, (GameActivity.CAMERA_HEIGHT / 2) - 10 + (giftIconHeight/2), 400, 500-giftIconHeight, Scrollable.SCROLL_VERTICAL);
        itemsPane.setPadding(32);
        this.attachChild(itemsPane);
        this.registerTouchArea(itemsPane);

        ITextureRegion bannerRegion = GameUI.getInstance().getGreenBanner();
        Sprite banner = new Sprite(whiteRect.getX(), whiteRect.getY()+(whiteRect.getHeight()/2), bannerRegion, PhoeniciaContext.vboManager);
        Text name = new Text(banner.getWidth()/2, 120, GameFonts.defaultHUDDisplay(), game.locale.inventoryBlock.name, game.locale.inventoryBlock.name.length(), new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        banner.attachChild(name);
        this.attachChild(banner);

        final int columns = 4;
        int startX = (int) (itemsPane.getWidth() / 2) - (columns * 32) - 16;
        int startY = (int) itemsPane.getHeight() - 50;

        int offsetX = 0;
        int offsetY = startY;

        List<InventoryItem> items = Inventory.getInstance().items();
        for (int i = 0; i < items.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 100;
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
                    sellLetter(block);
                }
            });
            itemsPane.registerTouchArea(block);
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
                    sellWord(block);
                }
            });
            itemsPane.registerTouchArea(block);
            itemsPane.attachChild(block);
            offsetX++;

        }

        if (Market.getInstance().filledCount() >= game.locale.marketBlock.gifts_after) {
            ITextureRegion giftIcon = GameUI.getInstance().getGiftIcon();
            ButtonSprite sendGiftButton = new ButtonSprite(whiteRect.getWidth() - (giftIcon.getWidth() / 2), (giftIcon.getHeight() / 2), giftIcon, PhoeniciaContext.vboManager);
            whiteRect.attachChild(sendGiftButton);
            this.registerTouchArea(sendGiftButton);
            sendGiftButton.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                    sendGift();
                }
            });
        }
    }

    protected void sendGift() {
        game.hudManager.showSendGift(game);
    }
    protected void sellLetter(LetterSprite block) {
        try {
            Inventory.getInstance().subtract(block.getLetter().name);
            Bank.getInstance().credit(block.getLetter().sell);
            final int newCount = Inventory.getInstance().getCount(block.getLetter().name);
            block.setCount(newCount);
            if (newCount < 1) {
                block.setEnabled(false);
            }
            GameSounds.play(GameSounds.COLLECT);
        } catch (Exception e) {
            Debug.d("Could not sell "+block.getLetter().name, e);

        }
    }

    protected void sellWord(WordSprite block) {
        try {
            Inventory.getInstance().subtract(block.getWord().name);
            Bank.getInstance().credit(block.getWord().sell);
            final int newCount = Inventory.getInstance().getCount(block.getWord().name);
            block.setCount(newCount);
            if (newCount < 1) {
                block.setEnabled(false);
            }
            GameSounds.play(GameSounds.COLLECT);
        } catch (Exception e) {
            Debug.d("Could not sell "+block.getWord().name, e);

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

    @Override
    public void finish() {
        game.hudManager.clear();
    }


}
