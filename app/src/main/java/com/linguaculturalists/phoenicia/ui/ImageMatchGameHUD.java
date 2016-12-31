package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Button;
import com.linguaculturalists.phoenicia.components.Dialog;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.GameTile;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameSounds;
import com.linguaculturalists.phoenicia.util.GameTextures;
import com.linguaculturalists.phoenicia.util.GameUI;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.Entity;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.ButtonSprite;
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
public class ImageMatchGameHUD extends PhoeniciaHUD {
    private Level level;
    private int current_round;
    private GameTile tile;
    private List<Word> random_word_list;
    private int max_rounds = 10;
    private int max_choices = 3;

    private Font choiceWordFont;
    private Entity cardPane;
    private Entity resultsPane;
    private int result_number;
    private List<Word> winnings;
    private List<Entity> touchAreas;

    public ImageMatchGameHUD(final PhoeniciaGame phoeniciaGame, final Level level, final GameTile tile) {
        super(phoeniciaGame);
        this.setBackgroundEnabled(false);
        this.level = level;
        this.tile = tile;
        this.current_round = 0;
        this.random_word_list = new ArrayList<Word>(level.words);
        Collections.shuffle(this.random_word_list);

        Debug.d("ImageMatchGame level: " + level.name);
        Debug.d("ImageMatchGame words: " + level.words.size());

        if (this.random_word_list.size() < this.max_rounds) {
            this.max_rounds = this.random_word_list.size();
        }
        Debug.d("ImageMatchGame  rounds: "+this.max_rounds);

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

    private void pass(Word word, final float wordX) {
        //TODO: count success
        Debug.d("ImageMatchGame : pass!");
        GameSounds.play(GameSounds.COMPLETE);
        this.winnings.add(word);
        float wordY = this.cardPane.getHeight() - 150;

        ITiledTextureRegion sprite_region = this.game.wordSprites.get(word);
        Sprite winning_sprite = new Sprite(wordX, wordY, sprite_region.getTextureRegion(1), PhoeniciaContext.vboManager);
        this.resultsPane.attachChild(winning_sprite);
        winning_sprite.registerEntityModifier(new ParallelEntityModifier(
                new MoveYModifier(0.5f, wordY, 80),
                new MoveXModifier(0.5f, wordX, 40+(this.result_number*80))
                ));
        this.result_number++;
    }

    private void fail(Word word, final float wordX) {
        //TODO: count failure
        Debug.d("ImageMatchGame : fail!");
        GameSounds.play(GameSounds.FAILED);
        float wordY = this.cardPane.getHeight() - 150;
        ITiledTextureRegion sprite_region = this.game.wordSprites.get(word);
        Sprite missed_sprite = new Sprite(wordX, wordY, sprite_region.getTextureRegion(2), PhoeniciaContext.vboManager);
        this.resultsPane.attachChild(missed_sprite);
        missed_sprite.registerEntityModifier(new ParallelEntityModifier(
                new MoveYModifier(0.5f, wordY, 80),
                new MoveXModifier(0.5f, wordX, 48+(this.result_number*80))
                ));
        this.result_number++;
    }

    private void next_round() {
        this.current_round++;
        this.cardPane.detachChildren();
        for (Entity toucharea : this.touchAreas) {
            this.unregisterTouchArea(toucharea);
        }
        this.touchAreas.clear();

        if (this.current_round < this.max_rounds) {
            this.show_round(this.current_round);
        } else {
            // TODO: Show winnings
            this.end_game();
        }
    }

    private void end_game() {
        if ((this.max_rounds - this.winnings.size()) < 3) {
            this.show_reward();
        } else {
            this.show_sorry();
        }
        this.tile.reset(PhoeniciaContext.context);
    }
    private void show_sorry() {
        Dialog sorry_dialog = new Dialog(400, 150, Dialog.Buttons.OK, PhoeniciaContext.vboManager, new Dialog.DialogListener() {
            @Override
            public void onDialogButtonClicked(Dialog dialog, Dialog.DialogButton dialogButton) {
                dialog.close();
                unregisterTouchArea(dialog);
                finish();
            }
        });
        Text sorry_text = new Text(sorry_dialog.getWidth()/2, sorry_dialog.getHeight()-48, GameFonts.dialogText(), "Sorry, no winnings", 18,  new TextOptions(AutoWrap.WORDS, sorry_dialog.getWidth()*0.8f, HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        sorry_dialog.attachChild(sorry_text);

        this.registerTouchArea(sorry_dialog);

        sorry_dialog.open(this);
        GameSounds.play(GameSounds.FAILED);
    }

    private void show_reward() {
        Collections.shuffle(this.winnings);
        final Word reward_word = this.winnings.get(0);
        final int reward_coins = Math.round(reward_word.sell * this.tile.game.reward);
        final int reward_points = Math.round(reward_word.points * this.tile.game.reward);

        Dialog reward_dialog = new Dialog(400, 300, Dialog.Buttons.OK, PhoeniciaContext.vboManager, new Dialog.DialogListener() {
            @Override
            public void onDialogButtonClicked(Dialog dialog, Dialog.DialogButton dialogButton) {
                Inventory.getInstance().add(reward_word.name, 1);
                Bank.getInstance().credit(reward_coins);
                game.session.addExperience(reward_points);
                GameSounds.play(GameSounds.COLLECT);
                dialog.close();
                unregisterTouchArea(dialog);
                finish();
            }
        });

        ITiledTextureRegion sprite_region = this.game.wordSprites.get(reward_word);
        Sprite reward_sprite = new Sprite(reward_dialog.getWidth()/2, reward_dialog.getHeight() - 100, sprite_region.getTextureRegion(1), PhoeniciaContext.vboManager);
        reward_dialog.attachChild(reward_sprite);

        ITextureRegion coinRegion = GameUI.getInstance().getCoinsButton();
        Sprite coinIcon = new Sprite(reward_dialog.getWidth()/2, 112, coinRegion, PhoeniciaContext.vboManager);
        Text iconDisplay = new Text(100, coinIcon.getHeight()/2, GameFonts.defaultHUDDisplay(), String.valueOf(reward_coins), 10, new TextOptions(HorizontalAlign.LEFT), PhoeniciaContext.vboManager);
        coinIcon.attachChild(iconDisplay);
        reward_dialog.attachChild(coinIcon);

        this.registerTouchArea(reward_dialog);
        reward_dialog.open(this);
        GameSounds.play(GameSounds.COMPLETE);
    }

    private void show_round(int round) {
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
            final float wordX = i*(this.cardPane.getWidth()/choice_words.size());
            final float wordY = this.cardPane.getHeight() - 150;
            ITextureRegion sprite_region = this.game.wordSprites.get(word);
            ButtonSprite wordSprite = new ButtonSprite(wordX+(available_width/2), wordY, sprite_region, PhoeniciaContext.vboManager, new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                    if (word == challenge_word) {
                        pass(word, wordX);
                    } else {
                        fail(word, wordX);
                    }
                    next_round();
                }
            });

            this.registerTouchArea(wordSprite);
            this.touchAreas.add(wordSprite);
            this.cardPane.attachChild(wordSprite);
        }

        Font wordFont = GameFonts.introText();
        Text challenge_text = new Text(this.cardPane.getWidth()/2, this.cardPane.getHeight() - 300, wordFont, String.valueOf(challenge_word.chars), challenge_word.chars.length, new TextOptions(HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        challenge_text.setX(this.cardPane.getWidth() / 2 - challenge_text.getWidth() / 2);
        this.cardPane.attachChild(challenge_text);
    }

    /**
     * Capture scene touch events
     * @param pSceneTouchEvent
     * @return
     */
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        return true;
    }

    @Override
    public void finish() {
        game.hudManager.clear();
    }

}
