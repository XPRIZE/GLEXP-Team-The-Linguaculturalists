package com.linguaculturalists.phoenicia.tour;

import android.media.MediaPlayer;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.locale.tour.Message;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.ui.DefaultHUD;
import com.linguaculturalists.phoenicia.ui.HUDManager;
import com.linguaculturalists.phoenicia.ui.PhoeniciaHUD;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.Entity;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.modifier.ScaleModifier;
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
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.io.IOException;
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
    public enum MessageBox {
        Top, Bottom;
    }
    private Text displayText;
    private MapBlockSprite targetSprite;
    private Entity focusSprite;
    private PhoeniciaHUD backgroundHUD;
    private PhoeniciaHUD managedHUD;

    public static final String SPOTLIGHT_NONE = "textures/tour/tour-focus-none.png";
    public static final String SPOTLIGHT_CENTER = "textures/tour/tour-focus-center.png";
    public static final String SPOTLIGHT_TOP_LEFT = "textures/tour/tour-focus-topleft.png";
    public static final String SPOTLIGHT_TOP_RIGHT = "textures/tour/tour-focus-topright.png";
    public static final String SPOTLIGHT_BOTTOM_LEFT = "textures/tour/tour-focus-bottomleft.png";
    public static final String SPOTLIGHT_BOTTOM_RIGHT = "textures/tour/tour-focus-bottomright.png";
    private Sprite spotlight;

    public TourOverlay(final PhoeniciaGame game, final Stop stop) {
        super(game.camera);
        this.setOnAreaTouchTraversalFrontToBack();
        this.game = game;
        this.stop = stop;

        this.setBackgroundEnabled(false);

        final float messageBoxHeight = 256;

        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                Debug.d("Tour HUD clicked, playing="+messagePlaying);
                if (!messagePlaying) {
                    stop.onClicked();
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

    public void showMessage(Message message) {
        this.showMessage(message, MessageBox.Top);
    }
    public void showMessage(Message message, MessageBox position) {
        String messageText = message.text;
        String messageSound = message.sound;
        this.messageBox.detachChildren();
        this.positionMessageBox(position);
        this.displayText = new Text(this.messageBox.getWidth()/2+16, this.messageBox.getHeight()/2, GameFonts.introText(), messageText, messageText.length(), new TextOptions(HorizontalAlign.LEFT), PhoeniciaContext.vboManager);
        this.displayText.setAutoWrapWidth(this.messageBox.getWidth() - 32);
        this.displayText.setAutoWrap(AutoWrap.WORDS);

        this.messageBox.setHeight(this.displayText.getHeight() + 64);
        this.displayText.setPosition(this.messageBox.getWidth() / 2 + 16, this.messageBox.getHeight() - (this.displayText.getHeight() / 2));

        this.messageBox.attachChild(this.displayText);
        if (messageSound != null && messageSound.length() > 0) {
            Debug.d("Playing tour message sound: '" + messageSound + "'");
            this.messagePlaying = true;
            this.game.playLevelSound(messageSound, this);
        }
    }
    private void positionMessageBox(MessageBox pos) {
        switch (pos) {
            case Bottom:
                this.guideSprite.setY(160);
                this.messageBox.setY(160);
                break;
            case Top:
                this.guideSprite.setY(this.getHeight() - 120);
                this.messageBox.setY(this.getHeight() - 120);
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        this.messagePlaying = false;
    }

    public void show() {
        this.stop.start(this);
        this.stop.next();
    }

    public void close() {
        this.clearManagedHUD();
        this.clearSpotlight();
        this.clearFocus();
    }

    public void showGuide() {
        this.guideSprite.registerEntityModifier(new MoveXModifier(0.5f, -128, 128));
    }

    public void clearSpotlight() {
        if (this.spotlight != null) {
            this.detachChild(this.spotlight);
            this.spotlight = null;
        }
    }
    public void setSpotlight(String texture) {
        try {
            final AssetBitmapTexture spotlight_texture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, texture);
            spotlight_texture.load();
            TextureRegion spotlight_region = TextureRegionFactory.extractFromTexture(spotlight_texture);
            this.clearSpotlight();
            this.spotlight = new Sprite(this.getWidth()/2, this.getHeight()/2, spotlight_region, PhoeniciaContext.vboManager);
            this.spotlight.setZIndex(this.guideSprite.getZIndex()-1);
            this.attachChild(this.spotlight);
            this.sortChildren();
        } catch (IOException e) {
            Debug.e("Failed to load spotlight texture: "+texture);
        }
    }

    public void focusOn(final MapBlockSprite target) { this.focusOn(target, null);}

    public void focusOn(final MapBlockSprite target, final MapBlockSprite.OnClickListener clickListener) {
        this.targetSprite = target;
        this.game.camera.setCenterDirect(target.getX(), target.getY());
        ((SmoothCamera)this.game.camera).setZoomFactor(2.0f);
        float[] targetSceneCoordinates = this.game.camera.getCameraSceneCoordinatesFromSceneCoordinates(target.getSceneCenterCoordinates());
        Debug.d("Tour focus sprite coordinates are: " + targetSceneCoordinates[1] + "x" + targetSceneCoordinates[1]);
        final ClickDetector spriteClickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                if (clickListener != null) {
                    clickListener.onClick(target, v, v1);
                }
            }
        });
        this.focusSprite = new Sprite(targetSceneCoordinates[0], targetSceneCoordinates[1], target.getTextureRegion(), PhoeniciaContext.vboManager) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                return spriteClickDetector.onManagedTouchEvent(pSceneTouchEvent);
            }
        };

        this.focusSprite.setScale(2.0f);
        this.focusSprite.registerEntityModifier(new ScaleModifier(0.5f, 2.0f, 3.0f));
        this.focusSprite.registerEntityModifier(new MoveYModifier(0.5f, this.focusSprite.getY(), this.focusSprite.getY() + 32));
        this.attachChild(this.focusSprite);
        this.registerTouchArea(this.focusSprite);
    }

    public void clearFocus() {
        if (this.focusSprite == null) return;
        this.detachChild(this.focusSprite);
        this.unregisterTouchArea(this.focusSprite);
    }

    public void setBackgroundHUD(PhoeniciaHUD hud) {
        this.backgroundHUD = hud;
        this.game.hudManager.push(hud);
    }

    public void setManagedHUD(PhoeniciaHUD hud) {
        this.setManagedHUD(hud, null);
    }
    public void setManagedHUD(PhoeniciaHUD hud, IOnSceneTouchListener touchListener) {
        if (hud == null) {
            this.clearManagedHUD();
            return;
        }
        if (this.managedHUD != null) {
            this.detachChild(this.managedHUD);
        }
        this.managedHUD = hud;
        Debug.d("Attaching HUD to tour overlay: " + hud);
        this.game.hudManager.setHudLayerVisible(false);
        this.attachChild(this.managedHUD);
        if (touchListener != null) {
            this.managedHUD.setOnSceneTouchListener(touchListener);
        }
    }
    public void clearManagedHUD() {
        if (this.managedHUD == null) return;
        this.game.hudManager.setHudLayerVisible(true);
        this.detachChild(this.managedHUD);
        this.managedHUD = null;
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
        boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);

        if (!handled && this.managedHUD != null) handled = this.managedHUD.onSceneTouchEvent(pSceneTouchEvent);
        if (!handled && this.spotlight != null) this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);

        // Don't allow click-through to background hud or game scene, the tour should handle
        // any interactions
        return this.spotlight != null;
    }

}
