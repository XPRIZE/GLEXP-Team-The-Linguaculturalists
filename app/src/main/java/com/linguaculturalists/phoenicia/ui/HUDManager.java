package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;

import org.andengine.engine.camera.hud.HUD;
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

    public Scene currentHUD;
    public Stack<Scene> hudStack;
    private PhoeniciaGame game;

    public HUDManager(final PhoeniciaGame game) {
        this.game = game;
        this.hudStack = new Stack<Scene>();
    }

    public void showDefault(final Level level) {
        this.push(new DefaultHUD(this.game, level));
    }

    public void showLetterPlacement(final Level level) {
        LetterPlacementHUD.init(this.game, level);
        this.push(LetterPlacementHUD.getInstance());
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
            Scene previousHUD = this.hudStack.pop();
            if (previousHUD != null) {
                if (this.currentHUD instanceof Closeable) {
                    try { ((Closeable)this.currentHUD).close(); } catch (IOException e) { Debug.e("Failed to close HUD"); }
                }
                this.currentHUD = previousHUD;
                this.setChildScene(previousHUD);
            }
        } catch (EmptyStackException e) {
            Debug.d("Nothing to pop off the stack");
            return;
        }
    }

    public void push(Scene s) {
        this.hudStack.push(this.currentHUD);
        this.setChildScene(s);
        this.currentHUD = s;
    }
}
