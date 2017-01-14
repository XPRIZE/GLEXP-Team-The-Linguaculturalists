package com.linguaculturalists.phoenicia.components;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.primitive.vbo.IRectangleVertexBufferObject;
import org.andengine.opengl.vbo.IVertexBufferObject;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 1/12/17.
 */
public class BorderRectangle extends Rectangle {
    private Rectangle border;
    private boolean detatchBorderColor = false;

    public BorderRectangle(float pX, float pY, float pWidth, float pHeight, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pWidth, pHeight, pVertexBufferObjectManager);
        this.border = new Rectangle(this.getWidth()/2, this.getHeight()/2, this.getWidth()+4, this.getHeight()+4, pVertexBufferObjectManager);
        this.border.setZIndex(this.getZIndex()-1);
        this.border.setColor(0.3f, 0.3f, 0.3f);
        this.attachChild(border);
    }

    public void setBorderColor(float r, float g, float b) {
        this.border.setColor(r, g, b);
        this.detatchBorderColor = true;
    }

    public void setBorderColor(Color c) {
        this.border.setColor(c);
        this.detatchBorderColor = true;
    }

    @Override
    protected void onUpdateColor() {
        super.onUpdateColor();
        if (this.border != null && !this.detatchBorderColor) {
            this.border.setColor(this.getRed() * 0.3f, this.getGreen() * 0.3f, this.getBlue() * 0.3f);
        }
    }

    @Override
    public void setWidth(float pWidth) {
        super.setWidth(pWidth);
        if (this.border != null) {
            this.border.setWidth(pWidth + 4);
            this.border.setX(this.getWidth()/2);
        }
    }

    @Override
    public void setHeight(float pHeight) {
        super.setHeight(pHeight);
        if (this.border != null) {
            this.border.setHeight(pHeight + 4);
            this.border.setY(this.getHeight()/2);
        }
    }

    @Override
    public void detachChildren() {
        super.detachChildren();
        this.attachChild(border);
    }
}
