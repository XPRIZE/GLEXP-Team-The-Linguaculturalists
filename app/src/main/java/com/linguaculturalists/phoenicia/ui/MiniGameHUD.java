package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.models.GameTile;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;

import java.io.IOException;

/**
 * Created by mhall on 2/18/17.
 */
public class MiniGameHUD extends PhoeniciaHUD {

    public static final float WINDOW_WIDTH = 800;
    public static final float WINDOW_HEIGHT = 600;
    public static final int BACKGROUND_WIDTH = 0;
    public static final int BACKGROUND_HEIGHT = 0;
    public static final int FOREGROUND_WIDTH = 0;
    public static final int FOREGROUND_HEIGHT = 0;

    public MiniGameHUD(final PhoeniciaGame phoeniciaGam, final Level level, final GameTile tile) {
        super(phoeniciaGam);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();

        try {
            AssetBitmapTexture background_texture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, tile.game.background_texture);
            background_texture.load();

            ITextureRegion background_region = TextureRegionFactory.extractFromTexture(background_texture, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
            ITextureRegion foreground_region = TextureRegionFactory.extractFromTexture(background_texture, 0, BACKGROUND_HEIGHT, FOREGROUND_WIDTH, FOREGROUND_HEIGHT);


        } catch (IOException e) {
            // TODO: gracefully handle missing background
        }
    }


    @Override
    public void finish() {
        game.hudManager.pop();
    }
}
