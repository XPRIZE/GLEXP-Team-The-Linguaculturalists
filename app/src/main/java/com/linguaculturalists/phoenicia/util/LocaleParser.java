package com.linguaculturalists.phoenicia.util;

import com.linguaculturalists.phoenicia.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by mhall on 7/17/15.
 */
public class LocaleParser extends DefaultHandler {

    private static final String TAG_MAP = "map";
    private static final String TAG_LETTERS = "letters";
    private static final String TAG_LETTER = "letter";
    private static final String TAG_WORDS = "words";
    private static final String TAG_WORD = "word";
    private static final String TAG_LEVELS = "levels";
    private static final String TAG_LEVEL = "level";
    private static final String TAG_HELP = "help";
    private boolean inLettersList = false;
    private boolean inLetterDefinition = false;
    private boolean inWordsList = false;
    private boolean inWordDefinition = false;
    private boolean inLevelsList = false;
    private boolean inLevelDefinition = false;
    private boolean inLevelHelp = false;
    private Locale.Letter currentLetter;
    private Locale.Word currentWord;
    private Locale.Level currentLevel;

    private Locale locale;

    public LocaleParser() {
        super();
        this.locale = new Locale();
    }



    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }
}
