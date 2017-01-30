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
    static Font dialogFont;
    static Font defaultHUDDisplayFont;
    static Font introTextFont;
    static Font inventoryCountFont;
    static Font itemCostFont;
    static Font buttonFont;
    static Font progressFont;

    /**
     * Font used for displaying text in a Button
     * @return
     */
    public static Font dialogText() {
        if (dialogFont == null) {
            BitmapTextureAtlas texture = new BitmapTextureAtlas(PhoeniciaContext.textureManager, 1024, 1024, TextureOptions.BILINEAR);
            dialogFont = FontFactory.create(PhoeniciaContext.fontManager, texture, Typeface.create(Typeface.MONOSPACE, Typeface.BOLD), 32, true, new Color(0.5f, 0.5f, 0.5f).getABGRPackedInt());
            dialogFont.load();
        }
        return dialogFont;
    }

    /**
     * Font used for displaying the cost of an InventoryItem.
     * @return
     */
    public static Font itemCost() {
        if (itemCostFont == null) {
            BitmapTextureAtlas texture = new BitmapTextureAtlas(PhoeniciaContext.textureManager, 1024, 1024, TextureOptions.BILINEAR);
            itemCostFont = FontFactory.createStroke(PhoeniciaContext.fontManager, texture, Typeface.create(Typeface.MONOSPACE, Typeface.BOLD), 24, true, new Color(0.96f, 0.70f, 0f).getARGBPackedInt(), 0.5f, new Color(0.95f, 0.61f, 0f).getARGBPackedInt());
            itemCostFont.load();
        }
        return itemCostFont;
    }

    /**
     * Font used for displaying the quantity of an InventoryItem.
     * @return
     */
    public static Font inventoryCount() {
        if (inventoryCountFont == null) {
            BitmapTextureAtlas texture = new BitmapTextureAtlas(PhoeniciaContext.textureManager, 1024, 1024, TextureOptions.BILINEAR);
            inventoryCountFont = FontFactory.create(PhoeniciaContext.fontManager, texture, Typeface.create(Typeface.MONOSPACE, Typeface.BOLD), 24, true, new Color(0.5f, 0.5f, 0.5f).getABGRPackedInt());
            inventoryCountFont.load();
        }
        return inventoryCountFont;
    }

    /**
     * Font used for displaying the level and account balance in the DefaultHUD.
     * @return
     */
    public static Font defaultHUDDisplay() {
        if (defaultHUDDisplayFont == null) {
            BitmapTextureAtlas texture = new BitmapTextureAtlas(PhoeniciaContext.textureManager, 1024, 1024, TextureOptions.BILINEAR);
            defaultHUDDisplayFont = FontFactory.create(PhoeniciaContext.fontManager, texture, Typeface.create(Typeface.MONOSPACE, Typeface.BOLD), 30, true, Color.WHITE_ARGB_PACKED_INT);
            defaultHUDDisplayFont.load();
        }
        return defaultHUDDisplayFont;
    }

    public static Font introText() {
        if (introTextFont == null) {
            BitmapTextureAtlas texture = new BitmapTextureAtlas(PhoeniciaContext.textureManager, 1024, 1024, TextureOptions.BILINEAR);
            introTextFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 36, Color.BLUE_ARGB_PACKED_INT);
            introTextFont.load();
        }
        return introTextFont;
    }
    /**
     * Font used for displaying text in a Button
     * @return
     */
    public static Font buttonText() {
        if (buttonFont == null) {
            Color borderColor = new Color(20/255f, 91/255f, 164/255f);
            BitmapTextureAtlas texture = new BitmapTextureAtlas(PhoeniciaContext.textureManager, 1024, 1024, TextureOptions.BILINEAR);
            buttonFont = FontFactory.createStroke(PhoeniciaContext.fontManager, texture, Typeface.create(Typeface.MONOSPACE, Typeface.BOLD), 32, true, Color.WHITE_ARGB_PACKED_INT, 0.75f, borderColor.getARGBPackedInt());
            buttonFont.load();
        }
        return buttonFont;
    }

    public static Font progressText() {
        if (progressFont == null) {
            BitmapTextureAtlas texture = new BitmapTextureAtlas(PhoeniciaContext.textureManager, 1024, 1024, TextureOptions.BILINEAR);
            progressFont = FontFactory.createStroke(PhoeniciaContext.fontManager, texture, Typeface.create(Typeface.MONOSPACE, Typeface.BOLD), 24, true, Color.WHITE_ARGB_PACKED_INT, 0.5f, Color.BLUE_ARGB_PACKED_INT);
            progressFont.load();
        }
        return progressFont;
    }
}
