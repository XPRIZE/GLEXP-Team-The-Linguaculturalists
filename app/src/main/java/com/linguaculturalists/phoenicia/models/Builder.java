package com.linguaculturalists.phoenicia.models;

import com.orm.androrm.Model;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.IntegerField;

/**
 * Created by mhall on 7/17/15.
 */
public abstract class Builder extends Model {
    public static final int NONE = 0;
    public static final int SCHEDULED = 1;
    public static final int BUILDING = 2;
    public static final int COMPLETE = 3;
    public BuildStatusUpdateHandler updateHandler;

    public CharField item_name;
    public IntegerField time;
    public IntegerField progress;
    public IntegerField status;

    public int update() {
        int currentStatus = this.status.get();
        if (currentStatus == Builder.NONE) { return  0; }
        if (currentStatus == Builder.COMPLETE) { return  100; }

        // Indicate that the build has started
        if (currentStatus == Builder.SCHEDULED) { this.start(); }

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
        this.status.set(Builder.SCHEDULED);
        this.updateHandler.onScheduled(this);
    }

    public void start() {
        this.status.set(Builder.BUILDING);
        this.updateHandler.onStarted(this);
    }

    public void complete() {
        this.status.set(Builder.COMPLETE);
        this.updateHandler.onCompleted(this);
    }

    private void progressChanged() {
        this.updateHandler.onProgressChanged(this);
    }

    // Callbacks to handle changes in the build queue item
    public interface BuildStatusUpdateHandler {
        public void onScheduled(Builder buildItem);
        public void onStarted(Builder buildItem);
        public void onCompleted(Builder buildItem);
        public void onProgressChanged(Builder builtItem);
    }
    public abstract class AbstractBuildStatusUpdateHandler implements BuildStatusUpdateHandler {
        public void onScheduled(Builder buildItem) { return; }
        public void onStarted(Builder buildItem) { return; }
        public void onCompleted(Builder buildItem) { return; }
        public void onProgressChanged(Builder builtItem) { return; }
    }

    public void setUpdateHandler(BuildStatusUpdateHandler handler) {
        this.updateHandler = handler;
    }


}
