package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.DoubleField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;
import com.orm.androrm.migration.Migrator;

import org.andengine.util.debug.Debug;

import java.util.Date;

/**
 * Created by mhall on 3/19/17.
 */
public class Gift extends Model {

    public ForeignKeyField<GameSession> game; /**< reference to the GameSession this gift is a part of */
    public IntegerField requestType;
    public IntegerField requestItem; /**< index of this item in the locale definition */
    public IntegerField requestCode; /**< code received as a GiftRequest */
    public IntegerField responseCode; /**< code to verify the Gift was given */
    public DoubleField sent; /**< time when the gift was sent */

    public Gift() {
        super();
        this.game = new ForeignKeyField<GameSession>(GameSession.class);
        this.requestCode = new IntegerField();
        this.requestType = new IntegerField();
        this.requestItem = new IntegerField();
        this.requestCode = new IntegerField();
        this.responseCode = new IntegerField();
        this.sent = new DoubleField();
    }

    public static Gift newForRequest(final GameSession game, final int requestCode) {
        Debug.d("Generating Gift from request code: " + requestCode);
        Gift newGift = new Gift();
        newGift.game.set(game);
        newGift.requestCode.set(requestCode);
        GiftRequest request = GiftRequest.fromRequestCode(requestCode);
        newGift.requestType.set(request.itemType.get());
        newGift.requestItem.set(request.itemIndex.get());

        int requestKey = request.checkKey.get();
        int responseKey = (int)Math.round(Math.random() * 99);
        Debug.d("Generating Gift with response key: "+responseKey);

        int code = requestCode % 10000;// **0000 - **9999
        code = GiftRequest.encode(responseKey, code);
        code += responseKey * 10000;// 00**** - 99****
        newGift.responseCode.set(code);
        Debug.d("Generating Gift with response code: "+code);

        Date now = new Date();
        newGift.sent.set((double) now.getTime());
        return newGift;
    }


    public static final QuerySet<Gift> objects(Context context) {
        return objects(context, Gift.class);
    }

    @Override
    protected void migrate(Context context) {
        Migrator<Gift> migrator = new Migrator<Gift>(Gift.class);

        // roll out all migrations
        migrator.migrate(context);
        return;
    }

}
