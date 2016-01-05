package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.locale.IntroPage;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;

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
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mhall on 8/26/15.
 */
public class LevelIntroHUD extends PhoeniciaHUD implements IOnSceneTouchListener {
    private PhoeniciaGame game;
    private Scrollable textPanel;
    private Level level;
    private int current_page;
    private Font introPageFont;

    public LevelIntroHUD(final PhoeniciaGame game, final Level level) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.game = game;
        this.level = level;
        this.current_page = 0;
        this.setOnSceneTouchListener(this);

        Rectangle whiteRect = new Rectangle(game.activity.CAMERA_WIDTH / 2, game.activity.CAMERA_HEIGHT / 2, 400, 400, game.activity.getVertexBufferObjectManager());
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);

        textPanel = new Scrollable(game.activity.CAMERA_WIDTH / 2, game.activity.CAMERA_HEIGHT / 2, 400, 400, Scrollable.SCROLL_VERTICAL);
        this.attachChild(textPanel);
        //textPanel.setClip(true);

        introPageFont = FontFactory.create(game.activity.getFontManager(), game.activity.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.BLUE_ARGB_PACKED_INT);
        introPageFont.load();
        this.showPage(0);


        this.registerTouchArea(textPanel);
        this.registerTouchArea(textPanel.contents);
        //this.attachChild(textPanel);

        final Font introFont = FontFactory.create(game.activity.getFontManager(), game.activity.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.RED_ARGB_PACKED_INT);
        introFont.load();

        Debug.d("Finished instantiating LevelIntroHUD");
    }

    private void showPage(int page_index) {
        this.current_page = page_index;
        final String nextPage = level.intro.get(page_index).text;
        final TextOptions introTextOptions = new TextOptions(AutoWrap.WORDS, 400, HorizontalAlign.CENTER);
        final Text introPageText = new Text(-textPanel.getWidth()/2, textPanel.getHeight()/2, introPageFont, nextPage, introTextOptions, game.activity.getVertexBufferObjectManager());
        textPanel.detachChildren();
        textPanel.attachChild(introPageText);

    }
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
            if (this.level.intro.size() > this.current_page + 1) {
                this.showPage(this.current_page + 1);
            } else {
                this.game.hudManager.pop();
            }
        }
        return true;
    }

    public void close() {
        return;
    }
}