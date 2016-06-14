package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.IntegerField;

import org.andengine.util.debug.Debug;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for maintaining the build status and progress for \link Letter Letters \endlink and \link Word Words \endlink.
 */
public abstract class Builder extends Model {
    public static final int NONE = 0; /**< Builder has been created but nothing else */
    public static final int SCHEDULED = 1; /**< Builder has been set to start, but has not started yet */
    public static final int BUILDING = 2; /**< Builder is actively running */
    public static final int COMPLETE = 3; /**< Builder has run through to completion */
    protected List<BuildStatusUpdateHandler> updateHandlers;

    public CharField item_name; /**< name of the InventoryItem this builder is creating */
    public IntegerField time; /**< time (in seconds) that it takes for this build to finish */
    public IntegerField progress; /**< time (in seconds) this build has been running */
    public IntegerField status; /**< current status of this build */

    public Builder() {
        super();
        this.updateHandlers = new ArrayList<BuildStatusUpdateHandler>();
    }
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
        this.save(PhoeniciaContext.context);
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
        this.save(PhoeniciaContext.context);
        for (BuildStatusUpdateHandler handler : new ArrayList<BuildStatusUpdateHandler>(this.updateHandlers)) {
            handler.onScheduled(this);
        }
    }

    /**
     * Set status to Builder.BUILDING and notify the Builder.updateHandler.
     */
    public void start() {
        this.status.set(Builder.BUILDING);
        this.save(PhoeniciaContext.context);
        for (BuildStatusUpdateHandler handler : new ArrayList<BuildStatusUpdateHandler>(this.updateHandlers)) {
            handler.onStarted(this);
        }
    }

    /**
     * Set status to Builder.COMPLETE and notify the Builder.updateHandler.
     */
    public void complete() {
        this.status.set(Builder.COMPLETE);
        this.save(PhoeniciaContext.context);
        for (BuildStatusUpdateHandler handler : new ArrayList<BuildStatusUpdateHandler>(this.updateHandlers)) {
            handler.onCompleted(this);
        }
    }

    private void progressChanged() {
        //Debug.d("Builder progress changed: " + this.progress.get());
        for (BuildStatusUpdateHandler handler : new ArrayList<BuildStatusUpdateHandler>(this.updateHandlers)) {
            handler.onProgressChanged(this);
        }
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

    public void addUpdateHandler(BuildStatusUpdateHandler handler) {
        this.updateHandlers.remove(handler);
        this.updateHandlers.add(handler);
    }

    public void removeUpdateHandler(BuildStatusUpdateHandler handler) {
        this.updateHandlers.remove(handler);
    }


}
