package com.linguaculturalists.phoenicia.locale;

import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model for storing information about a GameSession's locale.
 */
public class Locale {

    public String name;
    public String display_name;
    public String lang;

    public String map_src;
    public String shell_src;

    public String music_src;

    public InventoryBlock inventoryBlock;
    public MarketBlock marketBlock;
    public WorkshopBlock workshopBlock;

    public List<Person> people;
    public Map<String, Person> person_map;

    public List<Game> games;
    public Map<String, Game> game_map;

    public List<Decoration> decorations;
    public Map<String, Decoration> decoration_map;

    public String letters_texture;
    public List<Letter> letters;
    public Map<String, Letter> letter_map;

    public String words_texture;
    public List<Word> words;
    public Map<String, Word> word_map;

    public List<Level> levels;
    public Map<String, Level> level_map;

    public Locale() {
        this.people = new ArrayList<Person>();
        this.person_map = new HashMap<String, Person>();

        this.games = new ArrayList<Game>();
        this.game_map = new HashMap<String, Game>();

        this.decorations = new ArrayList<Decoration>();
        this.decoration_map = new HashMap<String, Decoration>();

        this.letters = new ArrayList<Letter>();
        this.letter_map = new HashMap<String, Letter>();

        this.words = new ArrayList<Word>();
        this.word_map = new HashMap<String, Word>();

        this.levels = new ArrayList<Level>();
        this.level_map = new HashMap<String, Level>();
    }
}
