package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Level;

import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.opengl.texture.region.ITextureRegion;

/**
 * Created by mhall on 9/25/15.
 */
public class DefaultHUD extends CameraScene {

    private PhoeniciaGame game;

    public DefaultHUD(final PhoeniciaGame game, final Level level) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.game = game;

        ITextureRegion letterRegion = game.wordTiles.getTextureRegion(114);
        ButtonSprite letterBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-(64*3), 64, letterRegion, game.activity.getVertexBufferObjectManager());
        letterBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showLetterPlacement(level);
            }
        });
        this.registerTouchArea(letterBlock);
        this.attachChild(letterBlock);

        ITextureRegion wordRegion = game.wordTiles.getTextureRegion(157);
        ButtonSprite wordBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-64, 64, wordRegion, game.activity.getVertexBufferObjectManager());
        wordBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showWordPlacement(level);
            }
        });
        this.registerTouchArea(wordBlock);
        this.attachChild(wordBlock);

        ITextureRegion clearRegion = game.wordTiles.getTextureRegion(159);
        ButtonSprite clearBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-32, GameActivity.CAMERA_HEIGHT-48, clearRegion, game.activity.getVertexBufferObjectManager());
        clearBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.restart();
            }
        });
        this.registerTouchArea(clearBlock);
        this.attachChild(clearBlock);

    }
}
