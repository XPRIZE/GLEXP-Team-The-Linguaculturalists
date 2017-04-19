package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Word;
import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.DoubleField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;
import com.orm.androrm.migration.Migrator;

import java.util.Date;

/**
 * Created by mhall on 3/14/17.
 */
public class GiftRequest extends Model {

    public static final int LETTER_REQUEST = 0;
    public static final int WORD_REQUEST = 1;

    public ForeignKeyField<GameSession> game; /**< reference to the GameSession this request is a part of */
    public IntegerField itemType;
    public IntegerField itemIndex; /**< index of this item in the locale definition */
    public IntegerField checkKey; /**< random number used to verify the request response */
    public IntegerField requestCode; /**< code to share to get help with this request */
    public DoubleField requested; /**< time when the request was created */
    public DoubleField received; /**< time when the git was received */
    public ForeignKeyField<MarketRequest> marketRequest; /**< Market Request that started this Gift Request */

    public GiftRequest() {
        super();
        this.game = new ForeignKeyField<GameSession>(GameSession.class);
        this.itemType = new IntegerField();
        this.itemIndex = new IntegerField();
        this.checkKey = new IntegerField();
        this.requestCode = new IntegerField();
        this.marketRequest = new ForeignKeyField<MarketRequest>(MarketRequest.class);
        this.requested = new DoubleField();
        this.received = new DoubleField();
    }

    public static GiftRequest fromRequestCode(int requestCode) {
        int checkKey = (requestCode)/ 10000;
        int encData = (requestCode % 10000);
        int data = decode(checkKey, encData);

        int itemIndex = (data % 1000);
        data -= itemIndex;
        int itemType = (data % 10000) / 1000;

        GiftRequest newRequest = new GiftRequest();
        newRequest.itemType.set(itemType);
        newRequest.itemIndex.set(itemIndex);
        newRequest.checkKey.set(checkKey);
        newRequest.requestCode.set(requestCode);
        return newRequest;
    }

    public static GiftRequest newRequest(final PhoeniciaGame game, Letter letter, final MarketRequest marketRequest) {
        int letterIndex = game.locale.letters.indexOf(letter);
        return GiftRequest.newRequest(game, LETTER_REQUEST, letterIndex, marketRequest);
    }

    public static GiftRequest newRequest(final PhoeniciaGame game, Word word, final MarketRequest marketRequest) {
        int wordIndex = game.locale.words.indexOf(word);
        return GiftRequest.newRequest(game, WORD_REQUEST, wordIndex, marketRequest);
    }

    private static GiftRequest newRequest(final PhoeniciaGame game, final int itemType, final int itemIndex, final MarketRequest marketRequest) {
        GiftRequest request = new GiftRequest();
        request.game.set(game.session);
        request.itemType.set(itemType);
        request.itemIndex.set(itemIndex);
        request.marketRequest.set(marketRequest);

        int randomKey = (int)Math.round(Math.random() * 89)+10;
        request.checkKey.set(randomKey);

        // TODO: encode type and index with the random key
        int code = 0;
        code += itemIndex;        // ***000 - ***999
        code += itemType * 1000;  // **0*** - **1***
        code = encode(randomKey, code);
        code += randomKey * 10000;// 00**** - 99****
        request.requestCode.set(code);

        Date now = new Date();
        request.requested.set((double) now.getTime());
        return request;
    }

    public boolean verify(int responseCode) {
        int responseKey = (responseCode)/ 10000;
        int encData = (responseCode % 10000);
        int respData = decode(responseKey, encData);
        int data = decode(this.checkKey.get(), respData);

        int itemIndex = (data % 1000);
        data -= itemIndex;
        int itemType = (data % 10000) / 1000;

        if (this.itemType.get() == itemType && this.itemIndex.get() == itemIndex) {
            return true;
        } else {
            return false;
        }
    }

    public static int encode(final int checkKey, final int rawData) {
        int encData = rawData;
        // Encode item data with the check key
        int encKey = checkKey * checkKey;
        encData = rawData ^ encKey;
        return encData;
    }
    public static int decode(final int checkKey, final int encData) {
        int rawData = encData;
        // Decode item data with the check key
        int encKey = checkKey * checkKey;
        rawData = encData ^ encKey;
        return rawData;
    }

    public static final QuerySet<GiftRequest> objects(Context context) {
        return objects(context, GiftRequest.class);
    }

    @Override
    protected void migrate(Context context) {
        Migrator<GiftRequest> migrator = new Migrator<GiftRequest>(GiftRequest.class);

        // roll out all migrations
        migrator.migrate(context);
        return;
    }
}
