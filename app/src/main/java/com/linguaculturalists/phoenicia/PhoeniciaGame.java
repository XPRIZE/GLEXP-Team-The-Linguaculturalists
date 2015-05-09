package com.linguaculturalists.phoenicia;

import android.content.res.AssetManager;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.atlas.TextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;

import java.io.IOException;
import java.util.List;

/**
 * Created by mhall on 3/22/15.
 */
public class PhoeniciaGame {

    private TextureManager textureManager;
    private AssetManager assetManager;
    private VertexBufferObjectManager vboManager;
    public Scene scene;
    public HUD hud;
    public AssetBitmapTexture terrainTexture;
    public ITiledTextureRegion terrainTiles;

    public Sprite[][] placedSprites;

    public WorldMap map;

    public boolean placeBlocks = false;

    public PhoeniciaGame(TextureManager textureManager, AssetManager assetManager, VertexBufferObjectManager vbo) {
        this.textureManager = textureManager;
        this.assetManager = assetManager;
        this.vboManager = vbo;
        scene = new Scene();
        scene.setBackground(new Background(new Color(0, 255, 0)));
        hud = new HUD();
    }

    public void load() throws IOException {
        terrainTexture = new AssetBitmapTexture(textureManager, assetManager, "textures/terrain.png");
        terrainTexture.load();
        terrainTiles = TextureRegionFactory.extractTiledFromTexture(terrainTexture, 0, 0, 640, 1024, 10, 16);
    }

    private int isoX(int x, int y) { return ((x * 32) - (y * 32)); }
    private int isoY(int x, int y) { return ((x * 16) + (y * 16)); }

    private int tileXat(int x, int y) { return (((x / 32) + (y / 16)) /2); }
    private int tileYat(int x, int y) { return (((y / 16) - (x / 32)) /2); }

    public void setWorldMap(WorldMap map) {
        this.map = map;
        this.placedSprites = new Sprite[this.map.width][this.map.height];
        int[][] tiles = this.map.data;
        for (int i=0; i < this.map.height; i++) {
            for (int j=this.map.width-1; j >= 0 ; j--) {
                ITextureRegion tileRegion = terrainTiles.getTextureRegion(tiles[i][j]);
                // Map tiles are background, so offset them down by the size of the tile (64px)
                Sprite tile = new Sprite(isoX(j, -i), isoY(j, -i)-64, tileRegion, vboManager);
                scene.attachChild(tile);
            }
        }

        // Create HUD
        ITextureRegion blockRegion = terrainTiles.getTextureRegion(145);
        ButtonSprite block = new ButtonSprite(64, 48, blockRegion, vboManager);
        block.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                System.out.println("Block button clicked!");
                placeBlocks = !placeBlocks;
            }
        });
        hud.registerTouchArea(block);
        hud.attachChild(block);
    }

    public void placeBlock(int x, int y) {
        if (!placeBlocks) {
            return;
        }
        int tileX = tileXat(x+16, y);// tiles are 32px wide, assume the touch is targeting the middle
        int tileY = tileYat(x+16, y);// tiles are 32px wide, assume the touch is targeting the middle
        int tileZ =  ((-tileX * 10) + (tileY) + 1);
        /*if (placedSprites[tilex][tiley] != null) {
            scene.detachChild(placedSprites[tilex][tiley]);
            placedSprites[tilex][tiley] = null;
        }*/

        System.out.println("  px-x: "+x);
        System.out.println("  px-y: "+y);
        System.out.println("tile-x: "+tileX);
        System.out.println("tile-y: "+tileY);
        System.out.println("tile-z: "+tileZ);

        ITextureRegion blockRegion = terrainTiles.getTextureRegion(145);
        Sprite block = new Sprite(isoX(tileX, tileY), isoY(tileX, tileY), blockRegion, vboManager);
        //block.setZIndex(tileZ);
        //placedSprites[tilex][tiley] = block;
        scene.attachChild(block);
    }

}
