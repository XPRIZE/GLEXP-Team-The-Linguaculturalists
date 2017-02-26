package com.linguaculturalists.phoenicia.locale;

/**
* Model for storing a definition of a Letter in a \a Locale.
*/
public class Number {
    public String name; /**< reference name for this number (can be different from the number character itself */
    public String sound; /**< path to audio file for this number's name */
    public char[] chars; /**< character sequence for this number. Will usually be just one character, but multiple are supported */
    public int intval; /**< integer value of this number */
    public String sprite_texture; /**< path to texture file for this number's sprites */

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
