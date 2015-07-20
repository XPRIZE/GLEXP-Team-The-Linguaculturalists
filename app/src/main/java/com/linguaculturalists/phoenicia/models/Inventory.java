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
public class Inventory extends Model {

    public ForeignKeyField<GameSession> game;
    public CharField item_name;
    public IntegerField quantity;

    public static final QuerySet<Inventory> objects(Context context) {
        return objects(context, Inventory.class);
    }

    public Inventory() {
        super();
        this.game = new ForeignKeyField<>(GameSession.class);
        this.item_name = new CharField(32);
        this.quantity = new IntegerField();
    }
}
