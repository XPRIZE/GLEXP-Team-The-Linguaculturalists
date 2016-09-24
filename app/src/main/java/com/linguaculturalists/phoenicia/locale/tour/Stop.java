package com.linguaculturalists.phoenicia.locale.tour;

import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.ui.PhoeniciaHUD;
import com.linguaculturalists.phoenicia.tour.TourOverlay;

import org.andengine.engine.camera.SmoothCamera;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhall on 9/8/16.
 */
public abstract class Stop {
    public Tour tour;
    private MapBlockSprite focus;
    private List<Message> messages;
    protected TourOverlay overlay;

    public Stop(Tour tour) {
        this.tour = tour;
        this.messages = new ArrayList<Message>();
    }

    public abstract void run(TourOverlay overlay);
    public abstract void next();
    public abstract void close();

    public boolean hasFocus() {
        return this.focus != null;
    }

    public void setFocus(MapBlockSprite focus) {
        this.focus = focus;
    }

    public MapBlockSprite getFocus() {
        return this.focus;
    }

    public List<Message> getMessages() {
        return this.messages;
    }
    public void addMessage(Message m) {
        m.stop = this;
        this.messages.add(m);
    }
    public void removeMessage(Message m) {
        this.messages.remove(m);
        m.stop = null;
    }

    public void moveCameraTo(MapBlockSprite target) {
        this.tour.game.camera.setCenter(target.getX(), target.getY() - target.getHeight());
        ((SmoothCamera)this.tour.game.camera).setZoomFactor(2.0f);
    }

}
