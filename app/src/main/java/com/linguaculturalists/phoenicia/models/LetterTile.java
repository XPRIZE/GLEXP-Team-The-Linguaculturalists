package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.PlacedBlockSprite;
import com.linguaculturalists.phoenicia.locale.Letter;
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
public class LetterTile extends Model implements BuildQueue.BuildStatusUpdateHandler, IOnAreaTouchListener, PlacedBlockSprite.OnClickListener {

    public static final int TYPE_LETTER = 0;
    public static final int TYPE_WORD = 1;

    public ForeignKeyField<GameSession> game;
    public ForeignKeyField<BuildQueue> builder;
    public IntegerField isoX;
    public IntegerField isoY;
    public CharField item_name;

    public PhoeniciaGame phoeniciaGame;
    public Letter letter;
    public PlacedBlockSprite sprite;

    private boolean isTouchDown = false;
    private LetterTileListener eventListener;
    private boolean isCompleted = false;

    public LetterTile() {
        super();
        this.game = new ForeignKeyField<GameSession>(GameSession.class);
        this.builder = new ForeignKeyField<BuildQueue>(BuildQueue.class);
        this.isoX = new IntegerField();
        this.isoY = new IntegerField();
        this.item_name = new CharField(32);
    }

    public LetterTile(PhoeniciaGame game, Letter letter) {
        this();
        this.phoeniciaGame = game;
        this.letter = letter;
        this.game.set(game.session);
        this.item_name.set(letter.name);
    }

    public static final QuerySet<LetterTile> objects(Context context) {
        return objects(context, LetterTile.class);
    }

    public PlacedBlockSprite getSprite() {

        return this.sprite;
    }

    public void setSprite(PlacedBlockSprite sprite) {
        this.sprite = sprite;
    }

    public BuildQueue getBuilder(Context context) {
        BuildQueue builder = this.builder.get(context);
        if (builder != null) {
            builder.setUpdateHandler(this);
            this.onProgressChanged(builder);
            phoeniciaGame.addBuilder(builder);
        }
        return builder;
    }

    public void setBuilder(BuildQueue builder) {
        builder.setUpdateHandler(this);
        this.builder.set(builder);
        this.onProgressChanged(builder);
    }

    public void onScheduled(BuildQueue buildItem) { Debug.d("PlacedBlock.onScheduled"); this.isCompleted = false; return; }
    public void onStarted(BuildQueue buildItem) { Debug.d("PlacedBlock.onStarted"); this.isCompleted = false; return; }
    public void onCompleted(BuildQueue buildItem) {
        Debug.d("PlacedBlock.onCompleted");
        this.isCompleted = true;
        if (this.eventListener != null) {
            this.eventListener.onLetterTileBuildCompleted(this);
        }
        return;
    }

    public void onProgressChanged(BuildQueue builtItem) {
        if (sprite != null) {
            sprite.setProgress(builtItem.progress.get(), letter.time);
        }
        if (builtItem.progress.get() >= letter.time) {
            builtItem.complete();
        }
        return;
    }


    public void reset(Context context) {
        if (this.sprite != null) {
            this.sprite.setProgress(0, letter.time);
        }
        BuildQueue builder = this.getBuilder(context);
        if (builder != null) {
            Debug.d("Resetting LetterTile builder");
            builder.progress.set(0);
            builder.start();
            builder.save(context);
            this.phoeniciaGame.addBuilder(builder);
        } else {
            Debug.e("Could not reset LetterTile builder, because it was missing");
        }
    }

    @Override
    protected void migrate(Context context) {
        Migrator<LetterTile> migrator = new Migrator<LetterTile>(LetterTile.class);

        migrator.addField("builder", new ForeignKeyField<BuildQueue>(BuildQueue.class));

        // roll out all migrations
        migrator.migrate(context);
        return;
    }

    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
        if (pSceneTouchEvent.isActionDown()) {
            this.isTouchDown = true;
        } else if (isTouchDown && pSceneTouchEvent.isActionUp()) {

            if (this.eventListener != null) {
                this.eventListener.onLetterTileClicked(this);
            }
            this.isTouchDown = false;
            return true;
        } else {
            this.isTouchDown = false;
        }
        return false;

    }

    public void onClick(PlacedBlockSprite buttonSprite, float v, float v2) {
        Debug.d("Clicked block: "+String.valueOf(this.letter.chars));
        BuildQueue builder = this.getBuilder(phoeniciaGame.activity.getApplicationContext());
        if (builder != null) {
            if (builder.status.get() == BuildQueue.COMPLETE) {
                Debug.d("Clicked block was completed");
                phoeniciaGame.playBlockSound(this.letter.phoneme);
                Inventory.getInstance().add(this.letter.name);
                this.reset(phoeniciaGame.activity.getApplicationContext());
            } else {
                Debug.d("Clicked block was NOT completed");
                phoeniciaGame.playBlockSound(this.letter.sound);
            }
        } else {
            Debug.e("Clicked block has no builder");
        }
    }

    public void setListener(final LetterTileListener listener) {
        this.eventListener = listener;
    }

    public interface LetterTileListener {
        public void onLetterTileClicked(final LetterTile letterTile);
        public void onLetterTileBuildCompleted(final LetterTile letterTile);
    }
}

