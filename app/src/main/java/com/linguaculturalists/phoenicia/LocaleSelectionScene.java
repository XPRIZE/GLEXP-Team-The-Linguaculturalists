package com.linguaculturalists.phoenicia;

import android.content.res.AssetManager;

import com.linguaculturalists.phoenicia.components.Button;
import com.linguaculturalists.phoenicia.locale.LocaleManager;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Filter;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.controller.MultiTouchController;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mhall on 6/2/15.
 */
public class LocaleSelectionScene extends Scene {
    Sprite background;
    private AssetBitmapTexture backgroundTexture;
    private ITextureRegion backgroundTextureRegion;
    private PhoeniciaGame game;
    public List<String> available_locales;

    public LocaleSelectionScene(PhoeniciaGame game) {
        super();
        this.game = game;
        available_locales = new ArrayList<String>();

        this.setBackground(new Background(new Color(100, 100, 100)));
        try {
            backgroundTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, "textures/locale_selection_background.png", TextureOptions.BILINEAR);
            backgroundTextureRegion = TextureRegionFactory.extractFromTexture(backgroundTexture);
            backgroundTexture.load();
            background = new Sprite(0, 0, backgroundTextureRegion, PhoeniciaContext.vboManager);

            //background.setScale(1.5f);
            background.setPosition(GameActivity.CAMERA_WIDTH/2, GameActivity.CAMERA_HEIGHT/2);
            this.attachChild(background);
        } catch (final IOException e) {
            System.err.println("Error loading background!");
            e.printStackTrace(System.err);
        }

        float startY = GameActivity.CAMERA_HEIGHT * 0.8f;

        LocaleManager lm = new LocaleManager();
        try {
            Map<String, String> locales = lm.scan("locales");

            for (final String locale_src : locales.keySet()) {
                if (locale_src.equals("locales/en_us_test/manifest.xml")) continue;
                final String locale_display = locales.get(locale_src);
                Button locale_button = new Button(game.camera.getWidth()/2, startY, game.camera.getWidth()/2, 100, locale_display, PhoeniciaContext.vboManager, new Button.OnClickListener() {
                    @Override
                    public void onClicked(Button button) {
                        Debug.d(locale_display+" Locale clicked");
                        startGame(locale_src);
                    }
                });
                this.attachChild(locale_button);
                this.registerTouchArea(locale_button);
                startY -= 120;
                available_locales.add(locale_src);
            }
        } catch (IOException e) {
            Debug.e("Failed to find Locales", e.getMessage());
            e.printStackTrace();
        }
    }


    public void startGame(final String locale_src) {
        final LoadingScene loadingScene = new LoadingScene(game);
        GameSession session = GameSession.start(locale_src);

        session.save(PhoeniciaContext.context);
        this.detachSelf();
        game.activity.getEngine().setScene(loadingScene);
        game.activity.getEngine().registerUpdateHandler(loadingScene);
        loadingScene.load(session);
    }
}
