package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.BorderRectangle;
import com.linguaculturalists.phoenicia.components.Dialog;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Assets;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.WordTileBuilder;
import com.linguaculturalists.phoenicia.models.WordTile;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameSounds;
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
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.ease.EaseBackOut;

import java.util.List;

/**
 * HUD for selecting \link Word Words \endlink to be placed as tiles onto the map.
 */
public class WordPlacementHUD extends PhoeniciaHUD implements Bank.BankUpdateListener {

    private Rectangle whiteRect; /**< Background of this HUD */
    private Scrollable blockPanel; /**< Scrollpane containing the word icons */
    private ClickDetector clickDetector;

    private boolean placementDone = false;
    private static final int costMultiplier = 5;

    /**
     * A HUD which allows the selection of new word blocks to be placed on the map
     *
     * @param game Refernece to the current PhoeniciaGame the HUD is running in
     * @param level The level whos words will be displayed in the HUD
     */
    public WordPlacementHUD(final PhoeniciaGame game, final Level level) {
        super(game);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        Bank.getInstance().addUpdateListener(this);
        this.game = game;

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

        BorderRectangle background = new BorderRectangle(GameActivity.CAMERA_WIDTH/2, GameActivity.CAMERA_HEIGHT/2, 550, 300, PhoeniciaContext.vboManager);
        background.setColor(new Color(0.9f, 0.9f, 0.9f));
        background.setBorderColor(new Color(0.8f, 0.8f, 0.8f));
        this.attachChild(background);
        this.blockPanel = new Scrollable(GameActivity.CAMERA_WIDTH/2, GameActivity.CAMERA_HEIGHT/2, 550, 300, Scrollable.SCROLL_HORIZONTAL);
        this.blockPanel.setPadding(16);

        this.registerTouchArea(blockPanel);
        this.registerTouchArea(blockPanel.contents);
        this.attachChild(blockPanel);

        final Font inventoryCountFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.RED_ARGB_PACKED_INT);
        inventoryCountFont.load();

        Debug.d("Loading words for level: " + level.name);
        // We actually use the next level's words to hint at things to come
        int next_available_level = game.locale.levels.indexOf(level)+1;
        if (game.locale.levels.size() <= next_available_level) next_available_level--;
        final Level next = game.locale.levels.get(next_available_level);
        final List<Word> words = next.words;
        for (int i = 0; i < words.size(); i++) {
            final Word currentWord = words.get(i);
            Debug.d("Adding HUD word: " + currentWord.name + " (tile: " + currentWord.tile + ")");
            Rectangle card = new Rectangle((110 * ((i * 2)+1)), blockPanel.getHeight()/2, 200, 200, PhoeniciaContext.vboManager);
            card.setColor(Color.WHITE);
            this.blockPanel.attachChild(card);

            Text blockName = new Text(card.getWidth()/2, card.getHeight()-16, GameFonts.inventoryCount(), String.valueOf(currentWord.chars), currentWord.chars.length, new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
            card.attachChild(blockName);

            ITiledTextureRegion blockRegion = game.wordSprites.get(currentWord);
            ButtonSprite block = new ButtonSprite(card.getWidth()/2, card.getHeight()*2/3, blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    game.playBlockSound(currentWord.sound);
                }
            });
            this.registerTouchArea(block);
            card.attachChild(block);

