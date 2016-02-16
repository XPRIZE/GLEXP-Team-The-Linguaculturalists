package com.linguaculturalists.phoenicia.locale;

/**
 * Created by mhall on 2/15/16.
 */
public class DefaultBlock {
    public String name;
    public int mapCol;
    public int mapRow;
    public String texture_src;

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
