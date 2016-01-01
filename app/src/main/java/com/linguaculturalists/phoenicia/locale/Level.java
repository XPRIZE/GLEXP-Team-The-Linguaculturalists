package com.linguaculturalists.phoenicia.locale;

import android.content.Context;

import org.andengine.util.debug.Debug;

import java.util.List;

/**
* Created by mhall on 7/21/15.
*/
public class Level {
    public String name;
    public List<Letter> letters;
    public List<Word> words;
    public List<Letter> help_letters;
    public List<Word> help_words;
    public List<IntroPage> intro;
    public List<Requirement> requirements;

    public boolean check(Context context) {
        Debug.d("Checking if player passes level " + name);
        for (int i = 0; i < requirements.size(); i++) {
            Debug.d("Checking "+requirements.get(i));
            if (!requirements.get(i).check(context)) {
                // abort on the first requirement failure
                return false;
            }
        }
        return true;
    }
}
