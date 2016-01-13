package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.PlacedBlockSprite;
import com.linguaculturalists.phoenicia.locale.Word;
import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;
import com.orm.androrm.migration.Migrator;

import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 12/23/15.
 */
public class WordTile extends Model implements Builder.BuildStatusUpdateHandler, IOnAreaTouchListener, PlacedBlockSprite.OnClickListener {

    public ForeignKeyField<GameSession> game;
    public ForeignKeyField<WordBuilder> builder;
    public IntegerField isoX;
    public IntegerField isoY;
    public CharField item_name;

    public PhoeniciaGame phoeniciaGame;
    public Word word;
    public PlacedBlockSprite sprite;

    private boolean isTouchDown = false;
    private WordTileListener eventListener;
    private boolean isCompleted = false;

    public WordTile() {
        super();
        this.game = new ForeignKeyField<GameSession>(GameSession.class);
        this.builder = new ForeignKeyField<WordBuilder>(WordBuilder.class);
        this.isoX = new IntegerField();
        this.isoY = new IntegerField();
        this.item_name = new CharField(32);
    }

    public WordTile(PhoeniciaGame game, Word word) {
        this();
        this.phoeniciaGame = game;
        this.word = word;
        this.game.set(game.session);
        this.item_name.set(word.name);
    }

    public static final QuerySet<WordTile> objects(Context context) {
        return objects(context, WordTile.class);
    }

    public PlacedBlockSprite getSprite() {

        return this.sprite;
    }

    public void setSprite(PlacedBlockSprite sprite) {
        this.sprite = sprite;
    }

    public WordBuilder getBuilder(Context context) {
        WordBuilder builder = this.builder.get(context);
        if (builder != null) {
            builder.setUpdateHandler(this);
            this.onProgressChanged(builder);
            phoeniciaGame.addBuilder(builder);
        }
        return builder;
    }

    public void setBuilder(WordBuilder builder) {
        builder.setUpdateHandler(this);
        this.builder.set(builder);
        this.onProgressChanged(builder);
    }

    public void onScheduled(Builder buildItem) { Debug.d("WordTile.onScheduled"); this.isCompleted = false; return; }
    public void onStarted(Builder buildItem) { Debug.d("WordTile.onStarted"); this.isCompleted = false; return; }
    public void onCompleted(Builder buildItem) {
        Debug.d("WordTile.onCompleted");
        this.isCompleted = true;
        if (this.eventListener != null) {
            this.eventListener.onWordTileBuildCompleted(this);
        }
        return;
    }

    public void onProgressChanged(Builder builtItem) {
        if (sprite != null) {
            sprite.setProgress(builtItem.progress.get(), word.time);
        }
        if (builtItem.progress.get() >= word.time) {
            builtItem.complete();
        }
        return;
    }


    public void reset(Context context) {
        if (this.sprite != null) {
            this.sprite.setProgress(0, word.time);
        }
        WordBuilder builder = this.getBuilder(context);
        if (builder != null) {
            Debug.d("Resetting WordTile builder");
            builder.progress.set(0);
            builder.start();
            builder.save(context);
            this.phoeniciaGame.addBuilder(builder);
        } else {
            Debug.e("Could not reset WordTile builder, because it was missing");
        }
    }

    @Override
    protected void migrate(Context context) {
        Migrator<WordTile> migrator = new Migrator<WordTile>(WordTile.class);

        // roll out all migrations
        migrator.migrate(context);
        return;
    }

    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
        if (pSceneTouchEvent.isActionDown()) {
            this.isTouchDown = true;
        } else if (isTouchDown && pSceneTouchEvent.isActionUp()) {

            if (this.eventListener != null) {
                this.eventListener.onWordTileClicked(this);
            }
            this.isTouchDown = false;
            return true;
        } else {
            this.isTouchDown = false;
        }
        return false;

    }

    public void onClick(PlacedBlockSprite buttonSprite, float v, float v2) {
        Debug.d("Clicked block: "+String.valueOf(this.word.chars));
        WordBuilder builder = this.getBuilder(phoeniciaGame.activity.getApplicationContext());
        if (builder != null) {
            if (builder.status.get() == LetterBuilder.COMPLETE) {
                Debug.d("Clicked block was completed");
                phoeniciaGame.hudManager.showWordBuilder(phoeniciaGame.locale.level_map.get(phoeniciaGame.current_level), this.word);
            } else {
                Debug.d("Clicked block was NOT completed");
                phoeniciaGame.playBlockSound(this.word.sound);
            }
        } else {
            Debug.e("Clicked block has no builder");
        }
    }

    public void setListener(final WordTileListener listener) {
        this.eventListener = listener;
    }

    public interface WordTileListener {
        public void onWordTileClicked(final WordTile wordTile);
        public void onWordTileBuildCompleted(final WordTile wordTile);
    }
}

