package com.linguaculturalists.phoenicia.components;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.primitive.vbo.IRectangleVertexBufferObject;
import org.andengine.opengl.vbo.IVertexBufferObject;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;

/**
 * Created by mhall on 1/12/17.
 */
public class BorderRectangle extends Rectangle {
    private Rectangle border;
    public BorderRectangle(float pX, float pY, float pWidth, float pHeight, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pWidth, pHeight, pVertexBufferObjectManager);
        this.border = new Rectangle(this.getWidth()/2, this.getHeight()/2, this.getWidth()+4, this.getHeight()+4, pVertexBufferObjectManager);
        this.border.setZIndex(this.getZIndex()-1);
        this.border.setColor(0.3f, 0.3f, 0.3f);
        this.attachChild(border);
    }

    @Override
    protected void onUpdateColor() {
        super.onUpdateColor();
        if (this.border != null) {
            this.border.setColor(this.getRed() * 0.3f, this.getGreen() * 0.3f, this.getBlue() * 0.3f);
        }
    }

}
