package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.BorderRectangle;
import com.linguaculturalists.phoenicia.components.Button;
import com.linguaculturalists.phoenicia.components.Dialog;
import com.linguaculturalists.phoenicia.components.LetterSprite;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.components.WordSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Person;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.GiftRequest;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.Market;
import com.linguaculturalists.phoenicia.models.MarketRequest;
import com.linguaculturalists.phoenicia.models.RequestItem;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameSounds;
import com.linguaculturalists.phoenicia.util.GameTextures;
import com.linguaculturalists.phoenicia.util.GameUI;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.Entity;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
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
    private Rectangle whiteRect;
    private Scrollable requestsPane;
    private Rectangle requestItemsPane;
    private ClickDetector clickDetector;

    private List<MarketRequest> requestQueue;
    private Map<MarketRequest, Sprite> requestPerson;
    private Map<MarketRequest, Text> requestName;
    private MarketRequest currentRequest;

    private static final int columns = 2;

    private List<Entity> touchAreas;
    /**
     * A HUD used to display a list of Marketplace requests and facilitate those sales
     * @param game Reference to the current PhoeniciaGame this HUD is running in
     */
    public MarketHUD(final PhoeniciaGame game) {
        super(game);
        this.setBackgroundEnabled(false);
        this.setOnAreaTouchTraversalFrontToBack();
        this.game = game;
        this.requestQueue = new ArrayList<MarketRequest>();
        this.requestPerson = new HashMap<MarketRequest, Sprite>();
        this.requestName = new HashMap<MarketRequest, Text>();
        this.touchAreas = new ArrayList<Entity>();

        // Close the HUD if the user clicks outside the whiteRect
        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                finish();
            }
        });

        this.whiteRect = new BorderRectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 768, 512, PhoeniciaContext.vboManager) {

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

        this.requestsPane = new Scrollable((this.whiteRect.getX() - this.whiteRect.getWidth()/4), this.whiteRect.getY() - 32, (int)(this. whiteRect.getWidth()*0.5), (int)(this.whiteRect.getHeight()*0.9)-16, Scrollable.SCROLL_VERTICAL);
        this.attachChild(requestsPane);
        this.registerTouchArea(requestsPane);
        this.requestsPane.setPadding(32);
        //requestsPane.setClip(false);

        this.requestItemsPane = new Rectangle((this.whiteRect.getWidth()*0.75f)-2, (this.whiteRect.getHeight()*0.5f)+2, (this.whiteRect.getWidth()*0.5f)-4, (this.whiteRect.getHeight()*0.8f)-4, PhoeniciaContext.vboManager);
        this.requestItemsPane.setColor(new Color(0.99f, 0.99f, 0.99f));
        Rectangle borderDark = new Rectangle(this.requestItemsPane.getX(), this.requestItemsPane.getY(), this.requestItemsPane.getWidth()+4f, this.requestItemsPane.getHeight()+4f, PhoeniciaContext.vboManager);
        borderDark.setColor(new Color(0.85f, 0.85f, 0.85f));
        Rectangle borderLight = new Rectangle(this.requestItemsPane.getX()+2f, this.requestItemsPane.getY()-2f, this.requestItemsPane.getWidth()+2f, this.requestItemsPane.getHeight()+2f, PhoeniciaContext.vboManager);
        borderLight.setColor(new Color(0.95f, 0.95f, 0.95f));
        whiteRect.attachChild(borderDark);
        whiteRect.attachChild(borderLight);
        whiteRect.attachChild(this.requestItemsPane);

        ITextureRegion bannerRegion = GameUI.getInstance().getGreenBanner();
        Sprite banner = new Sprite(whiteRect.getWidth()/2, whiteRect.getHeight(), bannerRegion, PhoeniciaContext.vboManager);
        Text name = new Text(banner.getWidth()/2, 120, GameFonts.defaultHUDDisplay(), game.locale.marketBlock.name, game.locale.marketBlock.name.length(), new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        name.setScaleX(0.5f);
        banner.setScaleX(2.0f);
        banner.attachChild(name);
        whiteRect.attachChild(banner);

        for (MarketRequest request : this.getRequests()) {
            this.addRequestToQueue(request, false);
        }
    }

    private void addRequestToQueue(final MarketRequest request, final boolean animate) {
        float startX = (this.requestsPane.getWidth()) - (this.columns * 192) + 96;
        float startY = this.requestsPane.getHeight() - 128;

        float column = this.requestPerson.size() % 2;
        float row = (int)(this.requestPerson.size()/2);
        final Person currentPerson = game.locale.person_map.get(request.person_name.get());
        if (currentPerson == null) {
            Debug.d("Market Request without person!");
            return;
        }
        Debug.d("Adding Market request: " + currentPerson.name);
        final ITextureRegion personRegion = game.personTiles.get(currentPerson);

        final ButtonSprite block = new ButtonSprite(startX + (192 * column), startY - (288 * row), personRegion, PhoeniciaContext.vboManager);
        block.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                Debug.d("Request from " + currentPerson.name + " clicked");
                populateRequestItems(request);
            }
        });
        requestsPane.registerTouchArea(block);
        requestsPane.attachChild(block);
        this.requestPerson.put(request, block);

        Text personName = new Text(block.getWidth()/2, -16, GameFonts.dialogText(), currentPerson.name, currentPerson.name.length(),  new TextOptions(AutoWrap.WORDS, 192, HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        block.attachChild(personName);
        this.requestName.put(request, personName);

        this.requestQueue.add(request);
        if (animate) {
            block.registerEntityModifier(new MoveYModifier(0.5f, -(block.getHeight()/2), startY - (288 * row)));
        }

    }

    protected  List<MarketRequest> getRequests() {
        // Make sure we have all requests we can get
        Market.getInstance().populate();
        return Market.getInstance().requests();
    }

    /**
     * When a MarketRequest is selected, display it's list of RequestItems, the offered coins and
     * experience points, and a button to attempt the sale
     * @param request The activated MarketRequest to display items for
     */
    protected void populateRequestItems(final MarketRequest request) {
        this.requestItemsPane.detachChildren();
        for (Entity touchArea : this.touchAreas) {
            this.unregisterTouchArea(touchArea);
        }
        this.currentRequest = request;

        final int columns = 3;
        float startX = this.requestItemsPane.getWidth() / 2 - (columns * 32) - 16;
        int offsetX = 0;
        float startY = this.requestItemsPane.getHeight() - 32;
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
                LetterSprite requestItemSprite = new LetterSprite(startX, startY, currentLetter, Inventory.getInstance().getCount(item.item_name.get()), item.quantity.get(), game.letterSprites.get(currentLetter), PhoeniciaContext.vboManager);
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
                WordSprite requestItemSprite = new WordSprite(startX, startY, currentWord, Inventory.getInstance().getCount(item.item_name.get()), item.quantity.get(), game.wordSprites.get(currentWord), PhoeniciaContext.vboManager);
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

        ITextureRegion coinRegion = GameUI.getInstance().getCoinsButton();
        ButtonSprite coinIcon = new ButtonSprite(coinRegion.getWidth()*2/3, 48, coinRegion, PhoeniciaContext.vboManager);
        Text iconDisplay = new Text(100, coinIcon.getHeight()/2, GameFonts.defaultHUDDisplay(), request.coins.get().toString(), 10, new TextOptions(HorizontalAlign.LEFT), PhoeniciaContext.vboManager);
        coinIcon.attachChild(iconDisplay);
        coinIcon.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                attemptSale(request);
            }
        });
        this.requestItemsPane.attachChild(coinIcon);
        this.registerTouchArea(coinIcon);
        this.touchAreas.add(coinIcon);


        ITextureRegion cancelRegion = GameUI.getInstance().getCancelIcon();
        ButtonSprite cancelButton = new ButtonSprite(this.requestItemsPane.getWidth() - 64, 48, cancelRegion, PhoeniciaContext.vboManager);
        cancelButton.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                declineSale(request);
            }
        });
        this.requestItemsPane.attachChild(cancelButton);
        this.registerTouchArea(cancelButton);
        this.touchAreas.add(cancelButton);
    }

    protected void declineSale(MarketRequest request) {
        Market.getInstance().cancelRequest(request);
        GameSounds.play(GameSounds.FAILED);
        this.removeRequestFromQueue(request);

        for (int i = 0; i < Market.getInstance().neededToFill(); i++) {
            MarketRequest newRequest = Market.getInstance().createRequest();
            this.addRequestToQueue(newRequest, true);
        }

    }
    /**
     * Check if the player has enough inventory to complete the sale, otherwise abort the sale
     * @param request
     */
    protected void attemptSale(MarketRequest request) {
        Debug.d("Attempting sale to " + request.person_name.get());
        for (RequestItem item : request.getItems(PhoeniciaContext.context)) {
            int available = Inventory.getInstance().getCount(item.item_name.get());
            if (available < item.quantity.get()) {
                abortSale(item, (item.quantity.get() - available), request);
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
    protected void completeSale(MarketRequest request) {
        GameSounds.play(GameSounds.COLLECT);
        Market.getInstance().fulfillRequest(request);

        this.removeRequestFromQueue(request);

        for (int i = 0; i < Market.getInstance().neededToFill(); i++) {
            MarketRequest newRequest = Market.getInstance().createRequest();
            this.addRequestToQueue(newRequest, true);
        }
    }

    private void removeRequestFromQueue(MarketRequest request) {
        requestItemsPane.detachChildren();
        int requestIndex = this.requestQueue.indexOf(request);
        if (requestIndex < 0) {
            Debug.e("Request for "+request.person_name.get()+" is not in the queue!");
        }
        Sprite personSprite = requestPerson.get(request);
        if (personSprite != null) {
            unregisterTouchArea(personSprite);
            personSprite.detachSelf();
            requestPerson.remove(request);
            this.requestsPane.unregisterTouchArea(personSprite);
        } else {
            Debug.e("No person sprite found for request "+request.person_name.get());
        }

        float startX = (this.requestsPane.getWidth()) - (this.columns * 192) + 96;
        float startY = this.requestsPane.getHeight() - 128;

        Debug.d("Moving requests to the right of "+requestIndex);
        for (int i = requestIndex+1; i < this.requestQueue.size(); i++) {
            float column = (i-1) % 2;
            float row = (int)((i-1)/2);
            MarketRequest nextRequest = this.requestQueue.get(i);
            Sprite nextSprite = this.requestPerson.get(nextRequest);
            nextSprite.registerEntityModifier(new ParallelEntityModifier(
                new MoveXModifier(0.5f, nextSprite.getX(), startX + (192 * column)),
                new MoveYModifier(0.5f, nextSprite.getY(), startY - (288 * row))
            ));
        }
        this.requestQueue.remove(request);
    }
    /**
     * Abort the sale with a message to the player indicating item who's lack of inventory caused it
     * @param item Item that the player does not have enough of to complete the sale
     * @param needed How many more of this item is needed
     */
    protected void abortSale(RequestItem item, int needed, final MarketRequest request) {
        Debug.d("Aborting sale due to not enough " + item.item_name.get());
        final Dialog confirmDialog = new Dialog(450, 250, Dialog.Buttons.NONE, PhoeniciaContext.vboManager, null);
        Text confirmText = new Text(confirmDialog.getWidth()/2 + 48, confirmDialog.getHeight() - 64, GameFonts.dialogText(), " - "+needed, 6,  new TextOptions(AutoWrap.WORDS, confirmDialog.getWidth()*0.8f, HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        confirmText.setColor(Color.RED);
        confirmDialog.attachChild(confirmText);

        final Letter isLetter = game.locale.letter_map.get(item.item_name.get());
        final Word isWord = game.locale.word_map.get(item.item_name.get());
        if (isLetter != null) {
            LetterSprite sprite = new LetterSprite(confirmDialog.getWidth()/2 - 48, confirmDialog.getHeight() - 64, isLetter, needed, game.letterSprites.get(isLetter), PhoeniciaContext.vboManager);
            sprite.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                    game.playBlockSound(isLetter.sound);
                }
            });
            sprite.showCount(false);
            confirmDialog.registerTouchArea(sprite);
            confirmDialog.attachChild(sprite);
        } else if (isWord != null) {
            WordSprite sprite = new WordSprite(confirmDialog.getWidth()/2 - 48, confirmDialog.getHeight() - 64, isWord, needed, game.wordSprites.get(isWord), PhoeniciaContext.vboManager);
            sprite.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                    game.playBlockSound(isWord.sound);
                }
            });
            sprite.showCount(false);
            confirmDialog.registerTouchArea(sprite);
            confirmDialog.attachChild(sprite);
        }

        ButtonSprite giftButton = new ButtonSprite(confirmDialog.getWidth()/3, 64, GameUI.getInstance().getGiftIcon(), PhoeniciaContext.vboManager);
        giftButton.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                GiftRequest giftReq;
                if (isLetter != null) {
                    giftReq = GiftRequest.newRequest(game, isLetter, request);
                } else {
                    giftReq = GiftRequest.newRequest(game, isWord, request);
                }
                game.hudManager.showRequestGift(game, giftReq);
                unregisterTouchArea(confirmDialog);
                confirmDialog.close();
            }
        });
        confirmDialog.attachChild(giftButton);
        confirmDialog.registerTouchArea(giftButton);

        ButtonSprite returnButton = new ButtonSprite(confirmDialog.getWidth()*2/3, 64, GameUI.getInstance().getRetryIcon(), PhoeniciaContext.vboManager);
        returnButton.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                unregisterTouchArea(confirmDialog);
                confirmDialog.close();
            }
        });
        confirmDialog.attachChild(returnButton);
        confirmDialog.registerTouchArea(returnButton);

        this.registerTouchArea(confirmDialog);
        confirmDialog.open(this);
        GameSounds.play(GameSounds.FAILED);
    }

    @Override
    public void show() {
        if (this.currentRequest != null) {
            this.populateRequestItems(this.currentRequest);
        }
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

    @Override
    public void finish() {
        game.hudManager.clear();
    }


}
