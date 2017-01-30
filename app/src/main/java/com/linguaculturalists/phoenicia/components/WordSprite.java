package com.linguaculturalists.phoenicia.components;

import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.util.GameFonts;

import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;

/**
 * A ButtonSprite that displays a letter and it's corresponding inventory count.
 * A ButtonSprite and Text combination for displaying letters with an inventory count. This class is
 * not an InventoryUpdateListener, so the count must be update by other code
 */
public class WordSprite extends ButtonSprite {
    private Word word;
    private int count;
    private int needed;
    private Text count_text;

    /**
     * Construct a new LetterSprite
     * @param pX the X coordinate of the scene to place this LetterSprite
     * @param pY the Y coordinate of the scene to place this LetterSprite
     * @param word the locale Word this sprite will represent
     * @param count number to show below the sprite
     * @param region the ITextureRegion containing the tiles for this letter
     * @param vbo the game's VertexBufferObjectManager
     */
    public WordSprite(float pX, float pY, Word word, int count, ITiledTextureRegion region, VertexBufferObjectManager vbo) {
        this(pX, pY, word, count, 0, region, vbo);
    }
    /**
     * Construct a new LetterSprite
     * @param pX the X coordinate of the scene to place this LetterSprite
     * @param pY the Y coordinate of the scene to place this LetterSprite
     * @param word the locale Word this sprite will represent
     * @param count how many you have
     * @param needed how many you need
     * @param region the ITextureRegion containing the tiles for this letter
     * @param vbo the game's VertexBufferObjectManager
     */
    public WordSprite(float pX, float pY, Word word, int count, int needed, ITiledTextureRegion region, VertexBufferObjectManager vbo) {
        super(pX, pY, region, vbo);
        this.word = word;
        this.count = count;
        this.needed = needed;

        this.count_text = new Text(32, -10, GameFonts.inventoryCount(), "", 8, new TextOptions(HorizontalAlign.CENTER), vbo);
        this.setCount(count);
        this.attachChild(count_text);

    }

    /**
     * Update the inventory count text for this LetterSprite
     * @param count new inventory count value
     */
    public void setCount(int count) {
        this.count = count;
        if (this.needed > 0) {
            this.count_text.setText(String.format("%1$d/%2$d", count, needed));
        } else {
            this.count_text.setText(String.valueOf(count));
        }
        this.count_text.setPosition((this.getWidth()/2), this.count_text.getY());
        if (count < needed) {
            this.count_text.setColor(Color.RED);
        } else {
            this.count_text.setColor(Color.WHITE);
        }
    }

    public void showCount(final boolean show) {
        this.count_text.setVisible(show);
    }

    public Word getWord() {
        return word;
    }
}
