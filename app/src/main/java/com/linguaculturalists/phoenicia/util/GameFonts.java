package com.linguaculturalists.phoenicia.util;

import android.graphics.Typeface;

import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.util.adt.color.Color;

import java.lang.reflect.Type;

/**
 * Manager class for getting pre-defined font instances for Phoenicia.
 */
public class GameFonts {
    static Font defaultHUDDisplayFont;
    static Font inventoryCountFont;

    /**
     * Font used for displaying the quantity of an InventoryItem.
     * @return
     */
    public static Font inventoryCount() {
        if (inventoryCountFont == null) {
            BitmapTextureAtlas texture = new BitmapTextureAtlas(PhoeniciaContext.textureManager, 1024, 1024, TextureOptions.BILINEAR);
            inventoryCountFont = FontFactory.createStroke(PhoeniciaContext.fontManager, texture, Typeface.create(Typeface.MONOSPACE, Typeface.BOLD), 32, true, Color.YELLOW_ARGB_PACKED_INT, 1, Color.RED_ARGB_PACKED_INT);
            inventoryCountFont.load();
        }
        return inventoryCountFont;
    }

    /**
     * Fond used for displaying the level and account balance in the DefaultHUD.
     * @return
     */
    public static Font defaultHUDDisplay() {
        if (defaultHUDDisplayFont == null) {
            BitmapTextureAtlas texture = new BitmapTextureAtlas(PhoeniciaContext.textureManager, 1024, 1024, TextureOptions.BILINEAR);
            defaultHUDDisplayFont = FontFactory.createStroke(PhoeniciaContext.fontManager, texture, Typeface.create(Typeface.MONOSPACE, Typeface.BOLD), 32, true, Color.GREEN_ARGB_PACKED_INT, 1, Color.WHITE_ARGB_PACKED_INT);
            defaultHUDDisplayFont.load();
        }
        return defaultHUDDisplayFont;
    }

}
