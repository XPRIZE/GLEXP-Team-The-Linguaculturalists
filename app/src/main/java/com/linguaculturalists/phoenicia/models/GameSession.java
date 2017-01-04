package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.locale.Locale;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Filter;
import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.BooleanField;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.DoubleField;
import com.orm.androrm.field.IntegerField;
import com.orm.androrm.migration.Migrator;

import org.andengine.util.debug.Debug;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Database model representing a single user's game session.
 */
public class GameSession extends Model {

    public CharField session_name; /**< user defined name for this session */
    public CharField person_name; /**< locale person representing this player */
    public CharField locale_pack; /**< path to the locale pack used by this session */
    public DoubleField start_timestamp; /**< time when this session was created (in seconds from epoch) */
    public DoubleField last_timestamp; /**< last time this session was used (in seconds from epoch) */
    public IntegerField sessions_played; /**< incremented every time the session is used */
    public IntegerField days_played; /**< number of unique days when this session was used */
    public CharField current_level; /**< the name of the current level from the locale that the session has reached */
    public IntegerField points; /**< accumulated points from various in-game actions */
    public IntegerField account_balance; /**< current amount of in-game currency held by the player */
    public IntegerField gross_income; /**< cumulative total of in-game currency earned over the course of this session */

    public BooleanField is_active; /**< Whether this session is active (not deleted) and should be displayed */
    public BooleanField pref_music; /**< Store user preference to play background music or not */

    public Filter filter;
    public Filter session_filter;

    private List<ExperienceChangeListener> listeners;

    public static final QuerySet<GameSession> objects(Context context) {
        return objects(context, GameSession.class);
    }

    public GameSession(){
        super();
        this.session_name = new CharField(32);
        this.person_name = new CharField(32);
        this.locale_pack = new CharField(32);
        this.start_timestamp = new DoubleField();
        this.last_timestamp = new DoubleField();
        this.sessions_played = new IntegerField();
        this.days_played = new IntegerField();
        this.current_level = new CharField();
        this.points = new IntegerField();
        this.account_balance = new IntegerField();
        this.gross_income = new IntegerField();

        this.is_active = new BooleanField();
        this.pref_music = new BooleanField();

        this.listeners = new ArrayList<ExperienceChangeListener>();
    }

    /**
     * Creates a new GameSession for the given local.
     * @param locale for the new session
     * @return a new session
     */
    static public GameSession start(Locale locale) {
        GameSession session = new GameSession();
        session.locale_pack.set(locale.name);
        Date now = new Date();
        session.start_timestamp.set((double)now.getTime());
        session.last_timestamp.set((double)now.getTime());
        session.sessions_played.set(0);
        session.days_played.set(0);
        session.points.set(0);
        session.account_balance.set(0);
        session.gross_income.set(0);
        session.is_active.set(true);
        session.pref_music.set(true);
        return session;
    }

    static public GameSession start(String locale_path) {
        GameSession session = new GameSession();
        session.locale_pack.set(locale_path);
        Date now = new Date();
        session.start_timestamp.set((double)now.getTime());
        session.last_timestamp.set((double)now.getTime());
        session.sessions_played.set(0);
        session.days_played.set(0);
        session.points.set(0);
        session.account_balance.set(0);
        session.gross_income.set(0);
        session.is_active.set(true);
        session.pref_music.set(true);
        return session;
    }

    public void reset() {
        Date now = new Date();
        this.start_timestamp.set((double)now.getTime());
        this.last_timestamp.set((double)now.getTime());
        this.sessions_played.set(0);
        this.days_played.set(0);
        this.current_level.reset();
        this.points.set(0);
        this.account_balance.set(0);
        this.gross_income.set(0);
        this.is_active.set(true);
        this.pref_music.set(true);
    }

    public int addExperience(int points) {
        this.points.set(this.points.get()+points);
        this.experienceChanged(this.points.get());
        return this.points.get();
    }
    public int removeExperience(int points) {
        this.points.set(this.points.get()-points);
        this.experienceChanged(this.points.get());
        return this.points.get();
    }
    private void experienceChanged(int new_xp) {
        Debug.d("Experience points changed");
        for (int i = 0; i < this.listeners.size(); i++) {
            Debug.d("Calling update listener: "+this.listeners.get(i).getClass());
            this.listeners.get(i).onExperienceChanged(new_xp);
        }
    }
    public void addExperienceChangeListener(ExperienceChangeListener listener) {
        this.listeners.add(listener);
    }
    public void removeUpdateListener(ExperienceChangeListener listener) {
        this.listeners.remove(listener);
    }
    @Override
    public boolean save(Context context) {
        final boolean retval = super.save(context);
        if (retval && this.filter == null) {
            this.filter = new Filter();
            this.filter.is("game", this);
            this.session_filter = new Filter();
            this.session_filter.is("session", this);
        }
        return retval;
    }

    /**
     * Inform the session of a new instance of play
     *
     * Updates #last_timestamp, #sessions_played, and if it has been more than a day since the last
     * time, also updates the #days_played3
     */
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
        migrator.addField("person_name", new CharField());

        // Add state fields
        migrator.addField("is_active", new BooleanField());
        migrator.addField("pref_music", new BooleanField());

        // roll out all migrations
        migrator.migrate(context);
        return;
    }

    /**
     * Called by the GameSession class whenever the player's experience points changes
     */
    public interface ExperienceChangeListener {
        public void onExperienceChanged(int new_xp);
    }
}
