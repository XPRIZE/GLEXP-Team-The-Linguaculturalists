package com.linguaculturalists.phoenicia.components;

import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.util.GameFonts;

import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * A ButtonSprite that displays a letter and it's corresponding inventory count.
 * A ButtonSprite and Text combination for displaying letters with an inventory count. This class is
 * not an InventoryUpdateListener, so the count must be update by other code
 */
public class WordSprite extends ButtonSprite {
    private Word word;
    private int count;
    private Text count_text;

    /**
     * Construct a new LetterSprite
     * @param pX the X coordinate of the scene to place this LetterSprite
     * @param pY the Y coordinate of the scene to place this LetterSprite
     * @param word the locale Word this sprite will represent
     * @param region the ITextureRegion containing the tiles for this letter
     * @param vbo the game's VertexBufferObjectManager
     */
    public WordSprite(float pX, float pY, Word word, int count, ITextureRegion region, VertexBufferObjectManager vbo) {
        super(pX, pY, region, vbo);
        this.word = word;
        this.count = count;

        this.count_text = new Text(32, -8, GameFonts.inventoryCount(), String.valueOf(count), 4, vbo);
        this.attachChild(count_text);

    }

    /**
     * Update the inventory count text for this LetterSprite
     * @param count new inventory count value
     */
    public void setCount(int count) {
        this.count = count;
        this.count_text.setText(String.valueOf(count));
    }

}
