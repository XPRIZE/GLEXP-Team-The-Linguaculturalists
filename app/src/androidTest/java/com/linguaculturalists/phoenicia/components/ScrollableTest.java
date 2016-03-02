package com.linguaculturalists.phoenicia.components;

import android.test.ActivityInstrumentationTestCase2;

import com.linguaculturalists.phoenicia.TestActivity;

/**
 * Created by mhall on 3/1/16.
 */
public class ScrollableTest extends ActivityInstrumentationTestCase2<TestActivity> {

    private TestActivity activity;
    public ScrollableTest() { super(TestActivity.class); }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.activity = getActivity();
    }

    @Override
    protected void tearDown() throws Exception {
        this.activity.scene.detachChildren();
        super.tearDown();
    }

    public void testCreateScrollable() {
        Scrollable component = new Scrollable(this.activity.CAMERA_WIDTH/2, this.activity.CAMERA_HEIGHT/2, this.activity.CAMERA_WIDTH/2, this.activity.CAMERA_HEIGHT/2);
        assertNotNull(component);
        this.activity.scene.attachChild(component);

    }
}
