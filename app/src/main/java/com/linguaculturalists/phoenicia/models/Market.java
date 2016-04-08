package com.linguaculturalists.phoenicia.models;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Locale;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Filter;

import org.andengine.util.debug.Debug;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Managing class for generating and querying market requests
 */
public class Market {
    private static Market instance;
    private PhoeniciaGame game;
    private GameSession session;
    private List<MarketUpdateListener> listeners;

    private Market(PhoeniciaGame game) {
        this.game = game;
        this.session = game.session;
        this.listeners = new LinkedList<MarketUpdateListener>();
    }

    /**
     * Initialize the singleton for a given session
     * @param session
     */
    public static void init(PhoeniciaGame game) {
        instance = new Market(game);
    }

    /**
     * Returns an Singleton instance of the Inventory class.
     * You must call init(PhoeniciaGame) before calling this method
     * @return
     */
    public static Market getInstance() {
        return instance;
    }

    /**
     * Retrieve a list of all active requests in the market
     * @return a list of active \link MarketRequest MarketRequests \endlink
     */
    public List<MarketRequest> requests() {
        Filter openRequests = new Filter();
        openRequests.is("status", MarketRequest.REQUESTED);
        List<MarketRequest> items = MarketRequest.objects(PhoeniciaContext.context).filter(this.session.filter).filter(openRequests).toList();
        Debug.d("Found "+items.size()+" requests");
        return items;
    }

    public void populate() {
        Debug.d("Populating marketplace");
        int limit = this.game.locale.level_map.get(this.game.current_level).marketRequests;
        Debug.d("Level " + this.game.current_level + " accepts up to " + limit + " requests");
        int needed = limit - this.requests().size();
        Debug.d("Need "+needed+" more requests");
        if (needed > 0) {
            for (int i = 0; i < needed; i++) {
                MarketRequest newRequest = this.createRequest();
                Debug.d("Adding request from "+newRequest.person_name.get());
            }
        }
    }

    public MarketRequest createRequest() {
        Date now = new Date();
        MarketRequest request = new MarketRequest();
        request.game.set(this.session);
        // TODO: pick random available person
        int person_id = Math.round((float)Math.random());
        request.person_name.set(this.game.locale.people.get(person_id).name);
        request.status.set(MarketRequest.REQUESTED);
        request.requested.set((double) now.getTime());
        request.save(PhoeniciaContext.context);

        // TODO: pick letters and words based on inventory, history and level
        final List<Letter> levelLetters = this.game.locale.level_map.get(this.game.current_level).letters;
        if (levelLetters.size() > 0) {
            RequestItem requestLetter = new RequestItem();
            requestLetter.game.set(this.session);
            requestLetter.request.set(request);
            int randomLetter = (int) (Math.random() * levelLetters.size());
            requestLetter.item_name.set(levelLetters.get(randomLetter).name);
            requestLetter.quantity.set((int) (Math.random() * 5) + 1);
            requestLetter.save(PhoeniciaContext.context);
        }

        final List<Word> levelWords = this.game.locale.level_map.get(this.game.current_level).words;
        if (levelWords.size() > 0) {
            RequestItem requestWord = new RequestItem();
            requestWord.game.set(this.session);
            requestWord.request.set(request);
            int randomWord = (int) (Math.random() * levelWords.size());
            requestWord.item_name.set(levelWords.get(randomWord).name);
            requestWord.quantity.set((int) (Math.random() * 5) + 1);
            requestWord.save(PhoeniciaContext.context);
        }
        this.requestAdded(request);
        return request;
    }

    /**
     * Delete all Market requests
     */
    public void clear() {
        List<MarketRequest> items = MarketRequest.objects(PhoeniciaContext.context).filter(this.session.filter).toList();
        for (int i = 0; i < items.size(); i++) {
            items.get(i).delete(PhoeniciaContext.context);
        }
    }

    private void requestAdded(MarketRequest request) {
        for (MarketUpdateListener listener : this.listeners) {
            listener.onMarketRequestAdded(request);
        }
    }

    private void requestFulfilled(MarketRequest request) {
        for (MarketUpdateListener listener : this.listeners) {
            listener.onMarketRequestAdded(request);
        }
    }

    private void requestCanceled(MarketRequest request) {
        for (MarketUpdateListener listener : this.listeners) {
            listener.onMarketRequestAdded(request);
        }
    }

    public void addUpdateListener(MarketUpdateListener listener) {
        this.listeners.add(listener);
    }
    public void removeUpdateListener(MarketUpdateListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Callback listener for changes to item quantities in the inventory.
     */
    public interface MarketUpdateListener {
        public void onMarketRequestAdded(final MarketRequest request);
        public void onMarketRequestFulfilled(final MarketRequest request);
        public void onMarketRequestCanceled(final MarketRequest request);
    }

}
