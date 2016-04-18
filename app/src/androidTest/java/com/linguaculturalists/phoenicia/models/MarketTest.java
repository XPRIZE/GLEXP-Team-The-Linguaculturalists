package com.linguaculturalists.phoenicia.models;

import com.linguaculturalists.phoenicia.PhoeniciaGameTest;
import com.linguaculturalists.phoenicia.mock.MockMethod;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

/**
 * Created by mhall on 4/16/16.
 */
public class MarketTest extends PhoeniciaGameTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.loadGame();
        assertNotNull("Null context", PhoeniciaContext.context);
        Market.getInstance().clear();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void fillInventory(MarketRequest request) {
        for (RequestItem item : request.getItems(PhoeniciaContext.context)) {
            Inventory.getInstance().add(item.item_name.get(), item.quantity.get());
        }
    }

    public void testRequests() {
        assertEquals(0, Market.getInstance().requests().size());
        MarketRequest testRequest = Market.getInstance().createRequest();
        assertNotNull(testRequest);
        assertEquals(1, Market.getInstance().requests().size());
    }

    public void testFulfill() {
        MarketRequest testRequest = Market.getInstance().createRequest();
        this.fillInventory(testRequest);
        assertNotNull(testRequest);
        assertEquals(1, Market.getInstance().requests().size());
        Market.getInstance().fulfillRequest(testRequest);
        assertEquals(0, Market.getInstance().requests().size());
    }

    public void testCanceled() {
        MarketRequest testRequest = Market.getInstance().createRequest();
        assertNotNull(testRequest);
        assertEquals(1, Market.getInstance().requests().size());
        Market.getInstance().cancelRequest(testRequest);
        assertEquals(0, Market.getInstance().requests().size());
    }

    public void testPopulate() {
        Level currentLevel = game.locale.level_map.get(game.current_level);
        currentLevel.marketRequests = 0;
        assertEquals(0, Market.getInstance().requests().size());
        Market.getInstance().populate();
        assertEquals(0, Market.getInstance().requests().size());

        currentLevel.marketRequests = 1;
        Market.getInstance().populate();
        assertEquals(1, Market.getInstance().requests().size());

        currentLevel.marketRequests = 2;
        Market.getInstance().populate();
        assertEquals(2, Market.getInstance().requests().size());

    }

    public void testMoreRequestsThanPeople() {
        Level currentLevel = game.locale.level_map.get(game.current_level);
        final int tooManyRequests = game.locale.people.size() + 1;
        currentLevel.marketRequests = tooManyRequests;
        Market.getInstance().populate();
        assertEquals(tooManyRequests, Market.getInstance().requests().size());
    }

    public void testMarketListenerRequestAdded() {
        MockMarketUpdateListener listener = new MockMarketUpdateListener();
        Market.getInstance().addUpdateListener(listener);

        assertFalse(listener.requestAdded.called);
        MarketRequest testRequest = Market.getInstance().createRequest();
        assertNotNull(testRequest);
        assertTrue(listener.requestAdded.called);
        assertEquals(1, listener.requestAdded.call_count);
    }

    public void testMarketListenerRequestFulfilled() {
        MockMarketUpdateListener listener = new MockMarketUpdateListener();
        Market.getInstance().addUpdateListener(listener);

        assertFalse(listener.requestFulfilled.called);
        MarketRequest testRequest = Market.getInstance().createRequest();
        assertNotNull(testRequest);
        Market.getInstance().fulfillRequest(testRequest);
        assertTrue(listener.requestFulfilled.called);
        assertEquals(1, listener.requestFulfilled.call_count);
    }

    public void testMarketListenerRequestCanceled() {
        MockMarketUpdateListener listener = new MockMarketUpdateListener();
        Market.getInstance().addUpdateListener(listener);

        assertFalse(listener.requestCanceled.called);
        MarketRequest testRequest = Market.getInstance().createRequest();
        assertNotNull(testRequest);
        Market.getInstance().cancelRequest(testRequest);
        assertTrue(listener.requestCanceled.called);
        assertEquals(1, listener.requestCanceled.call_count);
    }

    class MockMarketUpdateListener implements Market.MarketUpdateListener {
        public MockMethod requestAdded = new MockMethod();
        public MockMethod requestFulfilled = new MockMethod();
        public MockMethod requestCanceled = new MockMethod();

        @Override
        public void onMarketRequestAdded(MarketRequest request) {
            this.requestAdded.call();
        }

        @Override
        public void onMarketRequestFulfilled(MarketRequest request) {
            this.requestFulfilled.call();
        }

        @Override
        public void onMarketRequestCanceled(MarketRequest request) {
            this.requestCanceled.call();
        }
    }


}