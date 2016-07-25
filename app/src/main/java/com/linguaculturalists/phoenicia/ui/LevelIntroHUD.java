package com.linguaculturalists.phoenicia.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.media.MediaPlayer;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Button;
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
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
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
public class LevelIntroHUD extends PhoeniciaHUD implements IOnSceneTouchListener, ClickDetector.IClickDetectorListener, MediaPlayer.OnCompletionListener {
    private PhoeniciaGame game;
    private Scrollable textPanel;
    private Level level;
    private int current_page;
    private Font introPageFont;
    private ITiledTextureRegion[] introPageImages;

    private ClickDetector clickDetector;
    private Button nextButton;

    /**
     * Display level transition text pages, images and sound.
     * @param game Reference to the PhoeniciaGame this HUD is running in
     * @param level The level to display introduction pages for
     */
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

        this.introPageImages = new ITiledTextureRegion[level.intro.size()];
        for (int i = 0; i < level.intro.size(); i++) {
            IntroPage page = level.intro.get(i);
            if (page.texture_src != null && page.texture_src != "") {
                try {
                    final Texture imageTexture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, page.texture_src);
                    imageTexture.load();
                    this.introPageImages[i] = TextureRegionFactory.extractTiledFromTexture(imageTexture, 0, 0, 128 * 4, 128 * 1, 4, 1);
                } catch (IOException e) {
                    Debug.e("Failed to load IntroPage texture: "+page.texture_src, e);
                }
            }
        }

        this.nextButton = new Button(whiteRect.getWidth() - 150, 50, 150, 80, "Next", PhoeniciaContext.vboManager, new Button.OnClickListener() {
            @Override
            public void onClicked(Button button) {
                if (current_page+1 < level.intro.size()) {
                    nextButton.setVisible(false);
                    showPage(current_page + 1);
                } else {
                    game.hudManager.pop();
                }
            }
        });
        nextButton.setVisible(false);
        this.registerTouchArea(nextButton);
        whiteRect.attachChild(nextButton);
        Debug.d("Finished instantiating LevelIntroHUD");

        this.clickDetector = new ClickDetector(this);
    }

    /**
     * Trigger showing the current page when the HUD is displayed to the player
     */
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
        introPageText.setPosition(textPanel.getWidth() / 2, textPanel.getHeight() - (introPageText.getHeight() / 2));

        textPanel.detachChildren();
        textPanel.attachChild(introPageText);

        if (this.introPageImages[page_index] != null) {
            final AnimatedSprite introImage = new AnimatedSprite(textPanel.getWidth()/2, textPanel.getHeight() - introPageText.getHeight() - 64, this.introPageImages[page_index], PhoeniciaContext.vboManager);
            textPanel.attachChild(introImage);
            introImage.animate(500);
        }
        game.playLevelSound(level.intro.get(page_index).sound, this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextButton.setVisible(true);
    }


    /**
     * Capture click events to close the HUD if the last intro page has finished playing
     * @param clickDetector
     * @param pointerId
     * @param sceneX
     * @param sceneY
     */
    @Override
    public void onClick(ClickDetector clickDetector, int pointerId, float sceneX, float sceneY) {
        if (this.current_page+1 >= this.level.intro.size() && nextButton.isVisible()) {
            this.game.hudManager.pop();
        }

    }

    /**
     * Capture scene touch events and check for click events
     * @param pScene
     * @param pSceneTouchEvent
     * @return
     */
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
        return true;// Don't allow touch events to fall through to the scene below
    }

}