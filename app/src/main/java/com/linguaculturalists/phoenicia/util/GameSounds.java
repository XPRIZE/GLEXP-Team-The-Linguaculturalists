package com.linguaculturalists.phoenicia.util;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mhall on 7/31/16.
 */
public class GameSounds {
    public static final String COLLECT = "sounds/collect.ogg";
    public static final String COMPLETE = "sounds/completed.ogg";
    public static final String FAILED = "sounds/negative.ogg";
    private static Map<String, Sound> sounds;

    public static void init() throws IOException {
        GameSounds.sounds = new HashMap<String, Sound>();
        GameSounds.sounds.put(COLLECT, SoundFactory.createSoundFromAsset(PhoeniciaContext.soundManager, PhoeniciaContext.context, COLLECT));
        GameSounds.sounds.put(COMPLETE, SoundFactory.createSoundFromAsset(PhoeniciaContext.soundManager, PhoeniciaContext.context, COMPLETE));
        GameSounds.sounds.put(FAILED, SoundFactory.createSoundFromAsset(PhoeniciaContext.soundManager, PhoeniciaContext.context, FAILED));
    }

    public static void play(String sound) {
        if (GameSounds.sounds != null && GameSounds.sounds.containsKey(sound)) {
            GameSounds.sounds.get(sound).play();
        }
    }
}
