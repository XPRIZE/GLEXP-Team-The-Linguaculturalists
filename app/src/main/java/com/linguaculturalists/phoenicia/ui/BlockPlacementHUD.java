package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.PhoeniciaGame;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.opengl.texture.region.ITextureRegion;

/**
 * Created by mhall on 6/19/15.
 */
public class BlockPlacementHUD extends HUD {
    private static int placeBlock;
    private static BlockPlacementHUD instance;

    private BlockPlacementHUD(PhoeniciaGame game) {
        ITextureRegion blockRegion = game.terrainTiles.getTextureRegion(145);
        ButtonSprite block = new ButtonSprite(64, 48, blockRegion, game.activity.getVertexBufferObjectManager());
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
        this.registerTouchArea(block);
        this.attachChild(block);

        ITextureRegion greyBlockRegion = game.terrainTiles.getTextureRegion(144);
        ButtonSprite greyBlock = new ButtonSprite(64 * 3, 48, greyBlockRegion, game.activity.getVertexBufferObjectManager());
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
        this.registerTouchArea(greyBlock);
        this.attachChild(greyBlock);

        ITextureRegion bushBlockRegion = game.terrainTiles.getTextureRegion(126);
        ButtonSprite bushBlock = new ButtonSprite(64 * 5, 48, bushBlockRegion, game.activity.getVertexBufferObjectManager());
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
        this.registerTouchArea(bushBlock);
        this.attachChild(bushBlock);

    }

    public static void init(PhoeniciaGame game) {
        instance = new BlockPlacementHUD(game);
    }

    public static BlockPlacementHUD getInstance() {
        return instance;
    }

    public static void setPlaceBlock(int block_id) {
        instance.placeBlock = block_id;
    }
    public static int getPlaceBlock() {
        return instance.placeBlock;
    }
}