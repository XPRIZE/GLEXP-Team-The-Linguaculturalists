package com.linguaculturalists.phoenicia.ui;

import android.media.MediaPlayer;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.BorderRectangle;
import com.linguaculturalists.phoenicia.components.Button;
import com.linguaculturalists.phoenicia.locale.IntroPage;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameUI;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.io.IOException;

/**
 * Display level transition text pages, images and sound.
 */
public class LevelIntroHUD extends PhoeniciaHUD implements IOnSceneTouchListener, ClickDetector.IClickDetectorListener, MediaPlayer.OnCompletionListener {
    private Rectangle messageBox;
    private Sprite guideSprite;
    private Level level;
    private int current_page;

    private ClickDetector clickDetector;
    private ButtonSprite nextButton;

    /**
     * Display level transition text pages, images and sound.
     * @param game Reference to the PhoeniciaGame this HUD is running in
     * @param level The level to display introduction pages for
     */
    public LevelIntroHUD(final PhoeniciaGame game, final Level level) {
        super(game);
        this.setBackgroundEnabled(false);
        this.game = game;
        this.level = level;
        this.current_page = 0;
        this.setOnSceneTouchListener(this);

        Debug.d("New level guide: "+game.locale.tour.guide.name);
        this.guideSprite = new Sprite(-112, 160, game.personTiles.get(game.locale.tour.guide), PhoeniciaContext.vboManager);
        this.attachChild(guideSprite);
        final float messageBoxWidth = this.getWidth()-this.guideSprite.getWidth()-32;
        final float messageBoxHeight = 256;

        String texture = "textures/tour/tour-focus-none.png";
        try {
            final AssetBitmapTexture spotlight_texture = new AssetBitmapTexture(PhoeniciaContext.textureManager, PhoeniciaContext.assetManager, texture);
            spotlight_texture.load();
            TextureRegion spotlight_region = TextureRegionFactory.extractFromTexture(spotlight_texture);
            Sprite backdrop = new Sprite(this.getWidth()/2, this.getHeight()/2, spotlight_region, PhoeniciaContext.vboManager);
            backdrop.setZIndex(guideSprite.getZIndex()-1);
            this.attachChild(backdrop);
            this.sortChildren();
        } catch (IOException e) {
            Debug.e("Failed to load spotlight texture: "+texture);
        }

        Debug.d("New Level into guide sprite width: "+this.guideSprite.getWidth());
        this.messageBox = new BorderRectangle((messageBoxWidth/2)+this.guideSprite.getWidth(), this.guideSprite.getY(), messageBoxWidth, messageBoxHeight, PhoeniciaContext.vboManager);
        this.messageBox.setColor(Color.WHITE);
        this.attachChild(messageBox);
        this.registerTouchArea(messageBox);

        ITextureRegion nextRegion = GameUI.getInstance().getNextIcon();
        this.nextButton = new ButtonSprite(messageBox.getWidth() - 48, 50, nextRegion, PhoeniciaContext.vboManager);
        this.nextButton.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                if (current_page+1 < level.intro.size()) {
                    nextButton.setVisible(false);
                    showPage(current_page + 1);
                } else {
                    finish();
                }
            }
        });
        this.nextButton.setVisible(false);
        this.registerTouchArea(this.nextButton);
        this.messageBox.attachChild(this.nextButton);
        Debug.d("Finished instantiating LevelIntroHUD");

        this.clickDetector = new ClickDetector(this);
    }

    /**
     * Trigger showing the current page when the HUD is displayed to the player
     */
    @Override
    public void show() {
        this.showPage(this.current_page);
        this.guideSprite.registerEntityModifier(new MoveXModifier(0.5f, -128, 128));
    }

    /**
     * Change the display to the specified IntroPage
     * @param page_index page to display
     */
    private void showPage(int page_index) {
        Debug.d("Showing page: "+page_index);
        this.current_page = page_index;
        final String nextPage = level.intro.get(page_index).text;
        final TextOptions introTextOptions = new TextOptions(AutoWrap.WORDS, messageBox.getWidth()-64, HorizontalAlign.LEFT);
        final Text introPageText = new Text(messageBox.getWidth()/2 - 32, messageBox.getHeight()/2, GameFonts.introText(), nextPage, introTextOptions, PhoeniciaContext.vboManager);
        introPageText.setPosition(messageBox.getWidth() / 2, messageBox.getHeight() - (introPageText.getHeight() / 2));

        this.messageBox.setHeight(introPageText.getHeight() + 64);
        introPageText.setPosition(this.messageBox.getWidth() / 2 + 16, this.messageBox.getHeight() - (introPageText.getHeight() / 2));

        messageBox.detachChildren();
        messageBox.attachChild(introPageText);
        messageBox.attachChild(this.nextButton);

        game.playLevelSound(level.intro.get(page_index).sound, this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Debug.d("Showing next button now!");
        this.nextButton.setVisible(true);
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
            this.finish();
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

    @Override
    public void finish() {
        game.hudManager.clear();
    }


}