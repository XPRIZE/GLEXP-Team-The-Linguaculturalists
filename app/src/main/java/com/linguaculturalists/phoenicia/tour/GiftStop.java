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
import com.linguaculturalists.phoenicia.util.GameSounds;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Filter;

import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.debug.Debug;

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
    private static final int MSG_MARKETFINISH = 7;

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

    public GiftStop(Tour tour) {
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
        tour.game.hudManager.clear();
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
            case MSG_MARKETFINISH:
                this.showMarketSale();
                break;
            default:
                this.close();
        }

    }

    private void showMarketRequest() {
        this.overlay.focusOn(this.marketBlock);
        this.overlay.clearFocus();
        final GiftStop stop = this;
        this.tourRequest = createMarketRequestForGift();

        this.marketHUD = new MarketHUD(this.tour.game) {

            @Override
            protected void abortSale(RequestItem item, int needed, MarketRequest request) {
                final MarketHUD.MissingItemsDialog dialog = new MissingItemsDialog(450, 300, item, needed, request, PhoeniciaContext.vboManager) {
                    @Override
                    protected void onGiftClicked() {
                        stop.next();
                    }

                    @Override
                    protected void onReturnClicked() {
                        // Can't go back to the Market HUD
                        GameSounds.play(GameSounds.FAILED);
                    }
                };
                this.registerTouchArea(dialog);
                dialog.open(this);
                this.activeDialog = dialog;
            }

            @Override
            public void attemptSale(MarketRequest request) {
                // Can't attempt a sale, wait for the tour to initiate it
                if (stop.currentMessageIndex == MSG_REQUESTINTRO) {
                    return;
                } else {
                    super.attemptSale(request);
                    //stop.next();
                }
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
                Debug.d("Current message: "+stop.currentMessageIndex);
                return stop.currentMessageIndex == MSG_REQUESTINTRO;
            }
        });
        this.marketHUD.populateRequestItems(tourRequest);
        this.overlay.showMessage(this.getMessage(MSG_REQUESTINTRO), TourOverlay.MessageBox.Bottom, true);
    }

    private void showGiftRequestButton() {
        this.marketHUD.attemptSale(tourRequest);
        this.overlay.showMessage(this.getMessage(MSG_REQUESTSTART), TourOverlay.MessageBox.Top);
    }

    public void showGiftRequestCode() {
        final GiftStop stop = this;
        this.giftRequest = GiftRequest.newRequest(this.tour.game, this.requestWord, this.tourRequest);
        this.requestHUD = new RequestGiftHUD(this.tour.game, this.giftRequest) {

            @Override
            protected boolean verifyGift() {
                // don't verify anything yet
                return false;
            }
        };
        this.requestHUD.setNumberpadVisible(false);
        this.overlay.setManagedHUD(this.requestHUD, new IOnSceneTouchListener() {
            @Override
            public boolean onSceneTouchEvent(Scene scene, TouchEvent touchEvent) {
                // Capture scene touches
                stop.next();
                return false;
            }
        });
        this.overlay.showMessage(this.getMessage(MSG_REQUESTCODE), TourOverlay.MessageBox.Bottom, true);
    }

    public void showGiftResponseButton() {
        final GiftStop stop = this;
        this.overlay.focusOn(this.inventoryBlock);
        this.overlay.clearFocus();
        this.inventoryHUD = new InventoryHUD(this.tour.game) {
            @Override
            protected void sellLetter(LetterSprite block) {
                return;//Can't do that during the tour
            }

            @Override
            protected void sellWord(WordSprite block) {
                return;//Can't do that during the tour
            }

            @Override
            protected void sendGift() {
                stop.next();
            }
        };
        this.overlay.setManagedHUD(this.inventoryHUD);
        this.overlay.showMessage(this.getMessage(MSG_RESPONSESTART), TourOverlay.MessageBox.Top);
    }

    public void showGiftResponseCheck() {
        final GiftStop stop = this;
        this.responseHUD = new SendGiftHUD(this.tour.game) {
            @Override
            public void showResponseCode(Gift gift) {
                if (stop.currentMessageIndex == MSG_RESPONSECONFIRM) {
                    stop.next();
                } else {

                    super.showResponseCode(gift);
                }
            }

        };
        this.responseHUD.setNumberpadVisible(false);
        this.overlay.setManagedHUD(this.responseHUD);
        this.overlay.showMessage(this.getMessage(MSG_RESPONSECONFIRM), TourOverlay.MessageBox.Bottom, new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                for (int number : GiftCode.toArray(giftRequest.requestCode.get())) {
                    Number n = tour.game.locale.number_map.get(number);
                    responseHUD.inputNumber(n);
                    tour.game.playBlockSound(n.sound);
                    try{ Thread.sleep(500); } catch (Exception e) {}
                }
                Inventory.getInstance().add(requestWord.name); // add the one showResponseCode will remove
                responseHUD.showRequestItem();
            }
        });
    }

    public void showGiftResponseCode() {
        this.gift = Gift.newForRequest(tour.game.session, giftRequest.requestCode.get());
        this.overlay.showMessage(this.getMessage(MSG_RESPONSECODE), TourOverlay.MessageBox.Top, true, new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                responseHUD.showResponseCode(gift);
            }
        });
    }

    public void showGiftRequestAccept() {
        this.overlay.focusOn(this.marketBlock);
        this.overlay.clearFocus();
        this.overlay.setManagedHUD(this.requestHUD);
        this.overlay.showMessage(this.getMessage(MSG_REQUESTFINISH), TourOverlay.MessageBox.Top, true, new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                for (int number : GiftCode.toArray(gift.responseCode.get())) {
                    Number n = tour.game.locale.number_map.get(number);
                    requestHUD.inputNumber(n);
                    tour.game.playBlockSound(n.sound);
                    try{ Thread.sleep(500); } catch (Exception e) {}
                }
                requestHUD.acceptGift();
                try{ Thread.sleep(500); } catch (Exception e) {}
                //next();
            }
        });
    }

    public void showMarketSale() {
        this.overlay.setManagedHUD(this.marketHUD);
        this.marketHUD.closeActiveDialog();
        this.marketHUD.populateRequestItems(tourRequest);
        this.overlay.showMessage(this.getMessage(MSG_MARKETFINISH), TourOverlay.MessageBox.Bottom, true);

    }

    @Override
    public void close() {
        this.marketHUD.completeSale(tourRequest);
        super.close();
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
        final List<InventoryItem> inventoryItems = InventoryItem.objects(PhoeniciaContext.context).filter(this.tour.game.session.filter).orderBy("quantity").toList();
        Debug.d("Found " + inventoryItems.size() + " inventory items that could be used for the gift tour");
        for (InventoryItem item : inventoryItems) {
            Debug.d("Checking item "+item.item_name+" to see if it can be used for the gift tour");
            // Make sure it's a word for the tutorial text to match
            if (this.tour.game.locale.word_map.containsKey(item.item_name.get())) {
                this.requestWord = this.tour.game.locale.word_map.get(item.item_name.get());
                Debug.d("Adding Word " + requestWord.name + " to MarketHUD for gift tour");
                newItem.item_name.set(item.item_name.get());
                if (item.quantity.get() < 0) {item.quantity.set(0); item.save(PhoeniciaContext.context); }
                newItem.quantity.set(item.quantity.get() + 1);
                break;
            }
        }
        newItem.save(PhoeniciaContext.context);
        newRequest.coins.set(newItem.quantity.get() * requestWord.sell);
        newRequest.points.set(newItem.quantity.get() * requestWord.points);
        newRequest.save(PhoeniciaContext.context);

        return newRequest;
    }
}
