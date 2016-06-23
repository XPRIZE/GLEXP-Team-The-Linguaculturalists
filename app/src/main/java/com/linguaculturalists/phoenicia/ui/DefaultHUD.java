package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameTextures;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.linguaculturalists.phoenicia.util.RepeatedClickDetectorListener;

import org.andengine.entity.Entity;
import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.ease.EaseBackOut;

/**
 * The default HUD for Phoenicia
 *
 * Displays the current level, bank account balance, and buttons for adding letter or work tiles
 */
public class DefaultHUD extends PhoeniciaHUD implements PhoeniciaGame.LevelChangeListener, Bank.BankUpdateListener, GameSession.ExperienceChangeListener {

    private PhoeniciaGame game;
    private Sprite levelIcon;
    private Text levelDisplay;
    private Sprite coinIcon;
    private Text balanceDisplay;
    private Sprite xpIcon;
    private Text xpDisplay;
    private ButtonSprite helpButton;
    //private ButtonSprite inventoryBlock;
    private ButtonSprite letterBlock;
    private ButtonSprite wordBlock;
    private ButtonSprite gameBlock;

    private ClickDetector debugClickDetector;
    /**
     * Displays the current level, bank account balance, and buttons for adding letter or work tiles
     * @param game Reference to the PhoeniciaGame this HUD is running in
     */
    public DefaultHUD(final PhoeniciaGame game) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.game = game;
        this.game.addLevelListener(this);
        this.game.session.addExperienceChangeListener(this);
        Bank.getInstance().addUpdateListener(this);

        ITextureRegion levelRegion = game.shellTiles.getTextureRegion(GameTextures.LEVEL_ICON);
        levelIcon = new Sprite(32, GameActivity.CAMERA_HEIGHT - 24, levelRegion, PhoeniciaContext.vboManager);
        levelDisplay = new Text(160, GameActivity.CAMERA_HEIGHT - 24, GameFonts.defaultHUDDisplay(), game.current_level, 20, new TextOptions(HorizontalAlign.LEFT), PhoeniciaContext.vboManager);
        this.attachChild(levelIcon);
        this.attachChild(levelDisplay);
        levelDisplay.setPosition(64 + (levelDisplay.getWidth() / 2), levelDisplay.getY());

        ITextureRegion xpRegion = game.shellTiles.getTextureRegion(GameTextures.XP_ICON);
        xpIcon = new Sprite(32, GameActivity.CAMERA_HEIGHT-64, xpRegion, PhoeniciaContext.vboManager);
        xpDisplay = new Text(160, GameActivity.CAMERA_HEIGHT-64, GameFonts.defaultHUDDisplay(), game.session.points.get().toString(), 20, new TextOptions(HorizontalAlign.LEFT), PhoeniciaContext.vboManager);
        this.attachChild(xpIcon);
        this.attachChild(xpDisplay);
        xpDisplay.setPosition(64 + (xpDisplay.getWidth() / 2), xpDisplay.getY());

        ITextureRegion coinRegion = game.shellTiles.getTextureRegion(GameTextures.COIN_ICON);
        coinIcon = new Sprite(32, GameActivity.CAMERA_HEIGHT-104, coinRegion, PhoeniciaContext.vboManager);
        balanceDisplay = new Text(160, GameActivity.CAMERA_HEIGHT-104, GameFonts.defaultHUDDisplay(), game.session.account_balance.get().toString(), 20, new TextOptions(HorizontalAlign.LEFT), PhoeniciaContext.vboManager);
        this.attachChild(coinIcon);
        this.attachChild(balanceDisplay);
        balanceDisplay.setPosition(64 + (balanceDisplay.getWidth() / 2), balanceDisplay.getY());


