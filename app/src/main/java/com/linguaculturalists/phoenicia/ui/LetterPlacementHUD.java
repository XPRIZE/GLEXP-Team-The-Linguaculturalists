package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mhall on 6/19/15.
 */
public class LetterPlacementHUD extends CameraScene implements Inventory.InventoryUpdateListener {
    private static Letter placeBlock = null;
    private static LetterPlacementHUD instance;
    private Map<String, Text> inventoryCounts;
    private PhoeniciaGame game;


    private LetterPlacementHUD(final PhoeniciaGame game, final Level level) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.inventoryCounts = new HashMap<String, Text>();
        Inventory.getInstance().addUpdateListener(this);
        this.game = game;

        Rectangle whiteRect = new Rectangle(game.activity.CAMERA_WIDTH/2, 64, 400, 96, game.activity.getVertexBufferObjectManager());
        whiteRect .setColor(Color.WHITE);
        this.attachChild(whiteRect);

        Scrollable blockPanel = new Scrollable(game.activity.CAMERA_WIDTH/2, 64, 400, 96, Scrollable.SCROLL_HORIZONTAL);
        Rectangle redRect = new Rectangle(game.activity.CAMERA_WIDTH/2, 64, 512, 128, game.activity.getVertexBufferObjectManager());
        redRect.setColor(Color.RED);
        //blockPanel.attachChild(redRect);


        this.registerTouchArea(blockPanel);
        this.registerTouchArea(blockPanel.contents);
        this.attachChild(blockPanel);

        final Font inventoryCountFont = FontFactory.create(game.activity.getFontManager(), game.activity.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.RED_ARGB_PACKED_INT);
        inventoryCountFont.load();
        final List<Letter> letters = level.letters;
        final int tile_start = 130;
        final int startX = (int)(blockPanel.getWidth()/2);
        for (int i = 0; i < letters.size(); i++) {
            final Letter currentLetter = letters.get(i);
            Debug.d("Adding HUD letter: "+currentLetter.name+" (sprite: "+currentLetter.sprite+")");
            final int tile_id = currentLetter.sprite;
            ITiledTextureRegion blockRegion = new TiledTextureRegion(game.lettersTexture,
                    game.letterTiles.getTextureRegion(tile_id),
                    game.letterTiles.getTextureRegion(tile_id+1),
                    game.letterTiles.getTextureRegion(tile_id+2));
            final ButtonSprite block = new ButtonSprite((64 * ((i * 2)+1))-startX, 48, blockRegion, game.activity.getVertexBufferObjectManager());
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    Debug.d("Activated block: " + currentLetter.name);
                    if (game.setPlaceBlock(currentLetter)) {
                        // TODO: Switch to active tile
                    } else {
                        // TODO: handle failure
                    }
                }
            });
            this.registerTouchArea(block);
            blockPanel.attachChild(block);

            final Text inventoryCount = new Text((64 * ((i * 2)+1))-startX+24, 20, inventoryCountFont, ""+game.inventory.getCount(currentLetter.name), 4, game.activity.getVertexBufferObjectManager());
            blockPanel.attachChild(inventoryCount);
            this.inventoryCounts.put(currentLetter.name, inventoryCount);
        }
        Debug.d("Finished loading HUD letters");

        ITextureRegion wordRegion = game.wordTiles.getTextureRegion(157);
        ButtonSprite wordBlock = new ButtonSprite(64, 64, wordRegion, game.activity.getVertexBufferObjectManager());
        wordBlock.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                game.hudManager.showWordPlacement(level);
            }
        });
        this.registerTouchArea(wordBlock);
        this.attachChild(wordBlock);

        Debug.d("Finished instantiating BlockPlacementHUD");
    }

    public static void init(PhoeniciaGame game, Level level) {
        Debug.d("Initializing BlockPlacementHUD");
        instance = new LetterPlacementHUD(game, level);
    }

    public static LetterPlacementHUD getInstance() {
        return instance;
    }

    public static void setPlaceBlock(Letter letter) {
        instance.placeBlock = letter;
    }
    public static Letter getPlaceBlock() {
        return instance.placeBlock;
    }

    public void onInventoryUpdated(final InventoryItem[] items) {
        Debug.d("Updating BlockPlacementHUD inventory");
        for (int i = 0; i < items.length; i++) {
            Debug.d("Updating BlockPlacementHUD count for "+items[i].item_name.get());
            if (this.inventoryCounts.containsKey(items[i].item_name.get())) {
                Debug.d("New HUD count: "+items[i].quantity.get().toString());
                final Text countText = this.inventoryCounts.get(items[i].item_name.get());
                countText.setText(items[i].quantity.get().toString());
                //countText.setText("9");
            } else {
                Debug.e("No HUD item for "+items[i].item_name.get());
            }
        }
    }
}