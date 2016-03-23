package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;

/**
 * Builder for creating new Letter items.
 */
public class LetterBuilder extends Builder {
    public ForeignKeyField<GameSession> game; /**< reference to the GameSession this item is a part of */
    public ForeignKeyField<LetterTile> tile; /**< reference to the LetterTile the builder is attached to */

    public static final QuerySet<LetterBuilder> objects(Context context) {
        return objects(context, LetterBuilder.class);
    }

    /**
     * Create empty LetterBuilder.
     */
    public LetterBuilder() {
        super();
        this.game = new ForeignKeyField<GameSession>(GameSession.class);
        this.tile = new ForeignKeyField<LetterTile>(LetterTile.class);
        this.item_name = new CharField(32);
        this.time = new IntegerField();
        this.progress = new IntegerField();
        this.status = new IntegerField();
        this.progress.set(0);
        this.status.set(NONE);

        this.setUpdateHandler(new AbstractBuildStatusUpdateHandler() { });
    }

    /**
     * Create a new LetterBuilder for the specified GameSession and LetterTile
     * @param session GameSession the builder is a part of
     * @param tile LetterTile the builder is building for
     * @param item_name InventoryItem for what the builder is creating
     * @param time time (in seconds) the build will take to finish
     */
    public LetterBuilder(GameSession session, LetterTile tile, String item_name, int time) {
        this(session, tile, item_name, time, null);
    }

    /**
     * Create a new LetterBuilder for the specified GameSession and LetterTile
     * @param session GameSession the builder is a part of
     * @param tile LetterTile the builder is building for
     * @param item_name InventoryItem for what the builder is creating
     * @param time time (in seconds) the build will take to finish
     * @param updateHandler callback to notify when the build status changes
     */
    public LetterBuilder(GameSession session, LetterTile tile, String item_name, int time, BuildStatusUpdateHandler updateHandler) {
        this();
        this.game.set(session);
        this.tile.set(tile);
        this.item_name.set(item_name);
        this.time.set(time);

        if (updateHandler != null) {
            this.updateHandler = updateHandler;
        } else {
            this.updateHandler = new AbstractBuildStatusUpdateHandler() { };
        }
    }

    @Override
    protected void migrate(Context context) {
        return;
    }
}