            if (level.words.contains(currentWord)) {
                int cost = currentWord.buy * (int)Math.pow(costMultiplier, Assets.getInsance().getWordTileCount(currentWord));

                final ITextureRegion coinRegion = GameUI.getInstance().getCoinsButton();
                final ButtonSprite coinIcon = new ButtonSprite((card.getWidth()/2), (coinRegion.getHeight()/2), coinRegion, PhoeniciaContext.vboManager);
                coinIcon.setOnClickListener(new ButtonSprite.OnClickListener() {
                    @Override
                    public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                        float[] cameraCenter = getCamera().getSceneCoordinatesFromCameraSceneCoordinates(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2);
                        TMXTile mapTile = game.getTileAt(cameraCenter[0], cameraCenter[1]);
                        addWordTile(currentWord, mapTile);
                    }
                });
                final Text purchaseCost = new Text(100, coinIcon.getHeight()/2, GameFonts.defaultHUDDisplay(), String.valueOf(cost), String.valueOf(cost).length(), PhoeniciaContext.vboManager);
                coinIcon.attachChild(purchaseCost);
                card.attachChild(coinIcon);
                this.registerTouchArea(coinIcon);
            } else {
                ITextureRegion levelRegion = GameUI.getInstance().getLevelButton();
                final ButtonSprite levelIcon = new ButtonSprite((card.getWidth()/2), levelRegion.getHeight()/2, levelRegion, PhoeniciaContext.vboManager);
                levelIcon.setOnClickListener(new ButtonSprite.OnClickListener() {
                    @Override
                    public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                        game.hudManager.showNextLevelReq(level);
                    }
                });
                final Text levelName = new Text(110, levelIcon.getHeight()/2, GameFonts.defaultHUDDisplay(), level.next.name, level.next.name.length(), PhoeniciaContext.vboManager);
                levelIcon.attachChild(levelName);
                card.attachChild(levelIcon);
                this.registerTouchArea(levelIcon);
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
     * Handles changes to the player's account balance by enablind or disabling word options
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
     * @param word Word to create the tile for
     * @param onTile Map tile to place the new tile on
     */
    protected void addWordTile(final Word word, final TMXTile onTile) {

        final int cost = word.buy * (int)Math.pow(costMultiplier, Assets.getInsance().getWordTileCount(word));
        if (game.session.account_balance.get() < cost) {
            Dialog lowBalanceDialog = new Dialog(500, 300, Dialog.Buttons.OK, PhoeniciaContext.vboManager, new Dialog.DialogListener() {
                @Override
                public void onDialogButtonClicked(Dialog dialog, Dialog.DialogButton dialogButton) {
                    dialog.close();
                    unregisterTouchArea(dialog);
                }
            });
            int difference = cost - game.session.account_balance.get();
            Text confirmText = new Text(lowBalanceDialog.getWidth()/2 + 48, lowBalanceDialog.getHeight()/2 + 32, GameFonts.dialogText(), " -"+difference, 6,  new TextOptions(AutoWrap.WORDS, lowBalanceDialog.getWidth()*0.8f, HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
            lowBalanceDialog.attachChild(confirmText);

            ITextureRegion coinRegion = GameUI.getInstance().getCoinsIcon();
            Sprite coinIcon = new Sprite(lowBalanceDialog.getWidth()/2 - 48, lowBalanceDialog.getHeight()/2 + 32, coinRegion, PhoeniciaContext.vboManager);
            lowBalanceDialog.attachChild(coinIcon);

            lowBalanceDialog.open(this);
            this.registerTouchArea(lowBalanceDialog);
            GameSounds.play(GameSounds.FAILED);
            return;
        }
        Debug.d("Placing word "+word.name+" at "+onTile.getTileColumn()+"x"+onTile.getTileRow());
        final WordTile wordTile = new WordTile(this.game, word);

        wordTile.isoX.set(onTile.getTileColumn());
        wordTile.isoY.set(onTile.getTileRow());

        game.createWordSprite(wordTile, new PhoeniciaGame.CreateWordSpriteCallback() {
            @Override
            public void onWordSpriteCreated(WordTile tile) {
                WordTileBuilder builder = new WordTileBuilder(game.session, wordTile, wordTile.item_name.get(), word.construct);
                builder.start();
                builder.save(PhoeniciaContext.context);
                game.addBuilder(builder);

                wordTile.setBuilder(builder);
                wordTile.save(PhoeniciaContext.context);
                wordTile.restart(PhoeniciaContext.context);
                Bank.getInstance().debit(cost);
                placementDone = true;
            }

            @Override
            public void onWordSpriteCreationFailed(WordTile tile) {
                Debug.d("Failed to create word sprite");
            }
        });

    }

    @Override
    public void finish() {
        game.hudManager.pop();
    }


}