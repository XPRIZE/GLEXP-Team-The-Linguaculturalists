package com.linguaculturalists.phoenicia.util;

import android.content.Context;
import android.content.res.AssetManager;

import com.linguaculturalists.phoenicia.GameActivity;

import org.andengine.audio.music.MusicManager;
import org.andengine.audio.sound.SoundManager;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Utiltiy class for quick access to common Android, AndEngine and AndrOrm state varaibles.
 *
 * Must be populated at the start of the game activity.
 */
public class PhoeniciaContext {
    public static Context context; /**< Application context, used for database calls */
    public static GameActivity activity; /**< Reference to the Android Activity the game is runing in */
    public static TextureManager textureManager; /**< Common AndEngine TextureManager used throughout the game */
    public static AssetManager assetManager; /**< Common Android AssetManager used throughout the game */
    public static VertexBufferObjectManager vboManager; /**< Common AndEngine VBO used throughout the game */
    public static SoundManager soundManager; /**< Common AndEngine SoundManager used throughout the game */
    public static FontManager fontManager; /**< Common AndEngine FontManager used throughout the game */
    public static MusicManager musicManager; /**< Commont AndEngine MusicManager used throughout the game */
}
