package com.linguaculturalists.phoenicia.util;

import com.linguaculturalists.phoenicia.Locale;
import com.linguaculturalists.phoenicia.locale.CollectLetterReq;
import com.linguaculturalists.phoenicia.locale.CollectWordReq;
import com.linguaculturalists.phoenicia.locale.IntroPage;
import com.linguaculturalists.phoenicia.locale.Letter;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.Requirement;
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
    private static final String TAG_SHELL = "shell";
    private static final String TAG_LETTERS = "letters";
    private static final String TAG_LETTER = "letter";
    private static final String TAG_WORDS = "words";
    private static final String TAG_WORD = "word";
    private static final String TAG_LEVELS = "levels";
    private static final String TAG_LEVEL = "level";
    private static final String TAG_INTRO = "intro";
    private static final String TAG_PAGE = "page";
    private static final String TAG_HELP = "help";
    private static final String TAG_REQ = "req";
    private static final String TAG_REQLETTER = "gather_letter";
    private static final String TAG_REQWORD = "gather_word";
    private boolean inLocale = false;
    private boolean inLettersList = false;
    private boolean inLetterDefinition = false;
    private boolean inWordsList = false;
    private boolean inWordDefinition = false;
    private boolean inLevelsList = false;
    private boolean inLevelDefinition = false;
    private boolean inLevelIntro = false;
    private boolean inLevelIntroPage = false;
    private boolean inLevelLetters = false;
    private boolean inLevelWords = false;
    private boolean inLevelHelp = false;
    private boolean inLevelHelpLetters = false;
    private boolean inLevelHelpWords = false;
    private boolean inLevelReq = false;
    private boolean inLevelReqLetter = false;
    private boolean inLevelReqWord = false;
    private Letter currentLetter;
    private Word currentWord;
    private Level currentLevel;
    private IntroPage currentPage;
    private CollectLetterReq currentReqLetter;
    private CollectWordReq currentReqWord;

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
        } else if (this.inLocale && localName.equals(LocaleParser.TAG_SHELL)) {
            this.parseShell(attributes);
        } else if (this.inLocale && !this.inLevelDefinition && localName.equals(LocaleParser.TAG_LETTERS)) {
            if (!this.inLevelDefinition) {
                this.inLettersList = true;
            }
        } else if (this.inLocale && this.inLettersList && localName.equals(LocaleParser.TAG_LETTER)) {
            this.inLetterDefinition = true;
            this.parseLetterDefinition(attributes);
        } else if (this.inLocale && !this.inLevelDefinition && localName.equals(LocaleParser.TAG_WORDS)) {
            if (!this.inLevelDefinition) {
                this.inWordsList = true;
            }
        } else if (this.inLocale && this.inWordsList && localName.equals(LocaleParser.TAG_WORD)) {
            this.inWordDefinition = true;
            this.parseWordDefinition(attributes);
        } else if (this.inLocale && localName.equals(LocaleParser.TAG_LEVELS)) {
            this.inLevelsList = true;
        } else if (this.inLocale && this.inLevelsList && localName.equals(LocaleParser.TAG_LEVEL)) {
            this.inLevelDefinition = true;
            this.parseLevel(attributes);
        } else if (this.inLocale && this.inLevelDefinition && localName.equals(LocaleParser.TAG_INTRO)) {
            this.inLevelIntro = true;
        } else if (this.inLevelIntro && localName.equals(LocaleParser.TAG_PAGE)) {
            this.inLevelIntroPage = true;
            this.parseLevelIntroPage(attributes);
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
        } else if (this.inLocale && this.inLevelDefinition && localName.equals(LocaleParser.TAG_REQ)) {
            this.inLevelReq = true;
        } else if (this.inLocale && this.inLevelDefinition && this.inLevelReq && localName.equals(LocaleParser.TAG_REQLETTER)) {
            this.inLevelReqLetter = true;
            this.currentReqLetter = new CollectLetterReq();
            this.parseLevelReqLetter(attributes);
        } else if (this.inLocale && this.inLevelDefinition && this.inLevelReq && localName.equals(LocaleParser.TAG_REQWORD)) {
            this.inLevelReqWord = true;
            this.currentReqWord = new CollectWordReq();
            this.parseLevelReqWord(attributes);
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

    private void parseShell(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale shell");
        this.locale.shell_src = attributes.getValue("src");
    }

    private void parseLetterDefinition(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale letter");
        this.currentLetter = new Letter();
        this.currentLetter.name = attributes.getValue("name");
        this.currentLetter.sound = attributes.getValue("sound");
        this.currentLetter.phoneme = attributes.getValue("phoneme");
        this.currentLetter.time = Integer.parseInt(attributes.getValue("time"));
        this.currentLetter.texture_src = attributes.getValue("texture");
    }

    private void parseWordDefinition(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale word");
        this.currentWord = new Word();
        this.currentWord.name = attributes.getValue("name");
        this.currentWord.sound = attributes.getValue("sound");
        this.currentWord.time = Integer.parseInt(attributes.getValue("time"));
        this.currentWord.texture_src = attributes.getValue("texture");
    }

    private void parseLevel(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale level");
        this.currentLevel = new Level();
        this.currentLevel.name = attributes.getValue("name");
        this.currentLevel.letters = new ArrayList<Letter>();
        this.currentLevel.words = new ArrayList<Word>();
        this.currentLevel.help_letters = new ArrayList<Letter>();
        this.currentLevel.help_words = new ArrayList<Word>();
        this.currentLevel.intro = new ArrayList<IntroPage>();
        this.currentLevel.requirements = new ArrayList<Requirement>();
    }

    private void parseLevelIntroPage(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale level intro");
        this.currentPage = new IntroPage();
        this.currentPage.sound = attributes.getValue("sound");
    }

    private void parseLevelReqLetter(Attributes attributes) throws SAXException {
        this.currentReqLetter = new CollectLetterReq();
        this.currentReqLetter.setCount(Integer.parseInt(attributes.getValue("count")));
    }

    private void parseLevelReqWord(Attributes attributes) throws SAXException {
        this.currentReqWord = new CollectWordReq();
        this.currentReqWord.setCount(Integer.parseInt(attributes.getValue("count")));
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
        } else if (this.inLocale && this.inLevelIntroPage && localName.equals(LocaleParser.TAG_INTRO)) {
            this.inLevelIntro = false;
        } else if (this.inLevelIntroPage && localName.equals(LocaleParser.TAG_PAGE)) {
            this.inLevelIntroPage = false;
            this.currentLevel.intro.add(this.currentPage);
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
        } else if (this.inLocale && this.inLevelReq && localName.equals(LocaleParser.TAG_REQ)) {
            this.inLevelReq = false;
        } else if (this.inLocale && this.inLevelReqLetter && localName.equals(LocaleParser.TAG_REQLETTER)) {
            this.inLevelReqLetter = false;
        } else if (this.inLocale && this.inLevelReqWord && localName.equals(LocaleParser.TAG_REQWORD)) {
            this.currentLevel.requirements.add(this.currentReqWord);
            this.currentReqWord = null;
            this.inLevelReqWord = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String text = new String(ch, start, length);
        if (this.inLocale && this.inLettersList && this.inLetterDefinition) {
            Debug.v("Adding Letter chars: "+text);
            this.currentLetter.chars = text.toCharArray();
        } else if (this.inLocale && this.inWordsList && this.inWordDefinition) {
            Debug.v("Adding Word chars: "+text);
            this.currentWord.chars = text.toCharArray();
        } else if (this.inLevelIntroPage) {
            Debug.v("Adding Intro page chars: "+text);
            this.currentPage.text = text;
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
        } else if (this.inLocale && this.inLevelDefinition && this.inLevelReq && this.inLevelReqLetter) {
            String[] letters =  StringUtils.split(text, ",");
            for (int i = 0; i < letters.length; i++) {
                Debug.v("Adding req letter "+letters[i]+" to Level "+this.currentLevel.name);
                this.currentReqLetter.addLetter(this.locale.letter_map.get(letters[i]));
            }
        } else if (this.inLocale && this.inLevelDefinition && this.inLevelReq && this.inLevelReqWord) {
            String[] words =  StringUtils.split(text, ",");
            for (int i = 0; i < words.length; i++) {
                Debug.v("Adding req word "+words[i]+" to Level "+this.currentLevel.name);
                this.currentReqWord.addWord(this.locale.word_map.get(words[i]));
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }
}
