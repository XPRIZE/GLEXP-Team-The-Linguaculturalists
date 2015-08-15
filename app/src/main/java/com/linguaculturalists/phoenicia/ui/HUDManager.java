package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Level;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.Scene;

/**
 * Created by mhall on 8/15/15.
 */
public class HUDManager extends HUD {

    public Scene currentHUD;
    public static final int BlockPlacement = 1;

    public HUDManager(final PhoeniciaGame game, final Level level) {
        BlockPlacementHUD.init(game, level);
    }

    public void show(int hud) {
        switch (hud) {
            case HUDManager.BlockPlacement:
                this.setChildScene(BlockPlacementHUD.getInstance());
                return;
        }
    }
    public void setChildScene(Scene s) {
        super.setChildScene(s);
        this.currentHUD = s;
    }
}
