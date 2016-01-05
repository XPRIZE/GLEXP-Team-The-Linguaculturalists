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
 * Created by mhall on 1/3/16.
 */
public class LetterSprite extends ButtonSprite {
    private Letter letter;
    private int count;
    private Text count_text;

    public LetterSprite(float pX, float pY, Letter letter, int count, ITextureRegion region, VertexBufferObjectManager vbo) {
        super(pX, pY, region, vbo);
        this.letter = letter;
        this.count = count;

        this.count_text = new Text(32, -8, GameFonts.inventoryCount(), String.valueOf(count), 4, vbo);
        this.attachChild(count_text);

    }

    /*
    @Override
    public float getHeight() {
        if (this.count_text == null) {
            return super.getHeight();
        } else {
            return super.getHeight() + this.count_text.getHeight();
        }
    }
*/

    public void setCount(int count) {
        this.count = count;
        this.count_text.setText(String.valueOf(count));
    }

}
