package com.linguaculturalists.phoenicia.components;

import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.view.MotionEvent;

import com.linguaculturalists.phoenicia.PhoeniciaGameTest;
import com.linguaculturalists.phoenicia.TestActivity;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.input.touch.TouchEvent;

/**
 * Created by mhall on 3/1/16.
 */
public class ButtonTest extends PhoeniciaGameTest {

    @Override
    public void tearDown() throws Exception {
        this.game.scene.detachChildren();
        super.tearDown();
    }

    public void testCreateButton() {
        Button component = new Button(this.activity.CAMERA_WIDTH/2, this.activity.CAMERA_HEIGHT/2, this.activity.CAMERA_WIDTH/2, this.activity.CAMERA_HEIGHT/2, "Test", PhoeniciaContext.vboManager, null);
        assertNotNull(component);
        this.game.scene.attachChild(component);
    }

    public void testButtonClickedListener() {
        ClickTestListener listener = new ClickTestListener();
        Button component = new Button(this.activity.CAMERA_WIDTH/2, this.activity.CAMERA_HEIGHT/2, this.activity.CAMERA_WIDTH/2, this.activity.CAMERA_HEIGHT/2, "Test", PhoeniciaContext.vboManager, listener);
        assertNotNull(component);

        assertFalse(listener.wasClicked);
        component.onClick(null, 0, 0f, 0f);
        assertTrue(listener.wasClicked);
    }

    public void testButtonTouchArea() {
        this.startGame();
        ClickTestListener listener = new ClickTestListener();
        Button component = new Button(this.activity.CAMERA_WIDTH/2, this.activity.CAMERA_HEIGHT/2, this.activity.CAMERA_WIDTH/2, this.activity.CAMERA_HEIGHT/2, "Test", PhoeniciaContext.vboManager, listener);
        assertNotNull(component);

        assertFalse(listener.wasClicked);

        final long uptimeMillis = SystemClock.uptimeMillis();
        float[] surfaceCoordinates = this.activity.main_camera.getSurfaceCoordinatesFromSceneCoordinates(this.activity.CAMERA_WIDTH/2, this.activity.CAMERA_HEIGHT/2);
        component.onAreaTouched(TouchEvent.obtain(this.activity.CAMERA_WIDTH / 2, this.activity.CAMERA_HEIGHT / 2, TouchEvent.ACTION_DOWN, 0, MotionEvent.obtain(uptimeMillis, uptimeMillis, MotionEvent.ACTION_DOWN, 0, 0, 0)), this.activity.CAMERA_WIDTH/2, this.activity.CAMERA_HEIGHT/2);
        component.onAreaTouched(TouchEvent.obtain(this.activity.CAMERA_WIDTH / 2, this.activity.CAMERA_HEIGHT / 2, TouchEvent.ACTION_UP, 0, MotionEvent.obtain(uptimeMillis, uptimeMillis, MotionEvent.ACTION_UP, 0, 0, 0)), this.activity.CAMERA_WIDTH/2, this.activity.CAMERA_HEIGHT/2);
        assertTrue(listener.wasClicked);
    }

    private class ClickTestListener implements Button.OnClickListener {
        public boolean wasClicked = false;
        @Override
        public void onClicked(Button button) {
            this.wasClicked = true;
        }
    }
}
