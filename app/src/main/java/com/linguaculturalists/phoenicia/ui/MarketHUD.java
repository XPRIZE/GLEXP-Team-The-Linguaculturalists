package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Button;
import com.linguaculturalists.phoenicia.components.Dialog;
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
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameSounds;
import com.linguaculturalists.phoenicia.util.GameTextures;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A HUD used to display a list of Marketplace requests and facilitate those sales
 */
public class MarketHUD extends PhoeniciaHUD {
    private PhoeniciaGame game;
    private Rectangle whiteRect;
    private Entity requestItemsPane;
    private ClickDetector clickDetector;

    private Map<MarketRequest, Sprite> requestPerson;
    private Map<MarketRequest, Text> requestName;

    private List<Entity> touchAreas;
    /**
     * A HUD used to display a list of Marketplace requests and facilitate those sales
     * @param game Reference to the current PhoeniciaGame this HUD is running in
     */
    public MarketHUD(final PhoeniciaGame game) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        this.game = game;
        this.requestPerson = new HashMap<MarketRequest, Sprite>();
        this.requestName = new HashMap<MarketRequest, Text>();
        this.touchAreas = new ArrayList<Entity>();

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

        Scrollable requestsPane = new Scrollable((int)(this.whiteRect.getWidth()*0.3), (int)(this.whiteRect.getHeight()*0.55), (int)(this. whiteRect.getWidth()*0.6), (int)(this.whiteRect.getHeight()*0.9), Scrollable.SCROLL_VERTICAL);
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
            this.requestPerson.put(request, block);

            Text personName = new Text(startX + (272 * offsetX), offsetY-128-16, GameFonts.dialogText(), currentPerson.name, currentPerson.name.length(),  new TextOptions(AutoWrap.WORDS, 256, HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
            requestsPane.attachChild(personName);
            this.requestName.put(request, personName);
            offsetX++;

        }
    }

