package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
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

    public NewLevelHUD(final PhoeniciaGame game, final Level level) {
        super(game);
        this.game = game;
        this.level = level;
        this.setBackgroundEnabled(false);

        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                Debug.d("Background clicked");
                finish();
            }
        });

        List<Letter> new_letters = new ArrayList<Letter>(level.letters);
        new_letters.removeAll(level.prev.letters);

        List<Word> new_words = new ArrayList<Word>(level.words);
        new_words.removeAll(level.prev.words);

        this.whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 400, 400, PhoeniciaContext.vboManager) {
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

        ITextureRegion levelRegion = GameUI.getInstance().getLevelIcon();
        this.levelStar = new Sprite(levelRegion.getWidth()/2, whiteRect.getHeight()-levelRegion.getHeight()/2, levelRegion, PhoeniciaContext.vboManager);
        whiteRect.attachChild(levelStar);
        Text levelNum = new Text(levelRegion.getWidth()/2, levelRegion.getHeight()/2, GameFonts.buttonText(), level.name, level.name.length(), new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        levelNum.setScale(0.4f);
        levelStar.attachChild(levelNum);

        ITextureRegion coinRegion = GameUI.getInstance().getCoinsIcon();
        this.levelCoins = new Sprite(0, whiteRect.getHeight()-(coinRegion.getHeight()/2), coinRegion, PhoeniciaContext.vboManager);
        this.levelCoinsCount = new Text(0, whiteRect.getHeight()-(coinRegion.getHeight()/2), GameFonts.inventoryCount(), String.valueOf(level.coinsEarned), String.valueOf(level.coinsEarned).length(), new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        this.levelCoinsCount.setPosition(whiteRect.getWidth() - (this.levelCoinsCount.getWidth() / 2) - 16, this.levelCoinsCount.getY());
        this.levelCoins.setPosition(whiteRect.getWidth() - this.levelCoinsCount.getWidth() - 32, this.levelCoins.getY());
        whiteRect.attachChild(this.levelCoins);
        whiteRect.attachChild(this.levelCoinsCount);

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
    }

    @Override
    public void show() {
        this.levelStar.registerEntityModifier(new ScaleModifier(0.4f, 5.0f, 1.5f) {
            @Override
            protected void onModifierFinished(IEntity pItem) {
                super.onModifierFinished(pItem);
            }
        });
        GameSounds.play(GameSounds.COLLECT);
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
