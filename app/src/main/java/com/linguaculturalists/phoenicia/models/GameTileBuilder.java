package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;

/**
 * Builder for creating new Word items.
 */
public class GameTileBuilder extends Builder {

    public ForeignKeyField<GameSession> game; /**< reference to the GameSession this item is a part of */
    public ForeignKeyField<GameTile> tile; /**< reference to the WordTile the builder is attached to */

    public static final QuerySet<GameTileBuilder> objects(Context context) {
        return objects(context, GameTileBuilder.class);
    }

    /**
     * Create empty WordBuilder.
     */
    public GameTileBuilder() {
        super();
        this.game = new ForeignKeyField<GameSession>(GameSession.class);
        this.tile = new ForeignKeyField<GameTile>(GameTile.class);
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
     * @param tile GameTile the builder is building for
     * @param item_name InventoryItem for what the builder is creating
     * @param time time (in seconds) the build will take to finish
     */
    public GameTileBuilder(GameSession session, GameTile tile, String item_name, int time) {
        this(session, tile, item_name, time, null);
    }

    /**
     * Create a new WordBuilder for the specified GameSession and WordTile
     * @param session GameSession the builder is a part of
     * @param tile GameTile the builder is building for
     * @param item_name InventoryItem for what the builder is creating
     * @param time time (in seconds) the build will take to finish
     * @param updateHandler callback to notify when the build status changes
     */
    public GameTileBuilder(GameSession session, GameTile tile, String item_name, int time, BuildStatusUpdateHandler updateHandler) {
        this();
        this.game.set(session);
        this.tile.set(tile);
        this.item_name.set(item_name);
        this.time.set(time);

        if (updateHandler != null) {
            this.addUpdateHandler(updateHandler);
        }
    }

    @Override
    protected void migrate(Context context) {
        return;
    }

}
