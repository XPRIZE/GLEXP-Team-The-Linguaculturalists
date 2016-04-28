package com.linguaculturalists.phoenicia.locale;

/**
* Model for storing a definition of a Letter in a \a Locale.
*/
public class Letter {
    public String name; /**< reference name for this letter (can be different from the letter character itself */
    public int columns; /**< number of map columns this letter block occupies. Default is 1 */
    public int rows; /**< number of map rows this letter block occupies. Default is 1 */
    public String restriction; /**< tile class this letter can be placed on */
    public int time; /**< time (in seconds) it takes to build this letter */
    public final int tile = 4; /**< start tile index for a PlacedBlockSprite for this letter */
    public final int sprite = 0; /**< start tile index for a LetterSprite for this letter */
    public String sound; /**< path to audio file for this letter's name */
    public String phoneme; /**< path to audio file for this letter's sound */
    public char[] chars; /**< character sequence for this letter. Will usually be just one character, but multiple are supported */
    public String texture_src; /**< path to texture file for this letter */
    public int points; /**< number of in-game points earned from the creation of this letter */
    public int buy; /**< amount of in-game currency required to create this letter */
    public int sell; /**< amount of in-game currency obtained from selling this letter */

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
