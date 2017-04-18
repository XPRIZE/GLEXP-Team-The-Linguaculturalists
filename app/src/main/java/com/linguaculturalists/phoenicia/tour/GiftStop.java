package com.linguaculturalists.phoenicia.tour;

import android.media.MediaPlayer;

import com.linguaculturalists.phoenicia.components.GiftCode;
import com.linguaculturalists.phoenicia.components.LetterSprite;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.components.WordSprite;
import com.linguaculturalists.phoenicia.locale.*;
import com.linguaculturalists.phoenicia.locale.Number;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.locale.tour.Tour;
import com.linguaculturalists.phoenicia.models.Gift;
import com.linguaculturalists.phoenicia.models.GiftRequest;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.MarketRequest;
import com.linguaculturalists.phoenicia.models.RequestItem;
import com.linguaculturalists.phoenicia.ui.InventoryHUD;
import com.linguaculturalists.phoenicia.ui.MarketHUD;
import com.linguaculturalists.phoenicia.ui.RequestGiftHUD;
import com.linguaculturalists.phoenicia.ui.SendGiftHUD;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Filter;

import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhall on 4/15/17.
 */
public class GiftStop extends Stop {

    private static final int MSG_REQUESTINTRO = 0;
    private static final int MSG_REQUESTSTART = 1;
    private static final int MSG_REQUESTCODE = 2;
    private static final int MSG_RESPONSESTART = 3;
    private static final int MSG_RESPONSECONFIRM = 4;
    private static final int MSG_RESPONSECODE = 5;
    private static final int MSG_REQUESTFINISH = 6;

    private MapBlockSprite marketBlock;
    private MapBlockSprite inventoryBlock;

    private Word requestWord;
    private MarketRequest tourRequest;
    private GiftRequest giftRequest;
    private Gift gift;

    private MarketHUD marketHUD;
    private InventoryHUD inventoryHUD;
    private RequestGiftHUD requestHUD;
    private SendGiftHUD responseHUD;

    GiftStop(Tour tour) {
        super(tour);
    }

    @Override
    public void onClicked() {
        return;
    }

    @Override
    public void start(TourOverlay overlay) {
        this.overlay = overlay;
        this.currentMessageIndex = -1;
    }

    @Override
    public void show(int messageIndex) {
        switch (messageIndex) {
            case MSG_REQUESTINTRO:
                this.showMarketRequest();
                break;
            case MSG_REQUESTSTART:
                this.showGiftRequestButton();
                break;
            case MSG_REQUESTCODE:
                this.showGiftRequestCode();
                break;
            case MSG_RESPONSESTART:
                this.showGiftResponseButton();
                break;
            case MSG_RESPONSECONFIRM:
                this.showGiftResponseCheck();
                break;
            case MSG_RESPONSECODE:
                this.showGiftResponseCode();
                break;
            case MSG_REQUESTFINISH:
                this.showGiftRequestAccept();
                break;
            default:
                this.close();
        }

    }

    private void showMarketRequest() {
        this.overlay.focusOn(this.marketBlock);
        final GiftStop stop = this;
        this.tourRequest = createMarketRequestForGift();

        this.marketHUD = new MarketHUD(this.tour.game) {
            @Override
            protected void abortSale(RequestItem item, int needed, MarketRequest request) {
                MarketHUD.MissingItemsDialog dialog = new MissingItemsDialog(450, 300, item, needed, request, PhoeniciaContext.vboManager) {
                    @Override
                    protected void onGiftClicked() {
                        stop.next();
                    }

                    @Override
                    protected void onReturnClicked() {
                        // Can't go back to the Market HUD
                    }
                };
                dialog.open(this);
            }

            @Override
            protected void declineSale(MarketRequest request) {
                // Can't decline sales during the tour
            }

            @Override
            protected List<MarketRequest> getRequests() {
                // Only present one request
                List<MarketRequest> requests = new ArrayList<MarketRequest>();
                requests.add(tourRequest);
                return requests;
            }

            @Override
            public void finish() {
                return;
            }
        };

        this.overlay.showGuide();
        this.overlay.setManagedHUD(this.marketHUD, new IOnSceneTouchListener() {
            @Override
            public boolean onSceneTouchEvent(Scene scene, TouchEvent touchEvent) {
                // Swallow touch events during the intro message, but allow them in the start message
                return currentMessageIndex == MSG_REQUESTINTRO;
            }
        });
        this.marketHUD.populateRequestItems(tourRequest);
        this.overlay.showMessage(this.getMessage(MSG_REQUESTINTRO), TourOverlay.MessageBox.Bottom);
    }

    private void showGiftRequestButton() {
        this.marketHUD.completeSale(tourRequest);
        this.overlay.showMessage(this.getMessage(MSG_REQUESTSTART), TourOverlay.MessageBox.Top);
    }

