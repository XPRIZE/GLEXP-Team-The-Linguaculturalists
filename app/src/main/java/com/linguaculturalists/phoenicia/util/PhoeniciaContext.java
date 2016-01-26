package com.linguaculturalists.phoenicia.util;

import android.content.Context;
import android.content.res.AssetManager;

import com.linguaculturalists.phoenicia.GameActivity;

import org.andengine.audio.sound.SoundManager;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Utiltiy class for quick access to common Android, AndEngine and AndrOrm state varaibles.
 */
public class PhoeniciaContext {
    public static Context context;
    public static GameActivity activity;
    public static TextureManager textureManager;
    public static AssetManager assetManager;
    public static VertexBufferObjectManager vboManager;
    public static SoundManager soundManager;
    public static FontManager fontManager;

}
