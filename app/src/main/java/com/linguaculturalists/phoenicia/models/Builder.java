package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.IntegerField;

/**
 * Class for maintaining the build status and progress for \link Letter Letters \endlink and \link Word Words \endlink.
 */
public abstract class Builder extends Model {
    public static final int NONE = 0; /**< Builder has been created but nothing else */
    public static final int SCHEDULED = 1; /**< Builder has been set to start, but has not started yet */
    public static final int BUILDING = 2; /**< Builder is actively running */
    public static final int COMPLETE = 3; /**< Builder has run through to completion */
    public BuildStatusUpdateHandler updateHandler;

    public CharField item_name; /**< name of the InventoryItem this builder is creating */
    public IntegerField time; /**< time (in seconds) that it takes for this build to finish */
    public IntegerField progress; /**< time (in seconds) this build has been running */
    public IntegerField status; /**< current status of this build */

    /**
     * Increment the progress by one second.
     * @return percentage complete
     */
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

    /**
     * Set status to Builder.SCHEDULED and notify the Builder.updateHandler.
     */
    public void schedule() {
        this.status.set(Builder.SCHEDULED);
        this.updateHandler.onScheduled(this);
    }

    /**
     * Set status to Builder.BUILDING and notify the Builder.updateHandler.
     */
    public void start() {
        this.status.set(Builder.BUILDING);
        this.updateHandler.onStarted(this);
    }

    /**
     * Set status to Builder.COMPLETE and notify the Builder.updateHandler.
     */
    public void complete() {
        this.status.set(Builder.COMPLETE);
        this.updateHandler.onCompleted(this);
    }

    private void progressChanged() {
        this.updateHandler.onProgressChanged(this);
    }

    /**
     * Callbacks to handle changes in the build queue item
     */
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
