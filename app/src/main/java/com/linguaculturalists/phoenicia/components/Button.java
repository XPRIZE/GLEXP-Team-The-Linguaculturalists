package com.linguaculturalists.phoenicia.components;

import com.linguaculturalists.phoenicia.util.GameFonts;

import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 3/25/16.
 */
public class Button extends Entity implements ClickDetector.IClickDetectorListener {

    private ClickDetector clickDetector;
    private OnClickListener clickListener;
    private Rectangle background;
    private Text buttonText;

    public Button(float x, float y, float w, float h, String text, VertexBufferObjectManager pVertexBufferObjectManager, OnClickListener pOnClickListener) {
        super(x, y, w, h);
        this.clickDetector = new ClickDetector(this);
        this.clickListener = pOnClickListener;

        this.background = new Rectangle(this.getWidth()/2, this.getHeight()/2, w, h, pVertexBufferObjectManager);
        this.background.setColor(Color.BLUE);
        this.attachChild(this.background);

        final Font buttonFont = GameFonts.buttonText();
        this.buttonText = new Text(this.getWidth()/2, this.getHeight()/2, buttonFont, text, text.length(), new TextOptions(HorizontalAlign.CENTER), pVertexBufferObjectManager);
        this.attachChild(this.buttonText);
    }

    @Override
    public boolean onAreaTouched(final TouchEvent pTouchEvent, final float touchX, final float touchY) {
        return this.clickDetector.onManagedTouchEvent(pTouchEvent);
    }

    @Override
    public void onClick(ClickDetector clickDetector, int i, float clickX, float clickY) {
        Debug.d("Button clicked!");
        if (this.clickListener != null) {
            this.clickListener.onClicked(this);
        }
    }

    public interface OnClickListener {
        public void onClicked(Button button);
    }
}
