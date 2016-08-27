package com.linguaculturalists.phoenicia.locale;

/**
 * Created by mhall on 2/15/16.
 */
public class DefaultBlock {
    public String name;
    public String level; /** level where this item becomes unlocked **/
    public int mapCol;
    public int mapRow;
    public int columns; /**< number of map columns this block occupies. Default is 1 */
    public int rows; /**< number of map rows this block occupies. Default is 1 */
    public String block_texture;

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
