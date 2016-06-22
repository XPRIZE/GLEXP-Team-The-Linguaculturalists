package com.linguaculturalists.phoenicia.locale;

/**
 * Created by mhall on 6/21/16.
 */
public class Game {
    public String name; /**< display name for this game */
    public String type; /**< type of mini-game this block will be */
    public String restriction; /**< tile class this block can be placed on */
    public String block_texture;/**< path to texture file for this game's block */

    public int points;/**< number of experience points needed to unlock this game */
    public int construct;/**< time (in seconds) it takes to build this block */
    public int time;/**< time (in seconds) before you can replay this game */
    public int buy;/**< amount of in-game currency required to create this game */
    public float reward;/**< amount to multiply the word sell value by when the player wins */
}
