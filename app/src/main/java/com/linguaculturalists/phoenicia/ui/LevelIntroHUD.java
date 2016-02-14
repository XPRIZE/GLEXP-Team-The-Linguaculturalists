package com.linguaculturalists.phoenicia.ui;

import android.content.Context;
import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.locale.IntroPage;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.audio.sound.SoundManager;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Display level transition text pages, images and sound.
 */
public class LevelIntroHUD extends PhoeniciaHUD implements IOnSceneTouchListener, ClickDetector.IClickDetectorListener {
    private PhoeniciaGame game;
    private Scrollable textPanel;
    private Level level;
    private int current_page;
    private Font introPageFont;

    private ClickDetector clickDetector;

    public LevelIntroHUD(final PhoeniciaGame game, final Level level) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.game = game;
        this.level = level;
        this.current_page = 0;
        this.setOnSceneTouchListener(this);

        final float dialogWidth = GameActivity.CAMERA_WIDTH * 0.6f;
        final float dialogHeight = GameActivity.CAMERA_HEIGHT * 0.75f;
        Rectangle whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, dialogWidth, dialogHeight, PhoeniciaContext.vboManager);
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);

        textPanel = new Scrollable(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, dialogWidth, dialogHeight, Scrollable.SCROLL_VERTICAL);
        this.attachChild(textPanel);

        introPageFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 36, Color.BLUE_ARGB_PACKED_INT);
        introPageFont.load();

        this.registerTouchArea(textPanel);
        this.registerTouchArea(textPanel.contents);
        //this.attachChild(textPanel);

        Debug.d("Finished instantiating LevelIntroHUD");

        this.clickDetector = new ClickDetector(this);
    }

    @Override
    public void show() {
        this.showPage(this.current_page);
    }

    /**
     * Change the display to the specified IntroPage
     * @param page_index page to display
     */
    private void showPage(int page_index) {
        Debug.d("Showing page: "+page_index);
        this.current_page = page_index;
        final String nextPage = level.intro.get(page_index).text;
        final TextOptions introTextOptions = new TextOptions(AutoWrap.WORDS, textPanel.getWidth(), HorizontalAlign.CENTER);
        final Text introPageText = new Text(textPanel.getWidth()/2, textPanel.getHeight()/2, introPageFont, nextPage, introTextOptions, PhoeniciaContext.vboManager);
        introPageText.setPosition(textPanel.getWidth()/2, textPanel.getHeight()-(introPageText.getHeight()/2));

        textPanel.detachChildren();
        textPanel.attachChild(introPageText);
        game.playBlockSound(level.intro.get(page_index).sound);
    }

    @Override
    public void onClick(ClickDetector clickDetector, int pointerId, float sceneX, float sceneY) {
        Debug.d("Intro page size: " + this.level.intro.size());
        Debug.d("Current page: " + this.current_page);
        if (this.current_page+1 < this.level.intro.size()) {
            this.showPage(this.current_page + 1);
        } else {
            this.game.hudManager.pop();
        }

    }

    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
        return true;// Don't allow touch events to fall through to the scene below
    }

}