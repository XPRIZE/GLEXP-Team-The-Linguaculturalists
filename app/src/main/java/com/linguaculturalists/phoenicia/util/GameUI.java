package com.linguaculturalists.phoenicia.util;

import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;

/**
 * Created by mhall on 12/30/16.
 */
public class GameUI {
    private static final int GU = 96;
    private static final int H = 512;
    private static GameUI instance;
    private Texture gameui;

    private GameUI(Texture gameui) {
        this.gameui = gameui;
    }

    public static void init(Texture gameui) {
        instance = new GameUI(gameui);
    }

    public static GameUI getInstance() {
        return instance;
    }

    public ITextureRegion getLevelDisplay() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*5, GU*0, GU*3, GU*1);
    }

    public ITextureRegion getCoinsDisplay() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*5, GU*1, GU*3, GU*1);
    }

    public ITextureRegion getOkIcon() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*0, GU*0, GU*1, GU*1);
    }

    public ITextureRegion getCancelIcon() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*1, GU*0, GU*1, GU*1);
    }

    public ITextureRegion getHelpIcon() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*2, GU*0, GU*1, GU*1);
    }

    public ITextureRegion getPrevIcon() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*3, GU*0, GU*1, GU*1);
    }

    public ITextureRegion getNextIcon() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*4, GU*0, GU*1, GU*1);
    }

    public ITextureRegion getLetterLauncher() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*0, GU*1, GU*1, GU*1);
    }

    public ITextureRegion getWordLauncher() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*1, GU*1, GU*1, GU*1);
    }

    public ITextureRegion getGameLauncher() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*2, GU*1, GU*1, GU*1);
    }

    public ITextureRegion getDecorationLauncher() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*3, GU*1, GU*1, GU*1);
    }

    public ITextureRegion getRedButton() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*0, GU*4, GU*2, GU*1);
    }
    public ITextureRegion getCoinsButton() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*4, GU*2, GU*2, GU*1);
    }

    public ITextureRegion getCoinsIcon() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*7, GU*4, GU*1, GU*1);
    }

    public ITextureRegion getLevelButton() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*6, GU*2, GU*2, GU*1);
    }

    public ITextureRegion getLevelIcon() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*6, GU*4, GU*1, GU*1);
    }

    public ITextureRegion getGreenBlock() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*4, GU*3, GU*2, GU*1);
    }

    public ITextureRegion getGreyBlock() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*6, GU*3, GU*2, GU*1);
    }

    public ITextureRegion getGreenBanner() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*0, GU*4, GU*6, GU*2);
    }

    public ITextureRegion getBlueBanner() {
        return TextureRegionFactory.extractFromTexture(this.gameui, GU*0, GU*6, GU*6, GU*2);
    }

    public ITiledTextureRegion getMusicIcon() {
        return TextureRegionFactory.extractTiledFromTexture(this.gameui, GU * 6, GU * 5, GU * 2, GU * 1, 2, 1);
    }

    public ITiledTextureRegion getSoundIcon() {
        return TextureRegionFactory.extractTiledFromTexture(this.gameui, GU*6, GU*5, GU*2, GU*1, 2, 1);
    }
}
