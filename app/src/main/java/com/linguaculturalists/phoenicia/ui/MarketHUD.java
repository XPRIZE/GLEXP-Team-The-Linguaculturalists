package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Button;
import com.linguaculturalists.phoenicia.components.LetterSprite;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.components.WordSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Person;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.Market;
import com.linguaculturalists.phoenicia.models.MarketRequest;
import com.linguaculturalists.phoenicia.models.RequestItem;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.util.List;

/**
 * Display the \link InventoryItem InventoryItems \endlink with a positive balance and allow selling them.
 */
public class MarketHUD extends PhoeniciaHUD {
    private PhoeniciaGame game;
    private Rectangle whiteRect;
    private Entity requestItemsPane;
    private ClickDetector clickDetector;

    public MarketHUD(final PhoeniciaGame game) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        this.game = game;
        // Close the HUD if the user clicks outside the whiteRect
        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                game.hudManager.pop();
            }
        });

        this.whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, (int)(GameActivity.CAMERA_WIDTH * 0.9), (int)(GameActivity.CAMERA_HEIGHT * 0.9), PhoeniciaContext.vboManager) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                //Debug.d("Market dialog touched");
                super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
                return true;
            }
        };
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);
        this.registerTouchArea(whiteRect);

        Scrollable requestsPane = new Scrollable((int)(this.whiteRect.getWidth()*0.3), (int)(this.whiteRect.getHeight()*0.55), (int)(this. whiteRect.getWidth()*0.6), (int)(this.whiteRect.getHeight()*0.9));
        whiteRect.attachChild(requestsPane);
        this.registerTouchArea(requestsPane);

        this.requestItemsPane = new Entity((int)(this.whiteRect.getWidth()*0.8), (int)(this.whiteRect.getHeight()*0.55)-64, (int)(this. whiteRect.getWidth()*0.3), (int)(this.whiteRect.getHeight()*0.9)-64);
        whiteRect.attachChild(this.requestItemsPane);

        final int columns = 2;
        int startX = (int) (whiteRect.getWidth() / 2) - (columns * 128) - 128;
        int startY = (int) whiteRect.getHeight() - 256;

        int offsetX = 0;
        int offsetY = startY;

        // Make sure we have all requests we can get
        Market.getInstance().populate();

        List<MarketRequest> requests = Market.getInstance().requests();
        for (int i = 0; i < requests.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 288;
                offsetX = 0;
            }
            final MarketRequest request = requests.get(i);
            final Person currentPerson = game.locale.person_map.get(request.person_name.get());
            if (currentPerson == null) {
                Debug.d("Market Request without person!");
                continue;
            }
            Debug.d("Adding Market request: " + currentPerson.name);
            final ITextureRegion personRegion = game.personTiles.get(currentPerson);

            final ButtonSprite block = new ButtonSprite(startX + (272 * offsetX), offsetY, personRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    Debug.d("Request from " + currentPerson.name + " clicked");
                    populateRequestItems(request);
                }
            });
            this.registerTouchArea(block);
            requestsPane.attachChild(block);
            offsetX++;

        }
    }

    private void populateRequestItems(MarketRequest request) {
        this.requestItemsPane.detachChildren();
        final int columns = 3;
        float startX = this.requestItemsPane.getWidth() / 2 - (columns * 32) - 16;
        int offsetX = 0;
        float startY = this.requestItemsPane.getHeight() - 48;
        for (RequestItem item : request.getItems(PhoeniciaContext.context)) {
            if (offsetX >= columns) {
                startY -= 96;
                startX = this.requestItemsPane.getWidth() / 2 - (columns * 32) - 16;
                offsetX = 0;
            }            final Letter currentLetter = game.locale.letter_map.get(item.item_name.get());
            final Word currentWord = game.locale.word_map.get(item.item_name.get());
            if (currentLetter != null) {
                Debug.d("Request Letter: "+item.item_name.get());
                LetterSprite requestItemSprite = new LetterSprite(startX, startY, currentLetter, item.quantity.get(), game.letterTiles.get(currentLetter).getTextureRegion(0), PhoeniciaContext.vboManager);
                this.requestItemsPane.attachChild(requestItemSprite);
            } else if (currentWord != null) {
                Debug.d("Request Word: " + item.item_name.get());
                WordSprite requestItemSprite = new WordSprite(startX, startY, currentWord, item.quantity.get(), game.wordTiles.get(currentWord).getTextureRegion(0), PhoeniciaContext.vboManager);
                this.requestItemsPane.attachChild(requestItemSprite);
            } else {
                continue;
            }
            offsetX++;
            startX += 96;
        }

        Button sellButton = new Button(this.requestItemsPane.getWidth() / 2, 32, this.requestItemsPane.getWidth(), 64, "Sell", PhoeniciaContext.vboManager, new Button.OnClickListener() {
            @Override
            public void onClicked(Button button) {
                Debug.d("Market Request sold!");
            }
        });
        this.requestItemsPane.attachChild(sellButton);
        this.registerTouchArea(sellButton);
    }

    @Override
    public void close() {
        Market.getInstance().clear();
    }

    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        // Block touch events
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        //Debug.d("Inventory HUD touched, handled? "+handled);
        if (handled) return true;
        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
        // TODO: Fix inventory selling
    }
}
