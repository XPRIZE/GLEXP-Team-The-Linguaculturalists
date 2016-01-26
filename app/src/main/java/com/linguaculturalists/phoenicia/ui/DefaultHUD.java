package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.ease.EaseBackOut;

/**
 * The default HUD for Phoenicia
 *
 * Displays the current level, bank account balance, and buttons for adding letter or work tiles
 */
public class DefaultHUD extends PhoeniciaHUD implements PhoeniciaGame.LevelChangeListener, Bank.BankUpdateListener {

    private PhoeniciaGame game;
    private Text levelDisplay;
    private Text balanceDisplay;
    private ButtonSprite inventoryBlock;
    private ButtonSprite letterBlock;
    private ButtonSprite wordBlock;

    public DefaultHUD(final PhoeniciaGame game) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.game = game;
        this.game.addLevelListener(this);

        levelDisplay = new Text(96, game.camera.getHeight()-24, GameFonts.defaultHUDDisplay(), "Level: "+game.current_level, 10, PhoeniciaContext.vboManager);
        this.attachChild(levelDisplay);

        balanceDisplay = new Text(96, game.camera.getHeight()-64, GameFonts.defaultHUDDisplay(), "Coins: "+game.session.account_balance.get(), 10, PhoeniciaContext.vboManager);
        this.attachChild(balanceDisplay);

        ITextureRegion inventoryRegion = game.shellTiles.getTextureRegion(2);
        this.inventoryBlock = new ButtonSprite(64, 64, inventoryRegion, PhoeniciaContext.vboManager);
        inventoryBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showInventory();
            }
        });
        this.registerTouchArea(inventoryBlock);
        this.attachChild(inventoryBlock);

        ITextureRegion letterRegion = game.shellTiles.getTextureRegion(0);
        this.letterBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-(64*3), 64, letterRegion, PhoeniciaContext.vboManager);
        letterBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showLetterPlacement();
            }
        });
        this.registerTouchArea(letterBlock);
        this.attachChild(letterBlock);

        ITextureRegion wordRegion = game.shellTiles.getTextureRegion(1);
        this.wordBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-64, 64, wordRegion, PhoeniciaContext.vboManager);
        wordBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showWordPlacement();
            }
        });
        this.registerTouchArea(wordBlock);
        this.attachChild(wordBlock);

        ITextureRegion clearRegion = game.shellTiles.getTextureRegion(7);
        ButtonSprite clearBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-32, GameActivity.CAMERA_HEIGHT-48, clearRegion, PhoeniciaContext.vboManager);
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

    /**
     * Animate the on-screen elements entering the scene
     */
    @Override
    public void show() {
        inventoryBlock.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
        letterBlock.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
        wordBlock.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));

        levelDisplay.registerEntityModifier(new MoveYModifier(0.5f, GameActivity.CAMERA_HEIGHT + 48, GameActivity.CAMERA_HEIGHT - 24, EaseBackOut.getInstance()));
        balanceDisplay.registerEntityModifier(new MoveYModifier(0.5f, GameActivity.CAMERA_HEIGHT + 16, GameActivity.CAMERA_HEIGHT - 64, EaseBackOut.getInstance()));

    }

    /**
     * Move the on-screen elements offscreen for later animation coming it
     */
    @Override
    public void hide() {
        inventoryBlock.setY(-48);
        letterBlock.setY(-48);
        wordBlock.setY(-48);
        levelDisplay.setY(GameActivity.CAMERA_HEIGHT + 48);
        balanceDisplay.setY(GameActivity.CAMERA_HEIGHT + 16);
    }

    /**
     * Called when the game's level has changed
     * @param next the new level being started
     */
    public void onLevelChanged(Level next) {
        this.levelDisplay.setText("Level: "+next.name);
    }

    /**
     * Called when the player's bank account balance changes
     * @param new_balance the player's new account balance
     */
    public void onBankAccountUpdated(int new_balance) {
        Debug.d("New balance: " + new_balance);
        this.balanceDisplay.setText("Coins: "+new_balance);
    }
}
