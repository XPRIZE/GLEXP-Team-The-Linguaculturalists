package com.linguaculturalists.phoenicia;

import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mhall on 7/17/15.
 */
public class Locale {

    public String name;
    public String display_name;
    public String lang;

    public String map_src;
    public String shell_src;

    public String letters_texture;
    public List<Letter> letters;
    public Map<String, Letter> letter_map;

    public String words_texture;
    public List<Word> words;
    public Map<String, Word> word_map;

    public List<Level> levels;
    public Map<String, Level> level_map;

    public Locale() {
        this.letters = new ArrayList<Letter>();
        this.letter_map = new HashMap<String, Letter>();

        this.words = new ArrayList<Word>();
        this.word_map = new HashMap<String, Word>();

        this.levels = new ArrayList<Level>();
        this.level_map = new HashMap<String, Level>();
    }
}
