package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.PhoeniciaGame;

/**
 * Created by mhall on 3/23/17.
 */
public class SendGiftHUD extends PhoeniciaHUD {
    private PhoeniciaGame game;

    public SendGiftHUD(final PhoeniciaGame game) {
        super(game);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        this.game = game;
    }

    @Override
    public void finish() {
        game.hudManager.pop();
    }
}
