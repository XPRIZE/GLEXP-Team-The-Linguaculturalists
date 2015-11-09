package com.linguaculturalists.phoenicia.locale;

import android.content.Context;

import com.linguaculturalists.phoenicia.models.Inventory;

import org.andengine.util.debug.Debug;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhall on 11/1/15.
 */
public class CollectLetterReq implements Requirement {

    private List<Letter> letters;
    private int count;

    public CollectLetterReq() {
        this(new ArrayList<Letter>(), 0);
    }
    public CollectLetterReq(List<Letter> letters, int count) {
        this.letters = letters;
        this.count = count;
    }

    public List<Letter> getLetters() {
        return letters;
    }

    public void addLetter(Letter l) {
        letters.add(l);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean check(Context context) {
        Debug.d("Checking letter history for " + this.letters.toString() + " is " + count);
        int total = 0;
        for (int i = 0; i < letters.size(); i++) {
            total += Inventory.getInstance().getHistory(letters.get(i).name);
        }
        return total >= count;
    }
}
