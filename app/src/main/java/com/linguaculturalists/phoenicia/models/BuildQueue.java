package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.IntegerField;

import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 7/17/15.
 */
public class BuildQueue extends Model {
    public static final int NONE = 0;
    public static final int SCHEDULED = 1;
    public static final int BUILDING = 2;
    public static final int COMPLETE = 3;
    private BuildStatusUpdateHandler updateHandler;

    public ForeignKeyField<GameSession> game;
    public ForeignKeyField<PlacedBlock> tile;
    public CharField item_name;
    public IntegerField time;
    public IntegerField progress;
    public IntegerField status;

    public static final QuerySet<BuildQueue> objects(Context context) {
        return objects(context, BuildQueue.class);
    }

    public BuildQueue() {
        super();
        this.game = new ForeignKeyField<>(GameSession.class);
        this.tile = new ForeignKeyField<>(PlacedBlock.class);
        this.item_name = new CharField(32);
        this.time = new IntegerField();
        this.progress = new IntegerField();
        this.status = new IntegerField();
        this.progress.set(0);
        this.status.set(NONE);
    }

    public BuildQueue(PlacedBlock block, String item_name, int time) {
        this(block, item_name, time, null);
    }
    public BuildQueue(PlacedBlock block, String item_name, int time, BuildStatusUpdateHandler updateHandler) {
        this();
        this.tile.set(block);
        this.item_name.set(item_name);
        this.time.set(time);

        if (updateHandler != null) {
            this.updateHandler = updateHandler;
        } else {
            this.updateHandler = new AbstractBuildStatusUpdateHandler() { };
        }
    }


    public int update() {
        int currentStatus = this.status.get();
        if (currentStatus == BuildQueue.NONE) { return  0; }
        if (currentStatus == BuildQueue.COMPLETE) { return  100; }

        // Indicate that the build has started
        if (currentStatus == BuildQueue.SCHEDULED) { this.start(); }

        // Increment progress
        int newProgress = this.progress.get() + 1;
        this.progress.set(newProgress);
        this.progressChanged();

        // Check for completeness
        int buildTime = this.time.get();
        if (newProgress >= buildTime) { this.complete(); return 100; }

        // Return percent completed
        return (int) newProgress / buildTime;
    }

    public void schedule() {
        this.status.set(BuildQueue.SCHEDULED);
        this.updateHandler.onScheduled(this);
    }

    public void start() {
        this.status.set(BuildQueue.BUILDING);
        this.updateHandler.onStarted(this);
    }

    public void complete() {
        this.status.set(BuildQueue.COMPLETE);
        this.updateHandler.onCompleted(this);
    }

    private void progressChanged() {
        this.updateHandler.onProgressChanged(this);
    }

    // Callbacks to handle changes in the build queue item
    public interface BuildStatusUpdateHandler {
        public void onScheduled(BuildQueue buildItem);
        public void onStarted(BuildQueue buildItem);
        public void onCompleted(BuildQueue buildItem);
        public void onProgressChanged(BuildQueue builtItem);
    }
    public abstract class AbstractBuildStatusUpdateHandler implements BuildStatusUpdateHandler {
        public void onScheduled(BuildQueue buildItem) { return; }
        public void onStarted(BuildQueue buildItem) { return; }
        public void onCompleted(BuildQueue buildItem) { return; }
        public void onProgressChanged(BuildQueue builtItem) { return; }
    }

    public void setUpdateHandler(BuildStatusUpdateHandler handler) {
        this.updateHandler = handler;
    }

    @Override
    protected void migrate(Context context) {
        return;
    }
}
