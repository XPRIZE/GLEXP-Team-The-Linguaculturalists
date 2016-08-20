package com.linguaculturalists.phoenicia;

import android.content.res.AssetManager;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;

import java.io.IOException;

/**
 * Created by mhall on 6/2/15.
 */
public class SplashScene extends Scene {
    Sprite splash;
    private AssetBitmapTexture splashTexture;
    private ITextureRegion splashTextureRegion;

    public SplashScene(TextureManager textureManager, AssetManager assetManager, VertexBufferObjectManager vbo, Camera camera) {
        super();
        this.setBackground(new Background(new Color(100, 100, 100)));
        try {
            splashTexture = new AssetBitmapTexture(textureManager, assetManager, "textures/splash.png", TextureOptions.BILINEAR);
            splashTextureRegion = TextureRegionFactory.extractFromTexture(splashTexture);
            splashTexture.load();
            splash = new Sprite(0, 0, splashTextureRegion, vbo);

            final float scale_factor = GameActivity.CAMERA_HEIGHT / splashTextureRegion.getHeight();
            splash.setScale(scale_factor+0.1f);
            splash.setPosition((camera.getWidth()) * 0.5f, (camera.getHeight()) * 0.5f);
            this.attachChild(splash);
        } catch (final IOException e) {
            System.err.println("Error loading splash!");
            e.printStackTrace(System.err);
        }
    }


}
