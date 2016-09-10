package com.linguaculturalists.phoenicia.locale.tour;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Person;
import com.linguaculturalists.phoenicia.locale.tour.stops.InventoryStop;
import com.linguaculturalists.phoenicia.locale.tour.stops.WelcomeStop;
import com.linguaculturalists.phoenicia.models.GameSession;

/**
 * Created by mhall on 9/8/16.
 */
public class Tour {
    public PhoeniciaGame game;
    public Person guide;

    public WelcomeStop welcome;
    public InventoryStop inventory;

    public Tour() {
        this.welcome = new WelcomeStop(this);
        this.inventory = new InventoryStop(this);
    }

    public void init(PhoeniciaGame game) {
        this.game = game;
        this.guide = game.locale.person_map.get(game.session.person_name.get());

    }
}
