package com.linguaculturalists.phoenicia.locale;

import com.linguaculturalists.phoenicia.locale.tour.Message;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.locale.tour.Tour;
import com.linguaculturalists.phoenicia.tour.TourOverlay;

import org.andengine.util.debug.Debug;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Read the locale XML and build it into a Locale object.
 */
public class LocaleParser extends DefaultHandler {

    private boolean inLevelDefinition = false;
    private Number currentNumber;
    private Letter currentLetter;
    private Word currentWord;
    private Level currentLevel;
    private IntroPage currentPage;
    private CollectLetterReq currentReqLetter;
    private CollectWordReq currentReqWord;
    private Stop currentTourStop;
    private Message currentTourStopMessage;

    private Locale locale;
    private Stack<String> nodeStack;

    public LocaleParser() {
        super();
        this.nodeStack = new Stack<String>();
    }



    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.nodeStack.push(localName);
        String nodePath = StringUtils.join(this.nodeStack, '/');
        //Debug.d("Parser start: "+nodePath);

        if (nodePath.equals("/locale")) {
            this.parseLocale(attributes);
        } else if (nodePath.equals("/locale/map")) {
            this.parseMap(attributes);
        } else if (nodePath.equals("/locale/shell")) {
            this.parseShell(attributes);
        } else if (nodePath.equals("/locale/music")) {
            this.parseMusic(attributes);
        } else if (nodePath.equals("/locale/inventory")) {
            this.parseInventory(attributes);
        } else if (nodePath.equals("/locale/market")) {
            this.parseMarket(attributes);
        } else if (nodePath.equals("/locale/workshop")) {
            this.parseWorkshop(attributes);
        } else if (nodePath.equals("/locale/people/person")) {
            this.parsePerson(attributes);
        } else if (nodePath.equals("/locale/games/game")) {
            this.parseGame(attributes);
        } else if (nodePath.equals("/locale/decorations/decoration")) {
            this.parseDecoration(attributes);
        } else if (nodePath.equals("/locale/tour")) {
            this.parseTourGuide(attributes);
        } else if (nodePath.equals("/locale/tour/stop")) {
            this.parseTourStop(attributes);
        } else if (nodePath.equals("/locale/tour/stop/message")) {
            this.parseTourStopMessage(attributes);
        } else if (nodePath.equals("/locale/numbers/number")) {
            this.parseNumberDefinition(attributes);
        } else if (nodePath.equals("/locale/letters/letter")) {
            this.parseLetterDefinition(attributes);
        } else if (nodePath.equals("/locale/words/word")) {
            this.parseWordDefinition(attributes);
        } else if (nodePath.equals("/locale/levels/level")) {
            this.parseLevel(attributes);
        } else if (nodePath.equals("/locale/levels/level/intro/page")) {
            this.parseLevelIntroPage(attributes);
        } else if (nodePath.equals("/locale/levels/level/req/gather_letter")) {
            this.currentReqLetter = new CollectLetterReq();
            this.parseLevelReqLetter(attributes);
        } else if (nodePath.equals("/locale/levels/level/req/gather_word")) {
            this.currentReqWord = new CollectWordReq();
            this.parseLevelReqWord(attributes);
        }
    }

    private void parseLocale(Attributes attributes) throws SAXException {
        this.locale = new Locale();
        this.locale.name = attributes.getValue("name");
        this.locale.display_name = attributes.getValue("display_name");
        this.locale.lang = attributes.getValue("lang");
        this.locale.tour = new Tour();
    }

    private void parseMap(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale map");
        this.locale.map_src = attributes.getValue("src");
    }

    private void parseShell(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale shell");
        this.locale.shell_src = attributes.getValue("src");
    }

    private void parseMusic(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale music");
        this.locale.music_src = attributes.getValue("src");
    }

    private void parseInventory(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale inventory");
        this.locale.inventoryBlock = new InventoryBlock();
        this.locale.inventoryBlock.name = attributes.getValue("name");
        this.locale.inventoryBlock.level = attributes.getValue("level");
        String size = attributes.getValue("size");
        if (size == null || size == "" || size == "1x1" || size == "1x1x1") {
            this.locale.inventoryBlock.columns = 1;
            this.locale.inventoryBlock.rows = 1;
            this.locale.inventoryBlock.height = 1;
        } else {
            try {
                String[] dimensions = size.split("x");
                this.locale.inventoryBlock.columns = Integer.parseInt(dimensions[0]);
                this.locale.inventoryBlock.rows = Integer.parseInt(dimensions[1]);
                if (dimensions.length > 2) {
                    this.locale.inventoryBlock.height = Integer.parseInt(dimensions[2]);
                } else {
                    this.locale.inventoryBlock.height = 1;
                }
            } catch (Exception e) {
                Debug.e("Failed to parse size for: "+this.locale.inventoryBlock.name);
                e.printStackTrace();
            }
        }
        this.locale.inventoryBlock.block_texture = attributes.getValue("block");
        this.locale.inventoryBlock.mapCol = Integer.parseInt(attributes.getValue("col"));
        this.locale.inventoryBlock.mapRow = Integer.parseInt(attributes.getValue("row"));
    }

    private void parseMarket(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale market");
        this.locale.marketBlock = new MarketBlock();
        this.locale.marketBlock.name = attributes.getValue("name");
        this.locale.marketBlock.level = attributes.getValue("level");
        String size = attributes.getValue("size");
        if (size == null || size == "" || size == "1x1" || size == "1x1x1") {
            this.locale.marketBlock.columns = 1;
            this.locale.marketBlock.rows = 1;
            this.locale.marketBlock.height = 1;
        } else {
            try {
                String[] dimensions = size.split("x");
                this.locale.marketBlock.columns = Integer.parseInt(dimensions[0]);
                this.locale.marketBlock.rows = Integer.parseInt(dimensions[1]);
                if (dimensions.length > 2) {
                    this.locale.marketBlock.height = Integer.parseInt(dimensions[2]);
                } else {
                    this.locale.marketBlock.height = 1;
                }
            } catch (Exception e) {
                Debug.e("Failed to parse size for: "+this.locale.marketBlock.name);
                e.printStackTrace();
            }
        }
        this.locale.marketBlock.block_texture = attributes.getValue("block");
        this.locale.marketBlock.mapCol = Integer.parseInt(attributes.getValue("col"));
        this.locale.marketBlock.mapRow = Integer.parseInt(attributes.getValue("row"));
    }

    private void parseWorkshop(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale workshop");
        this.locale.workshopBlock = new WorkshopBlock();
        this.locale.workshopBlock.name = attributes.getValue("name");
        this.locale.workshopBlock.level = attributes.getValue("level");
        String size = attributes.getValue("size");
        if (size == null || size == "" || size == "1x1" || size == "1x1x1") {
            this.locale.workshopBlock.columns = 1;
            this.locale.workshopBlock.rows = 1;
            this.locale.workshopBlock.height = 1;
        } else {
            try {
                String[] dimensions = size.split("x");
                this.locale.workshopBlock.columns = Integer.parseInt(dimensions[0]);
                this.locale.workshopBlock.rows = Integer.parseInt(dimensions[1]);
                if (dimensions.length > 2) {
                    this.locale.workshopBlock.height = Integer.parseInt(dimensions[2]);
                } else {
                    this.locale.workshopBlock.height = 1;
                }
            } catch (Exception e) {
                Debug.e("Failed to parse size for: "+this.locale.workshopBlock.name);
                e.printStackTrace();
            }
        }
        this.locale.workshopBlock.block_texture = attributes.getValue("block");
        this.locale.workshopBlock.mapCol = Integer.parseInt(attributes.getValue("col"));
        this.locale.workshopBlock.mapRow = Integer.parseInt(attributes.getValue("row"));
    }

    private void parsePerson(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale person");
        Person newPerson = new Person();
        newPerson.name = attributes.getValue("name");
        newPerson.texture_src = attributes.getValue("texture");
        this.locale.people.add(newPerson);
        this.locale.person_map.put(newPerson.name, newPerson);
    }

    private void parseGame(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale game");
        Game newGame = new Game();
        newGame.name = attributes.getValue("name");
        newGame.level = attributes.getValue("level");
        String size = attributes.getValue("size");
        if (size == null || size == "" || size == "1x1" || size == "1x1x1") {
            newGame.columns = 1;
            newGame.rows = 1;
            newGame.height = 1;
        } else {
            try {
                String[] dimensions = size.split("x");
                newGame.columns = Integer.parseInt(dimensions[0]);
                newGame.rows = Integer.parseInt(dimensions[1]);
                if (dimensions.length > 2) {
                    newGame.height = Integer.parseInt(dimensions[2]);
                } else {
                    newGame.height = 1;
                }
            } catch (Exception e) {
                Debug.e("Failed to parse size for: "+newGame.name);
                e.printStackTrace();
            }
        }
        newGame.restriction = attributes.getValue("restrict");
        newGame.type = attributes.getValue("type");
        newGame.sprite_texture = attributes.getValue("sprite");
        newGame.block_texture = attributes.getValue("block");
        newGame.background_texture = attributes.getValue("background");
        newGame.host = this.locale.person_map.get(attributes.getValue("host"));
        newGame.buy = Integer.parseInt(attributes.getValue("buy"));
        newGame.construct = Integer.parseInt(attributes.getValue("construct"));
        newGame.time = Integer.parseInt(attributes.getValue("time"));
        newGame.reward = Float.parseFloat(attributes.getValue("reward"));
        this.locale.games.add(newGame);
        this.locale.game_map.put(newGame.name, newGame);
    }

    private void parseDecoration(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale decoration");
        Decoration newDecoration = new Decoration();
        newDecoration.name = attributes.getValue("name");
        newDecoration.level = attributes.getValue("level");
        String size = attributes.getValue("size");
        if (size == null || size == "" || size == "1x1" || size == "1x1x1") {
            newDecoration.columns = 1;
            newDecoration.rows = 1;
            newDecoration.height = 1;
        } else {
            try {
                String[] dimensions = size.split("x");
                newDecoration.columns = Integer.parseInt(dimensions[0]);
                newDecoration.rows = Integer.parseInt(dimensions[1]);
                if (dimensions.length > 2) {
                    newDecoration.height = Integer.parseInt(dimensions[2]);
                } else {
                    newDecoration.height = 1;
                }
            } catch (Exception e) {
                Debug.e("Failed to parse size for: "+newDecoration.name);
                e.printStackTrace();
            }
        }
        newDecoration.restriction = attributes.getValue("restrict");
        newDecoration.sprite_texture = attributes.getValue("sprite");
        newDecoration.block_texture = attributes.getValue("block");
        newDecoration.buy = Integer.parseInt(attributes.getValue("buy"));
        this.locale.decorations.add(newDecoration);
        this.locale.decoration_map.put(newDecoration.name, newDecoration);
    }

    private void parseTourGuide(Attributes attributes) throws SAXException {
        String guideName = attributes.getValue("guide");
        Debug.v("Setting tour guide to: " + guideName);
        if (locale.person_map.containsKey(guideName)) {
            locale.tour.guide = locale.person_map.get(guideName);
        }
    }

    private void parseTourStop(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale tour stop");
        String stopId = attributes.getValue("id");
        if (stopId.equals("welcome")) {
            this.currentTourStop = locale.tour.welcome;
        } else if (stopId.equals("words")) {
            this.currentTourStop = locale.tour.words;
        } else if (stopId.equals("inventory")) {
            this.currentTourStop = locale.tour.inventory;
        } else if (stopId.equals("market")) {
            this.currentTourStop = locale.tour.market;
        } else if (stopId.equals("workshop")) {
            this.currentTourStop = locale.tour.workshop;
        }

    }

    private void parseTourStopMessage(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale tour stop message for "+this.currentTourStop.getClass());
        Message newMessage = new Message();
        newMessage.sound = attributes.getValue("sound");
        newMessage.texture_src = attributes.getValue("texture");
        newMessage.text = "";
        this.currentTourStop.addMessage(newMessage);
        this.currentTourStopMessage = newMessage;
    }

    private void parseNumberDefinition(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale number");
        this.currentNumber = new Number();
        this.currentNumber.name = attributes.getValue("name");
        Debug.v("Parsing locale number: "+this.currentNumber.name);
        this.currentLetter.sound = attributes.getValue("sound");
        this.currentLetter.sprite_texture = attributes.getValue("sprite");
    }

    private void parseLetterDefinition(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale letter");
        this.currentLetter = new Letter();
        this.currentLetter.name = attributes.getValue("name");
        Debug.v("Parsing locale letter: "+this.currentLetter.name);
        String size = attributes.getValue("size");
        if (size == null || size == "" || size == "1x1" || size == "1x1x1") {
            this.currentLetter.columns = 1;
            this.currentLetter.rows = 1;
            this.currentLetter.height = 1;
        } else {
            try {
                String[] dimensions = size.split("x");
                this.currentLetter.columns = Integer.parseInt(dimensions[0]);
                this.currentLetter.rows = Integer.parseInt(dimensions[1]);
                if (dimensions.length > 2) {
                    this.currentLetter.height = Integer.parseInt(dimensions[2]);
                } else {
                    this.currentLetter.height = 1;
                }
            } catch (Exception e) {
                Debug.e("Failed to parse letter size for: "+this.currentLetter.name);
                e.printStackTrace();
            }
        }
        this.currentLetter.restriction = attributes.getValue("restrict");
        this.currentLetter.sound = attributes.getValue("sound");
        this.currentLetter.phoneme = attributes.getValue("phoneme");
        this.currentLetter.time = Integer.parseInt(attributes.getValue("time"));
        this.currentLetter.buy = Integer.parseInt(attributes.getValue("buy"));
        this.currentLetter.sell = Integer.parseInt(attributes.getValue("sell"));
        this.currentLetter.points = Integer.parseInt(attributes.getValue("points"));
        this.currentLetter.sprite_texture = attributes.getValue("sprite");
        this.currentLetter.block_texture = attributes.getValue("block");
    }

    private void parseWordDefinition(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale word");
        this.currentWord = new Word();
        this.currentWord.name = attributes.getValue("name");
        String size = attributes.getValue("size");
        if (size == null || size == "" || size == "1x1" || size == "1x1x1") {
            this.currentWord.columns = 1;
            this.currentWord.rows = 1;
            this.currentWord.height = 1;
        } else {
            try {
                String[] dimensions = size.split("x");
                this.currentWord.columns = Integer.parseInt(dimensions[0]);
                this.currentWord.rows = Integer.parseInt(dimensions[1]);
                if (dimensions.length > 2) {
                    this.currentWord.height = Integer.parseInt(dimensions[2]);
                } else {
                    this.currentWord.height = 1;
                }
            } catch (Exception e) {
                Debug.e("Failed to parse word size for: "+this.currentWord.name);
                e.printStackTrace();
            }
        }
        this.currentWord.restriction = attributes.getValue("restrict");
        this.currentWord.sound = attributes.getValue("sound");
        this.currentWord.construct = Integer.parseInt(attributes.getValue("construct"));
        this.currentWord.time = Integer.parseInt(attributes.getValue("time"));
        this.currentWord.buy = Integer.parseInt(attributes.getValue("buy"));
        this.currentWord.sell = Integer.parseInt(attributes.getValue("sell"));
        this.currentWord.points = Integer.parseInt(attributes.getValue("points"));
        this.currentWord.sprite_texture = attributes.getValue("sprite");
        this.currentWord.block_texture = attributes.getValue("block");
    }

    private void parseLevel(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale level");
        Level lastLevel = this.currentLevel;
        this.currentLevel = new Level();
        if (lastLevel == null) {
            this.currentLevel.prev = this.currentLevel;
        } else {
            this.currentLevel.prev = lastLevel;
            lastLevel.next = this.currentLevel;
        }
        this.currentLevel.next = this.currentLevel;

        this.currentLevel.name = attributes.getValue("name");
        if (attributes.getValue("market") != null) {
            this.currentLevel.marketRequests = Integer.parseInt(attributes.getValue("market"));
        } else {
            this.currentLevel.marketRequests = 0;
        }
        if (attributes.getValue("coins") != null) {
            this.currentLevel.coinsEarned = Integer.parseInt(attributes.getValue("coins"));
        } else {
            this.currentLevel.coinsEarned = 0;
        }
        if (attributes.getValue("points") != null) {
            this.currentLevel.pointsEarned = Integer.parseInt(attributes.getValue("coins"));
        } else {
            this.currentLevel.pointsEarned = 0;
        }
        if (lastLevel != null) {
            this.currentLevel.letters = new ArrayList<Letter>(lastLevel.letters);
            this.currentLevel.letter_count = new HashMap<Letter, Integer>(lastLevel.letter_count);
            this.currentLevel.words = new ArrayList<Word>(lastLevel.words);
            this.currentLevel.word_count = new HashMap<Word, Integer>(lastLevel.word_count);
        } else {
            this.currentLevel.letters = new ArrayList<Letter>();
            this.currentLevel.letter_count = new HashMap<Letter, Integer>();
            this.currentLevel.words = new ArrayList<Word>();
            this.currentLevel.word_count = new HashMap<Word, Integer>();
        }
        this.currentLevel.help_letters = new ArrayList<Letter>();
        this.currentLevel.help_words = new ArrayList<Word>();
        this.currentLevel.intro = new ArrayList<IntroPage>();
        this.currentLevel.requirements = new ArrayList<Requirement>();
        this.currentLevel.games = new ArrayList<Game>();
        this.currentLevel.decorations = new ArrayList<Decoration>();

    }

    private void parseLevelIntroPage(Attributes attributes) throws SAXException {
        Debug.v("Parsing locale level intro");
        this.currentPage = new IntroPage();
        this.currentPage.sound = attributes.getValue("sound");
        this.currentPage.texture_src = attributes.getValue("texture");
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
        String nodePath = StringUtils.join(this.nodeStack, '/');
        Debug.v("Parser end: "+nodePath);

        if (nodePath.equals("/locale/letters/letter")) {
            this.locale.letters.add(this.currentLetter);
            this.locale.letter_map.put(this.currentLetter.name, this.currentLetter);
            this.currentLetter = null;
        } else if (nodePath.equals("/locale/words/word")) {
            this.locale.words.add(this.currentWord);
            this.locale.word_map.put(this.currentWord.name, this.currentWord);
            this.currentWord = null;
        } else if (nodePath.equals("/locale/levels/level")) {
            this.locale.levels.add(this.currentLevel);
            this.locale.level_map.put(this.currentLevel.name, this.currentLevel);
            for (Game g : this.locale.games) {
                if (this.locale.isLevelReached(g.level, this.currentLevel)) this.currentLevel.games.add(g);
            }
            for (Decoration d : this.locale.decorations) {
                if (this.locale.isLevelReached(d.level, this.currentLevel)) this.currentLevel.decorations.add(d);
            }
            this.inLevelDefinition = false;
        } else if (nodePath.equals("/locale/levels/level/intro/page")) {
            this.currentLevel.intro.add(this.currentPage);
        } else if (nodePath.equals("/locale/levels/level/req/gather_letter")) {
            this.currentLevel.requirements.add(this.currentReqLetter);
            this.currentReqLetter = null;
        } else if (nodePath.equals("/locale/levels/level/req/gather_word")) {
            this.currentLevel.requirements.add(this.currentReqWord);
            this.currentReqWord = null;
        }

        if (this.nodeStack.peek().equals(localName)) {
            this.nodeStack.pop();
        } else {
            Debug.e("Closing tag "+localName+" was not the last opened tag: "+this.nodeStack.peek());
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String nodePath = StringUtils.join(this.nodeStack, '/');

        Debug.v("Reading characteres for "+nodePath);

        String text = new String(ch, start, length);
        if (nodePath.equals("/locale/tour/stop/message")) {
            this.currentTourStopMessage.text = text;
        } else if (nodePath.equals("/locale/numbers/number")) {
            Debug.v("Adding Number chars: "+text);
            this.currentNumber.chars = text.toCharArray();
        } else if (nodePath.equals("/locale/letters/letter")) {
            Debug.v("Adding Letter chars: "+text);
            this.currentLetter.chars = text.toCharArray();
        } else if (nodePath.equals("/locale/words/word")) {
            Debug.v("Adding Word chars: "+text);
            if (this.currentWord.chars == null) {
                this.currentWord.chars = text.toCharArray();
            } else {
                char[] appended = new char[this.currentWord.chars.length + text.length()];
                System.arraycopy(this.currentWord.chars, 0, appended, 0, this.currentWord.chars.length);
                System.arraycopy(text.toCharArray(), 0, appended, this.currentWord.chars.length, text.length());
                this.currentWord.chars = appended;
            }
        } else if (nodePath.equals("/locale/levels/level/intro/page")) {
            Debug.v("Adding Intro page chars: "+text);
            if (this.currentPage.text == null) {
                this.currentPage.text = text;
            } else {
                this.currentPage.text += text;
            }
        } else if (nodePath.equals("/locale/levels/level/letters")) {
            String[] letters =  StringUtils.split(text, ",");
            for (int i = 0; i < letters.length; i++) {
                Letter letter = this.locale.letter_map.get(letters[i]);
                if (!this.currentLevel.letter_count.containsKey(letter)) {
                    Debug.v("Adding Letter "+letters[i]+" to Level "+this.currentLevel.name);
                    this.currentLevel.letters.add(letter);
                    this.currentLevel.letter_count.put(letter, 1);
                } else {
                    this.currentLevel.letter_count.put(letter, this.currentLevel.letter_count.get(letter));
                }
            }
        } else if (nodePath.equals("/locale/levels/level/words")) {
            String[] words =  StringUtils.split(text, ",");
            for (int i = 0; i < words.length; i++) {
                Word word = this.locale.word_map.get(words[i]);
                if (!this.currentLevel.word_count.containsKey(word)) {
                    Debug.v("Adding Word "+words[i]+" to Level "+this.currentLevel.name);
                    this.currentLevel.words.add(word);
                    this.currentLevel.word_count.put(word, 1);
                } else {
                    this.currentLevel.word_count.put(word, this.currentLevel.word_count.get(word));
                }
            }
        } else if (nodePath.equals("/locale/levels/level/help/letters")) {
            String[] letters =  StringUtils.split(text, ",");
            for (int i = 0; i < letters.length; i++) {
                Debug.v("Adding help Letter "+letters[i]+" to Level "+this.currentLevel.name);
                this.currentLevel.help_letters.add(this.locale.letter_map.get(letters[i]));
            }
        } else if (nodePath.equals("/locale/levels/level/help/words")) {
            String[] words =  StringUtils.split(text, ",");
            for (int i = 0; i < words.length; i++) {
                Debug.v("Adding help Word "+words[i]+" to Level "+this.currentLevel.name);
                this.currentLevel.help_words.add(this.locale.word_map.get(words[i]));
            }
        } else if (nodePath.equals("/locale/levels/level/req/gather_letter")) {
            String[] letters =  StringUtils.split(text, ",");
            for (int i = 0; i < letters.length; i++) {
                Debug.v("Adding req letter "+letters[i]+" to Level "+this.currentLevel.name);
                this.currentReqLetter.addLetter(this.locale.letter_map.get(letters[i]));
            }
        } else if (nodePath.equals("/locale/levels/level/req/gather_word")) {
            String[] words =  StringUtils.split(text, ",");
            for (int i = 0; i < words.length; i++) {
                Debug.v("Adding req word "+words[i]+" to Level "+this.currentLevel.name);
                this.currentReqWord.addWord(this.locale.word_map.get(words[i]));
            }
        }
    }


    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        this.nodeStack.push("");
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        this.nodeStack.pop();
    }
}