    public void showGiftRequestCode() {
        final Stop stop = this;
        this.giftRequest = GiftRequest.newRequest(this.tour.game, this.requestWord, this.tourRequest);
        this.requestHUD = new RequestGiftHUD(this.tour.game, this.giftRequest) {
            @Override
            public void inputNumber(com.linguaculturalists.phoenicia.locale.Number n) {
                // don't input anythign yet
            }

            @Override
            protected boolean verifyGift() {
                // don't verify anything yet
                return false;
            }
        };
        this.overlay.setManagedHUD(this.requestHUD, new IOnSceneTouchListener() {
            @Override
            public boolean onSceneTouchEvent(Scene scene, TouchEvent touchEvent) {
                // Capture scene touches
                stop.next();
                return false;
            }
        });
        this.overlay.showMessage(this.getMessage(MSG_REQUESTCODE), TourOverlay.MessageBox.Bottom);
    }

    public void showGiftResponseButton() {
        this.overlay.focusOn(this.inventoryBlock);
        this.inventoryHUD = new InventoryHUD(this.tour.game) {
            @Override
            protected void sellLetter(LetterSprite block) {
                return;//Can't do that during the tour
            }

            @Override
            protected void sellWord(WordSprite block) {
                return;//Can't do that during the tour
            }
        };
        this.overlay.setManagedHUD(this.inventoryHUD, new IOnSceneTouchListener() {
            @Override
            public boolean onSceneTouchEvent(Scene scene, TouchEvent touchEvent) {
                return true;
            }
        });
        this.overlay.showMessage(this.getMessage(MSG_RESPONSESTART), TourOverlay.MessageBox.Bottom);
    }

    public void showGiftResponseCheck() {
        this.responseHUD = new SendGiftHUD(this.tour.game) {

        };
        this.overlay.setManagedHUD(this.requestHUD);
        this.overlay.showMessage(this.getMessage(MSG_RESPONSECONFIRM), TourOverlay.MessageBox.Bottom, new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                for (int number : GiftCode.toArray(giftRequest.requestCode.get())) {
                    Number n = tour.game.locale.number_map.get(number);
                    responseHUD.inputNumber(n);
                    tour.game.playBlockSound(n.sound);
                }
                responseHUD.showRequestItem();
            }
        });
    }

    public void showGiftResponseCode() {
        this.gift = Gift.newForRequest(tour.game.session, giftRequest.requestCode.get());
        this.overlay.showMessage(this.getMessage(MSG_RESPONSECODE), TourOverlay.MessageBox.Top, new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Inventory.getInstance().add(requestWord.name); // add the one showResponseCode will remove
                responseHUD.showResponseCode(gift);
            }
        });
    }

    public void showGiftRequestAccept() {
        this.overlay.setManagedHUD(this.requestHUD);
        this.overlay.showMessage(this.getMessage(MSG_REQUESTFINISH), TourOverlay.MessageBox.Top, new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                for (int number : GiftCode.toArray(gift.responseCode.get())) {
                    Number n = tour.game.locale.number_map.get(number);
                    requestHUD.inputNumber(n);
                    tour.game.playBlockSound(n.sound);
                }
                requestHUD.acceptGift();
            }
        });
    }

    @Override
    public void setFocus(MapBlockSprite focus) {
        throw new RuntimeException("GiftStop has two stops, Market and Inventory, set them specifically");
    }

    public void setMarketFocus(MapBlockSprite market) {
        this.marketBlock = market;
    }

    public void setInventoryFocus(MapBlockSprite inventory) {
        this.inventoryBlock = inventory;
    }

    private MarketRequest createMarketRequestForGift() {
        MarketRequest newRequest = new MarketRequest();
        newRequest.game.set(this.tour.game.session);
        newRequest.person_name.set(this.tour.guide.name);
        newRequest.status.set(MarketRequest.REQUESTED);
        newRequest.save(PhoeniciaContext.context);

        RequestItem newItem = new RequestItem();
        newItem.game.set(this.tour.game.session);
        newItem.request.set(newRequest);

        // Make a request item asking for one more instance of a word than the player has
        final List<InventoryItem> inventoryItems = InventoryItem.objects(PhoeniciaContext.context).orderBy("#quantity").limit(1).toList();
        for (InventoryItem item : inventoryItems) {
            // Make sure it's a word for the tutorial text to match
            if (this.tour.game.locale.word_map.containsKey(item.item_name.get())) {
                requestWord = this.tour.game.locale.word_map.get(item.item_name.get());
                newItem.item_name.set(inventoryItems.get(0).item_name.get());
                newItem.quantity.set(inventoryItems.get(0).quantity.get() + 1);
                break;
            }
        }
        newItem.save(PhoeniciaContext.context);

        return newRequest;
    }
}
