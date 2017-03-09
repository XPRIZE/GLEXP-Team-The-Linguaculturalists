package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Dialog;
import com.linguaculturalists.phoenicia.components.SpriteGroup;
import com.linguaculturalists.phoenicia.locale.*;
import com.linguaculturalists.phoenicia.locale.Number;
import com.linguaculturalists.phoenicia.models.Bank;
import com.linguaculturalists.phoenicia.models.GameTile;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.GameSounds;
import com.linguaculturalists.phoenicia.util.GameUI;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.Entity;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
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
 * Created by mhall on 2/26/17.
 */
public class NumberMatchGameHUD extends MiniGameHUD {
    private Level level;
    private int current_round;
    private GameTile tile;
    private List<Word> random_word_list;
    private List<Number> random_number_list;
    private int max_rounds = 10;
    private int max_choices = 3;
    private int max_number = 9;

    private Entity cardPane;
    private Entity resultsPane;
    private int result_number;
    private List<Word> winnings;
    private List<Entity> touchAreas;

    public NumberMatchGameHUD(final PhoeniciaGame phoeniciaGame, final Level level, final GameTile tile) {
        super(phoeniciaGame, level, tile);
        this.setBackgroundEnabled(false);
        this.level = level;
        this.tile = tile;
        this.current_round = 0;
        this.max_number = phoeniciaGame.locale.levels.indexOf(level)/2;
        if (this.max_number > this.game.locale.numbers.size()) {
            this.max_number = this.game.locale.numbers.size(); // Maximum we have number images for
        } else if (this.max_number < 3) {
            this.max_number = 3; // Minimum needed to display 3 choices
        }
        this.random_number_list = this.game.locale.numbers.subList(0, this.max_number);
        Collections.shuffle(this.random_number_list);

        this.random_word_list = new ArrayList<Word>(level.words);
        Collections.shuffle(this.random_word_list);

        Debug.d("NumberMatchGame level: " + level.name);
        Debug.d("NumberMatchGame words: " + level.words.size());

        if (this.max_number < this.max_rounds) {
            this.max_rounds = this.max_number;
        }
        Debug.d("NumberMatchGame  rounds: "+this.max_rounds);


        this.touchAreas = new ArrayList<Entity>();

        this.cardPane = new Entity(WINDOW_WIDTH/2, 400, WINDOW_WIDTH, 400);
        this.content.attachChild(cardPane);

        this.resultsPane = new Entity(WINDOW_WIDTH/2, 100, WINDOW_WIDTH, 200);
        this.content.attachChild(this.resultsPane);
        this.winnings = new ArrayList<Word>();
        this.result_number = 0;
    }

    @Override
    public void show() {
        this.show_round(this.current_round);
    }

    private void pass(Word word, final float wordX) {
        Debug.d("NumberMatchGame : pass!");
        GameSounds.play(GameSounds.COMPLETE);
        this.winnings.add(word);
        float available_width = (this.cardPane.getWidth()/this.max_choices);
        float wordY = this.resultsPane.getHeight() + this.cardPane.getHeight() - 150;

        ITiledTextureRegion sprite_region = this.game.wordSprites.get(word);
        Sprite winning_sprite = new Sprite(wordX+(available_width/2), wordY, sprite_region.getTextureRegion(1), PhoeniciaContext.vboManager);
        this.resultsPane.attachChild(winning_sprite);
        winning_sprite.registerEntityModifier(new ParallelEntityModifier(
                new ScaleModifier(0.5f, 2.0f, 1.0f),
                new MoveYModifier(0.5f, wordY, 80),
                new MoveXModifier(0.5f, wordX+(available_width/2), 40+(this.result_number*80))
        ));
        this.result_number++;
    }

