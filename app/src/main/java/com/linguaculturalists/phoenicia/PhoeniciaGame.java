package com.linguaculturalists.phoenicia;

import android.content.res.AssetManager;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.TextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;
import org.andengine.extension.tmx.TMXLoader;
import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.util.debug.Debug;

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
    private TMXTiledMap mTMXTiledMap;

    public int placeBlock = -1;

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

        try {
            final TMXLoader tmxLoader = new TMXLoader(assetManager, textureManager, TextureOptions.BILINEAR_PREMULTIPLYALPHA, vboManager);
            this.mTMXTiledMap = tmxLoader.loadFromAsset("textures/map.tmx");
        } catch (final TMXLoadException e) {
            Debug.e(e);
        }
    }

    private int isoX(int x, int y) { return ((x * 32) - (-y * 32)); }
    private int isoY(int x, int y) { return ((x * 16) + (-y * 16)); }

    public void setWorldMap(WorldMap map) {
        for (TMXLayer tmxLayer : this.mTMXTiledMap.getTMXLayers()){
            scene.attachChild(tmxLayer);
        }
    }
    public void setWorldMap2(WorldMap map) {
        this.map = map;
        this.placedSprites = new Sprite[this.map.width][this.map.height];
        int[][] tiles = this.map.data;
        for (int i=0; i < this.map.height; i++) {
            for (int j=0; j < this.map.width ; j++) {
                ITextureRegion tileRegion = terrainTiles.getTextureRegion(tiles[j][i]);
                // Map tiles are background, so offset them down by half the size of the tile (64px)
                Sprite tile = new Sprite(isoX(this.map.width-1-j, i), isoY(this.map.width-1-j, i)-(this.map.tileWidth/2), tileRegion, vboManager);
                scene.attachChild(tile);
            }
        }

        ITextureRegion blockRegion = terrainTiles.getTextureRegion(145);
        ButtonSprite block = new ButtonSprite(64, 48, blockRegion, vboManager);
        block.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                if (placeBlock == 145) {
                    placeBlock = -1;
                } else {
                    placeBlock = 145;
                }
            }
        });
        hud.registerTouchArea(block);
        hud.attachChild(block);

        ITextureRegion greyBlockRegion = terrainTiles.getTextureRegion(144);
        ButtonSprite greyBlock = new ButtonSprite(64*3, 48, greyBlockRegion, vboManager);
        greyBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                if (placeBlock == 144) {
                    placeBlock = -1;
                } else {
                    placeBlock = 144;
                }
            }
        });
        hud.registerTouchArea(greyBlock);
        hud.attachChild(greyBlock);

        ITextureRegion bushBlockRegion = terrainTiles.getTextureRegion(126);
        ButtonSprite bushBlock = new ButtonSprite(64*5, 48, bushBlockRegion, vboManager);
        bushBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                if (placeBlock == 126) {
                    placeBlock = -1;
                } else {
                    placeBlock = 126;
                }
            }
        });
        hud.registerTouchArea(bushBlock);
        hud.attachChild(bushBlock);

    }

    public void placeBlock(int x, int y) {
        if (placeBlock < 0) {
            return;
        }
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(1);
        final TMXTile tmxTile = tmxLayer.getTMXTileAt(x, y);
        int tileX = (int)tmxTile.getTileX();// tiles are 64px wide, assume the touch is targeting the middle
        int tileY = (int)tmxTile.getTileY();// tiles are 64px wide, assume the touch is targeting the middle
        if (tileY < 0 || tileY >= this.map.height) { return; }
        if (tileX < 0 || tileX >= this.map.width) { return; }

        int tileZ = 100 + ((-tileX * 10) + (tileY) + 1);
        if (placedSprites[tileX][tileY] != null) {
            scene.detachChild(placedSprites[tileX][tileY]);
            placedSprites[tileX][tileY] = null;
        }

        System.out.println("  px-x: "+x);
        System.out.println("  px-y: "+y);
        System.out.println("tile-x: "+tileX);
        System.out.println("tile-y: "+tileY);
        System.out.println("tile-z: "+tileZ);

        ITextureRegion blockRegion = terrainTiles.getTextureRegion(placeBlock);
        Sprite block = new Sprite(isoX(tileX, tileY), isoY(tileX, tileY), blockRegion, vboManager);
        block.setZIndex(tileZ);
        placedSprites[tileX][tileY] = block;
        scene.attachChild(block);
    }

}
