package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;

/**
 * Created by mhall on 7/17/15.
 */
public class WordBuilder extends Builder {

    public ForeignKeyField<GameSession> game;
    public ForeignKeyField<WordTile> tile;

    public static final QuerySet<WordBuilder> objects(Context context) {
        return objects(context, WordBuilder.class);
    }

    public WordBuilder() {
        super();
        this.game = new ForeignKeyField<>(GameSession.class);
        this.tile = new ForeignKeyField<>(WordTile.class);
        this.item_name = new CharField(32);
        this.time = new IntegerField();
        this.progress = new IntegerField();
        this.status = new IntegerField();
        this.progress.set(0);
        this.status.set(NONE);

        this.setUpdateHandler(new AbstractBuildStatusUpdateHandler() { });
    }

    public WordBuilder(GameSession session, WordTile tile, String item_name, int time) {
        this(session, tile, item_name, time, null);
    }
    public WordBuilder(GameSession session, WordTile tile, String item_name, int time, BuildStatusUpdateHandler updateHandler) {
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
