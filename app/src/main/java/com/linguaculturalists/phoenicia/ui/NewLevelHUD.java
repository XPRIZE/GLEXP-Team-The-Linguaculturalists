package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.BorderRectangle;
import com.linguaculturalists.phoenicia.components.LetterSprite;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.components.WordSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameSounds;
import com.linguaculturalists.phoenicia.util.GameTextures;
import com.linguaculturalists.phoenicia.util.GameUI;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhall on 8/9/16.
 */
public class NewLevelHUD extends PhoeniciaHUD {
    private Scrollable itemsPanel;
    private Rectangle whiteRect;
    private Sprite levelStar;
    private Sprite levelCoins;
    private Text levelCoinsCount;
    private Level level;
    private ClickDetector clickDetector;
    private boolean collectCoins;

    public NewLevelHUD(final PhoeniciaGame game, final Level level, final boolean collectCoins) {
        super(game);
        this.game = game;
        this.level = level;
        this.collectCoins = collectCoins;
        this.setBackgroundEnabled(false);

        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                Debug.d("Background clicked");
                if (! collectCoins) {
                    finish();
                }
            }
        });

        List<Letter> new_letters = new ArrayList<Letter>(level.letters);
        new_letters.removeAll(level.prev.letters);

        List<Word> new_words = new ArrayList<Word>(level.words);
        new_words.removeAll(level.prev.words);

        this.whiteRect = new BorderRectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 400, 400, PhoeniciaContext.vboManager) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                Debug.d("Inventory dialog touched");
                super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
                return true;
            }
        };
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);
        this.registerTouchArea(whiteRect);

        ITextureRegion bannerRegion = GameUI.getInstance().getGreenBanner();
        Sprite banner = new Sprite(whiteRect.getX(), whiteRect.getY()+(whiteRect.getHeight()/2), bannerRegion, PhoeniciaContext.vboManager);
        this.attachChild(banner);

        ITextureRegion levelRegion = GameUI.getInstance().getLevelIcon();
        this.levelStar = new Sprite(banner.getWidth()/2 - 32, 120, levelRegion, PhoeniciaContext.vboManager);
        levelStar.setScale(0.75f);
        banner.attachChild(levelStar);

        Text levelNum = new Text(banner.getWidth()/2 + 32, 120, GameFonts.defaultHUDDisplay(), level.name, level.name.length(), new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        levelNum.setScale(1.5f);
        banner.attachChild(levelNum);

        this.itemsPanel = new Scrollable(GameActivity.CAMERA_WIDTH / 2, (GameActivity.CAMERA_HEIGHT / 2)-50, 400, 350, Scrollable.SCROLL_VERTICAL);
        itemsPanel.setPadding(50);
        //itemsPanel.setClip(false);


        final int columns = 3;
        int startX = (int) (itemsPanel.getWidth() / 2) - (columns * 32) - 16;
        int startY = (int) itemsPanel.getHeight() - 50;

        int offsetX = 0;
        int offsetY = startY;

        for (int i = 0; i < new_letters.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 118;
                offsetX = 0;
            }
            final Letter currentLetter = new_letters.get(i);
            if (currentLetter == null) {
                Debug.w("Unknown letter at " + 1);
                Debug.d("new letters: "+ new_letters);
                continue;
            }
            Debug.d("Adding new Letter: " + currentLetter.name);
            final int tile_id = currentLetter.sprite;
            final ITiledTextureRegion blockRegion = game.letterSprites.get(currentLetter);
            final LetterSprite block = new LetterSprite(startX + (96 * offsetX), offsetY, currentLetter, currentLetter.buy, blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    game.playBlockSound(currentLetter.sound);
                }
            });
            this.registerTouchArea(block);
            itemsPanel.attachChild(block);
            offsetX++;

        }
        // Do the same for words
        for (int i = 0; i < new_words.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 118;
                offsetX = 0;
            }
            final Word currentWord = new_words.get(i);
            Debug.d("Adding new Word: " + currentWord.name);
            final ITiledTextureRegion blockRegion = game.wordSprites.get(currentWord);
            final WordSprite block = new WordSprite(startX + (96 * offsetX), offsetY, currentWord, currentWord.buy, blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    game.playBlockSound(currentWord.sound);
                }
            });
            this.registerTouchArea(block);
            itemsPanel.attachChild(block);
            offsetX++;

        }

        this.attachChild(itemsPanel);
        this.registerTouchArea(itemsPanel);

        if (this.collectCoins) {
            final ITextureRegion coinRegion = GameUI.getInstance().getCoinsButton();
            final ButtonSprite coinIcon = new ButtonSprite((whiteRect.getWidth() / 2), (coinRegion.getHeight() / 2), coinRegion, PhoeniciaContext.vboManager);
            final Text purchaseCost = new Text(100, coinIcon.getHeight() / 2, GameFonts.defaultHUDDisplay(), String.valueOf(level.coinsEarned), String.valueOf(level.coinsEarned).length(), PhoeniciaContext.vboManager);
            coinIcon.attachChild(purchaseCost);
            coinIcon.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                    GameSounds.play(GameSounds.COLLECT);
                    finish();
                }
            });
            whiteRect.attachChild(coinIcon);
            this.registerTouchArea(coinIcon);
        }

    }

    @Override
    public void show() {
        this.levelStar.registerEntityModifier(new ScaleModifier(0.4f, 3.0f, 0.75f) {
            @Override
            protected void onModifierFinished(IEntity pItem) {
                super.onModifierFinished(pItem);
            }
        });
        GameSounds.play(GameSounds.COMPLETE);
    }

    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        // Block touch events
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        Debug.d("New Level HUD touched, handled? "+handled);
        if (handled) return true;
        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
        // TODO: Fix inventory selling
    }

    @Override
    public void finish() {
        game.hudManager.pop();
    }


}
