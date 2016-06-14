package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Button;
import com.linguaculturalists.phoenicia.components.LetterSprite;
import com.linguaculturalists.phoenicia.components.WordSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.ButtonSprite;
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
public class DebugHUD extends PhoeniciaHUD {
    private PhoeniciaGame game;
    private Rectangle whiteRect;
    private ClickDetector clickDetector;

    /**
     * Display the \link InventoryItem InventoryItems \endlink with a positive balance and allow selling them.
     * @param game Reference to the PhoeniciaGame this HUD is running in
     */
    public DebugHUD(final PhoeniciaGame game) {
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

        this.whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 600, 500, PhoeniciaContext.vboManager) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
                return true;
            }
        };
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);
        this.registerTouchArea(whiteRect);

        Button restartButton = new Button(whiteRect.getWidth() / 2, whiteRect.getHeight() - 75, 350, 80, "Restart Session", PhoeniciaContext.vboManager, new Button.OnClickListener() {
            @Override
            public void onClicked(Button button) {
                game.restart();
            }
        });
        whiteRect.attachChild(restartButton);
        this.registerTouchArea(restartButton);

        Button clearInventory = new Button(whiteRect.getWidth() / 2, whiteRect.getHeight() - 175, 350, 80, "Clear Inventory", PhoeniciaContext.vboManager, new Button.OnClickListener() {
            @Override
            public void onClicked(Button button) {
                Inventory.getInstance().clear();
            }
        });
        whiteRect.attachChild(clearInventory);
        this.registerTouchArea(clearInventory);

        Button clearBank = new Button(whiteRect.getWidth() / 2, whiteRect.getHeight() - 275, 350, 80, "Clear Bank", PhoeniciaContext.vboManager, new Button.OnClickListener() {
            @Override
            public void onClicked(Button button) {
                Bank.getInstance().clear();
            }
        });
        whiteRect.attachChild(clearBank);
        this.registerTouchArea(clearBank);

        Button creditBank = new Button(150, whiteRect.getHeight() - 375, 200, 80, "+10 Coins", PhoeniciaContext.vboManager, new Button.OnClickListener() {
            @Override
            public void onClicked(Button button) {
                Bank.getInstance().credit(10);
            }
        });
        whiteRect.attachChild(creditBank);
        this.registerTouchArea(creditBank);

        Button debitBank = new Button(450, whiteRect.getHeight() - 375, 200, 80, "-10 Coins", PhoeniciaContext.vboManager, new Button.OnClickListener() {
            @Override
            public void onClicked(Button button) {
                Bank.getInstance().debit(10);
            }
        });
        whiteRect.attachChild(debitBank);
        this.registerTouchArea(debitBank);
    }

    /**
     * Capture scene touch events and look for click events
     * @param pSceneTouchEvent
     * @return
     */
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        // Block touch events
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        Debug.d("Inventory HUD touched, handled? "+handled);
        if (handled) return true;
        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
        // TODO: Fix inventory selling
    }
}
