package com.linguaculturalists.phoenicia.ui;

import android.graphics.Typeface;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.Scrollable;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;
import com.linguaculturalists.phoenicia.models.LetterBuilder;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.WordBuilder;
import com.linguaculturalists.phoenicia.models.WordTile;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.ease.EaseBackOut;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HUD for selecting \link Word Words \endlink to be placed as tiles onto the map.
 */
public class WordPlacementHUD extends PhoeniciaHUD implements Inventory.InventoryUpdateListener {
    private static Word placeWord = null;
    private Map<String, Text> inventoryCounts;
    private PhoeniciaGame game;

    private Rectangle whiteRect;
    private Scrollable blockPanel;

    public WordPlacementHUD(final PhoeniciaGame game, final Level level) {
        super(game.camera);
        this.setBackgroundEnabled(false);
        this.inventoryCounts = new HashMap<String, Text>();
        Inventory.getInstance().addUpdateListener(this);
        this.game = game;

        this.whiteRect = new Rectangle(GameActivity.CAMERA_WIDTH/2, 64, 600, 96, PhoeniciaContext.vboManager);
        whiteRect.setColor(Color.WHITE);
        this.attachChild(whiteRect);

        this.blockPanel = new Scrollable(GameActivity.CAMERA_WIDTH/2, 64, 600, 96, Scrollable.SCROLL_HORIZONTAL);

        this.registerTouchArea(blockPanel);
        this.registerTouchArea(blockPanel.contents);
        this.attachChild(blockPanel);

        final Font inventoryCountFont = FontFactory.create(PhoeniciaContext.fontManager, PhoeniciaContext.textureManager, 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 16, Color.RED_ARGB_PACKED_INT);
        inventoryCountFont.load();
        final List<Word> words = level.words;
        final int tile_start = 130;
        final int startX = (int)(blockPanel.getWidth()/2);
        for (int i = 0; i < words.size(); i++) {
            final Word currentWord = words.get(i);
            Debug.d("Adding HUD word: " + currentWord.name + " (tile: " + currentWord.tile + ")");
            final int tile_id = currentWord.sprite;
            ITextureRegion blockRegion = new TiledTextureRegion(game.wordTextures.get(currentWord),
                    game.wordTiles.get(currentWord).getTextureRegion(0),
                    game.wordTiles.get(currentWord).getTextureRegion(1),
                    game.wordTiles.get(currentWord).getTextureRegion(2));
            ButtonSprite block = new ButtonSprite((64 * ((i * 2)+1)), 48, blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    float[] cameraCenter = getCamera().getSceneCoordinatesFromCameraSceneCoordinates(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2);
                    TMXTile mapTile = game.getTileAt(cameraCenter[0], cameraCenter[1]);
                    addWordTile(currentWord, mapTile);
                }
            });
            this.registerTouchArea(block);
            blockPanel.attachChild(block);

            final Text inventoryCount = new Text((64 * ((i * 2)+1))+24, 20, inventoryCountFont, ""+game.inventory.getCount(currentWord.name), 4, PhoeniciaContext.vboManager);
            blockPanel.attachChild(inventoryCount);
            this.inventoryCounts.put(currentWord.name, inventoryCount);
        }
        Debug.d("Finished loading HUD letters");

        Debug.d("Finished instantiating BlockPlacementHUD");

    }

    /**
     * Animate the bottom panel sliding up into view.
     */
    @Override
    public void show() {
        whiteRect.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
        blockPanel.registerEntityModifier(new MoveYModifier(0.5f, -48, 64, EaseBackOut.getInstance()));
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
                Debug.e("[WordPlacementHUD] No HUD item for "+items[i].item_name.get());
            }
        }
    }


    @Override
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        Debug.d("LetterPlacementHud touched at "+pSceneTouchEvent.getX()+"x"+pSceneTouchEvent.getY());

        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        if (handled) return true;

        return false;
    }

    /**
     * Create a new WordTile (with Sprite and Builder).
     * @param word Word to create the tile for
     * @param onTile Map tile to place the new tile on
     */
    private void addWordTile(final Word word, final TMXTile onTile) {
        Debug.d("Placing word "+word.name+" at "+onTile.getTileColumn()+"x"+onTile.getTileRow());
        final WordTile wordTile = new WordTile(this.game, word);

        wordTile.isoX.set(onTile.getTileColumn());
        wordTile.isoY.set(onTile.getTileRow());

        game.createWordSprite(wordTile, new PhoeniciaGame.CreateWordSpriteCallback() {
            @Override
            public void onWordSpriteCreated(WordTile tile) {
                WordBuilder builder = new WordBuilder(game.session, wordTile, wordTile.item_name.get(), word.construct);
                builder.start();
                builder.save(PhoeniciaContext.context);
                game.addBuilder(builder);

                wordTile.setBuilder(builder);
                wordTile.save(PhoeniciaContext.context);

            }

            @Override
            public void onWordSpriteCreationFailed(WordTile tile) {
                Debug.d("Failed to create word sprite");
            }
        });

    }

}