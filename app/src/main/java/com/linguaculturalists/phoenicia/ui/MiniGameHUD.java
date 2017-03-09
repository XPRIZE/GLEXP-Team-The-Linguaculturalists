package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.models.GameTile;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;

import java.io.IOException;

/**
 * Created by mhall on 2/18/17.
 */
public class MiniGameHUD extends PhoeniciaHUD {

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    public static final int FOREGROUND_WIDTH = 800;
    public static final int FOREGROUND_HEIGHT = 135;
    public static final int BACKGROUND_WIDTH = 800;
    public static final int BACKGROUND_HEIGHT = WINDOW_HEIGHT-FOREGROUND_HEIGHT;

    private static final String SPOTLIGHT_NONE = "textures/tour/tour-focus-none.png";

    protected Sprite background_sprite;
    protected Sprite foreground_sprite;
    public Entity content;
    public Sprite host_sprite;

    public MiniGameHUD(final PhoeniciaGame phoeniciaGam, final Level level, final GameTile tile) {
        super(phoeniciaGam);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();

        try {
            final AssetBitmapTexture spotlight_texture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, SPOTLIGHT_NONE);
            spotlight_texture.load();
            TextureRegion spotlight_region = TextureRegionFactory.extractFromTexture(spotlight_texture);
            Sprite spotlight = new Sprite(this.getWidth()/2, this.getHeight()/2, spotlight_region, PhoeniciaContext.vboManager);
            this.attachChild(spotlight);

            AssetBitmapTexture hud_texture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, tile.game.background_texture);
            hud_texture.load();

            ITextureRegion background_region = TextureRegionFactory.extractFromTexture(hud_texture, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
            ITextureRegion foreground_region = TextureRegionFactory.extractFromTexture(hud_texture, 0, BACKGROUND_HEIGHT, FOREGROUND_WIDTH, FOREGROUND_HEIGHT);

            final float background_y = (GameActivity.CAMERA_HEIGHT/2) + (hud_texture.getHeight()/2) - (background_region.getHeight()/2);
            final float foreground_y = (GameActivity.CAMERA_HEIGHT/2) - (hud_texture.getHeight()/2) + (foreground_region.getHeight()/2);
            background_sprite = new Sprite(GameActivity.CAMERA_WIDTH / 2, background_y, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, background_region, PhoeniciaContext.vboManager);
            foreground_sprite = new Sprite(GameActivity.CAMERA_WIDTH / 2, foreground_y, FOREGROUND_WIDTH, FOREGROUND_HEIGHT, foreground_region, PhoeniciaContext.vboManager);
            this.attachChild(background_sprite);
            this.attachChild(foreground_sprite);

            final ITextureRegion person_texture = phoeniciaGam.personTiles.get(tile.game.host);
            final float host_x = (GameActivity.CAMERA_WIDTH/2) - (WINDOW_HEIGHT/2) + (person_texture.getWidth()/4);
            final float host_y = (GameActivity.CAMERA_HEIGHT/2) - (WINDOW_HEIGHT/2) + (person_texture.getHeight()*2/3);
            host_sprite = new Sprite(host_x, host_y, person_texture, PhoeniciaContext.vboManager);
            this.attachChild(host_sprite);

            content = new Entity(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, WINDOW_WIDTH, WINDOW_HEIGHT);
            this.attachChild(content);

            spotlight.setZIndex(background_sprite.getZIndex()-1);
            host_sprite.setZIndex(background_sprite.getZIndex() + 1);
            foreground_sprite.setZIndex(background_sprite.getZIndex() + 2);
            content.setZIndex(background_sprite.getZIndex() + 3);
            this.sortChildren();
        } catch (IOException e) {
            // TODO: gracefully handle missing background
        }
    }


    @Override
    public void finish() {
        game.hudManager.pop();
    }
}
