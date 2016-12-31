package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Dialog;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Game;
import com.linguaculturalists.phoenicia.models.Assets;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.GameTile;
import com.linguaculturalists.phoenicia.models.GameTileBuilder;
import com.linguaculturalists.phoenicia.models.WordTile;
import com.linguaculturalists.phoenicia.models.WordTileBuilder;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameTextures;
import com.linguaculturalists.phoenicia.util.GameUI;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.ease.EaseBackOut;

import java.util.List;

/**
 * HUD for selecting \link Game Games \endlink to be placed as tiles onto the map.
 */
public class GamePlacementHUD extends PhoeniciaHUD implements Bank.BankUpdateListener {

    private Rectangle whiteRect; /**< Background of this HUD */
    private Scrollable blockPanel; /**< Scrollpane containing the game icons */

    private ClickDetector clickDetector;

    private boolean placementDone = false;
    private static final int costMultiplier = 10;
    /**
     * A HUD which allows the selection of new phoeniciaGame blocks to be placed on the map
     *
     * @param phoeniciaGame Refernece to the current PhoeniciaGame the HUD is running in
     */
    public GamePlacementHUD(final PhoeniciaGame phoeniciaGame) {
        super(phoeniciaGame);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        Bank.getInstance().addUpdateListener(this);

        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                finish();
            }
        });

        this.whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH/2, GameActivity.CAMERA_HEIGHT/2, 600, 400, PhoeniciaContext.vboManager){
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
                return true;
            }
        };
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);
        this.registerTouchArea(whiteRect);

        Rectangle background = new Rectangle(GameActivity.CAMERA_WIDTH/2, GameActivity.CAMERA_HEIGHT/2, 550, 300, PhoeniciaContext.vboManager);
        background.setColor(new Color(0.9f, 0.9f, 0.9f));
        this.attachChild(background);
        this.blockPanel = new Scrollable(GameActivity.CAMERA_WIDTH/2, GameActivity.CAMERA_HEIGHT/2, 550, 300, Scrollable.SCROLL_HORIZONTAL);
        this.blockPanel.setPadding(16);

        this.registerTouchArea(blockPanel);
        this.registerTouchArea(blockPanel.contents);
        this.attachChild(blockPanel);

        final List<Game> games = phoeniciaGame.locale.games;
        for (int i = 0; i < games.size(); i++) {
            final Game currentGame = games.get(i);
            Debug.d("Adding HUD phoeniciaGame: " + currentGame.name);
            Rectangle card = new Rectangle((110 * ((i * 2)+1)), blockPanel.getHeight()/2, 200, 200, PhoeniciaContext.vboManager);
            card.setColor(Color.WHITE);
            this.blockPanel.attachChild(card);

            Text blockName = new Text(card.getWidth()/2, card.getHeight()-16, GameFonts.inventoryCount(), currentGame.name, currentGame.name.length(), new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
            card.attachChild(blockName);

            ITiledTextureRegion blockRegion = phoeniciaGame.gameSprites.get(currentGame);
            ButtonSprite block = new ButtonSprite(card.getWidth()/2, card.getHeight()*2/3, blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    float[] cameraCenter = getCamera().getSceneCoordinatesFromCameraSceneCoordinates(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2);
                    TMXTile mapTile = phoeniciaGame.getTileAt(cameraCenter[0], cameraCenter[1]);
                    addGameTile(currentGame, mapTile);
                }
            });
            this.registerTouchArea(block);
            card.attachChild(block);

            if (phoeniciaGame.locale.isLevelReached(currentGame.level, phoeniciaGame.current_level)) {
                final int cost = currentGame.buy * (int)Math.pow(costMultiplier, Assets.getInsance().getGameTileCount(currentGame));
                ITextureRegion coinRegion = GameUI.getInstance().getCoinsButton();
                final Sprite coinIcon = new Sprite((card.getWidth()/2), coinRegion.getHeight()/2, coinRegion, PhoeniciaContext.vboManager);
                final Text purchaseCost = new Text(100, coinIcon.getHeight()/2, GameFonts.defaultHUDDisplay(), String.valueOf(cost), String.valueOf(cost).length(), PhoeniciaContext.vboManager);
                coinIcon.attachChild(purchaseCost);
                card.attachChild(coinIcon);
            } else {
                block.setEnabled(false);
                ITextureRegion levelRegion = GameUI.getInstance().getLevelButton();
                final Sprite levelIcon = new Sprite((card.getWidth()/2), levelRegion.getHeight()/2, levelRegion, PhoeniciaContext.vboManager);
                final Text levelName = new Text(110, levelIcon.getHeight()/2, GameFonts.defaultHUDDisplay(), currentGame.level, currentGame.level.length(), PhoeniciaContext.vboManager);
                levelIcon.attachChild(levelName);
                card.attachChild(levelIcon);
            }

        }
        Debug.d("Finished loading HUD letters");

        Debug.d("Finished instantiating BlockPlacementHUD");

    }

    /**
     * Animate the bottom panel sliding up into view.
     */
    @Override
    public void show() {
        if (this.placementDone) this.finish();
        //whiteRect.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
        //blockPanel.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
    }

    /**
     * Handles changes to the player's account balance by enablind or disabling game options
     * @param new_balance The player's new account balance
     */
    @Override
    public void onBankAccountUpdated(int new_balance) {
        // TODO: Enable/disable words based on available balance
    }

    /**
     * Capture scene touch events and allow them to pass through if not handled by anything in this HUD
     * @param pSceneTouchEvent
     * @return
     */
    @Override
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {

        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        if (handled) return true;

        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
    }

    /**
     * Create a new WordTile (with Sprite and Builder).
     * @param minigame Word to create the tile for
     * @param onTile Map tile to place the new tile on
     */
    private void addGameTile(final Game minigame, final TMXTile onTile) {
        final int cost = minigame.buy * (int)Math.pow(costMultiplier, Assets.getInsance().getGameTileCount(minigame));
        if (game.session.account_balance.get() < cost) {
            Dialog lowBalanceDialog = new Dialog(500, 300, Dialog.Buttons.OK, PhoeniciaContext.vboManager, new Dialog.DialogListener() {
                @Override
                public void onDialogButtonClicked(Dialog dialog, Dialog.DialogButton dialogButton) {
                    dialog.close();
                    unregisterTouchArea(dialog);
                }
            });
            int difference = cost - game.session.account_balance.get();
            Text confirmText = new Text(lowBalanceDialog.getWidth()/2, lowBalanceDialog.getHeight()-48, GameFonts.dialogText(), "You need "+difference+" more coins", 30,  new TextOptions(AutoWrap.WORDS, lowBalanceDialog.getWidth()*0.8f, HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
            lowBalanceDialog.attachChild(confirmText);

            lowBalanceDialog.open(this);
            this.registerTouchArea(lowBalanceDialog);
            return;
        }
        Debug.d("Placing game "+minigame.name+" at "+onTile.getTileColumn()+"x"+onTile.getTileRow());
        final GameTile gameTile = new GameTile(this.game, minigame);

        gameTile.isoX.set(onTile.getTileColumn());
        gameTile.isoY.set(onTile.getTileRow());

        game.createGameSprite(gameTile, new PhoeniciaGame.CreateGameSpriteCallback() {
            @Override
            public void onGameSpriteCreated(GameTile tile) {
                GameTileBuilder builder = new GameTileBuilder(game.session, gameTile, gameTile.item_name.get(), minigame.construct);
                builder.start();
                builder.save(PhoeniciaContext.context);
                game.addBuilder(builder);

                gameTile.setBuilder(builder);
                gameTile.save(PhoeniciaContext.context);
                gameTile.restart(PhoeniciaContext.context);
                Bank.getInstance().debit(cost);
                placementDone = true;
            }

            @Override
            public void onGameSpriteCreationFailed(GameTile tile) {
                Debug.d("Failed to create game sprite");
            }
        });

    }

    @Override
    public void finish() {
        game.hudManager.pop();
    }


}