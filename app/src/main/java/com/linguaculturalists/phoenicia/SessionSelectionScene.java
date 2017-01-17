package com.linguaculturalists.phoenicia;

import com.linguaculturalists.phoenicia.components.Button;
import com.linguaculturalists.phoenicia.components.Dialog;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.locale.Locale;
import com.linguaculturalists.phoenicia.locale.LocaleLoader;
import com.linguaculturalists.phoenicia.locale.LocaleManager;
import com.linguaculturalists.phoenicia.locale.Person;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Filter;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.Entity;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.input.touch.detector.HoldDetector;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.ease.EaseLinear;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mhall on 6/2/15.
 */
public class SessionSelectionScene extends Scene {
    Sprite background;
    private AssetBitmapTexture backgroundTexture;
    private ITextureRegion backgroundTextureRegion;
    private PhoeniciaGame game;
    private List<SessionSprite> sessionSprites;

    public SessionSelectionScene(final PhoeniciaGame game) {
        super();
        this.game = game;

        this.sessionSprites = new ArrayList<SessionSprite>();

        this.setBackground(new Background(new Color(100, 100, 100)));
        try {
            backgroundTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, "textures/session_selection_background.png", TextureOptions.BILINEAR);
            backgroundTextureRegion = TextureRegionFactory.extractFromTexture(backgroundTexture);
            backgroundTexture.load();
            background = new Sprite(0, 0, backgroundTextureRegion, PhoeniciaContext.vboManager);

            //background.setScale(1.5f);
            background.setPosition(GameActivity.CAMERA_WIDTH/2, GameActivity.CAMERA_HEIGHT/2);
            this.attachChild(background);
        } catch (final IOException e) {
            System.err.println("Error loading background!");
            e.printStackTrace(System.err);
        }

        List<GameSession> sessions = GameSession.objects(PhoeniciaContext.context).all().toList();
        Debug.d("Number of Sessions: "+sessions.size());

        Scrollable sessions_pane = new Scrollable(GameActivity.CAMERA_WIDTH/2, (GameActivity.CAMERA_HEIGHT/2)+100, GameActivity.CAMERA_WIDTH, 350, Scrollable.SCROLL_HORIZONTAL);
        sessions_pane.setPadding(16);
        this.attachChild(sessions_pane);
        this.registerTouchArea(sessions_pane);

        float startY = sessions_pane.getHeight() / 2;
        float startX = (sessions_pane.getWidth() / 2) - ((sessions.size()-1) * 136);
        if (startX < 136) {
            startX = 136f;
        }

        float offsetX = 0;
        for (int i = 0; i < sessions.size(); i++) {
            final GameSession session = sessions.get(i);

            SessionSprite sprite = new SessionSprite(startX + (272 * offsetX), startY, session, PhoeniciaContext.vboManager);
            this.registerTouchArea(sprite);
            sessions_pane.attachChild(sprite);

            sessionSprites.add(sprite);
            offsetX++;

        }

        //Button new_button = new Button(GameActivity.CAMERA_WIDTH/2, 100, 400, 100, "Mchezo Mpya", PhoeniciaContext.vboManager, new Button.OnClickListener() {
        Button new_button = new Button(GameActivity.CAMERA_WIDTH/2, 100, 400, 100, "New Game", PhoeniciaContext.vboManager, new Button.OnClickListener() {
            @Override
            public void onClicked(Button button) {
                newGame();
            }
        });
        this.attachChild(new_button);
        this.registerTouchArea(new_button);

