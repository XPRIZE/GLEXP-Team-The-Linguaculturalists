package com.linguaculturalists.phoenicia.models;

import android.test.AndroidTestCase;

import com.linguaculturalists.phoenicia.PhoeniciaGameTest;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhall on 4/16/16.
 */
public class MarketTest extends PhoeniciaGameTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.loadGame();
        assertNotNull("Null context", PhoeniciaContext.context);

    }

    @Override
    protected void tearDown() throws Exception {
        Market.getInstance().clear();
        super.tearDown();
    }

    public void testRequests() {
        assertEquals(0, Market.getInstance().requests().size());
        MarketRequest testRequest = Market.getInstance().createRequest();
        assertNotNull(testRequest);
        assertEquals(1, Market.getInstance().requests().size());
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
}