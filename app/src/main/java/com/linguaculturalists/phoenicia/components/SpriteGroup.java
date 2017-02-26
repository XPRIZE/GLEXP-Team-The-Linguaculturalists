package com.linguaculturalists.phoenicia.components;

import org.andengine.entity.Entity;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Created by mhall on 2/26/17.
 */
public class SpriteGroup extends Entity {
    private VertexBufferObjectManager vertexBufferObjectManager;
    private ButtonSprite.OnClickListener clickListener;
    private ClickDetector clickDetector;

    public SpriteGroup(final float pX, final float pY, final float width, final float height, final Sprite srcSprite, final int quantity, final VertexBufferObjectManager vertexBufferObjectManager) {
        this(pX, pY, width, height, srcSprite, quantity, vertexBufferObjectManager, null);
    }
    public SpriteGroup(final float pX, final float pY, final float width, final float height, final Sprite srcSprite, final int quantity, final VertexBufferObjectManager vertexBufferObjectManager, ButtonSprite.OnClickListener listener) {
        super(pX, pY, width, height);
        this.vertexBufferObjectManager = vertexBufferObjectManager;
        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                if (clickListener != null) {
                    clickListener.onClick(null, v, v1);
                }
            }
        });
        if (listener != null) {
            this.clickListener = listener;
        }

        switch(quantity) {
            case 1:
                this.doLayout1(srcSprite); break;
            case 2:
                this.doLayout2(srcSprite); break;
            case 3:
                this.doLayout3(srcSprite); break;
            case 4:
                this.doLayout4(srcSprite); break;
            case 5:
                this.doLayout5(srcSprite); break;
            case 6:
                this.doLayout6(srcSprite); break;
            case 7:
                this.doLayout7(srcSprite); break;
            case 8:
                this.doLayout8(srcSprite); break;
            case 9:
                this.doLayout9(srcSprite); break;
        }
    }

    private void doGenericLayout(final Sprite sprite, final int quantity) {
        // TODO use domino-style layouts instead of simple linear layout
        float scale = 1f / (float)quantity;
        for (int i = 0; i < quantity; i++) {
            float spriteX = (i * (this.getWidth()/quantity)) + ((this.getWidth()/quantity)/2);
            float spriteY = this.getHeight()/2;
            Sprite subSprite = new Sprite(spriteX, spriteY, sprite.getTextureRegion(), this.vertexBufferObjectManager);
            subSprite.setScale(scale);
            this.attachChild(subSprite);
        }
    }

    private void doLayout1(final Sprite sprite) {
        this.doGenericLayout(sprite, 1);
    }

    private void doLayout2(final Sprite sprite) {
        this.doGenericLayout(sprite, 2);
    }

    private void doLayout3(final Sprite sprite) {
        float scale = 0.5f;
        float[][] layout ={
                {(this.getWidth()/2), (this.getHeight()/2)+(this.getHeight()/4)},
                {(this.getWidth()/2)-(this.getWidth()/4), (this.getHeight()/2)-(this.getHeight()/4)},
                {(this.getWidth()/2)+(this.getWidth()/4), (this.getHeight()/2)-(this.getHeight()/4)}
        };
        for (int i = 0; i < 3; i++) {
            Sprite subSprite = new Sprite(layout[i][0], layout[i][1], sprite.getTextureRegion(), this.vertexBufferObjectManager);
            subSprite.setScale(scale);
            this.attachChild(subSprite);
        }
    }

    private void doLayout4(final Sprite sprite) {
        float scale = 0.5f;
        float[][] layout ={
                {(this.getWidth()/2)-(this.getWidth()/4), (this.getHeight()/2)+(this.getHeight()/4)},
                {(this.getWidth()/2)+(this.getWidth()/4), (this.getHeight()/2)+(this.getHeight()/4)},
                {(this.getWidth()/2)-(this.getWidth()/4), (this.getHeight()/2)-(this.getHeight()/4)},
                {(this.getWidth()/2)+(this.getWidth()/4), (this.getHeight()/2)-(this.getHeight()/4)}
        };
        for (int i = 0; i < 4; i++) {
            Sprite subSprite = new Sprite(layout[i][0], layout[i][1], sprite.getTextureRegion(), this.vertexBufferObjectManager);
            subSprite.setScale(scale);
            this.attachChild(subSprite);
        }
    }

    private void doLayout5(final Sprite sprite) {
        float scale = 0.4f;
        float[][] layout ={
                {(this.getWidth()/2)-(this.getWidth()/4), (this.getHeight()/2)+(this.getHeight()/4)},
                {(this.getWidth()/2)+(this.getWidth()/4), (this.getHeight()/2)+(this.getHeight()/4)},
                {(this.getWidth()/2)-(this.getWidth()/4), (this.getHeight()/2)-(this.getHeight()/4)},
                {(this.getWidth()/2)+(this.getWidth()/4), (this.getHeight()/2)-(this.getHeight()/4)},
                {(this.getWidth()/2), (this.getHeight()/2)}
        };
        for (int i = 0; i < 5; i++) {
            Sprite subSprite = new Sprite(layout[i][0], layout[i][1], sprite.getTextureRegion(), this.vertexBufferObjectManager);
            subSprite.setScale(scale);
            this.attachChild(subSprite);
        }
    }

    private void doLayout6(final Sprite sprite) {
        this.doGenericLayout(sprite, 6);
    }

    private void doLayout7(final Sprite sprite) {
        this.doGenericLayout(sprite, 7);
    }

    private void doLayout8(final Sprite sprite) {
        this.doGenericLayout(sprite, 8);
    }

    private void doLayout9(final Sprite sprite) {
        this.doGenericLayout(sprite, 9);
    }

    @Override
    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
    }
}
