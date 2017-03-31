package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.BorderRectangle;
import com.linguaculturalists.phoenicia.components.Numberpad;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Number;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.GiftRequest;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameSounds;
import com.linguaculturalists.phoenicia.util.GameUI;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 3/23/17.
 */
public class RequestGiftHUD extends PhoeniciaHUD {
    private Rectangle whiteRect;
    private PhoeniciaGame game;
    private ClickDetector clickDetector;
    private int inputCursor = 0;
    private Rectangle[] inputLocations;
    private int responseCode = 0;
    private GiftRequest request;

    private static final int CODE_LENGTH = 6;

    public RequestGiftHUD(final PhoeniciaGame game, final GiftRequest request) {
        super(game);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        this.game = game;
        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                finish();
            }
        });

        this.request = request;

        this.whiteRect = new BorderRectangle(GameActivity.CAMERA_WIDTH / 2, (GameActivity.CAMERA_HEIGHT / 2)+96, 600, 400, PhoeniciaContext.vboManager) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
                return true;
            }
        };
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);
        this.registerTouchArea(whiteRect);

        ITextureRegion bannerRegion = GameUI.getInstance().getBlueBanner();
        Sprite banner = new Sprite(whiteRect.getWidth()/2, whiteRect.getHeight(), bannerRegion, PhoeniciaContext.vboManager);
        Text name = new Text(banner.getWidth()/2, 120, GameFonts.defaultHUDDisplay(), "Request a Gift", 14, new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        float bannerScale = whiteRect.getWidth() / (bannerRegion.getWidth() * 0.6f);
        name.setScaleX(1 / bannerScale);
        banner.setScaleX(bannerScale);
        banner.attachChild(name);
        whiteRect.attachChild(banner);

        int requestCode = request.requestCode.get();
        Debug.d("Showing request code: " + requestCode);
        int startX = (int)(whiteRect.getWidth()/2 - (64 * CODE_LENGTH)/2) + 16;
        for (int i = 0; i < CODE_LENGTH; i++) {
            int power = (int)(Math.pow(10, CODE_LENGTH-i-1));
            Debug.d("Showing request code position: " + power);
            int digit = requestCode / power;
            Debug.d("Showing request code digit: " + digit);
            final Number requestDigit = game.locale.number_map.get(digit);
            ButtonSprite digitSprite = new ButtonSprite(startX+(64*i), whiteRect.getHeight()-96, game.numberSprites.get(requestDigit), PhoeniciaContext.vboManager);
            digitSprite.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                    game.playBlockSound(requestDigit.sound);
                }
            });
            whiteRect.attachChild(digitSprite);
            this.registerTouchArea(digitSprite);
            requestCode -= digit * power;
        }

        Sprite giftIcon = new Sprite(whiteRect.getWidth()/2, whiteRect.getHeight()/2, GameUI.getInstance().getGiftIcon(), PhoeniciaContext.vboManager);
        whiteRect.attachChild(giftIcon);

        startX += 16;
        inputLocations = new Rectangle[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            BorderRectangle digitBox = new BorderRectangle(startX+(64*i), 64, 48, 64, PhoeniciaContext.vboManager);
            digitBox.setColor(Color.WHITE);
            digitBox.setBorderColor(Color.RED);
            whiteRect.attachChild(digitBox);
            inputLocations[i] = digitBox;
        }

        Rectangle inputPanel = new Rectangle(this.getWidth()/2, 96, whiteRect.getWidth(), 192, PhoeniciaContext.vboManager);
        inputPanel.setColor(Color.WHITE);
        this.attachChild(inputPanel);
        Numberpad numPad = new Numberpad(this.getWidth()/2, 96, 96*5, 192, game);
        this.attachChild(numPad);
        this.registerTouchArea(numPad);
        numPad.setOnKeyClickListener(new Numberpad.KeyClickListener() {
            @Override
            public void onKeyClicked(Number n) {
                inputNumber(n);
                game.playBlockSound(n.sound);
                if (inputCursor == 6) {
                    if (verifyGift()) {
                        acceptGift();
                    } else {
                        giftFailed();
                    }
                }
            }
        });
    }

    private void acceptGift() {
        GameSounds.play(GameSounds.COLLECT);
        if (request.itemType.get() == GiftRequest.LETTER_REQUEST) {
            Letter addLetter = game.locale.letters.get(request.itemIndex.get());
            game.inventory.add(addLetter.name, 1, false);
        } else if (request.itemType.get() == GiftRequest.WORD_REQUEST) {
            Word addWord = game.locale.words.get(request.itemIndex.get());
            game.inventory.add(addWord.name, 1, false);
        }
        this.finish();
    }

    private void giftFailed() {
        GameSounds.play(GameSounds.FAILED);
        responseCode = 0;
        inputCursor = 0;
        for(Rectangle inputBox : inputLocations) {
            inputBox.detachChildren();
        }
    }

    protected void inputNumber(Number n) {
        responseCode += n.intval * (Math.pow(10, CODE_LENGTH-inputCursor-1));
        Rectangle inputBox = inputLocations[inputCursor];
        Sprite inputChar = new Sprite(inputBox.getWidth()/2, inputBox.getHeight()/2, game.numberSprites.get(n), PhoeniciaContext.vboManager);
        inputBox.attachChild(inputChar);
        inputCursor++;

    }
    protected boolean verifyGift() {
        return this.request.verify(this.responseCode);
    }

    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        // Block touch events
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        //Debug.d("Inventory HUD touched, handled? "+handled);
        if (handled) return true;
        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
    }

    @Override
    public void finish() {
        game.hudManager.pop();
    }
}
