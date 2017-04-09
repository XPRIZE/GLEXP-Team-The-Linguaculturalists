package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.BorderRectangle;
import com.linguaculturalists.phoenicia.components.Numberpad;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Number;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Gift;
import com.linguaculturalists.phoenicia.models.GiftRequest;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameSounds;
import com.linguaculturalists.phoenicia.util.GameUI;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 3/23/17.
 */
public class SendGiftHUD extends PhoeniciaHUD {
    private Rectangle whiteRect;
    private Entity itemPane;
    private PhoeniciaGame game;
    private ClickDetector clickDetector;
    private int inputCursor = 0;
    private Rectangle[] inputLocations;
    private int requestCode = 0;

    private static final int CODE_LENGTH = 6;

    public SendGiftHUD(final PhoeniciaGame game) {
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
        Text name = new Text(banner.getWidth()/2, 120, GameFonts.defaultHUDDisplay(), "Send a Gift", 11, new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        float bannerScale = whiteRect.getWidth() / (bannerRegion.getWidth() * 0.6f);
        name.setScaleX(1 / bannerScale);
        banner.setScaleX(bannerScale);
        banner.attachChild(name);
        whiteRect.attachChild(banner);

        int startX = (int)(whiteRect.getWidth()/2 - (64 * CODE_LENGTH)/2) + 32;
        Sprite giftIcon = new Sprite(startX-64, whiteRect.getHeight()-96, GameUI.getInstance().getGiftIcon(), PhoeniciaContext.vboManager);
        whiteRect.attachChild(giftIcon);
        inputLocations = new Rectangle[6];
        for (int i = 0; i < CODE_LENGTH; i++) {
            BorderRectangle digitBox = new BorderRectangle(startX+(64*i), whiteRect.getHeight()-96, 48, 64, PhoeniciaContext.vboManager);
            digitBox.setColor(Color.WHITE);
            digitBox.setBorderColor(Color.RED);
            whiteRect.attachChild(digitBox);
            inputLocations[i] = digitBox;
        }

        itemPane = new Entity(whiteRect.getWidth()/2, whiteRect.getHeight()/2, whiteRect.getWidth(), 64);
        whiteRect.attachChild(itemPane);

        Rectangle inputPanel = new Rectangle(this.getWidth()/2, 96, whiteRect.getWidth(), 192, PhoeniciaContext.vboManager) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
                return true;
            }
        };
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
                    showRequestItem();
                }
            }
        });
    }

    protected void showRequestItem() {
        final Gift gift = Gift.newForRequest(game.session, requestCode);
        ITiledTextureRegion itemRegion;
        if (gift.requestType.get()==GiftRequest.LETTER_REQUEST) {
            itemRegion = game.letterSprites.get(game.locale.letters.get(gift.requestItem.get()));
        } else {
            itemRegion = game.wordSprites.get(game.locale.words.get(gift.requestItem.get()));
        }

        ButtonSprite giftItem = new ButtonSprite(itemPane.getWidth()/2 - 64, itemPane.getHeight()/2, itemRegion, PhoeniciaContext.vboManager);
        giftItem.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                if (gift.requestType.get()==GiftRequest.LETTER_REQUEST) {
                    game.playBlockSound(game.locale.letters.get(gift.requestItem.get()).sound);
                } else {
                    game.playBlockSound(game.locale.words.get(gift.requestItem.get()).sound);
                }
            }
        });
        itemPane.attachChild(giftItem);
        registerTouchArea(giftItem);

        final ButtonSprite confirmButton = new ButtonSprite(itemPane.getWidth()/2 + 64, itemPane.getHeight()/2, GameUI.getInstance().getOkIcon(), PhoeniciaContext.vboManager);
        confirmButton.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                confirmButton.setEnabled(false);
                showResponseCode(gift);
            }
        });
        itemPane.attachChild(confirmButton);
        registerTouchArea(confirmButton);

    }

    protected void showResponseCode(final Gift gift) {
        int responseCode = gift.responseCode.get();

        try {
            if (gift.requestType.get() == GiftRequest.LETTER_REQUEST) {
                Letter addLetter = game.locale.letters.get(gift.requestItem.get());
                game.inventory.subtract(addLetter.name, 1);
            } else if (gift.requestType.get() == GiftRequest.WORD_REQUEST) {
                Word addWord = game.locale.words.get(gift.requestItem.get());
                game.inventory.subtract(addWord.name, 1);
            }

        } catch (Exception e) {
            //TODO: Show error dialog
            Debug.d("Could not subtract item, not enough in inventory");
            GameSounds.play(GameSounds.FAILED);
            return;
        }
        int startX = (int)(whiteRect.getWidth()/2 - (64 * CODE_LENGTH)/2) + 16;
        for (int i = 0; i < CODE_LENGTH; i++) {
            int power = (int)(Math.pow(10, CODE_LENGTH-i-1));
            Debug.d("Showing response code position: " + power);
            int digit = responseCode / power;
            Debug.d("Showing response code digit: " + digit);
            final Number requestDigit = game.locale.number_map.get(digit);
            ButtonSprite digitSprite = new ButtonSprite(startX+(64*i), 96, game.numberSprites.get(requestDigit), PhoeniciaContext.vboManager);
            digitSprite.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                    game.playBlockSound(requestDigit.sound);
                }
            });
            whiteRect.attachChild(digitSprite);
            this.registerTouchArea(digitSprite);
            responseCode -= digit * power;
        }

    }

    protected void inputNumber(Number n) {
        requestCode += n.intval * (Math.pow(10, CODE_LENGTH-inputCursor-1));
        Rectangle inputBox = inputLocations[inputCursor];
        Sprite inputChar = new Sprite(inputBox.getWidth()/2, inputBox.getHeight()/2, game.numberSprites.get(n), PhoeniciaContext.vboManager);
        inputBox.attachChild(inputChar);
        inputCursor++;

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
