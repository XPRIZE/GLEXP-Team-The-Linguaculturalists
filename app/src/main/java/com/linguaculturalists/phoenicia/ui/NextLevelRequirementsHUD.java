package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.LetterSprite;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.components.WordSprite;
import com.linguaculturalists.phoenicia.locale.CollectLetterReq;
import com.linguaculturalists.phoenicia.locale.CollectWordReq;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Requirement;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameTextures;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 8/15/16.
 */
public class NextLevelRequirementsHUD extends PhoeniciaHUD {

    private PhoeniciaGame game;
    private Level level;
    private Scrollable itemsPanel;
    private Rectangle whiteRect;
    private Sprite levelStar;
    private ClickDetector clickDetector;

    public NextLevelRequirementsHUD(final PhoeniciaGame game, final Level level) {
        super(game.camera);
        this.game = game;
        this.level = level;
        this.setBackgroundEnabled(false);

        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                Debug.d("Background clicked");
                game.hudManager.pop();
            }
        });

        this.whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 400, 400, PhoeniciaContext.vboManager) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
                return true;
            }
        };
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);
        this.registerTouchArea(whiteRect);

        this.levelStar = new Sprite(32, whiteRect.getHeight()-32, game.shellTiles.getTextureRegion(GameTextures.LEVEL_ICON),PhoeniciaContext.vboManager);
        whiteRect.attachChild(levelStar);
        Text levelNum = new Text(32, 32, GameFonts.buttonText(), level.name, level.name.length(), new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        levelNum.setScale(0.4f);
        levelStar.attachChild(levelNum);
        levelStar.setScale(1.5f);

        this.itemsPanel = new Scrollable(GameActivity.CAMERA_WIDTH / 2, (GameActivity.CAMERA_HEIGHT / 2)-50, 400, 350, Scrollable.SCROLL_VERTICAL);
        itemsPanel.setPadding(50);

        final int columns = 3;
        int startX = (int) (itemsPanel.getWidth() / 2) - (columns * 32) - 16;
        int startY = (int) itemsPanel.getHeight() - 50;

        int offsetX = 0;
        int offsetY = startY;

        for (Requirement unknown_req : level.requirements) {
            if (unknown_req instanceof CollectLetterReq) {
                CollectLetterReq req = (CollectLetterReq) unknown_req;
                for (final Letter currentLetter : req.getLetters()) {
                    int history = Inventory.getInstance().getHistory(currentLetter.name);

                    if (offsetX >= columns) {
                        offsetY -= 118;
                        offsetX = 0;
                    }

                    Debug.d("Adding new Letter: " + currentLetter.name);
                    final int tile_id = currentLetter.sprite;
                    final ITiledTextureRegion blockRegion = game.letterSprites.get(currentLetter);
                    final LetterSprite block = new LetterSprite(startX + (96 * offsetX), offsetY, currentLetter, history, req.getCount(), blockRegion, PhoeniciaContext.vboManager);
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
            }
            if (unknown_req instanceof CollectWordReq) {
                CollectWordReq req = (CollectWordReq) unknown_req;
                for (final Word currentWord : req.getWords()) {
                    int history = Inventory.getInstance().getHistory(currentWord.name);

                    if (offsetX >= columns) {
                        offsetY -= 118;
                        offsetX = 0;
                    }

                    Debug.d("Adding new word: " + currentWord.name);
                    final ITiledTextureRegion blockRegion = game.wordSprites.get(currentWord);
                    final WordSprite block = new WordSprite(startX + (96 * offsetX), offsetY, currentWord, history, req.getCount(), blockRegion, PhoeniciaContext.vboManager);
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
            }
        }
        this.attachChild(itemsPanel);
        this.registerTouchArea(itemsPanel);
    }

    @Override
    public void show() {
        super.show();
    }

    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        // Block touch events
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        Debug.d("New Level HUD touched, handled? "+handled);
        if (handled) return true;
        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
        // TODO: Fix inventory selling
    }
}
