package com.linguaculturalists.phoenicia.locale;

/**
 * Created by mhall on 6/21/16.
 */
public class Decoration {
    public String name; /**< display name for this decoration */
    public String level; /**< level where this decoration is unlcoked */
    public int columns; /**< number of map columns this block occupies. Default is 1 */
    public int rows; /**< number of map rows this block occupies. Default is 1 */
    public String restriction; /**< tile class this block can be placed on */
    public String sprite_texture;/**< path to texture file for this decoration's sprite */
    public String block_texture;/**< path to texture file for this decoration's block */

    public int points;/**< number of experience points needed to unlock this decoration */
    public int buy;/**< amount of in-game currency required to create this decoration */
}