        game.activity.getEngine().registerUpdateHandler(this);
    }

    private void newGame() {
        this.detachSelf();
        game.activity.getEngine().unregisterUpdateHandler(this);
        LocaleSelectionScene localeScene = new LocaleSelectionScene(this.game);
        game.activity.getEngine().setScene(localeScene);
        if (localeScene.available_locales.size() == 1) {
            localeScene.startGame(localeScene.available_locales.get(0));
        }
    }

    private void startGame(final GameSession session) {
        final LoadingScene loadingScene = new LoadingScene(game);
        session.save(PhoeniciaContext.context);
        this.detachSelf();
        game.activity.getEngine().unregisterUpdateHandler(this);
        game.activity.getEngine().setScene(loadingScene);
        game.activity.getEngine().registerUpdateHandler(loadingScene);
        loadingScene.load(session);
    }

    public void showDeleteConfirm(final SessionSprite sessionSprite) {
        final Scene scene = this;
        Dialog confirmDelete = new Dialog(400, 200, Dialog.Buttons.YES_NO, PhoeniciaContext.vboManager, new Dialog.DialogListener() {
            @Override
            public void onDialogButtonClicked(Dialog dialog, Dialog.DialogButton dialogButton) {
                if (dialogButton == Dialog.DialogButton.YES) {
                    Debug.d("Delete session");
                    deleteSession(sessionSprite);
                } else {
                    Debug.d("Don't delete session");
                }
                dialog.close();
                scene.unregisterTouchArea(dialog);
            }
        });
        confirmDelete.attachChild(new Text(200, 150, GameFonts.dialogText(), "Delete saved game?", 18, new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager));
        scene.registerTouchArea(confirmDelete);
        confirmDelete.open(scene);
    }

    public void deleteSession(final SessionSprite sessionSprite) {
        int spriteIndex = this.sessionSprites.indexOf(sessionSprite);
        sessionSprite.block.setVisible(false);
        for (int i = spriteIndex+1; i < sessionSprites.size(); i++) {
            SessionSprite next = sessionSprites.get(i);
            float start = next.getX();
            float end = next.getX()-272;
            Debug.d("Moving Sprite " + next.session.session_name.get() + " from " + start + " to " + end);
            next.setX(end);
            //next.registerEntityModifier(new MoveXModifier(5000, start, end, EaseLinear.getInstance()));
        }

        this.unregisterTouchArea(sessionSprite);
        this.detachChild(sessionSprite);
        sessionSprites.remove(sessionSprite);
        sessionSprite.session.delete(PhoeniciaContext.context);
    }

    private class SessionSprite extends Entity implements ClickDetector.IClickDetectorListener, HoldDetector.IHoldDetectorListener {
        public ClickDetector clickDetector;
        public HoldDetector holdDetector;
        public ButtonSprite block;
        public Text personName;
        public GameSession session;

        public SessionSprite(float px, float py, GameSession session, VertexBufferObjectManager vbo) {
            super(px, py, PhoeniciaGame.PERSON_TILE_WIDTH, PhoeniciaGame.PERSON_TILE_HEIGHT+32);
            Debug.d("Adding SessionSprite as "+px+","+py);
            this.session = session;
            if (session.session_name.get() == null) {
                Debug.d("Session has no name, setting it to Player " + (sessionSprites.size() + 1));
                session.session_name.set("Player "+(sessionSprites.size()+1));
                //session.session_name.set("Mchezaji"+(sessionSprites.size()+1));
                session.save(PhoeniciaContext.context);
            }
            Debug.i("Adding Game session: " + session.session_name.get() + " in " + session.locale_pack.get());
            LocaleLoader localeLoader = new LocaleLoader();
            Locale session_locale;
            try {
                session_locale = localeLoader.load(PhoeniciaContext.assetManager.open(session.locale_pack.get()));
                Person currentPerson = session_locale.person_map.get(session.person_name.get());
                if (currentPerson == null) {
                    Debug.w("Game Session without person!");
                    // TODO: use an "unknown user" image instead
                    int person_index = sessionSprites.size() % session_locale.people.size();
                    currentPerson = session_locale.people.get(person_index);
                    session.person_name.set(currentPerson.name);
                    session.save(PhoeniciaContext.context);
                }
                AssetBitmapTexture texture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, currentPerson.texture_src);
                texture.load();
                ITextureRegion personRegion = TextureRegionFactory.extractFromTexture(texture, 0, 0, game.PERSON_TILE_WIDTH, game.PERSON_TILE_HEIGHT);

                block = new ButtonSprite(this.getWidth()/2, py, personRegion, PhoeniciaContext.vboManager);

                personName = new Text(block.getWidth()/2, -16, GameFonts.dialogText(), session.session_name.get(), session.session_name.get().length(),  new TextOptions(AutoWrap.WORDS, 256, HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
                block.attachChild(personName);
                this.attachChild(block);
            } catch (final IOException e) {
                Debug.e("Failed to load game session person!", e);
            }

            clickDetector = new ClickDetector(this);
            holdDetector = new HoldDetector(this);
            holdDetector.setTriggerHoldMinimumMilliseconds(2000);
        }

        @Override
        public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
            Debug.d("Session " + session.session_name + " clicked");
            startGame(session);
        }

        @Override
        public void onHoldStarted(HoldDetector holdDetector, int i, float v, float v1) {
            Debug.d("Started Holding Session: " + session.session_name);
            showDeleteConfirm(this);
        }

        @Override
        public void onHold(HoldDetector holdDetector, long l, int i, float v, float v1) {

        }

        @Override
        public void onHoldFinished(HoldDetector holdDetector, long l, int i, float v, float v1) {

        }

        @Override
        public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
            if (holdDetector.onManagedTouchEvent(pSceneTouchEvent) && holdDetector.isHolding()) {
                return true;
            } else if (clickDetector.onManagedTouchEvent(pSceneTouchEvent)) {
                return true;
            }
            return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
        }

        @Override
        protected void onManagedUpdate(float pSecondsElapsed) {
            super.onManagedUpdate(pSecondsElapsed);
        }
    }
}
