package com.linguaculturalists.phoenicia;

import android.content.res.AssetManager;

import com.linguaculturalists.phoenicia.components.ProgressBar;
import com.linguaculturalists.phoenicia.components.ProgressDisplay;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.io.IOException;

/**
 * Created by mhall on 6/2/15.
 */
public class LoadingScene extends Scene implements ProgressDisplay {
    private PhoeniciaGame game;
    private Sprite splash;
    private ProgressBar progress;
    private ITextureRegion splashTextureRegion;
    private ITextureRegion progressbarTextureRegion;

    public LoadingScene(PhoeniciaGame game) {
        super();
        this.game = game;
        this.setBackground(new Background(new Color(100, 100, 100)));
        try {
            AssetBitmapTexture splashTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, "textures/loading.png", TextureOptions.BILINEAR);
            splashTextureRegion = TextureRegionFactory.extractFromTexture(splashTexture);
            splashTexture.load();
            splash = new Sprite(0, 0, splashTextureRegion, PhoeniciaContext.vboManager);

            final float scale_factor = (GameActivity.CAMERA_HEIGHT / splashTextureRegion.getHeight()) + 0.1f;
            splash.setScale(scale_factor);
            splash.setPosition((GameActivity.CAMERA_WIDTH) * 0.5f, (GameActivity.CAMERA_HEIGHT) * 0.5f);
            this.attachChild(splash);

            AssetBitmapTexture progressbarTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, "textures/progressbar.png", TextureOptions.BILINEAR);
            progressbarTexture.load();
            progressbarTextureRegion = TextureRegionFactory.extractFromTexture(progressbarTexture);
            progress = new ProgressBar((GameActivity.CAMERA_WIDTH) * 0.5f, (116 * scale_factor), progressbarTextureRegion, PhoeniciaContext.vboManager);
            progress.setScale(scale_factor);
            this.attachChild(progress);

        } catch (final IOException e) {
            System.err.println("Error loading splash!");
            e.printStackTrace(System.err);
        }
    }

    public void load(final GameSession session) {
        final LoadingScene thisProgress = this;
        Thread loadingThread = new Thread() {
            @Override
            public void run() {
                try {
                    game.load(session, thisProgress);
                    startGame();
                } catch (IOException e) {
                    abort(e);
                }
            }
        };
        loadingThread.start();
    }

    private void startGame() {
        Runnable startThread = new Runnable() {
            @Override
            public void run() {
                detachSelf();
                game.activity.getEngine().setScene(game.scene);
                game.activity.getEngine().registerUpdateHandler(game);
                game.start();
            }
        };
        this.game.activity.runOnUpdateThread(startThread);
    }

    private void abort(Exception e) {
        Debug.e("Failed to load game", e.getMessage());
        e.printStackTrace();
        this.detachSelf();
        game.activity.getEngine().setScene(new SessionSelectionScene(game));
    }

    public void setProgress(final float p) {
        Debug.d("LoadingScene.progress: " + p);
        this.game.activity.runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                progress.setProgress(p);
            }
        });
    }

    public float getProgress() {
        return this.progress.getProgress();
    }

}
