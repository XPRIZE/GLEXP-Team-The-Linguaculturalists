package com.linguaculturalists.phoenicia.locale;

/**
* Created by mhall on 7/21/15.
*/
public class Letter {
    public String name;
    public int time;
    public final int tile = 4;
    public final int sprite = 0;
    public String sound;
    public String phoneme;
    public char[] chars;
    public String texture_src;
    public int points;
    public int buy;
    public int sell;

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
