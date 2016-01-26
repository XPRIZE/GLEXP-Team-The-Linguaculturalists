package com.linguaculturalists.phoenicia.locale;

/**
* Created by mhall on 7/21/15.
*/
public class Word {
    public String name; /**< reference name for this word (can be different from the word itself */
    public int time;/**< time (in seconds) it takes to build this word */
    public int tile;/**< start tile index for a \a PlacedBlockSprite for this word */
    public int sprite;/**< start tile index for a \a LetterSprite for this word */
    public String sound;/**< path to audio file for this word */
    public char[] chars;/**< character sequence for this word */
    public String texture_src;/**< path to texture file for this word */

    public int points; /**< number of in-game points earned from the creation of this word */
    public int buy;/**< amount of in-game currency required to create this word */
    public int sell;/**< amount of in-game currency obtained from selling this word */
}
