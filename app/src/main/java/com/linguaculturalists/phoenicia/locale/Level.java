package com.linguaculturalists.phoenicia.locale;

import android.content.Context;

import org.andengine.util.debug.Debug;

import java.util.List;
import java.util.Map;

/**
* Model for storing a definition of a game level.
*/
public class Level {
    public String name; /**< reference name for this level */
    public int marketRequests; /**< number of market requests available */
    public int coinsEarned; /**< number coins gained by reaching this level */
    public List<Letter> letters; /**< list of \link Letter Letters \endlink available in this level */
    public Map<Letter, Integer> letter_count; /**< number of times this letter has been introduced */
    public List<Word> words; /**< list of \link Word Words \endlink available in this level */
    public Map<Word, Integer> word_count; /**< number of times this word has been introduced */
    public List<Game> games; /**< list of \link Game Games \endlink available in this level */
    public List<Letter> help_letters; /**< list of \link Letter Letters \endlink to give extra help with on this level */
    public List<Word> help_words; /**< list of \link Word Words \endlink to give extra help with on this level */
    public List<IntroPage> intro; /**< list of \link IntroPage IntroPages \endlink to be displayed at the start of this level */
    public List<Requirement> requirements; /**< list of \link Requirement Requirements \endlink that must be fulfilled to move on to the next level */

    /**
     * See if all #requirements for this level have been fulfilled
     * @param context ApplicationContext needed for database access
     * @return true if all requirements pass, otherwise false
     */
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