    /**
     * When a MarketRequest is selected, display it's list of RequestItems, the offered coins and
     * experience points, and a button to attempt the sale
     * @param request The activated MarketRequest to display items for
     */
    private void populateRequestItems(final MarketRequest request) {
        this.requestItemsPane.detachChildren();
        for (Entity touchArea : this.touchAreas) {
            this.unregisterTouchArea(touchArea);
        }
        final int columns = 3;
        float startX = this.requestItemsPane.getWidth() / 2 - (columns * 32) - 16;
        int offsetX = 0;
        float startY = this.requestItemsPane.getHeight() - 48;
        for (final RequestItem item : request.getItems(PhoeniciaContext.context)) {
            if (offsetX >= columns) {
                startY -= 96;
                startX = this.requestItemsPane.getWidth() / 2 - (columns * 32) - 16;
                offsetX = 0;
            }
            final Letter currentLetter = game.locale.letter_map.get(item.item_name.get());
            final Word currentWord = game.locale.word_map.get(item.item_name.get());
            if (currentLetter != null) {
                Debug.d("Request Letter: " + item.item_name.get());
                LetterSprite requestItemSprite = new LetterSprite(startX, startY, currentLetter, Inventory.getInstance().getCount(item.item_name.get()), item.quantity.get(), game.letterSprites.get(currentLetter).getTextureRegion(0), PhoeniciaContext.vboManager);
                requestItemSprite.setOnClickListener(new ButtonSprite.OnClickListener() {
                    @Override
                    public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                        game.playBlockSound(currentLetter.sound);
                    }
                });
                this.requestItemsPane.attachChild(requestItemSprite);
                this.registerTouchArea(requestItemSprite);
                this.touchAreas.add(requestItemSprite);
            } else if (currentWord != null) {
                Debug.d("Request Word: " + item.item_name.get());
                WordSprite requestItemSprite = new WordSprite(startX, startY, currentWord, Inventory.getInstance().getCount(item.item_name.get()), item.quantity.get(), game.wordSprites.get(currentWord).getTextureRegion(0), PhoeniciaContext.vboManager);
                requestItemSprite.setOnClickListener(new ButtonSprite.OnClickListener() {
                    @Override
                    public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                        game.playBlockSound(currentWord.sound);
                    }
                });
                this.requestItemsPane.attachChild(requestItemSprite);
                this.registerTouchArea(requestItemSprite);
                this.touchAreas.add(requestItemSprite);
            } else {
                continue;
            }
            offsetX++;
            startX += 96;
        }

        ITextureRegion coinRegion = game.shellTiles.getTextureRegion(GameTextures.COIN_ICON);
        Sprite coinIcon = new Sprite(32, 162, coinRegion, PhoeniciaContext.vboManager);
        Text iconDisplay = new Text(32, 162, GameFonts.dialogText(), request.coins.get().toString(), 10, new TextOptions(HorizontalAlign.LEFT), PhoeniciaContext.vboManager);
        iconDisplay.setPosition(64 + (iconDisplay.getWidth() / 2), iconDisplay.getY());
        this.requestItemsPane.attachChild(iconDisplay);
        this.requestItemsPane.attachChild(coinIcon);

        ITextureRegion pointsRegion = game.shellTiles.getTextureRegion(GameTextures.XP_ICON);
        Sprite pointsIcon = new Sprite((this.requestItemsPane.getWidth() / 2)+32, 162, pointsRegion, PhoeniciaContext.vboManager);
        Text pointsDisplay = new Text((this.requestItemsPane.getWidth() / 2)+32, 162, GameFonts.dialogText(), request.points.get().toString(), 10, new TextOptions(HorizontalAlign.LEFT), PhoeniciaContext.vboManager);
        pointsDisplay.setPosition((this.requestItemsPane.getWidth() / 2) + 64 + (pointsDisplay.getWidth() / 2), pointsDisplay.getY());
        this.requestItemsPane.attachChild(pointsDisplay);
        this.requestItemsPane.attachChild(pointsIcon);

        Button sellButton = new Button(this.requestItemsPane.getWidth() / 2, 102, this.requestItemsPane.getWidth(), 64, "Sell", new Color(0.12f, 0.72f, 0.02f), PhoeniciaContext.vboManager, new Button.OnClickListener() {
            @Override
            public void onClicked(Button button) {
                attemptSale(request);
            }
        });
        this.requestItemsPane.attachChild(sellButton);
        this.registerTouchArea(sellButton);
        this.touchAreas.add(sellButton);

        Button cancelButton = new Button(this.requestItemsPane.getWidth() / 2, 32, this.requestItemsPane.getWidth(), 64, "Decline", new Color(0.80f, 0.04f, 0.04f), PhoeniciaContext.vboManager, new Button.OnClickListener() {
            @Override
            public void onClicked(Button button) {
                Market.getInstance().cancelRequest(request);
                requestItemsPane.detachChildren();
                Sprite personSprite = requestPerson.get(request);
                unregisterTouchArea(personSprite);
                personSprite.detachSelf();
                requestPerson.remove(request);

                Text personName = requestName.get(request);
                personName.detachSelf();
                requestName.remove(request);

                Debug.d("Remaining requests: "+requestPerson.size());
                if (requestPerson.size() < 1) {
                    game.hudManager.pop();
                }
            }
        });
        this.requestItemsPane.attachChild(cancelButton);
        this.registerTouchArea(cancelButton);
        this.touchAreas.add(cancelButton);
    }

    /**
     * Check if the player has enough inventory to complete the sale, otherwise abort the sale
     * @param request
     */
    private void attemptSale(MarketRequest request) {
        Debug.d("Attempting sale to " + request.person_name.get());
        for (RequestItem item : request.getItems(PhoeniciaContext.context)) {
            int available = Inventory.getInstance().getCount(item.item_name.get());
            if (available < item.quantity.get()) {
                abortSale(item, (item.quantity.get() - available));
                return;
            }
        }
        completeSale(request);
        // TODO: remove request and replace it with a new one
    }

    /**
     * Tells the Market class to fulfill the request, deducting the items from the inventory and
     * crediting the player coins and experience points
     * @param request
     */
    public void completeSale(MarketRequest request) {
        GameSounds.play(GameSounds.COLLECT);
        Market.getInstance().fulfillRequest(request);
        this.requestItemsPane.detachChildren();
        game.hudManager.clear();
    }

    /**
     * Abort the sale with a message to the player indicating item who's lack of inventory caused it
     * @param item Item that the player does not have enough of to complete the sale
     * @param needed How many more of this item is needed
     */
    public void abortSale(RequestItem item, int needed) {
        Debug.d("Aborting sale due to not enough " + item.item_name.get());
        Dialog confirmDialog = new Dialog(500, 300, Dialog.Buttons.OK, PhoeniciaContext.vboManager, new Dialog.DialogListener() {
            @Override
            public void onDialogButtonClicked(Dialog dialog, Dialog.DialogButton dialogButton) {
                if (dialogButton == dialogButton.OK) {
                    Debug.d("Dialog Ok pressed");
                    dialog.close();
                    unregisterTouchArea(dialog);
                }
            }
        });
        Text confirmText = new Text(confirmDialog.getWidth()/2, confirmDialog.getHeight()-48, GameFonts.dialogText(), "You need more of these:", 24,  new TextOptions(AutoWrap.WORDS, confirmDialog.getWidth()*0.8f, HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        confirmDialog.attachChild(confirmText);
        final Letter isLetter = game.locale.letter_map.get(item.item_name.get());
        final Word isWord = game.locale.word_map.get(item.item_name.get());
        if (isLetter != null) {
            LetterSprite sprite = new LetterSprite(confirmDialog.getWidth()/2, confirmDialog.getHeight()/2, isLetter, needed, game.letterSprites.get(isLetter).getTextureRegion(0), PhoeniciaContext.vboManager);
            sprite.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                    game.playBlockSound(isLetter.sound);
                }
            });
            confirmDialog.registerTouchArea(sprite);
            confirmDialog.attachChild(sprite);
        } else if (isWord != null) {
            WordSprite sprite = new WordSprite(confirmDialog.getWidth()/2, confirmDialog.getHeight()/2, isWord, needed, game.wordSprites.get(isWord).getTextureRegion(0), PhoeniciaContext.vboManager);
            sprite.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                    game.playBlockSound(isWord.sound);
                }
            });
            confirmDialog.registerTouchArea(sprite);
            confirmDialog.attachChild(sprite);
        }
        this.registerTouchArea(confirmDialog);
        confirmDialog.open(this);
        GameSounds.play(GameSounds.FAILED);
    }

    /**
     * Capture scene touch events
     * @param pSceneTouchEvent
     * @return
     */
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        // Block touch events
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        //Debug.d("Inventory HUD touched, handled? "+handled);
        if (handled) return true;
        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
    }
}
