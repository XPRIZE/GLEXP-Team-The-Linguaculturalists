package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.util.List;

/**
 * Created by mhall on 6/19/15.
 */
public class BlockPlacementHUD extends CameraScene {
    private static Letter placeBlock = null;
    private static BlockPlacementHUD instance;

    private BlockPlacementHUD(final PhoeniciaGame game, final Level level) {
        super(game.camera);
        this.setBackgroundEnabled(false);

        final Font inventoryCountFont = FontFactory.create(game.activity.getFontManager(), game.activity.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.WHITE_ARGB_PACKED_INT);
        inventoryCountFont.load();
        final List<Letter> letters = level.letters;
        final int tile_start = 130;
        for (int i = 0; i < letters.size(); i++) {
            final Letter currentLetter = letters.get(i);
            Debug.d("Adding HUD letter: "+currentLetter.name+" (tile: "+currentLetter.tile+")");
            final int tile_id = currentLetter.tile;
            ITextureRegion blockRegion = game.terrainTiles.getTextureRegion(tile_id);
            ButtonSprite block = new ButtonSprite(64 * ((i * 2)+1), 48, blockRegion, game.activity.getVertexBufferObjectManager());
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    if (placeBlock == currentLetter) {
                        placeBlock = null;
                    } else {
                        placeBlock = currentLetter;
                    }
                }
            });
            this.registerTouchArea(block);
            this.attachChild(block);

            final Text inventoryCount = new Text(64 * ((i * 2)+1)+24, 20, inventoryCountFont, ""+(i+1), game.activity.getVertexBufferObjectManager());
            this.attachChild(inventoryCount);
        }
        Debug.d("Finished loading HUD letters");

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

        Debug.d("Finished instantiating BlockPlacementHUD");
    }

    public static void init(PhoeniciaGame game, Level level) {
        Debug.d("Initializing BlockPlacementHUD");
        instance = new BlockPlacementHUD(game, level);
    }

    public static BlockPlacementHUD getInstance() {
        return instance;
    }

    public static void setPlaceBlock(Letter letter) {
        instance.placeBlock = letter;
    }
    public static Letter getPlaceBlock() {
        return instance.placeBlock;
    }
}