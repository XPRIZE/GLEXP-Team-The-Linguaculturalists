package com.linguaculturalists.phoenicia;

import android.content.res.AssetManager;

import com.linguaculturalists.phoenicia.locale.LocaleManager;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by mhall on 6/2/15.
 */
public class LocaleSelectionScene extends Scene {
    Sprite background;
    private AssetBitmapTexture backgroundTexture;
    private ITextureRegion backgroundTextureRegion;

    public LocaleSelectionScene(TextureManager textureManager, AssetManager assetManager, VertexBufferObjectManager vbo, Camera camera) {
        super();
        this.setBackground(new Background(new Color(100, 100, 100)));
        try {
            backgroundTexture = new AssetBitmapTexture(textureManager, assetManager, "textures/locale_selection_background.png", TextureOptions.BILINEAR);
            backgroundTextureRegion = TextureRegionFactory.extractFromTexture(backgroundTexture);
            backgroundTexture.load();
            background = new Sprite(0, 0, backgroundTextureRegion, vbo);

            //background.setScale(1.5f);
            background.setPosition((camera.getWidth()) * 0.5f, (camera.getHeight()) * 0.5f);
            this.attachChild(background);
        } catch (final IOException e) {
            System.err.println("Error loading background!");
            e.printStackTrace(System.err);
        }

        float startY = GameActivity.CAMERA_HEIGHT * 0.75f;

        LocaleManager lm = new LocaleManager();
        Map<String, String> locales = lm.scan(new File("locales/"));

        for (String locale_src : locales.keySet()) {
            String locale_display = locales.get(locale_src);
            Text locale_text = new Text(GameActivity.CAMERA_WIDTH/2, startY, GameFonts.defaultHUDDisplay(), locale_display, 10, new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
            this.attachChild(locale_text);

        }
    }


}
