package com.linguaculturalists.phoenicia.mock;

/**
 * Created by mhall on 4/17/16.
 */
public class MockMethod {
    public boolean called = false;
    public int call_count = 0;
    public void call() {
        this.called = true;
        this.call_count++;
    }
}