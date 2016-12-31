package com.linguaculturalists.phoenicia.components;

import com.linguaculturalists.phoenicia.GameActivity;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.Constants;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhall on 4/10/16.
 */
public class Dialog extends Entity {

    private Scene openOn;
    private DialogListener listener;
    private List<ITouchArea> touchAreas;

    public enum DialogButton {
        OK, CANCEL, YES, NO
    }
    public enum Buttons {
        OK, OK_CANCEL, YES_NO
    }

    public Dialog(final float pWidth, final float pHeight, VertexBufferObjectManager vbo, final DialogListener listener) {
        this(pWidth, pHeight, Buttons.OK, vbo, listener);
    }

    public Dialog(final float pWidth, final float pHeight, Buttons buttons, VertexBufferObjectManager vbo, final DialogListener listener) {
        super(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2, pWidth, pHeight);
        this.listener = listener;
        Rectangle border = new Rectangle(pWidth/2, pHeight/2, pWidth+2, pHeight+2, vbo);
        border.setColor(new Color(0.3f, 0.3f, 0.3f));
        this.attachChild(border);

        Rectangle background = new Rectangle(pWidth/2, pHeight/2, pWidth, pHeight, vbo);
        background.setColor(new Color(0.95f, 0.95f, 0.95f));
        this.attachChild(background);

        this.touchAreas = new ArrayList<ITouchArea>();

        final Dialog that = this;
        if (buttons == Buttons.OK) {
            Button okButton = new Button(pWidth / 2, 32, 192, 48, "Ok", vbo, new Button.OnClickListener() {
                @Override
                public void onClicked(Button button) {
                    listener.onDialogButtonClicked(that, DialogButton.OK);
                }
            });
            this.attachChild(okButton);
            this.touchAreas.add(okButton);
        } else if (buttons == Buttons.OK_CANCEL) {
            Button okButton = new Button(pWidth / 2 - 82, 32, 160, 48, "Ok", vbo, new Button.OnClickListener() {
                @Override
                public void onClicked(Button button) {
                    listener.onDialogButtonClicked(that, DialogButton.OK);
                }
            });
            this.attachChild(okButton);
            this.touchAreas.add(okButton);

            Button cancelButton = new Button(pWidth / 2 + 82, 32, 160, 48, "Cancel", vbo, new Button.OnClickListener() {
                @Override
                public void onClicked(Button button) {
                    listener.onDialogButtonClicked(that, DialogButton.CANCEL);
                }
            });
            this.attachChild(cancelButton);
            this.touchAreas.add(cancelButton);
        } else if (buttons == Buttons.YES_NO) {
            Button okButton = new Button(pWidth / 2 - 82, 32, 160, 48, "Yes", vbo, new Button.OnClickListener() {
                @Override
                public void onClicked(Button button) {
                    listener.onDialogButtonClicked(that, DialogButton.YES);
                }
            });
            this.attachChild(okButton);
            this.touchAreas.add(okButton);

            Button cancelButton = new Button(pWidth / 2 + 82, 32, 160, 48, "No", vbo, new Button.OnClickListener() {
                @Override
                public void onClicked(Button button) {
                    listener.onDialogButtonClicked(that, DialogButton.NO);
                }
            });
            this.attachChild(cancelButton);
            this.touchAreas.add(cancelButton);
        }
    }

    public void registerTouchArea(ITouchArea area) {
        this.touchAreas.add(area);
    }
    public void unregisterTouchArea(ITouchArea area) {
        if (this.touchAreas.contains(area)) this.touchAreas.remove(area);
    }
    public void open(Scene target) {
        Debug.d("Opening dialog on "+target);
        this.openOn = target;
        //this.openOn.centerEntityInCamera(this);
        //this.setPosition(target.getCamera().getWidth()/2, target.getCamera().getHeight()/2);
        //this.setZIndex(target.getZIndex()+100);
        this.openOn.attachChild(this);
    }

    public void close() {
        Debug.d("Closing dialog");
        if (this.openOn != null) {
            this.openOn.detachChild(this);
            this.openOn = null;
        }
    }

    @Override
    public boolean onAreaTouched(TouchEvent touchEvent, float localTouchX, float localTouchY) {
        final float sceneTouchX = touchEvent.getX();
        final float sceneTouchY = touchEvent.getY();
        for (ITouchArea area: this.touchAreas) {
            if (area.contains(sceneTouchX, sceneTouchY)) {
                final float[] areaTouchCoordinates = area.convertSceneCoordinatesToLocalCoordinates(sceneTouchX, sceneTouchY);
                final float areaTouchX = areaTouchCoordinates[Constants.VERTEX_INDEX_X];
                final float areaTouchY = areaTouchCoordinates[Constants.VERTEX_INDEX_Y];
                final Boolean handled = area.onAreaTouched(touchEvent, areaTouchX, areaTouchY);
                if (handled != null && handled) {
                    return true;
                }
            }
        }
        return true;
    }

    public interface DialogListener {
        public void onDialogButtonClicked(Dialog dialog, DialogButton dialogButton);
    }
}
