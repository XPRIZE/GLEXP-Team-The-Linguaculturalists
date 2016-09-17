package com.linguaculturalists.phoenicia.ui;

import android.media.MediaPlayer;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.locale.tour.Message;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.debug.Debug;

import java.util.List;

/**
 * Created by mhall on 9/8/16.
 */
public class TourHUD extends PhoeniciaHUD implements MediaPlayer.OnCompletionListener {
    private PhoeniciaGame game;
    private Stop stop;
    private Sprite guideSprite;
    private ClickDetector clickDetector;
    private List<Message> messages;
    private int currentMessage;
    private boolean messagePlaying;
    private Text displayText;

    public TourHUD(final PhoeniciaGame game, final Stop stop) {
        super(game.camera);
        this.game = game;
        this.stop = stop;

        this.setBackgroundEnabled(false);
        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                Debug.d("Tour HUD clicked, playing="+messagePlaying);
                if (!messagePlaying) {
                    nextMessage();
                }
            }
        });

        this.guideSprite = new Sprite(-128, 192, game.personTiles.get(stop.tour.guide), PhoeniciaContext.vboManager);
        this.attachChild(guideSprite);

        this.messages = this.stop.getMessages();
        this.currentMessage = -1;
        this.messagePlaying = false;
    }

    private void nextMessage() {
        this.currentMessage++;
        Debug.d("Showing message: "+this.currentMessage);
        if (this.currentMessage >= this.messages.size()) {
            this.game.hudManager.pop();
            return;
        }
        String messageText = this.messages.get(this.currentMessage).text;
        String messageSound = this.messages.get(this.currentMessage).sound;
        if (this.displayText != null) {
            this.detachChild(this.displayText);
        }
        this.displayText = new Text((this.getWidth()-this.guideSprite.getWidth())/2, this.guideSprite.getY(), GameFonts.introText(), messageText, messageText.length(), new TextOptions(HorizontalAlign.LEFT), PhoeniciaContext.vboManager);
        this.displayText.setAutoWrapWidth(this.getWidth() - this.guideSprite.getWidth());
        this.displayText.setAutoWrap(AutoWrap.WORDS);
        this.attachChild(this.displayText);
        if (messageSound != null && messageSound.length() > 0) {
            Debug.d("Playing tour message sound: '" + messageSound + "'");
            this.messagePlaying = true;
            this.game.playLevelSound(messageSound, this);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        this.messagePlaying = false;
    }

    @Override
    public void open() {
        if (this.stop.hasFocus()) {
            MapBlockSprite focus = this.stop.getFocus();
            this.getCamera().setCenter(focus.getX(), focus.getY());
            ((SmoothCamera)this.getCamera()).setZoomFactor(2.0f);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void show() {
        this.guideSprite.registerEntityModifier(new MoveXModifier(0.5f, -128, 128));
        this.nextMessage();
    }

    /**
     * Capture scene touch events and look for click/touch events on the map to trigger placement.
     * All other touch events are passed through.
     *
     * @param pSceneTouchEvent
     * @return
     */
    @Override
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);

        return handled || this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
    }
}
