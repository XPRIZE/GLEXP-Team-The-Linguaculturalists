package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;

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
 * Created by mhall on 8/15/15.
 */
public class HUDManager extends HUD {

    public PhoeniciaHUD currentHUD;
    public Stack<PhoeniciaHUD> hudStack;
    private PhoeniciaGame game;
    private PhoeniciaHUD nextHUD;
    private float transitionWait = 0;

    public HUDManager(final PhoeniciaGame game) {
        this.game = game;
        this.hudStack = new Stack<PhoeniciaHUD>();
    }

    public void showDefault(final Level level) {
        this.push(new DefaultHUD(this.game, level));
    }

    public void showLevelIntro(final Level level) {
        this.push(new LevelIntroHUD(this.game, level));
    }

    public void showInventory() {
        InventoryHUD inventoryHUD = new InventoryHUD(this.game);
        this.push(inventoryHUD);
    }

    public void showLetterPlacement() {
        this.showLetterPlacement(this.game.locale.level_map.get(this.game.current_level));
    }
    public void showLetterPlacement(final Level level) {
        LetterPlacementHUD letterPlacementHUD = new LetterPlacementHUD(this.game, level);
        this.push(letterPlacementHUD);
    }

    public void showWordPlacement() {
        this.showWordPlacement(this.game.locale.level_map.get(this.game.current_level));
    }
    public void showWordPlacement(final Level level) {
        this.push(new WordPlacementHUD(this.game, level));
    }

    public void showWordBuilder(final Level level, final Word word) {
        WordBuilderHUD hud = new WordBuilderHUD(this.game, level, word);
        this.push(hud);
    }

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
