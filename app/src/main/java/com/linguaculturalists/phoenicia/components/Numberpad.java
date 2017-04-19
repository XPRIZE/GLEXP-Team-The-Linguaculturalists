package com.linguaculturalists.phoenicia.components;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Number;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.models.Assets;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.util.debug.Debug;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mhall on 1/11/17.
 */
public class Numberpad extends Scrollable {
    private PhoeniciaGame game;
    private int details;
    private KeyClickListener listener;

    public Numberpad(final float pX, final float pY, final float width, final float height, final PhoeniciaGame game) {
        super(pX, pY, width, height);
        this.game = game;

        /**
         * Start available numbers area
         */
        final int columns = 5;
        float startX = 50;

        int offsetX = 0;
        int offsetY = (int) height-32;

        for (int i = 0; i < game.locale.numbers.size(); i++) {
            if (offsetX >= columns) {
                offsetY -= 80;
                offsetX = 0;
            }
            final Number currentNumber = game.locale.numbers.get(i);
            Debug.d("Adding Numberpad number: " + currentNumber.name + " (column: " + offsetX + ")");
            final ITiledTextureRegion blockRegion = game.numberSprites.get(currentNumber);
            final ButtonSprite block = new ButtonSprite(startX + (96 * offsetX), offsetY, blockRegion, PhoeniciaContext.vboManager);
            block.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite buttonSprite, float v, float v2) {
                    if (listener != null) {
                        listener.onKeyClicked(currentNumber);
                    }
                }
            });
            this.registerTouchArea(block);
            this.attachChild(block);

            offsetX++;
        }

    }

    public void setOnKeyClickListener(KeyClickListener listener) {
        this.listener = listener;
    }

    public void unsetOnKeyClickListener() {
        this.listener = null;
    }

    public interface KeyClickListener {
        public void onKeyClicked(Number l);
    }
}
