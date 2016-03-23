package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.LetterSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
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
                Debug.d("Inventory dialog touched");
                super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
                return true;
            }
        };
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);
        this.registerTouchArea(whiteRect);

        final int columns = 4;
        int startX = (int) (whiteRect.getWidth() / 2) - (columns * 32) - 16;
        int startY = (int) whiteRect.getHeight() - 50;

        int offsetX = 0;
        int offsetY = startY;

        List<InventoryItem> items = Inventory.getInstance().items();
        for (int i = 0; i < items.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 80;
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
            final ITextureRegion blockRegion = new TiledTextureRegion(game.letterTextures.get(currentLetter),
                    game.letterTiles.get(currentLetter).getTextureRegion(0),
                    game.letterTiles.get(currentLetter).getTextureRegion(1),
                    game.letterTiles.get(currentLetter).getTextureRegion(2));
            final LetterSprite block = new LetterSprite(startX + (96 * offsetX), offsetY, currentLetter, Inventory.getInstance().getCount(currentLetter.name), blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    Debug.d("Inventory Item " + currentLetter.name + " clicked");
                    try {
                        Inventory.getInstance().subtract(currentLetter.name);
                        Bank.getInstance().credit(currentLetter.sell);
                        block.setCount(Inventory.getInstance().getCount(currentLetter.name));
                    } catch (Exception e) {
                        Debug.d("Could not sell "+currentLetter.name, e);

                    }
                }
            });
            this.registerTouchArea(block);
            whiteRect.attachChild(block);
            offsetX++;

        }
    }

    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        // Block touch events
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        Debug.d("Inventory HUD touched, handled? "+handled);
        if (handled) return true;
        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
        // TODO: Fix inventory selling
    }
}
