package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.util.GameFonts;

import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.ease.EaseBackIn;
import org.andengine.util.modifier.ease.EaseBackOut;

/**
 * Created by mhall on 9/25/15.
 */
public class DefaultHUD extends PhoeniciaHUD implements PhoeniciaGame.LevelChangeListener, Bank.BankUpdateListener {

    private PhoeniciaGame game;
    private Text levelDisplay;
    private Text balanceDisplay;
    private ButtonSprite inventoryBlock;
    private ButtonSprite letterBlock;
    private ButtonSprite wordBlock;

    public DefaultHUD(final PhoeniciaGame game, final Level levelx) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.game = game;
        this.game.addLevelListener(this);

        levelDisplay = new Text(96, game.camera.getHeight()-24, GameFonts.getDefaultHUDDisplay(), "Level: "+game.current_level, 10, game.activity.getVertexBufferObjectManager());
        this.attachChild(levelDisplay);

        balanceDisplay = new Text(96, game.camera.getHeight()-64, GameFonts.getDefaultHUDDisplay(), "Coins: "+game.session.account_balance.get(), 10, game.activity.getVertexBufferObjectManager());
        this.attachChild(balanceDisplay);

        ITextureRegion inventoryRegion = game.shellTiles.getTextureRegion(2);
        this.inventoryBlock = new ButtonSprite(64, 64, inventoryRegion, game.activity.getVertexBufferObjectManager());
        inventoryBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showInventory();
            }
        });
        this.registerTouchArea(inventoryBlock);
        this.attachChild(inventoryBlock);

        ITextureRegion letterRegion = game.shellTiles.getTextureRegion(0);
        this.letterBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-(64*3), 64, letterRegion, game.activity.getVertexBufferObjectManager());
        letterBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showLetterPlacement();
            }
        });
        this.registerTouchArea(letterBlock);
        this.attachChild(letterBlock);

        ITextureRegion wordRegion = game.shellTiles.getTextureRegion(1);
        this.wordBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-64, 64, wordRegion, game.activity.getVertexBufferObjectManager());
        wordBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showWordPlacement();
            }
        });
        this.registerTouchArea(wordBlock);
        this.attachChild(wordBlock);

        ITextureRegion clearRegion = game.shellTiles.getTextureRegion(7);
        ButtonSprite clearBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-32, GameActivity.CAMERA_HEIGHT-48, clearRegion, game.activity.getVertexBufferObjectManager());
        clearBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.restart();
            }
        });
        this.registerTouchArea(clearBlock);
        this.attachChild(clearBlock);

        Bank.getInstance().addUpdateListener(this);
    }

    public void show() {
        inventoryBlock.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
        letterBlock.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
        wordBlock.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));

        levelDisplay.registerEntityModifier(new MoveYModifier(0.5f, GameActivity.CAMERA_HEIGHT + 48, GameActivity.CAMERA_HEIGHT - 24, EaseBackOut.getInstance()));
        balanceDisplay.registerEntityModifier(new MoveYModifier(0.5f, GameActivity.CAMERA_HEIGHT + 16, GameActivity.CAMERA_HEIGHT - 64, EaseBackOut.getInstance()));

    }
    public void hide() {
        inventoryBlock.setY(-48);
        letterBlock.setY(-48);
        wordBlock.setY(-48);
        levelDisplay.setY(GameActivity.CAMERA_HEIGHT+48);
        balanceDisplay.setY(GameActivity.CAMERA_HEIGHT+16);
    }
    public void onLevelChanged(Level next) {
        this.levelDisplay.setText("Level: "+next.name);
    }

    public void onBankAccountUpdated(int new_balance) {
        Debug.d("New balance: " + new_balance);
        this.balanceDisplay.setText("Coins: "+new_balance);
    }
}
