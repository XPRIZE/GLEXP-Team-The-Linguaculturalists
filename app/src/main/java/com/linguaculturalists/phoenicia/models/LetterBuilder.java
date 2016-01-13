package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;

/**
 * Created by mhall on 7/17/15.
 */
public class LetterBuilder extends Builder {
    public ForeignKeyField<GameSession> game;
    public ForeignKeyField<LetterTile> tile;

    public static final QuerySet<LetterBuilder> objects(Context context) {
        return objects(context, LetterBuilder.class);
    }

    public LetterBuilder() {
        super();
        this.game = new ForeignKeyField<>(GameSession.class);
        this.tile = new ForeignKeyField<>(LetterTile.class);
        this.item_name = new CharField(32);
        this.time = new IntegerField();
        this.progress = new IntegerField();
        this.status = new IntegerField();
        this.progress.set(0);
        this.status.set(NONE);

        this.setUpdateHandler(new AbstractBuildStatusUpdateHandler() { });
    }

    public LetterBuilder(GameSession session, LetterTile tile, String item_name, int time) {
        this(session, tile, item_name, time, null);
    }
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
