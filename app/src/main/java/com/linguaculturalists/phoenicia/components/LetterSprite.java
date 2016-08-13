package com.linguaculturalists.phoenicia.components;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.util.GameFonts;

import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;

/**
 * A ButtonSprite that displays a letter and it's corresponding inventory count.
 * A ButtonSprite and Text combination for displaying letters with an inventory count. This class is
 * not an InventoryUpdateListener, so the count must be update by other code
 */
public class LetterSprite extends ButtonSprite {
    private Letter letter;
    private int count;
    private Text count_text;

    /**
     * Construct a new LetterSprite
     * @param pX the X coordinate of the scene to place this LetterSprite
     * @param pY the Y coordinate of the scene to place this LetterSprite
     * @param letter the locale Letter this sprite will represent
     * @param region the ITextureRegion containing the tiles for this letter
     * @param vbo the game's VertexBufferObjectManager
     */
    public LetterSprite(float pX, float pY, Letter letter, int count, ITextureRegion region, VertexBufferObjectManager vbo) {
        super(pX, pY, region, vbo);
        this.letter = letter;
        this.count = count;

        this.count_text = new Text(32, -8, GameFonts.inventoryCount(), String.valueOf(count), 8, vbo);
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
