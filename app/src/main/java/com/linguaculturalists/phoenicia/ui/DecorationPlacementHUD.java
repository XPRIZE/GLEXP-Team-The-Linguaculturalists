package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.BorderRectangle;
import com.linguaculturalists.phoenicia.components.Dialog;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.locale.Decoration;
import com.linguaculturalists.phoenicia.locale.Game;
import com.linguaculturalists.phoenicia.models.Assets;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.DecorationTile;
import com.linguaculturalists.phoenicia.models.GameTile;
import com.linguaculturalists.phoenicia.models.GameTileBuilder;
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
public class DecorationPlacementHUD extends PhoeniciaHUD implements Bank.BankUpdateListener {

    private Rectangle whiteRect; /**< Background of this HUD */
    private Scrollable blockPanel; /**< Scrollpane containing the game icons */

    private ClickDetector clickDetector;

    private boolean placementDone = false;
    private static final int costMultiplier = 1;
    /**
     * A HUD which allows the selection of new phoeniciaGame blocks to be placed on the map
     *
     * @param phoeniciaGame Refernece to the current PhoeniciaGame the HUD is running in
     */
    public DecorationPlacementHUD(final PhoeniciaGame phoeniciaGame) {
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

        this.whiteRect = new BorderRectangle(GameActivity.CAMERA_WIDTH/2, GameActivity.CAMERA_HEIGHT/2, 600, 400, PhoeniciaContext.vboManager){
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

        final Font inventoryCountFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.RED_ARGB_PACKED_INT);
        inventoryCountFont.load();
        final List<Decoration> decorations = phoeniciaGame.locale.decorations;
        final int tile_start = 130;
        final int startX = (int)(blockPanel.getWidth()/2);
        for (int i = 0; i < decorations.size(); i++) {
            final Decoration currentDecoration = decorations.get(i);
            Debug.d("Adding HUD decoration: " + currentDecoration.name);
            Rectangle card = new Rectangle((110 * ((i * 2)+1)), blockPanel.getHeight()/2, 200, 200, PhoeniciaContext.vboManager);
            card.setColor(Color.WHITE);
            this.blockPanel.attachChild(card);

            Text blockName = new Text(card.getWidth()/2, card.getHeight()-16, GameFonts.inventoryCount(), currentDecoration.name, currentDecoration.name.length(), new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
            card.attachChild(blockName);

            ITiledTextureRegion blockRegion = phoeniciaGame.decorationSprites.get(currentDecoration);
            Sprite block = new Sprite(card.getWidth()/2, card.getHeight()*2/3, blockRegion, PhoeniciaContext.vboManager);
            card.attachChild(block);
            if (phoeniciaGame.locale.isLevelReached(currentDecoration.level, phoeniciaGame.current_level)) {
                final int cost = currentDecoration.buy * (int)Math.pow(costMultiplier, Assets.getInsance().getDecorationTileCount(currentDecoration));

                ITextureRegion coinRegion = GameUI.getInstance().getCoinsButton();
                final ButtonSprite coinIcon = new ButtonSprite((card.getWidth()/2), coinRegion.getHeight()/2, coinRegion, PhoeniciaContext.vboManager);
                coinIcon.setOnClickListener(new ButtonSprite.OnClickListener() {
                    @Override
                    public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                        float[] cameraCenter = getCamera().getSceneCoordinatesFromCameraSceneCoordinates(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2);
                        TMXTile mapTile = phoeniciaGame.getTileAt(cameraCenter[0], cameraCenter[1]);
                        addDecorationTile(currentDecoration, mapTile);
                    }
                });
                final Text purchaseCost = new Text(100, coinIcon.getHeight()/2, GameFonts.defaultHUDDisplay(), String.valueOf(cost), String.valueOf(cost).length(), PhoeniciaContext.vboManager);
                card.attachChild(coinIcon);
                card.attachChild(purchaseCost);
                this.registerTouchArea(coinIcon);

            } else {

                ITextureRegion levelRegion = GameUI.getInstance().getLevelButton();

                final Sprite levelIcon = new Sprite((card.getWidth()/2),levelRegion.getHeight()/2, levelRegion, PhoeniciaContext.vboManager);
                final Text levelName = new Text(110, levelIcon.getHeight()/2, GameFonts.defaultHUDDisplay(), currentDecoration.level, currentDecoration.level.length(), PhoeniciaContext.vboManager);
                levelIcon.attachChild(levelName);
                card.attachChild(levelIcon);
            }

        }
        Debug.d("Finished loading HUD decorations");

        Debug.d("Finished instantiating DecorationPlacementHUD");

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
     * @param decoration Decoration to create the tile for
     * @param onTile Map tile to place the new tile on
     */
    private void addDecorationTile(final Decoration decoration, final TMXTile onTile) {
        final int cost = decoration.buy * (int)Math.pow(costMultiplier, Assets.getInsance().getDecorationTileCount(decoration));
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
        Debug.d("Placing decoration "+decoration.name+" at "+onTile.getTileColumn()+"x"+onTile.getTileRow());
        final DecorationTile decorationTile = new DecorationTile(this.game, decoration);

        decorationTile.isoX.set(onTile.getTileColumn());
        decorationTile.isoY.set(onTile.getTileRow());

        game.createDecorationSprite(decorationTile, new PhoeniciaGame.CreateDecorationSpriteCallback() {
            @Override
            public void onDecorationSpriteCreated(DecorationTile tile) {
                decorationTile.save(PhoeniciaContext.context);
                Bank.getInstance().debit(cost);
                Assets.getInsance().addDecorationTile(tile);
                placementDone = true;
            }

            @Override
            public void onDecorationSpriteCreationFailed(DecorationTile tile) {
                Debug.d("Failed to create decoration sprite");
            }
        });

    }

    @Override
    public void finish() {
        game.hudManager.pop();
    }


}