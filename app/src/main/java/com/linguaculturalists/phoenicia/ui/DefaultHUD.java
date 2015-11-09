package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Level;

import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.adt.color.Color;

/**
 * Created by mhall on 9/25/15.
 */
public class DefaultHUD extends CameraScene implements PhoeniciaGame.LevelChangeListener {

    private PhoeniciaGame game;
    private Text levelDisplay;
    public DefaultHUD(final PhoeniciaGame game, final Level levelx) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.game = game;
        this.game.addLevelListener(this);

        final Font levelFont = FontFactory.create(game.activity.getFontManager(), game.activity.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.YELLOW_ARGB_PACKED_INT);
        levelFont.load();
        levelDisplay = new Text(32, game.camera.getHeight()-24, levelFont, "Level: "+game.current_level, 10, game.activity.getVertexBufferObjectManager());
        this.attachChild(levelDisplay);

        ITextureRegion letterRegion = game.wordTiles.getTextureRegion(114);
        ButtonSprite letterBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-(64*3), 64, letterRegion, game.activity.getVertexBufferObjectManager());
        letterBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showLetterPlacement();
            }
        });
        this.registerTouchArea(letterBlock);
        this.attachChild(letterBlock);

        ITextureRegion wordRegion = game.wordTiles.getTextureRegion(157);
        ButtonSprite wordBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-64, 64, wordRegion, game.activity.getVertexBufferObjectManager());
        wordBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showWordPlacement();
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

    public void onLevelChanged(Level next) {
        this.levelDisplay.setText("Level: "+next.name);
    }
}
