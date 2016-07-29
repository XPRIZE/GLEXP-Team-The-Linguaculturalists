package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Button;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.GameTile;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mhall on 7/26/16.
 */
public class WordMatchGameHUD extends PhoeniciaHUD {
    private PhoeniciaGame phoeniciaGame;
    private Level level;
    private int current_round;
    private GameTile tile;
    private List<Word> random_word_list;
    private static int max_rounds = 10;
    private static int max_choices = 3;

    private Font choiceWordFont;
    private Entity cardPane;
    private Entity resultsPane;
    private int result_number;
    private List<Word> winnings;
    private List<Entity> touchAreas;

    public WordMatchGameHUD(final PhoeniciaGame phoeniciaGame, final Level level, final GameTile tile) {
        super(phoeniciaGame.camera);
        this.setBackgroundEnabled(false);
        this.phoeniciaGame = phoeniciaGame;
        this.level = level;
        this.tile = tile;
        this.current_round = 0;
        this.random_word_list = new ArrayList<Word>(level.words);
        Collections.shuffle(this.random_word_list);

        if (this.random_word_list.size() < this.max_rounds) {
            this.max_rounds = this.random_word_list.size();
        }

        if (this.random_word_list.size() < this.max_choices) {
            this.max_choices = this.random_word_list.size();
        }

        this.touchAreas = new ArrayList<Entity>();
        choiceWordFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 36, Color.BLUE_ARGB_PACKED_INT);
        choiceWordFont.load();

        final float dialogWidth = 800;
        final float dialogHeight = 600;
        Rectangle whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, dialogWidth, dialogHeight, PhoeniciaContext.vboManager);
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);

        this.cardPane = new Entity(whiteRect.getWidth()/2, 400, whiteRect.getWidth(), 400);
        whiteRect.attachChild(cardPane);

        this.resultsPane = new Entity(whiteRect.getWidth()/2, 100, whiteRect.getWidth(), 200);
        whiteRect.attachChild(this.resultsPane);
        this.winnings = new ArrayList<Word>();
        this.result_number = 0;
    }

    @Override
    public void show() {
        this.show_round(this.current_round);
    }

    private void pass(Word word) {
        //TODO: count success
        Debug.d("wordmatch: pass!");
        this.winnings.add(word);
        ITiledTextureRegion sprite_region = this.phoeniciaGame.wordSprites.get(word);
        Sprite winning_sprite = new Sprite(this.cardPane.getWidth()/2, this.cardPane.getHeight() - 300, sprite_region.getTextureRegion(1), PhoeniciaContext.vboManager);
        this.resultsPane.attachChild(winning_sprite);
        winning_sprite.registerEntityModifier(new ParallelEntityModifier(
                new MoveYModifier(0.5f, 300, 80),
                new MoveXModifier(0.5f, this.resultsPane.getWidth()/2, 48+(this.result_number*80))
                ));
        this.result_number++;
    }

    private void fail(Word word) {
        //TODO: count failure
        Debug.d("wordmatch: fail!");
        ITiledTextureRegion sprite_region = this.phoeniciaGame.wordSprites.get(word);
        Sprite missed_sprite = new Sprite(this.cardPane.getWidth()/2, this.cardPane.getHeight() - 300, sprite_region.getTextureRegion(2), PhoeniciaContext.vboManager);
        this.resultsPane.attachChild(missed_sprite);
        missed_sprite.registerEntityModifier(new ParallelEntityModifier(
                new MoveYModifier(0.5f, 300, 80),
                new MoveXModifier(0.5f, this.resultsPane.getWidth()/2, 48+(this.result_number*80))
                ));
        this.result_number++;
    }

    private void next_round() {
        if (this.current_round < this.max_rounds) {
            this.show_round(++this.current_round);
        } else {
            // TODO: Show winnings
            this.phoeniciaGame.hudManager.pop();
        }
    }
    private void show_round(int round) {
        this.cardPane.detachChildren();
        for (Entity toucharea : this.touchAreas) {
           this.unregisterTouchArea(toucharea);
        }
        this.touchAreas.clear();

        int word_index = round % this.max_rounds;
        final Word challenge_word = this.random_word_list.get(word_index);

        // Take the challenge word out of the list and shuffle it
        List<Word> draw_words = new ArrayList<Word>(this.random_word_list);
        draw_words.remove(challenge_word);
        Collections.shuffle(draw_words);

        // Draw max_choices-1 from the list, add the challenge word to it and shuffle again
        List<Word> choice_words = draw_words.subList(0, this.max_choices-1);
        choice_words.add(challenge_word);
        Collections.shuffle(choice_words);

        for (int i = 0; i < choice_words.size(); i++) {
            final Word word = choice_words.get(i);
            float available_width = (this.cardPane.getWidth()/choice_words.size());
            float wordX = i*(this.cardPane.getWidth()/choice_words.size());
            float wordY = this.cardPane.getHeight() - 150;
            Button wordText = new Button(wordX+(available_width/2), wordY, 200, 150, word.name, PhoeniciaContext.vboManager, new Button.OnClickListener() {
                @Override
                public void onClicked(Button button) {
                    if (word == challenge_word) {
                        pass(challenge_word);
                    } else {
                        fail(challenge_word);
                    }
                    next_round();
                }
            });
            this.registerTouchArea(wordText);
            this.touchAreas.add(wordText);
            this.cardPane.attachChild(wordText);
        }

        ITextureRegion sprite_region = this.phoeniciaGame.wordSprites.get(challenge_word);
        Sprite challenge_sprite = new Sprite(this.cardPane.getWidth()/2, this.cardPane.getHeight() - 300, sprite_region, PhoeniciaContext.vboManager);
        this.cardPane.attachChild(challenge_sprite);
    }

    /**
     * Capture scene touch events
     * @param pSceneTouchEvent
     * @return
     */
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        return true;
    }}
