package com.linguaculturalists.phoenicia.locale;

/**
* Created by mhall on 7/21/15.
*/
public class Word {
    public String name; /**< reference name for this word (can be different from the word itself */
    public int columns; /**< number of map columns this word block occupies. Default is 1 */
    public int rows; /**< number of map rows this word block occupies. Default is 1 */
    public int height; /**< number of map blocks high this block uses. Default is 1 */
    public String restriction; /**< tile class this letter can be placed on */
    public int construct;/**< time (in seconds) it takes to build this block */
    public int time;/**< time (in seconds) it takes to build this word */
    public int tile;/**< start tile index for a \a PlacedBlockSprite for this word */
    public int sprite;/**< start tile index for a \a LetterSprite for this word */
    public String sound;/**< path to audio file for this word */
    public char[] chars;/**< character sequence for this word */
    public String sprite_texture;/**< path to texture file for this word's sprite */
    public String block_texture;/**< path to texture file for this word's block */

    public int points; /**< number of in-game points earned from the creation of this word */
    public int buy;/**< amount of in-game currency required to create this word */
    public int sell;/**< amount of in-game currency obtained from selling this word */
}
