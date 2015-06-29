package com.linguaculturalists.phoenicia.models;

import android.content.Context;

import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.IntegerField;
/**
 * Created by mhall on 6/19/15.
 */
public class PlacedBlock extends Model {

    public IntegerField isoX;
    public IntegerField isoY;
    public IntegerField tileId;

    public PlacedBlock() {
        super();

        isoX = new IntegerField();
        isoY = new IntegerField();
        tileId = new IntegerField();

    }

    public static final QuerySet<PlacedBlock> objects(Context context) {
        return objects(context, PlacedBlock.class);
    }
}
