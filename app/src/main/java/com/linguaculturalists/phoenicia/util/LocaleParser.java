package com.linguaculturalists.phoenicia.util;

import com.linguaculturalists.phoenicia.Locale;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Word;

import org.andengine.util.debug.Debug;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Created by mhall on 7/17/15.
 */
public class LocaleParser extends DefaultHandler {

    private static final String TAG_LOCALE = "locale";
    private static final String TAG_MAP = "map";
    private static final String TAG_LETTERS = "letters";
    private static final String TAG_LETTER = "letter";
    private static final String TAG_WORDS = "words";
    private static final String TAG_WORD = "word";
    private static final String TAG_LEVELS = "levels";
    private static final String TAG_LEVEL = "level";
    private static final String TAG_HELP = "help";
    private boolean inLocale = false;
    private boolean inLettersList = false;
    private boolean inLetterDefinition = false;
    private boolean inWordsList = false;
    private boolean inWordDefinition = false;
    private boolean inLevelsList = false;
    private boolean inLevelDefinition = false;
    private boolean inLevelLetters = false;
    private boolean inLevelWords = false;
    private boolean inLevelHelp = false;
    private boolean inLevelHelpLetters = false;
    private boolean inLevelHelpWords = false;
    private Letter currentLetter;
    private Word currentWord;
    private Level currentLevel;

    private Locale locale;

