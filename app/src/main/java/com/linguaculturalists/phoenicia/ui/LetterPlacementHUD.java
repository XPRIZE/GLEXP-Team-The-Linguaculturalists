package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Dialog;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.LetterBuilder;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.LetterTile;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.ease.EaseBackOut;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HUD for selecting \link Letter Letters \endlink to be placed as tiles onto the map.
 */
public class LetterPlacementHUD extends PhoeniciaHUD implements Bank.BankUpdateListener {
    private Letter placeBlock = null;
    private Map<String, Text> inventoryCounts;
    private PhoeniciaGame game;

    private Rectangle whiteRect;
    private Scrollable blockPanel;

    /**
     * HUD for selecting \link Letter Letters \endlink to be placed as tiles onto the map.
     * @param game Reference to the current PhoeniciaGame this HUD is running in
     * @param level The level whos letters will be displayed
     */
    public LetterPlacementHUD(final PhoeniciaGame game, final Level level) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        this.inventoryCounts = new HashMap<String, Text>();
        Bank.getInstance().addUpdateListener(this);
        this.game = game;

        this.whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH/2, 64, 600, 96, PhoeniciaContext.vboManager);
        whiteRect .setColor(Color.WHITE);
        this.attachChild(whiteRect);

        this.blockPanel = new Scrollable(GameActivity.CAMERA_WIDTH/2, 64, 600, 96, Scrollable.SCROLL_HORIZONTAL);

        this.registerTouchArea(blockPanel);
        this.registerTouchArea(blockPanel.contents);
        this.attachChild(blockPanel);

        final Font inventoryCountFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.RED_ARGB_PACKED_INT);
        inventoryCountFont.load();

        Debug.d("Loading letters for level: "+this.game.current_level);
        final List<Letter> letters = level.letters;
        final int tile_start = 130;
        final int startX = (int)(blockPanel.getWidth()/2);
        for (int i = 0; i < letters.size(); i++) {
            final Letter currentLetter = letters.get(i);
            Debug.d("Adding HUD letter: "+currentLetter.name+" (sprite: "+currentLetter.sprite+")");
            final int tile_id = currentLetter.sprite;
            ITiledTextureRegion blockRegion = new TiledTextureRegion(game.letterTextures.get(currentLetter),
                    game.letterTiles.get(currentLetter).getTextureRegion(0),
                    game.letterTiles.get(currentLetter).getTextureRegion(1),
                    game.letterTiles.get(currentLetter).getTextureRegion(2));
            final ButtonSprite block = new ButtonSprite((64 * ((i * 2)+1)), 48, blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    float[] cameraCenter = getCamera().getSceneCoordinatesFromCameraSceneCoordinates(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2);
                    TMXTile mapTile = game.getTileAt(cameraCenter[0], cameraCenter[1]);
                    addLetterTile(currentLetter, mapTile);
                }
            });
            this.registerTouchArea(block);
            blockPanel.attachChild(block);

            final Text inventoryCount = new Text((64 * ((i * 2)+1))+24, 20, inventoryCountFont, ""+currentLetter.buy, 4, PhoeniciaContext.vboManager);
            blockPanel.attachChild(inventoryCount);
            this.inventoryCounts.put(currentLetter.name, inventoryCount);
        }
        Debug.d("Finished loading HUD letters");

        Debug.d("Finished instantiating LetterPlacementHUD");

        //this.clickDetector = new ClickDetector(this);
        //this.setOnSceneTouchListener(this);
    }

    /**
     * Animate the bottom panel sliding up into view.
     */
    @Override
    public void show() {
        whiteRect.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
        blockPanel.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
    }

    /**
     * Handles changes to the player's account balance by enablind or disabling letter options
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

        boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        if (handled) return true;

        return false;
    }

    /**
     * Create a new LetterTile (with Sprite and Builder).
     * @param letter Letter to create the tile for
     * @param onTile Map tile to place the new tile on
     */
    private void addLetterTile(final Letter letter, final TMXTile onTile) {
        if (game.session.account_balance.get() < letter.buy) {
            Dialog lowBalanceDialog = new Dialog(500, 300, Dialog.Buttons.OK, PhoeniciaContext.vboManager, new Dialog.DialogListener() {
                @Override
                public void onDialogButtonClicked(Dialog dialog, Dialog.DialogButton dialogButton) {
                    dialog.close();
                    unregisterTouchArea(dialog);
                }
            });
            int difference = letter.buy - game.session.account_balance.get();
            Text confirmText = new Text(lowBalanceDialog.getWidth()/2, lowBalanceDialog.getHeight()-48, GameFonts.dialogText(), "You need "+difference+" more coins", 30,  new TextOptions(AutoWrap.WORDS, lowBalanceDialog.getWidth()*0.8f, HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
            lowBalanceDialog.attachChild(confirmText);

            lowBalanceDialog.open(this);
            this.registerTouchArea(lowBalanceDialog);
            return;
        }
        Debug.d("Placing letter "+letter.name+" at "+onTile.getTileColumn()+"x"+onTile.getTileRow());
        final LetterTile letterTile = new LetterTile(this.game, letter);

        letterTile.isoX.set(onTile.getTileColumn());
        letterTile.isoY.set(onTile.getTileRow());

        game.createLetterSprite(letterTile, new PhoeniciaGame.CreateLetterSpriteCallback() {
            @Override
            public void onLetterSpriteCreated(LetterTile tile) {
                LetterBuilder builder = new LetterBuilder(game.session, letterTile, letterTile.item_name.get(), letter.time);
                builder.start();
                builder.save(PhoeniciaContext.context);
                game.addBuilder(builder);

                letterTile.setBuilder(builder);
                letterTile.save(PhoeniciaContext.context);
                Bank.getInstance().debit(letter.buy);
            }

            @Override
            public void onLetterSpriteCreationFailed(LetterTile tile) {
                Debug.d("Failed to create letter sprite");
            }
        });

    }

}