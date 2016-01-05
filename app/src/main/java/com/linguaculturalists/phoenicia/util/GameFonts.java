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
 * Created by mhall on 1/3/16.
 */
public class GameFonts {
    static FontManager fontManager;
    static TextureManager textureManager;

    static Font defaultHUDDisplayFont;
    static Font inventoryCountFont;

    public static void init(FontManager fm, TextureManager tm) {
        fontManager = fm;
        textureManager = tm;

    }

    public static Font inventoryCount() {
        if (inventoryCountFont == null) {
            BitmapTextureAtlas texture = new BitmapTextureAtlas(textureManager, 256, 256, TextureOptions.BILINEAR);
            inventoryCountFont = FontFactory.createStroke(fontManager, texture, Typeface.create(Typeface.MONOSPACE, Typeface.BOLD), 32, true, Color.YELLOW_ARGB_PACKED_INT, 1, Color.RED_ARGB_PACKED_INT);
            inventoryCountFont.load();
        }
        return inventoryCountFont;
    }

    public static Font getDefaultHUDDisplay() {
        if (defaultHUDDisplayFont == null) {
            BitmapTextureAtlas texture = new BitmapTextureAtlas(textureManager, 256, 256, TextureOptions.BILINEAR);
            defaultHUDDisplayFont = FontFactory.createStroke(fontManager, texture, Typeface.create(Typeface.MONOSPACE, Typeface.BOLD), 32, true, Color.GREEN_ARGB_PACKED_INT, 1, Color.WHITE_ARGB_PACKED_INT);
            defaultHUDDisplayFont.load();
        }
        return defaultHUDDisplayFont;
    }

}
