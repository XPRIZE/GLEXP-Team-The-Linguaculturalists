package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Word;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;

import org.andengine.util.debug.Debug;

/**
 * Builder for creating new Word items.
 */
public class WorkshopBuilder extends Builder {

    public ForeignKeyField<GameSession> game; /**< reference to the GameSession this item is a part of */
    public ForeignKeyField<DefaultTile> tile; /**< reference to the WordTile the builder is attached to */

    public static final QuerySet<WorkshopBuilder> objects(Context context) {
        return objects(context, WorkshopBuilder.class);
    }

    /**
     * Create empty WordBuilder.
     */
    public WorkshopBuilder() {
        super();
        this.game = new ForeignKeyField<GameSession>(GameSession.class);
        this.tile = new ForeignKeyField<DefaultTile>(DefaultTile.class);
        this.item_name = new CharField(32);
        this.time = new IntegerField();
        this.progress = new IntegerField();
        this.status = new IntegerField();
        this.progress.set(0);
        this.status.set(NONE);

    }

    /**
     * Create a new WordBuilder for the specified GameSession and WordTile
     * @param session GameSession the builder is a part of
     * @param tile WordTile the builder is building for
     */
    public WorkshopBuilder(GameSession session, DefaultTile tile) {
        this();
        this.game.set(session);
        this.tile.set(tile);
    }

    /**
     * Create a new WordBuilder for the specified GameSession and WordTile
     * @param session GameSession the builder is a part of
     * @param tile WordTile the builder is building for
     * @param item_name InventoryItem for what the builder is creating
     * @param time time (in seconds) the build will take to finish
     */
    public WorkshopBuilder(GameSession session, DefaultTile tile, String item_name, int time) {
        this(session, tile, item_name, time, null);
    }

    /**
     * Create a new WordBuilder for the specified GameSession and WordTile
     * @param session GameSession the builder is a part of
     * @param tile WordTile the builder is building for
     * @param item_name InventoryItem for what the builder is creating
     * @param time time (in seconds) the build will take to finish
     * @param updateHandler callback to notify when the build status changes
     */
    public WorkshopBuilder(GameSession session, DefaultTile tile, String item_name, int time, BuildStatusUpdateHandler updateHandler) {
        this();
        this.game.set(session);
        this.tile.set(tile);
        this.item_name.set(item_name);
        this.time.set(time);

        if (updateHandler != null) {
            this.addUpdateHandler(updateHandler);
        }
    }

    public void setUpdateHandler(final PhoeniciaGame phoeniciaGame, final DefaultTile workshopTile, final Word word) {
        this.addUpdateHandler(new Builder.BuildStatusUpdateHandler() {
            @Override
            public void onCompleted(Builder buildItem) {
                Debug.d("WordBuilder for " + buildItem.item_name.get() + " has completed");
                phoeniciaGame.playBlockSound(word.sound);
                buildItem.removeUpdateHandler(this);
                workshopTile.setAttention(true);
            }

            @Override
            public void onProgressChanged(Builder buildItem) {

            }

            @Override
            public void onScheduled(Builder buildItem) {

            }

            @Override
            public void onStarted(Builder buildItem) {

            }
        });
    }

    @Override
    protected void migrate(Context context) {
        return;
    }

}
