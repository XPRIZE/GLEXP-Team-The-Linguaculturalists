package com.linguaculturalists.phoenicia.components;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.models.Inventory;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.Entity;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.Constants;
import org.andengine.util.debug.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mhall on 1/11/17.
 */
public class Keyboard extends Entity {
    private PhoeniciaGame game;
    private int details;
    private KeyClickListener listener;
    private Map<String, Text> inventoryCounts;
    private Map<String, Integer> usedCounts;
    private List<ITouchArea> touchAreas;

    public static final int SHOW_NONE = 0;
    public static final int SHOW_INVENTORY = 1;
    public static final int SHOW_COST = 2;

    public Keyboard(final float pX, final float pY, final float width, final float height, final PhoeniciaGame game) {
        this(pX, pY, width, height, game, 0);
    }

    public Keyboard(final float pX, final float pY, final float width, final float height, final PhoeniciaGame game, final int details) {
        super(pX, pY, width, height);
        this.game = game;
        this.details = details;

        this.inventoryCounts = new HashMap<String, Text>();
        this.usedCounts = new HashMap<String, Integer>();
        this.touchAreas = new ArrayList<ITouchArea>();

        /**
         * Start available letters area
         */
        final int columns = 8;
        float startX = 50;

        int offsetX = 0;
        int offsetY = (int) height-32;

        for (int i = 0; i < game.locale.letters.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 80;
                offsetX = 0;
            }
            final Letter currentLetter = game.locale.letters.get(i);
            Debug.d("Adding Builder letter: " + currentLetter.name + " (column: " + offsetX + ")");
            final int tile_id = currentLetter.sprite;
            final ITiledTextureRegion blockRegion = game.letterSprites.get(currentLetter);
            final ButtonSprite block = new ButtonSprite(startX + (96 * offsetX), offsetY, blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    if (listener != null) {
                        listener.onKeyClicked(currentLetter);
                    }
                }
            });
            this.registerTouchArea(block);
            this.attachChild(block);

            Level level = game.locale.level_map.get(game.current_level);
            if (level.letters.contains(currentLetter)) {
                if (this.details == this.SHOW_INVENTORY) {
                        Debug.d("Checking inventory for " + currentLetter.name);
                        Debug.d("Inventory says: " + this.game.inventory.getCount(currentLetter.name));
                        final Text inventoryCount = new Text(startX + (96 * offsetX) + 20, offsetY - 40, GameFonts.inventoryCount(), "" + this.game.inventory.getCount(currentLetter.name), 4, PhoeniciaContext.vboManager);
                        this.attachChild(inventoryCount);
                        this.inventoryCounts.put(currentLetter.name, inventoryCount);
                        this.usedCounts.put(currentLetter.name, 0);
                } else if (this.details == this.SHOW_COST) {
                    final Text letterCost = new Text(startX + (96 * offsetX) - 20, offsetY - 40, GameFonts.itemCost() , "" + currentLetter.buy, 4, PhoeniciaContext.vboManager);
                    this.attachChild(letterCost);
                    this.inventoryCounts.put(currentLetter.name, letterCost);
                }
            } else {
                block.setEnabled(false);
            }

            offsetX++;
        }

    }

    public void registerTouchArea(ITouchArea area) {
        this.touchAreas.add(area);
    }
    public void unregisterTouchArea(ITouchArea area) {
        if (this.touchAreas.contains(area)) this.touchAreas.remove(area);
    }

    @Override
    public boolean onAreaTouched(final TouchEvent pTouchEvent, final float touchX, final float touchY) {
        final float sceneTouchX = pTouchEvent.getX();
        final float sceneTouchY = pTouchEvent.getY();
        for (ITouchArea area: this.touchAreas) {
            if (area.contains(sceneTouchX, sceneTouchY)) {
                final float[] areaTouchCoordinates = area.convertSceneCoordinatesToLocalCoordinates(sceneTouchX, sceneTouchY);
                final float areaTouchX = areaTouchCoordinates[Constants.VERTEX_INDEX_X];
                final float areaTouchY = areaTouchCoordinates[Constants.VERTEX_INDEX_Y];
                final Boolean ishandled = area.onAreaTouched(pTouchEvent, areaTouchX, areaTouchY);
                if (ishandled != null && ishandled) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setOnKeyClickListener(KeyClickListener listener) {
        this.listener = listener;
    }

    public void unsetOnKeyClickListener() {
        this.listener = null;
    }

    public interface KeyClickListener {
        public void onKeyClicked(Letter l);
    }
}
