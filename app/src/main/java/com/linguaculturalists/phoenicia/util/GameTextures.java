package com.linguaculturalists.phoenicia.util;

import com.linguaculturalists.phoenicia.PhoeniciaGame;

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
        size[0] = BASE_TILE_WIDTH + ((BASE_TILE_WIDTH/2) * (columns-1 + rows-1));
        // Calculate height
        size[1] = BASE_TILE_HEIGHT + ((BASE_TILE_HEIGHT/4) * Math.max(columns-1, rows-1));
        return size;
    }
}
