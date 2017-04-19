package com.linguaculturalists.phoenicia.tour;

import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.locale.tour.Tour;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.MarketRequest;
import com.linguaculturalists.phoenicia.models.RequestItem;
import com.linguaculturalists.phoenicia.ui.MarketHUD;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import java.util.List;

/**
 * Created by mhall on 10/15/16.
 */
public class MarketStop extends Stop {

    private static final int MSG_MAPBLOCK = 0;
    private static final int MSG_REQUESTS = 1;
    private static final int MSG_SELLING = 2;

    public MarketStop(Tour tour) { super(tour); }

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
            case MSG_MAPBLOCK:
                this.showBlock();
                break;
            case MSG_REQUESTS:
                this.showRequests();
                break;
            case MSG_SELLING:
                this.showSelling();
                break;
            default:
                this.close();
        }
    }

    private void showBlock() {
        this.overlay.focusOn(this.getFocus(), new MapBlockSprite.OnClickListener() {
            @Override
            public void onClick(MapBlockSprite pPlacedBlockSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                next();
            }

            @Override
            public void onHold(MapBlockSprite pPlacedBlockSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {

            }
        });
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
        this.overlay.showGuide();
        this.overlay.showMessage(this.getMessage(MSG_MAPBLOCK), TourOverlay.MessageBox.Top);
    }

    private void showRequests() {
        this.overlay.clearFocus();

        final MarketStop stop = this;
        final MarketHUD hud = new MarketHUD(this.tour.game) {
            @Override
            protected void declineSale(MarketRequest request) {
                // Can't decline sales during the tour
            }

            @Override
            public void completeSale(MarketRequest request) {
                super.completeSale(request);
                stop.next();
            }

            @Override
            protected List<MarketRequest> getRequests() {
                // Only present one request
                List<MarketRequest> requests =  super.getRequests().subList(0, 1);
                // Ensure that the player can fulfill the request
                for (RequestItem item : requests.get(0).getItems(PhoeniciaContext.context)) {
                    Inventory.getInstance().add(item.item_name.get(), item.quantity.get());
                }
                return requests;
            }

            @Override
            public void populateRequestItems(MarketRequest request) {
                super.populateRequestItems(request);
                stop.next();
            }

            @Override
            public void finish() {
                return;
            }
        };
        this.overlay.setSpotlight(TourOverlay.SPOTLIGHT_NONE);
        this.overlay.setManagedHUD(hud);
        this.overlay.showMessage(this.getMessage(MSG_REQUESTS), TourOverlay.MessageBox.Bottom);
    }

    private void showSelling() {
        this.overlay.showMessage(this.getMessage(MSG_SELLING), TourOverlay.MessageBox.Top);
    }


}
