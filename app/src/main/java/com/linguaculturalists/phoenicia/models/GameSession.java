package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.Locale;
import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.DoubleField;
import com.orm.androrm.field.IntegerField;
import com.orm.androrm.migration.Migrator;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    public CharField current_level;
    public IntegerField points;
    public IntegerField account_balance;
    public IntegerField gross_income;

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
        this.current_level = new CharField();
        this.points = new IntegerField();
        this.account_balance = new IntegerField();
        this.gross_income = new IntegerField();
    }

    static public GameSession start(Locale locale) {
        GameSession session = new GameSession();
        session.locale_pack.set(locale.name);
        Date now = new Date();
        session.start_timestamp.set((double)now.getTime());
        session.last_timestamp.set((double)now.getTime());
        session.sessions_played.set(0);
        session.days_played.set(0);
        session.current_level.set(locale.levels.get(0).name);
        session.points.set(0);
        session.account_balance.set(0);
        session.gross_income.set(0);
        return session;
    }

    public void update() {
        Date now = new Date();
        long diff = now.getTime() - this.last_timestamp.get().longValue();
        if (TimeUnit.MILLISECONDS.toDays(diff) > 1) {
            this.days_played.set(this.days_played.get() + 1);
        }
        this.sessions_played.set(this.sessions_played.get() + 1);
        this.last_timestamp.set((double)now.getTime());
    }
    @Override
    protected void migrate(Context context) {
        Migrator<GameSession> migrator = new Migrator<GameSession>(GameSession.class);

        // Add level field
        migrator.addField("current_level", new IntegerField());

        // Add points field
        migrator.addField("points", new IntegerField());
        migrator.addField("account_balance", new IntegerField());
        migrator.addField("gross_income", new IntegerField());

        // roll out all migrations
        migrator.migrate(context);
        return;
    }
}
