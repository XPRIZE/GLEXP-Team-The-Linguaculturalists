package com.linguaculturalists.phoenicia.locale;

import android.content.Context;

import com.linguaculturalists.phoenicia.models.Inventory;

import org.andengine.util.debug.Debug;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhall on 11/1/15.
 */
public class CollectWordReq implements Requirement {

    private List<Word> words;
    private int count;

    public CollectWordReq() {
        this(new ArrayList<Word>(), 0);
    }
    public CollectWordReq(List<Word> words, int count) {
        this.words = words;
        this.count = count;
    }

    public List<Word> getWords() {
        return words;
    }

    public void addWord(Word w) {
        words.add(w);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean check(Context context) {
        Debug.d("Checking word history for " + this.words.toString() + " is " + count);
        int total = 0;
        for (int i = 0; i < words.size(); i++) {
            total += Inventory.getInstance().getHistory(words.get(i).name);
        }
        return total >= count;
    }
}
