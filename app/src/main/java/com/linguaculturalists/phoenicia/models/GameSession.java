package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.DoubleField;
import com.orm.androrm.field.IntegerField;

/**
 * Created by mhall on 7/17/15.
 */
public class GameSession extends Model {

    public CharField session_name;
    public CharField locale_pack;
    public DoubleField start_timestamp;
    public DoubleField last_timestamp;
    public IntegerField sessions_played;
    public IntegerField days_played;

    public static final QuerySet<GameSession> objects(Context context) {
        return objects(context, GameSession.class);
    }

    public GameSession(){
        super();
        this.session_name = new CharField(32);
        this.locale_pack = new CharField(32);
        this.start_timestamp = new DoubleField();
        this.last_timestamp = new DoubleField();
        this.sessions_played = new IntegerField();
        this.days_played = new IntegerField();
    }
}
