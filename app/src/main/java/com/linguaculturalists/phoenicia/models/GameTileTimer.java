package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;

/**
 * Created by mhall on 8/1/16.
 */
public class GameTileTimer extends Builder {

    public ForeignKeyField<GameSession> game; /**< reference to the GameSession this item is a part of */
    public ForeignKeyField<GameTile> tile; /**< reference to the WordTile the builder is attached to */

    public static final QuerySet<GameTileTimer> objects(Context context) {
        return objects(context, GameTileTimer.class);
    }

    public GameTileTimer() {
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

    @Override
    protected void migrate(Context context) {
        return;
    }

}
