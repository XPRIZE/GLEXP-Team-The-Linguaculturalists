package com.linguaculturalists.phoenicia.locale;

import android.content.Context;

import com.linguaculturalists.phoenicia.models.Inventory;

import org.andengine.util.debug.Debug;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if a specified number of words has been collected by the player.
 */
public class CollectWordReq implements Requirement {

    private List<Word> words;
    private int count;

    public CollectWordReq() {
        this.words = new ArrayList<Word>();
        this.count = 0;
    }

    /**
     * New requirement that \a count words from the list \a words have been collected
     *
     * @param words set of words which can fulfill this requirement
     * @param count number of letters, in any combination from the set, required to pass
     */
    public CollectWordReq(List<Word> words, int count) {
        this.words = words;
        this.count = count;
    }

    /**
     * Return the set of words being checked
     * @return List of words
     */
    public List<Word> getWords() {
        return words;
    }

    /**
     * Adds the new word \a w to the set of words which can fulfill this requirement
     * @param w word to add
     */
    public void addWord(Word w) {
        words.add(w);
    }

    /**
     * Return the minimum number of letters from the set needed to fulfill this requirement
     * @return number required
     */
    public int getCount() {
        return count;
    }

    /**
     * Set the minimum number of letters from the set needed to fulfill this requirement
     * @param count number required
     */
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean check(Context context) {
        Debug.d("Checking word history for " + this.words.toString() + " is " + count);
        int total = 0;
        for (int i = 0; i < words.size(); i++) {
            total += Inventory.getInstance().getHistory(words.get(i).name);
        }
        return total >= count;
    }
}
