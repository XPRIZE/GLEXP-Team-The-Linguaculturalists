package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;
import com.orm.androrm.migration.Migrator;

/**
 * Database model representing a type of item in a player's Inventory.
 */
public class InventoryItem extends Model {

    public ForeignKeyField<GameSession> game; /**< reference to the GameSession this item is a part of */
    public CharField item_name; /**< unique name of this item */
    public IntegerField quantity; /**< current quantity of this item in the Inventory */
    public IntegerField history; /**< cumulative quantity of this item ever added to the Inventory */

    public static final QuerySet<InventoryItem> objects(Context context) {
        return objects(context, InventoryItem.class);
    }

    public InventoryItem() {
        super();
        this.game = new ForeignKeyField<>(GameSession.class);
        this.item_name = new CharField(32);
        this.quantity = new IntegerField();
        this.history = new IntegerField();
    }

    @Override
    protected void migrate(Context context) {
        Migrator<InventoryItem> migrator = new Migrator<InventoryItem>(InventoryItem.class);

        // Add history field
        migrator.addField("history", new IntegerField());

        // roll out all migrations
        migrator.migrate(context);
        return;
    }
}
