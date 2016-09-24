package com.linguaculturalists.phoenicia.tour;

import android.media.MediaPlayer;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.locale.tour.Message;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.ui.HUDManager;
import com.linguaculturalists.phoenicia.ui.PhoeniciaHUD;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.primitive.Gradient;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.EntityBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.util.List;

/**
 * Created by mhall on 9/8/16.
 */
public class TourOverlay extends CameraScene implements MediaPlayer.OnCompletionListener {
    private PhoeniciaGame game;
    private Stop stop;
    private Sprite guideSprite;
    private ClickDetector clickDetector;
    private List<Message> messages;
    private int currentMessage;
    private boolean messagePlaying;
    private Rectangle messageBox;
    private Text displayText;

    public TourOverlay(final PhoeniciaGame game, final Stop stop) {
        super(game.camera);
        this.setOnAreaTouchTraversalFrontToBack();
        this.game = game;
        this.stop = stop;

        this.setBackgroundEnabled(false);

        final float messageBoxHeight = 256;

        Gradient g = new Gradient(this.getWidth()/2, 160, this.getWidth(), 320, PhoeniciaContext.vboManager);
        g.setGradient(new Color(0, 0, 0, 0.8f), new Color(0, 0, 0, 0f), 0, 1);
        g.setGradientFitToBounds(true);
        g.setGradientDitherEnabled(true);
        this.attachChild(g);

        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                Debug.d("Tour HUD clicked, playing="+messagePlaying);
                if (!messagePlaying) {
                    stop.next();
                }
            }
        });

        this.guideSprite = new Sprite(-112, 160, game.personTiles.get(stop.tour.guide), PhoeniciaContext.vboManager);
        this.attachChild(guideSprite);
        final float messageBoxWidth = this.getWidth()-this.guideSprite.getWidth()-32;

        this.messages = this.stop.getMessages();
        this.currentMessage = -1;
        this.messagePlaying = false;

        Debug.d("Tour overlay guide sprite width: "+this.guideSprite.getWidth());
        this.messageBox = new Rectangle((messageBoxWidth/2)+this.guideSprite.getWidth(), this.guideSprite.getY(), messageBoxWidth, messageBoxHeight, PhoeniciaContext.vboManager);
        this.messageBox.setColor(Color.WHITE);
        this.attachChild(messageBox);

        Debug.d("TourOverlay ready");
    }

    public void nextMessage() {
        this.currentMessage++;
        Debug.d("Showing message: "+this.currentMessage);
        if (this.currentMessage >= this.messages.size()) {
            Debug.d("No more messages, closing the overlay");
            this.close();
            return;
        }
        String messageText = this.messages.get(this.currentMessage).text;
        String messageSound = this.messages.get(this.currentMessage).sound;
        this.messageBox.detachChildren();
        this.displayText = new Text(this.messageBox.getWidth()/2+16, this.messageBox.getHeight()/2, GameFonts.introText(), messageText, messageText.length(), new TextOptions(HorizontalAlign.LEFT), PhoeniciaContext.vboManager);
        this.displayText.setAutoWrapWidth(this.messageBox.getWidth() - 32);
        this.displayText.setAutoWrap(AutoWrap.WORDS);

        this.messageBox.setHeight(this.displayText.getHeight()+64);
        this.displayText.setPosition(this.messageBox.getWidth() / 2 + 16, this.messageBox.getHeight() - (this.displayText.getHeight() / 2));

        this.messageBox.attachChild(this.displayText);
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

    public void show() {
        this.stop.run(this);
    }

    private void close() {
        this.stop.close();
    }
    public void showGuide() {
        this.guideSprite.registerEntityModifier(new MoveXModifier(0.5f, -128, 128));
    }

    public void setBackgroundHUD(PhoeniciaHUD hud) {
        Debug.d("Attaching tour overlay to HUD: "+hud);
        this.game.hudManager.push(hud);
        hud.setChildScene(this, false, true, true);
        this.setVisible(true);
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
        Debug.v("Overlay touched");
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);

        if (!handled) this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);

        // Don't allow click-through to background hud or game scene, the tour should handle
        // any interactions
        return true;
    }

}