    public LocaleParser() {
        super();
    }



    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Debug.v("Parser start: "+localName);
        if (localName.equals(LocaleParser.TAG_LOCALE)) {
            this.inLocale = true;
            this.parseLocale(attributes);
        } else if (this.inLocale && localName.equals(LocaleParser.TAG_MAP)) {
            this.parseMap(attributes);
        } else if (this.inLocale && !this.inLevelDefinition && localName.equals(LocaleParser.TAG_LETTERS)) {
            if (!this.inLevelDefinition) {
                this.inLettersList = true;
                this.locale.letter_src = attributes.getValue("texture");
            }
        } else if (this.inLocale && this.inLettersList && localName.equals(LocaleParser.TAG_LETTER)) {
            this.inLetterDefinition = true;
            this.parseLetterDefinition(attributes);
        } else if (this.inLocale && !this.inLevelDefinition && localName.equals(LocaleParser.TAG_WORDS)) {
            if (!this.inLevelDefinition) {
                this.inWordsList = true;
            }
            this.locale.word_src = attributes.getValue("texture");
        } else if (this.inLocale && this.inWordsList && localName.equals(LocaleParser.TAG_WORD)) {
            this.inWordDefinition = true;
            this.parseWordDefinition(attributes);
        } else if (this.inLocale && localName.equals(LocaleParser.TAG_LEVELS)) {
            this.inLevelsList = true;
        } else if (this.inLocale && this.inLevelsList && localName.equals(LocaleParser.TAG_LEVEL)) {
            this.inLevelDefinition = true;
            this.parseLevel(attributes);
        } else if (this.inLocale && this.inLevelDefinition && localName.equals(LocaleParser.TAG_LETTERS)) {
            this.inLevelLetters = true;
        } else if (this.inLocale && this.inLevelDefinition && localName.equals(LocaleParser.TAG_HELP)) {
            this.inLevelHelp = true;
        } else if (this.inLocale && this.inLevelDefinition && localName.equals(LocaleParser.TAG_WORDS)) {
            this.inLevelWords = true;
        } else if (this.inLocale && this.inLevelDefinition && this.inLevelHelp && localName.equals(LocaleParser.TAG_LETTERS)) {
            this.inLevelHelpLetters = true;
        } else if (this.inLocale && this.inLevelDefinition && this.inLevelHelp && localName.equals(LocaleParser.TAG_WORDS)) {
            this.inLevelHelpWords = true;
        }
    }

    private void parseLocale(Attributes attributes) throws SAXException {
        this.locale = new Locale();
        this.locale.name = attributes.getValue("name");
        this.locale.display_name = attributes.getValue("display_name");
        this.locale.lang = attributes.getValue("lang");
    }

    private void parseMap(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale map");
        this.locale.map_src = attributes.getValue("src");
    }

    private void parseLetterDefinition(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale letter");
        this.currentLetter = new Letter();
        this.currentLetter.name = attributes.getValue("name");
        this.currentLetter.sound = attributes.getValue("sound");
        this.currentLetter.phoneme = attributes.getValue("phoneme");
        this.currentLetter.sprite = Integer.parseInt(attributes.getValue("sprite"));
        this.currentLetter.tile = Integer.parseInt(attributes.getValue("tile"));
        this.currentLetter.time = Integer.parseInt(attributes.getValue("time"));
    }

    private void parseWordDefinition(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale word");
        this.currentWord = new Word();
        this.currentWord.name = attributes.getValue("name");
        this.currentWord.sound = attributes.getValue("sound");
        this.currentWord.sprite = Integer.parseInt(attributes.getValue("sprite"));
        this.currentWord.tile = Integer.parseInt(attributes.getValue("tile"));
        this.currentWord.time = Integer.parseInt(attributes.getValue("time"));
    }

    private void parseLevel(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale level");
        this.currentLevel = new Level();
        this.currentLevel.name = attributes.getValue("name");
        this.currentLevel.letters = new ArrayList<Letter>();
        this.currentLevel.words = new ArrayList<Word>();
        this.currentLevel.help_letters = new ArrayList<Letter>();
        this.currentLevel.help_words = new ArrayList<Word>();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        Debug.v("Parser end: "+localName);
        if (localName.equals(LocaleParser.TAG_LOCALE)) {
            this.inLocale = false;
        } else if (this.inLocale && !this.inLevelDefinition && localName.equals(LocaleParser.TAG_LETTERS)) {
            if (!this.inLevelDefinition) {
                this.inLettersList = false;
            }
        } else if (this.inLocale && this.inLetterDefinition && localName.equals(LocaleParser.TAG_LETTER)) {
            this.locale.letters.add(this.currentLetter);
            this.locale.letter_map.put(this.currentLetter.name, this.currentLetter);
            this.currentLetter = null;
            this.inLetterDefinition = false;
        } else if (this.inLocale && !this.inLevelDefinition && localName.equals(LocaleParser.TAG_WORDS)) {
            if (!this.inLevelDefinition) {
                this.inWordsList = false;
            }
        } else if (this.inLocale && this.inWordDefinition && localName.equals(LocaleParser.TAG_WORD)) {
            this.locale.words.add(this.currentWord);
            this.locale.word_map.put(this.currentWord.name, this.currentWord);
            this.currentWord = null;
            this.inWordDefinition = false;
        } else if (this.inLocale && this.inLevelDefinition && localName.equals(LocaleParser.TAG_LEVEL)) {
            this.locale.levels.add(this.currentLevel);
            this.locale.level_map.put(this.currentLevel.name, this.currentLevel);
            this.currentLevel = null;
            this.inLevelDefinition = false;
        } else if (this.inLocale && this.inLevelDefinition && localName.equals(LocaleParser.TAG_LETTERS)) {
            this.inLevelLetters = false;
        } else if (this.inLocale && this.inLevelDefinition && localName.equals(LocaleParser.TAG_WORDS)) {
            this.inLevelWords = false;
        } else if (this.inLocale && this.inLevelDefinition && this.inLevelHelp && localName.equals(LocaleParser.TAG_LETTERS)) {
            this.inLevelHelpLetters = false;
        } else if (this.inLocale && this.inLevelDefinition && this.inLevelHelp && localName.equals(LocaleParser.TAG_WORDS)) {
            this.inLevelHelpWords = false;
        } else if (this.inLocale && this.inLevelHelp && localName.equals(LocaleParser.TAG_HELP)) {
            this.inLevelHelp = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String text = new String(ch, start, length);
        if (this.inLocale && this.inLettersList && this.inLetterDefinition) {
            Debug.v("Adding Letter chars: "+text);
            this.currentLetter.chars = text.toCharArray();
        } else if (this.inLocale && this.inWordsList && this.inLetterDefinition) {
            Debug.v("Adding Word chars: "+text);
            this.currentWord.chars = text.toCharArray();
        } else if (this.inLocale && this.inLevelDefinition && !this.inLevelHelp && this.inLevelLetters) {
            String[] letters =  StringUtils.split(text, ",");
            for (int i = 0; i < letters.length; i++) {
                Debug.v("Adding Letter "+letters[i]+" to Level "+this.currentLevel.name);
                this.currentLevel.letters.add(this.locale.letter_map.get(letters[i]));
            }
        } else if (this.inLocale && this.inLevelDefinition && !this.inLevelHelp && this.inLevelWords) {
            String[] words =  StringUtils.split(text, ",");
            for (int i = 0; i < words.length; i++) {
                Debug.v("Adding Word "+words[i]+" to Level "+this.currentLevel.name);
                this.currentLevel.words.add(this.locale.word_map.get(words[i]));
            }
        } else if (this.inLocale && this.inLevelDefinition && this.inLevelHelp && this.inLevelHelpLetters) {
            String[] letters =  StringUtils.split(text, ",");
            for (int i = 0; i < letters.length; i++) {
                Debug.v("Adding help Letter "+letters[i]+" to Level "+this.currentLevel.name);
                this.currentLevel.help_letters.add(this.locale.letter_map.get(letters[i]));
            }
        } else if (this.inLocale && this.inLevelDefinition && this.inLevelHelp && this.inLevelHelpWords) {
            String[] words =  StringUtils.split(text, ",");
            for (int i = 0; i < words.length; i++) {
                Debug.v("Adding help Word "+words[i]+" to Level "+this.currentLevel.name);
                this.currentLevel.help_words.add(this.locale.word_map.get(words[i]));
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }
}
