package com.linguaculturalists.phoenicia.ui;

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

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Display level transition text pages, images and sound.
 */
public class LevelIntroHUD extends PhoeniciaHUD implements IOnSceneTouchListener {
    private PhoeniciaGame game;
    private Scrollable textPanel;
    private Level level;
    private int current_page;
    private Font introPageFont;

    private boolean isPressed = false;

    public LevelIntroHUD(final PhoeniciaGame game, final Level level) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.game = game;
        this.level = level;
        this.current_page = 0;
        this.setOnSceneTouchListener(this);

        Rectangle whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 400, 400, PhoeniciaContext.vboManager);
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);

        textPanel = new Scrollable(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, 400, 400, Scrollable.SCROLL_VERTICAL);
        this.attachChild(textPanel);
        //textPanel.setClip(false);

        introPageFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 36, Color.BLUE_ARGB_PACKED_INT);
        introPageFont.load();
        this.showPage(0);


        this.registerTouchArea(textPanel);
        this.registerTouchArea(textPanel.contents);
        //this.attachChild(textPanel);

        Debug.d("Finished instantiating LevelIntroHUD");
    }

    /**
     * Change the display to the specified IntroPage
     * @param page_index page to display
     */
    private void showPage(int page_index) {
        Debug.d("Showing page: "+page_index);
        this.current_page = page_index;
        final String nextPage = level.intro.get(page_index).text;
        final TextOptions introTextOptions = new TextOptions(AutoWrap.WORDS, 400, HorizontalAlign.CENTER);
        final Text introPageText = new Text(textPanel.getWidth()/2, textPanel.getHeight()/2, introPageFont, nextPage, introTextOptions, PhoeniciaContext.vboManager);

        textPanel.detachChildren();
        textPanel.attachChild(introPageText);

    }
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
            this.isPressed = true;
            return true;
        } else if (this.isPressed && pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
            Debug.d("Intro page size: "+this.level.intro.size());
            Debug.d("Current page: "+this.current_page);
            if (this.current_page+1 < this.level.intro.size()) {
                this.showPage(this.current_page + 1);
            } else {
                this.game.hudManager.pop();
            }
        }
        this.isPressed = false;
        return true;
    }


}