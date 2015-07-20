package com.linguaculturalists.phoenicia;

import java.util.List;
import java.util.Map;

/**
 * Created by mhall on 7/17/15.
 */
public class Locale {

    public class Letter {
        public String name;
        public int time;
        public int tile;
        public int sprite;
        public String sound;
        public String phoneme;
        public char[] chars;
    }
    public class Word {
        public String name;
        public int time;
        public int tile;
        public int sprite;
        public String sound;
        public char[] chars;
    }
    public class Level{
        public String name;
        public List<Letter> letters;
        public List<Word> words;
        public List<Letter> help_letters;
        public List<Word> help_words;
    }

    public String map_src;

    public String letters_texture;
    public List<Letter> letters;
    public Map<String, Letter> letter_map;

    public String words_texture;
    public List<Word> words;
    public Map<String, Word> word_map;

    public List<Level> levels;
    public Map<String, Level> level_map;

}
