package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.PhoeniciaGame;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.opengl.texture.region.ITextureRegion;

/**
 * Created by mhall on 6/19/15.
 */
public class BlockPlacementHUD extends HUD {
    private static int placeBlock = -1;
    private static BlockPlacementHUD instance;

    private BlockPlacementHUD(final PhoeniciaGame game) {
        final char letters[] = {'a', 'b', 'c', 'd', 'e', 'f'};
        final int tile_start = 130;
        for (int i = 0; i < letters.length; i++) {
            final int tile_id = tile_start+i;
            ITextureRegion blockRegion = game.terrainTiles.getTextureRegion(tile_id);
            ButtonSprite block = new ButtonSprite(64 * ((i * 2)+1), 48, blockRegion, game.activity.getVertexBufferObjectManager());
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    if (placeBlock == tile_id) {
                        placeBlock = -1;
                    } else {
                        placeBlock = tile_id;
                    }
                }
            });
            this.registerTouchArea(block);
            this.attachChild(block);
        }

        ITextureRegion clearRegion = game.terrainTiles.getTextureRegion(159);
        ButtonSprite clearBlock = new ButtonSprite(game.camera.getWidth()-32, game.camera.getHeight()-48, clearRegion, game.activity.getVertexBufferObjectManager());
        clearBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.restart();
            }
        });
        this.registerTouchArea(clearBlock);
        this.attachChild(clearBlock);
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