        this.debugClickDetector = new ClickDetector(new RepeatedClickDetectorListener(10, 5*1000) {

            @Override
            public void onRepeatedClick(ClickDetector clickDetector, int i, float v, float v1) {
                game.hudManager.showDebugMode();
            }

        });
        Entity debugTouchArea = new Entity(50, GameActivity.CAMERA_HEIGHT - 50, 100, 100) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                return debugClickDetector.onManagedTouchEvent(pSceneTouchEvent);
            }
        };
        this.attachChild(debugTouchArea);
        this.registerTouchArea(debugTouchArea);

        ITextureRegion helpRegion = game.shellTiles.getTextureRegion(GameTextures.HELP);
        this.helpButton = new ButtonSprite(GameActivity.CAMERA_WIDTH-32, GameActivity.CAMERA_HEIGHT-48, helpRegion, PhoeniciaContext.vboManager);
        helpButton.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showLevelIntro(game.locale.level_map.get(game.current_level));
            }
        });
        this.registerTouchArea(helpButton);
        this.attachChild(helpButton);

        ITextureRegion letterRegion = game.shellTiles.getTextureRegion(GameTextures.LETTER_PLACEMENT);
        this.letterBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-(64*5), 64, letterRegion, PhoeniciaContext.vboManager);
        letterBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showLetterPlacement();
            }
        });
        this.registerTouchArea(letterBlock);
        this.attachChild(letterBlock);

        ITextureRegion wordRegion = game.shellTiles.getTextureRegion(GameTextures.WORD_PLACEMENT);
        this.wordBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-(64*3), 64, wordRegion, PhoeniciaContext.vboManager);
        wordBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showWordPlacement();
            }
        });
        this.registerTouchArea(wordBlock);
        this.attachChild(wordBlock);

        ITextureRegion gameRegion = game.shellTiles.getTextureRegion(GameTextures.GAME_PLACEMENT);
        this.gameBlock = new ButtonSprite(GameActivity.CAMERA_WIDTH-64, 64, gameRegion, PhoeniciaContext.vboManager);
        gameBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                // TODO: showGamePlacement()
                // game.hudManager.showWordPlacement();
            }
        });
        this.registerTouchArea(gameBlock);
        this.attachChild(gameBlock);

    }

    /**
     * Animate the on-screen elements entering the scene
     */
    @Override
    public void show() {
        //inventoryBlock.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
        letterBlock.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
        wordBlock.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
        gameBlock.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));

        helpButton.registerEntityModifier(new MoveYModifier(0.5f, GameActivity.CAMERA_HEIGHT + 32, GameActivity.CAMERA_HEIGHT - 48, EaseBackOut.getInstance()));

        levelIcon.registerEntityModifier(new MoveYModifier(0.5f, GameActivity.CAMERA_HEIGHT + 48, GameActivity.CAMERA_HEIGHT - 24, EaseBackOut.getInstance()));
        levelDisplay.registerEntityModifier(new MoveYModifier(0.5f, GameActivity.CAMERA_HEIGHT + 48, GameActivity.CAMERA_HEIGHT - 24, EaseBackOut.getInstance()));

        xpIcon.registerEntityModifier(new MoveYModifier(0.5f, GameActivity.CAMERA_HEIGHT - 12, GameActivity.CAMERA_HEIGHT - 64, EaseBackOut.getInstance()));
        xpDisplay.registerEntityModifier(new MoveYModifier(0.5f, GameActivity.CAMERA_HEIGHT - 12, GameActivity.CAMERA_HEIGHT - 64, EaseBackOut.getInstance()));

        coinIcon.registerEntityModifier(new MoveYModifier(0.5f, GameActivity.CAMERA_HEIGHT - 52, GameActivity.CAMERA_HEIGHT - 104, EaseBackOut.getInstance()));
        balanceDisplay.registerEntityModifier(new MoveYModifier(0.5f, GameActivity.CAMERA_HEIGHT -52, GameActivity.CAMERA_HEIGHT - 104, EaseBackOut.getInstance()));

    }

    /**
     * Move the on-screen elements offscreen for later animation coming it
     */
    @Override
    public void hide() {
        //inventoryBlock.setY(-48);
        letterBlock.setY(-48);
        wordBlock.setY(-48);
        gameBlock.setY(-48);
        levelDisplay.setY(GameActivity.CAMERA_HEIGHT + 48);
        balanceDisplay.setY(GameActivity.CAMERA_HEIGHT + 16);
        helpButton.setY(GameActivity.CAMERA_HEIGHT + 32);
    }

    /**
     * Called when the game's level has changed
     * @param next the new level being started
     */
    public void onLevelChanged(Level next) {
        this.levelDisplay.setText(next.name);
        this.levelDisplay.setPosition(64 + (this.levelDisplay.getWidth() / 2), this.levelDisplay.getY());
    }

    public void onExperienceChanged(int points) {
        Debug.d("New XP: " + points);
        this.xpDisplay.setText("" + points);
        this.xpDisplay.setPosition(64 + (this.xpDisplay.getWidth() / 2), this.xpDisplay.getY());
    }

    /**
     * Called when the player's bank account balance changes
     * @param new_balance the player's new account balance
     */
    public void onBankAccountUpdated(int new_balance) {
        Debug.d("New balance: " + new_balance);
        this.balanceDisplay.setText(""+new_balance);
        this.balanceDisplay.setPosition(64 + (this.balanceDisplay.getWidth() / 2), this.balanceDisplay.getY());
    }
}
