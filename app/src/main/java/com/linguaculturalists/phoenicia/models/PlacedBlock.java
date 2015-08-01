package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.Locale;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;
/**
 * Created by mhall on 6/19/15.
 */
public class PlacedBlock extends Model {

    public ForeignKeyField<GameSession> game;
    public IntegerField isoX;
    public IntegerField isoY;
    public IntegerField sprite_tile;
    public CharField item_name;
    public IntegerField progress;

    public PlacedBlock() {
        super();
        game = new ForeignKeyField<>(GameSession.class);
        isoX = new IntegerField();
        isoY = new IntegerField();
        sprite_tile = new IntegerField();
        item_name = new CharField(32);
        progress = new IntegerField();
    }

    public static final QuerySet<PlacedBlock> objects(Context context) {
        return objects(context, PlacedBlock.class);
    }

    public Letter getLetter(Locale locale) {
        return locale.letter_map.get(this.item_name.get());
    }
}
