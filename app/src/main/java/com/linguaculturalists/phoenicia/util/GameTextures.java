package com.linguaculturalists.phoenicia.util;

import com.linguaculturalists.phoenicia.PhoeniciaGame;

import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.tmx.TMXTile;

/**
 * Predined tile indexes from the Shell UI texture
 */
public class GameTextures {
    public static final int BASE_TILE_WIDTH = 64;
    public static final int BASE_TILE_HEIGHT = 64;

    public static final int LETTER_PLACEMENT = 0; /**< Tile for activating the LetterPlacementHUD */
    public static final int WORD_PLACEMENT = 1; /**< Tile for activating the WordPlacementHUD */
    public static final int INVENTORY_ICON = 2; /**< Tile for representing the Inventory */
    public static final int LEVEL_ICON = 3; /**< Icon to display before the current level */
    public static final int XP_ICON = 4; /**< Icon to denote experience points */
    public static final int COIN_ICON = 5; /**< Icon used to denote in-game coins */
    public static final int OK = 6; /**< Common image for Ok, Yes, or positive response */
    public static final int CANCEL = 7; /**< Common image for Cancel, No, or negative response */

    public static int[] calculateTileSize(int columns, int rows) {
        int[] size = new int[2];
        // Calculate width
        size[0] = BASE_TILE_WIDTH + ((BASE_TILE_WIDTH/2) * (columns+rows-2));
        // Calculate height
        size[1] = BASE_TILE_HEIGHT + ((BASE_TILE_HEIGHT/4) * (columns+rows-2));
        return size;
    }

    public static float[] calculateTilePosition(TMXTile mapTile, Sprite sprite, int columns, int rows) {
        return calculateTilePosition(mapTile, (int)sprite.getWidth(), (int)sprite.getHeight(), columns, rows);
    }
    public static float[] calculateTilePosition(TMXTile mapTile, int[] tileSize, int columns, int rows) {
        return calculateTilePosition(mapTile, tileSize[0], tileSize[1], columns, rows);
    }
    public static float[] calculateTilePosition(TMXTile mapTile, int width, int height, int columns, int rows) {
        float[] pos = new float[2];
        pos[0] = mapTile.getTileX() + (width/2) - ((GameTextures.BASE_TILE_WIDTH/2)*(columns-1));// anchor sprite center to tile center
        pos[1] = mapTile.getTileY() + (height/2);// anchor sprite center half the sprite's height higher than the tile bottom
        return pos;
    }
}
