package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.Locale;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Word;
import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;
import com.orm.androrm.migration.Migrator;

import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 6/19/15.
 */
public class PlacedBlock extends Model implements BuildQueue.BuildStatusUpdateHandler {

    public static final int TYPE_LETTER = 0;
    public static final int TYPE_WORD = 1;

    public ForeignKeyField<GameSession> game;
    public IntegerField isoX;
    public IntegerField isoY;
    public IntegerField sprite_type;
    public CharField item_name;
    public IntegerField progress;

    public PlacedBlock() {
        super();
        game = new ForeignKeyField<>(GameSession.class);
        isoX = new IntegerField();
        isoY = new IntegerField();
        sprite_type = new IntegerField();
        item_name = new CharField(32);
        progress = new IntegerField();
    }

    public static final QuerySet<PlacedBlock> objects(Context context) {
        return objects(context, PlacedBlock.class);
    }

    public Letter getLetter(Locale locale) {
        return locale.letter_map.get(this.item_name.get());
    }

    public Word getWord(Locale locale) {
        return locale.word_map.get(this.item_name.get());
    }

    public void onScheduled(BuildQueue buildItem) { Debug.d("PlacedBlock.onScheduled"); return; }
    public void onStarted(BuildQueue buildItem) { Debug.d("PlacedBlock.onStarted"); return; }
    public void onCompleted(BuildQueue buildItem) { Debug.d("PlacedBlock.onCompleted"); return; }
    public void onProgressChanged(BuildQueue builtItem) { return; }

    @Override
    protected void migrate(Context context) {
        Migrator<PlacedBlock> migrator = new Migrator<PlacedBlock>(PlacedBlock.class);

        // Add sprite type
        migrator.addField("sprite_type", new IntegerField());

        // roll out all migrations
        migrator.migrate(context);
        return;
    }
}