    private void fail(Word word, final float wordX) {
        Debug.d("NumberMatchGame : fail!");
        GameSounds.play(GameSounds.FAILED);
        float available_width = (this.cardPane.getWidth()/this.max_choices);
        float wordY = this.resultsPane.getHeight() + this.cardPane.getHeight() - 150;

        ITiledTextureRegion sprite_region = this.game.wordSprites.get(word);
        Sprite missed_sprite = new Sprite(wordX+(available_width/2), wordY, sprite_region.getTextureRegion(2), PhoeniciaContext.vboManager);
        this.resultsPane.attachChild(missed_sprite);
        missed_sprite.registerEntityModifier(new ParallelEntityModifier(
                new ScaleModifier(0.5f, 2.0f, 1.0f),
                new MoveYModifier(0.5f, wordY, 80),
                new MoveXModifier(0.5f, wordX + (available_width / 2), 48 + (this.result_number * 80))
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
        String counts = String.format("%1$d/%2$d", this.winnings.size(), this.max_rounds);
        Text sorry_text = new Text(sorry_dialog.getWidth()/2, sorry_dialog.getHeight()-48, GameFonts.dialogText(), counts, counts.length(),  new TextOptions(AutoWrap.WORDS, sorry_dialog.getWidth()*0.8f, HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        sorry_text.setColor(Color.RED);
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

        final Dialog reward_dialog = new Dialog(400, 300, Dialog.Buttons.OK, PhoeniciaContext.vboManager, new Dialog.DialogListener() {
            @Override
            public void onDialogButtonClicked(Dialog dialog, Dialog.DialogButton dialogButton) {
                finish();
                Inventory.getInstance().add(reward_word.name, 1, false);
                Bank.getInstance().credit(reward_coins);
                game.session.addExperience(reward_points);
                GameSounds.play(GameSounds.COLLECT);
                dialog.close();
                unregisterTouchArea(dialog);
            }
        });

        String counts = String.format("%1$d/%2$d", this.winnings.size(), this.max_rounds);
        Text reward_text = new Text(reward_dialog.getWidth()/2, reward_dialog.getHeight()-24, GameFonts.dialogText(), counts, counts.length(),  new TextOptions(AutoWrap.WORDS, reward_dialog.getWidth()*0.8f, HorizontalAlign.CENTER), PhoeniciaContext.vboManager);
        reward_text.setColor(Color.GREEN);
        reward_dialog.attachChild(reward_text);

        ITiledTextureRegion sprite_region = this.game.wordSprites.get(reward_word);
        Sprite reward_sprite = new Sprite(reward_dialog.getWidth()/2, reward_dialog.getHeight() - 100, sprite_region.getTextureRegion(1), PhoeniciaContext.vboManager);
        reward_dialog.attachChild(reward_sprite);

        ITextureRegion coinRegion = GameUI.getInstance().getCoinsIcon();
        Sprite coinIcon = new Sprite(reward_dialog.getWidth()/2 - 32, 112, coinRegion, PhoeniciaContext.vboManager);
        coinIcon.setScale(0.5f);
        reward_dialog.attachChild(coinIcon);

        Text iconDisplay = new Text(reward_dialog.getWidth()/2 + 32, 112, GameFonts.dialogText(), String.valueOf(reward_coins), 10, new TextOptions(HorizontalAlign.LEFT), PhoeniciaContext.vboManager);
        reward_dialog.attachChild(iconDisplay);

        this.registerTouchArea(reward_dialog);
        reward_dialog.open(this);
        GameSounds.play(GameSounds.COMPLETE);
    }

    private void show_round(int round) {
        int word_index = round % this.max_rounds;
        final Word challenge_word = this.random_word_list.get(word_index);

        final int number_int = (int)Math.round(Math.random() * this.max_number)+1;
        final Number challenge_number = this.game.locale.number_map.get(number_int);

        Debug.d("NumberMatchGame challenge number: "+number_int);

        // Take the challenge number out of the list and shuffle it
        List<Number> draw_numbers = new ArrayList<Number>(this.random_number_list);
        draw_numbers.remove(challenge_number);
        Collections.shuffle(draw_numbers);

        // Draw max_choices-1 from the list, add the challenge word to it and shuffle again
        List<Number> choice_numbers = draw_numbers.subList(0, this.max_choices-1);
        choice_numbers.add(challenge_number);
        Collections.shuffle(choice_numbers);

        ITextureRegion number_region = this.game.numberSprites.get(challenge_number);
        ButtonSprite numberSprite = new ButtonSprite(this.cardPane.getWidth()/2 - (number_region.getWidth()/2), this.cardPane.getHeight() - 300, number_region, PhoeniciaContext.vboManager, new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                game.playBlockSound(challenge_number.sound);
            }
        });
        this.cardPane.attachChild(numberSprite);
        this.touchAreas.add(numberSprite);
        this.registerTouchArea(numberSprite);

        ITextureRegion sprite_region = this.game.wordSprites.get(challenge_word);
        final ButtonSprite wordSprite = new ButtonSprite(this.cardPane.getWidth()/2 + (sprite_region.getWidth()/2), this.cardPane.getHeight() - 300, sprite_region, PhoeniciaContext.vboManager, new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                game.playBlockSound(challenge_word.sound);
            }
        });
        this.cardPane.attachChild(wordSprite);
        this.touchAreas.add(wordSprite);
        this.registerTouchArea(wordSprite);

        for (int i = 0; i < this.max_choices; i++) {
            final Number number = choice_numbers.get(i);
            float available_width = (this.cardPane.getWidth()/this.max_choices);
            final float wordX = i*(this.cardPane.getWidth()/this.max_choices);
            final float wordY = this.cardPane.getHeight() - 150;
            SpriteGroup wordsSprite = new SpriteGroup(wordX + (available_width / 2), wordY, sprite_region.getWidth(), sprite_region.getHeight(), wordSprite, number.intval, PhoeniciaContext.vboManager, new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                    if (number == challenge_number) {
                        pass(challenge_word, wordSprite.getX());
                    } else {
                        fail(challenge_word, wordSprite.getX());
                    }
                    next_round();
                }
            });
            wordsSprite.setScale(2.0f);

            this.registerTouchArea(wordsSprite);
            this.touchAreas.add(wordsSprite);
            this.cardPane.attachChild(wordsSprite);
        }

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
}
