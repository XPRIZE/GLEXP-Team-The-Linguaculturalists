package com.linguaculturalists.phoenicia;

import android.test.ActivityInstrumentationTestCase2;

import org.andengine.extension.tmx.TMXTile;

import java.io.IOException;

/**
 * Created by mhall on 2/29/16.
 */
public class TilePlacementTest extends PhoeniciaGameTest  {

    private static final int MAP_WIDTH = 30;
    private static final int MAP_HEIGHT = 30;

    public TilePlacementTest() {
        super();
    }

    public void testFindTileAtCoordinates() {
        this.loadGame();

        final int isoX = (int) Math.random() * MAP_WIDTH;
        final int isoY = (int) Math.random() * MAP_HEIGHT;
        final TMXTile target = game.getTileAtIso(isoX, isoY);
        final float x = target.getTileX() + (target.getTileWidth()/2);// 50% over
        final float y = target.getTileY() + (target.getTileHeight()/4);// 25% up
        final TMXTile actual = game.getTileAt(x, y);

        assertEquals(target.getTileColumn(), actual.getTileColumn());
        assertEquals(target.getTileRow(), actual.getTileRow());
    }
}
