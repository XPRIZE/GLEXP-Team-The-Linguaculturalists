package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.BorderRectangle;
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

import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
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
public class WordMatchGameHUD extends MiniGameHUD {
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
        super(phoeniciaGame, level, tile);

        this.level = level;
        this.tile = tile;
        this.current_round = 0;
        this.random_word_list = new ArrayList<Word>(level.words);
        Collections.shuffle(this.random_word_list);

        if (this.random_word_list.size() < this.max_rounds) {
            this.max_rounds = this.random_word_list.size();
        }
        Debug.d("WordMatchGame rounds: "+this.max_rounds);

        if (this.random_word_list.size() < this.max_choices) {
            this.max_choices = this.random_word_list.size();
        }

        this.touchAreas = new ArrayList<Entity>();
        choiceWordFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 36, Color.BLUE_ARGB_PACKED_INT);
        choiceWordFont.load();

//        final float dialogWidth = 800;
//        final float dialogHeight = 600;
//        Rectangle whiteRect = new BorderRectangle(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, dialogWidth, dialogHeight, PhoeniciaContext.vboManager);
//        whiteRect.setColor(Color.WHITE);
//        this.attachChild(whiteRect);

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

    private void pass(Word word) {
        //TODO: count success
        Debug.d("wordmatch: pass!");
        GameSounds.play(GameSounds.COMPLETE);
        this.winnings.add(word);
        ITiledTextureRegion sprite_region = this.game.wordSprites.get(word);
        Sprite winning_sprite = new Sprite(this.cardPane.getWidth()/2, this.cardPane.getHeight() - 300, sprite_region.getTextureRegion(1), PhoeniciaContext.vboManager);
        winning_sprite.setZIndex(foreground_sprite.getZIndex()+1);
        this.resultsPane.attachChild(winning_sprite);
        winning_sprite.registerEntityModifier(new ParallelEntityModifier(
                new ScaleModifier(0.5f, 2.0f, 1.0f),
                new MoveYModifier(0.5f, 300, 80),
                new MoveXModifier(0.5f, this.resultsPane.getWidth()/2, 40+(this.result_number*80))
                ));
        this.result_number++;
    }

    private void fail(Word word) {
        //TODO: count failure
        Debug.d("wordmatch: fail!");
        GameSounds.play(GameSounds.FAILED);
        ITiledTextureRegion sprite_region = this.game.wordSprites.get(word);
        Sprite missed_sprite = new Sprite(this.cardPane.getWidth()/2, this.cardPane.getHeight() - 300, sprite_region.getTextureRegion(2), PhoeniciaContext.vboManager);
        this.resultsPane.attachChild(missed_sprite);
        missed_sprite.registerEntityModifier(new ParallelEntityModifier(
                new ScaleModifier(0.5f, 2.0f, 1.0f),
                new MoveYModifier(0.5f, 300, 80),
                new MoveXModifier(0.5f, this.resultsPane.getWidth()/2, 48+(this.result_number*80))
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
        if (this.winnings.size() > 0 && (this.max_rounds - this.winnings.size()) < 3) {
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
            Button wordText = new Button(wordX+(available_width/2), wordY, 200, 100, String.valueOf(word.chars), PhoeniciaContext.vboManager, new Button.OnClickListener() {
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

        ITiledTextureRegion sprite_region = this.game.wordSprites.get(challenge_word);
        ButtonSprite challenge_sprite = new ButtonSprite(this.cardPane.getWidth()/2, this.cardPane.getHeight() - 300, sprite_region, PhoeniciaContext.vboManager);
        challenge_sprite.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v1) {
                game.playBlockSound(challenge_word.sound);
            }
        });
        challenge_sprite.setScale(2.0f);
        this.cardPane.attachChild(challenge_sprite);
        this.registerTouchArea(challenge_sprite);
        this.touchAreas.add(challenge_sprite);
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
        game.hudManager.pop();
    }


}
