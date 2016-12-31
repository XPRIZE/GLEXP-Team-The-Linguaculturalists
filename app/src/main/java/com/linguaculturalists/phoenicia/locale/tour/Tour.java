package com.linguaculturalists.phoenicia.locale.tour;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Person;
import com.linguaculturalists.phoenicia.tour.InventoryStop;
import com.linguaculturalists.phoenicia.tour.MarketStop;
import com.linguaculturalists.phoenicia.tour.WelcomeStop;
import com.linguaculturalists.phoenicia.tour.WordsStop;
import com.linguaculturalists.phoenicia.tour.WorkshopStop;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhall on 9/8/16.
 */
public class Tour {
    public PhoeniciaGame game;
    public Person guide;

    public WelcomeStop welcome;
    public InventoryStop inventory;
    public MarketStop market;
    public WorkshopStop workshop;
    public WordsStop words;

    public Tour() {
        this.welcome = new WelcomeStop(this);
        this.inventory = new InventoryStop(this);
        this.words = new WordsStop(this);
        this.market = new MarketStop(this);
        this.workshop = new WorkshopStop(this);
    }

    public List<Stop> getStops() {
        List<Stop> list = new ArrayList<Stop>();
        list.add(this.welcome);
        list.add(this.inventory);
        list.add(this.words);
        list.add(this.market);
        list.add(this.workshop);
        return list;
    }

    public void init(PhoeniciaGame game) {
        this.game = game;
        if (this.guide == null) {
            this.guide = game.locale.person_map.get(game.session.person_name.get());
        }
    }
}
