package com.linguaculturalists.phoenicia.locale;

import android.content.Context;

import com.linguaculturalists.phoenicia.models.Inventory;

import org.andengine.util.debug.Debug;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if a specified number of letters has been collected by the player
 */
public class CollectLetterReq implements Requirement {

    private List<Letter> letters;
    private int count;

    public CollectLetterReq() {
        this(new ArrayList<Letter>(), 0);
    }

    /**
     * New requirement that \a count letters from the list \a letters have been collected
     *
     * @param letters set of letters which can fulfill this requirement
     * @param count number of letters, in any combination from the set, required to pass
     */
    public CollectLetterReq(List<Letter> letters, int count) {
        this.letters = letters;
        this.count = count;
    }

    /**
     * Return the set of letters being checked
     * @return List of letters
     */
    public List<Letter> getLetters() {
        return letters;
    }

    /**
     * Adds the new letter \a l to the set of letters which can fulfill this requirement
     * @param l letter to add
     */
    public void addLetter(Letter l) {
        letters.add(l);
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
        Debug.d("Checking letter history for " + this.letters.toString() + " is " + count);
        int total = 0;
        for (int i = 0; i < letters.size(); i++) {
            total += Inventory.getInstance().getHistory(letters.get(i).name);
        }
        return total >= count;
    }
}
