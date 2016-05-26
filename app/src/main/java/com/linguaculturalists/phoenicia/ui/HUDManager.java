package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.WordTile;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.util.debug.Debug;

import java.io.Closeable;
import java.io.IOException;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Actual AndEngine HUD class used by the game scene
 *
 * Manages a stack of PhoeniciaHUD instances, with only the top one being displayed
 */
public class HUDManager extends HUD {

    public PhoeniciaHUD currentHUD; /**< The current top-most HUD on the stack */
    public Stack<PhoeniciaHUD> hudStack; /**< stack of PhoeniciaHUD instances */
    private PhoeniciaGame game;
    private PhoeniciaHUD nextHUD;
    private float transitionWait = 0;

    /**
     * Create a new instance for the given PhoeniciaGame
     * @param game game to attach HUDs to
     */
    public HUDManager(final PhoeniciaGame game) {
        this.game = game;
        this.hudStack = new Stack<PhoeniciaHUD>();
    }

    /**
     * Create a new instance of the default game hud and display it
     */
    public void showDefault() {
        this.push(new DefaultHUD(this.game));
    }

    /**
     * Create a new instance of the inter-level introduction HUD and display it
     * @param level
     */
    public void showLevelIntro(final Level level) {
        this.set(new LevelIntroHUD(this.game, level));
    }

    /**
     * Create a new instance of the inventory management HUD and display it
     */
    public void showInventory() {
        InventoryHUD inventoryHUD = new InventoryHUD(this.game);
        this.push(inventoryHUD);
    }

    /**
     * Create a new instance of the marketplace HUD and display it
     */
    public void showMarket() {
        MarketHUD marketHUD = new MarketHUD(this.game);
        this.push(marketHUD);
    }

    /**
     * Create a new instance of the letter placement HUD for the current game level
     */
    public void showLetterPlacement() {
        this.showLetterPlacement(this.game.locale.level_map.get(this.game.current_level));
    }
    /**
     * Create a new instance of the letter placement HUD for the specified game level
     */
    public void showLetterPlacement(final Level level) {
        LetterPlacementHUD letterPlacementHUD = new LetterPlacementHUD(this.game, level);
        this.push(letterPlacementHUD);
    }

    /**
     * Create a new instance of the word placement HUD for the current game level
     */
    public void showWordPlacement() {
        this.showWordPlacement(this.game.locale.level_map.get(this.game.current_level));
    }
    /**
     * Create a new instance of the word placement HUD for the specified game level
     */
    public void showWordPlacement(final Level level) {
        this.push(new WordPlacementHUD(this.game, level));
    }

    /**
     * Create a new instance of the word building HUD for the specified word
     */
    public void showWordBuilder(final Level level, final WordTile tile) {
        WordBuilderHUD hud = new WordBuilderHUD(this.game, level, tile);
        this.push(hud);
    }

    /**
     * Remove everything except the DefaultHUD from the stack
     */
    public void clear() {
        while (this.hudStack.size() >= 1) {
            this.pop();
        }
    }

    /**
     * Hide the currently displayed HUD, and show the one below it on the stack
     */
    public void pop() {
        Debug.d("Popping one off the HUD stack");
        try {
            PhoeniciaHUD previousHUD = this.hudStack.pop();
            if (previousHUD != null) {
                this.currentHUD.hide();
                this.currentHUD.close();
                this.setChildScene(previousHUD);
                previousHUD.show();
                this.currentHUD = previousHUD;
            }
        } catch (EmptyStackException e) {
            Debug.d("Nothing to pop off the stack");
            return;
        }
    }

    /**
     * Remove everything except the DefaultHUD and push newHUD on top of that
     * @param newHUD
     */
    public void set(PhoeniciaHUD newHUD) {
        this.clear();
        this.push(newHUD);
    }

    /**
     * Hide the current displayed HUD, and add the given HUD to the top of the stack and display it
     * @param newHUD new HUD to add to the top of the stack
     */
    public void push(PhoeniciaHUD newHUD) {
        if (this.currentHUD != null) {
            this.hudStack.push(this.currentHUD);
            this.currentHUD.hide();
        }
        newHUD.open();
        this.setChildScene(newHUD);
        newHUD.show();
        this.currentHUD = newHUD;
    }

    /**
     * TODO: Determine if this is still needed
     * @param pSecondsElapsed
     */
    public void update (float pSecondsElapsed) {
        if (this.nextHUD != null) {
            this.transitionWait += pSecondsElapsed;
            if (transitionWait >= 1f) {
                Debug.d("HUDManager completing transition");
                this.setChildScene(this.nextHUD);
                this.nextHUD.show();
                this.currentHUD.close();
                this.currentHUD = this.nextHUD;
                this.nextHUD = null;
                this.transitionWait = 0;
            }
        }
    }
}
