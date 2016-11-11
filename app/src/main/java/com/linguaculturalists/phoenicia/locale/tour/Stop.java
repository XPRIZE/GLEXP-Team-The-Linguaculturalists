package com.linguaculturalists.phoenicia.locale.tour;

import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.ui.PhoeniciaHUD;
import com.linguaculturalists.phoenicia.tour.TourOverlay;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.input.touch.TouchEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhall on 9/8/16.
 */
public abstract class Stop {
    public Tour tour;
    private MapBlockSprite focus;
    private List<Message> messages;
    protected int currentMessageIndex;
    protected TourOverlay overlay;

    public Stop(Tour tour) {
        this.tour = tour;
        this.messages = new ArrayList<Message>();
        this.currentMessageIndex = -1;
    }

    public abstract void start(TourOverlay overlay);

    public abstract void show(int messageIndex);

    public abstract void onClicked();

    public void next() {
        this.currentMessageIndex++;
        this.show(this.currentMessageIndex);
    }
    public void goTo(int messageIndex) {
        this.currentMessageIndex = messageIndex;
        this.show(messageIndex);
    }

    public void close() {
        this.tour.game.hudManager.endTour();
    }

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
    public Message getMessage(int messageIndex) {
        return this.messages.get(messageIndex);
    }
    public void removeMessage(Message m) {
        this.messages.remove(m);
        m.stop = null;
    }


}
