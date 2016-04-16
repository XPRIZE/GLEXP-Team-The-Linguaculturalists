package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.orm.androrm.Filter;
import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.DoubleField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;
import com.orm.androrm.migration.Migrator;

import java.util.List;

/**
 * Database model representing a type of item in a player's Inventory.
 */
public class MarketRequest extends Model {

    public static final int REQUESTED = 0;
    public static final int FULFILLED = 1;
    public static final int CANCELED = 2;

    public ForeignKeyField<GameSession> game; /**< reference to the GameSession this item is a part of */
    public CharField person_name; /**< name of the locale Person making the request */
    public IntegerField status; /**< status of this request */
    public IntegerField coins; /**< number of coins being offered */
    public IntegerField points; /**< number of experience points being offered */
    public DoubleField requested; /**< time when the request was created */
    public DoubleField fulfilled; /**< time when the request was fulfilled */

    private List<RequestItem> items;

    public static final QuerySet<MarketRequest> objects(Context context) {
        return objects(context, MarketRequest.class);
    }

    public MarketRequest() {
        super();
        this.game = new ForeignKeyField<GameSession>(GameSession.class);
        this.person_name = new CharField(32);
        this.status = new IntegerField();
        this.coins = new IntegerField();
        this.points = new IntegerField();
        this.requested = new DoubleField();
        this.fulfilled = new DoubleField();
    }

    public List<RequestItem> getItems(Context context) {
        if (this.items == null) {
            Filter byRequest = new Filter();
            byRequest.is("request", this);
            this.items = RequestItem.objects(context).filter(byRequest).toList();
        }
        return this.items;
    }

    @Override
    protected void migrate(Context context) {
        Migrator<MarketRequest> migrator = new Migrator<MarketRequest>(MarketRequest.class);

        migrator.addField("coins", new IntegerField());
        migrator.addField("points", new IntegerField());

        // roll out all migrations
        migrator.migrate(context);
        return;
    }
}
