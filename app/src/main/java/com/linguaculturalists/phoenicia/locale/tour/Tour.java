package com.linguaculturalists.phoenicia.locale.tour;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Person;
import com.linguaculturalists.phoenicia.tour.InventoryStop;
import com.linguaculturalists.phoenicia.tour.MarketStop;
import com.linguaculturalists.phoenicia.tour.WelcomeStop;
import com.linguaculturalists.phoenicia.tour.WorkshopStop;

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

    public Tour() {
        this.welcome = new WelcomeStop(this);
        this.inventory = new InventoryStop(this);
        this.market = new MarketStop(this);
        this.workshop = new WorkshopStop(this);
    }

    public void init(PhoeniciaGame game) {
        this.game = game;
        this.guide = game.locale.person_map.get(game.session.person_name.get());

    }
}
