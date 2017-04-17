package com.linguaculturalists.phoenicia.tour;

import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.locale.*;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.locale.tour.Tour;
import com.linguaculturalists.phoenicia.models.Gift;
import com.linguaculturalists.phoenicia.models.GiftRequest;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.MarketRequest;
import com.linguaculturalists.phoenicia.models.RequestItem;
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

    private static final int MSG_REQUESTSTART = 0;
    private static final int MSG_REQUESTCODE = 1;
    private static final int MSG_RESPONSESTART = 2;
    private static final int MSG_RESPONSECONFIRM = 3;
    private static final int MSG_RESPONSECODE = 4;
    private static final int MSG_REQUESTFINISH = 5;

    private MapBlockSprite marketBlock;
    private MapBlockSprite inventoryBlock;

    private Word requestWord;
    private MarketRequest tourRequest;
    private GiftRequest giftRequest;
    private Gift gift;

    private MarketHUD marketHUD;
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

    private void showGiftRequestButton() {
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
        this.overlay.setManagedHUD(this.marketHUD);
        this.marketHUD.populateRequestItems(tourRequest);
        this.marketHUD.completeSale(tourRequest);
        this.overlay.showMessage(this.getMessage(MSG_REQUESTSTART), TourOverlay.MessageBox.Top);
    }

    public void showGiftRequestCode() {
        final Stop stop = this;
        this.giftRequest = GiftRequest.newRequest(this.tour.game, this.requestWord, this.tourRequest);
        this.requestHUD = new RequestGiftHUD(this.tour.game, this.giftRequest) {
            @Override
            protected void inputNumber(com.linguaculturalists.phoenicia.locale.Number n) {
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

    }

    public void showGiftResponseCheck() {

    }

    public void showGiftResponseCode() {

    }

    public void showGiftRequestAccept() {

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
