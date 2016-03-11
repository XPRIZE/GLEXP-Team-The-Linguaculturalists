Localization
============
At the core of a Locale Pack is the `manifest.xml` file. The manifest defines everything you will see within Phoenicia, from the map to the letters and words and their assets. It is also responsible for defining the progression that a player will go through in learning how to read in that language.

Manifest
--------
Below is a very basic example of a Locale Pack manifest. Don't let the size concern you, we'll go through it piece by piece.

    <?xml version="1.0" encoding="utf-8"?>
    <locale name="en_us_rural" lang="en_us" display_name="US English, Rural Setting">
    
        <shell src="gameui.png" />
        <map src="map.tmx" />
    
        <inventory name="Barn" col="37" row="43" texture="inventory.png" />
        <market name="Market" col="41" row="41" texture="market.png" />
    
        <letters>
            <letter name="a" buy="0" sell="1" points="1" time="10" texture="a.png" sound="a-name.ogg" phoneme="a-phoneme.ogg">a</letter>
            <letter name="c" buy="2" sell="1" points="1" time="10" texture="c.png" sound="c-name.ogg" phoneme="c-phoneme.ogg">c</letter>
            <letter name="h" buy="4" sell="3" points="1" time="10" texture="h.png" sound="h-name.ogg" phoneme="h-phoneme.ogg">h</letter>
            <letter name="t" buy="3" sell="2" points="1" time="10" texture="t.png" sound="t-name.ogg" phoneme="t-phoneme.ogg">t</letter>
        </letters>
        
        <words>
            <word name="cat" buy="100" sell="10" points="10" construct="20"  time="60" texture="words/cat.png" sound="cat.ogg">cat</word>
            <word name="hat" buy="200" sell="20" points="20" construct="20" time="75" texture="words/hat.png" sound="hat.ogg">hat</word>
        </words>
        
        <levels>
            <level name="3">
            
                <intro>
                    <page sound="intro3p1.ogg">Now you're getting it! Lets grow some 'C's and you'll have enough letters to start building something</page>
                    <page sound="intro3p2.ogg" texture="levels/intro3p2.png">Use the Word images to build a 'cat' factory</page>
                </intro>
                
                <letters>a,t,c</letters>
                <words>cat</words>
                
                <help>
                    <letters>t,c</letters>
                    <words>cat</words>
                </help>
                
                <req>
                    <gather_word count="1">cat</gather_word>
                </req>
                
            </level>
        </levels>
    </locale>

### Locale
    <locale name="en_us_rural" lang="en_us" display_name="US English, Rural Setting">
The top-level element of a manifest is the `<locale>`. Here you will define a unique `name` for your manifest, specifiy the `language` code it will be using, as well the `display_name` that will be seen by the player.

### Shell
        <shell src="gameui.png" />
The next element is the `<shell>` which is used to specify the file containing image assests for various parts of the game's interface that are not part of the language-specific artwork.

### Map
        <map src="map.tmx" />
You will also need to supply a `<map>` definition for your locale. Phoenicia uses the popular TMX file format for it's maps, which means you can use existing tools like the [Tiled](http://www.mapeditor.org/) editor to make map creation easier.

### Default blocks
        <inventory name="Barn" col="37" row="43" texture="inventory.png" />
        <market name="Market" col="41" row="41" texture="market.png" />
After the map, you will define a couple of default blocks that will be pre-placed on the map at the start of a new game. The first is the `<inventory>` which is where the player can go to check how many letters and words they have collected. Then there is the `<market>` which will present the player with demand for their collected letters and words, which they will sell for in-game currency and experience points. 

Both of these blocks take the `row` and `column` for their initial placement (the player will be able to move them later), as well as an image `texture` to represent it in it's various states.
    
### Letters
        <letters>
            <letter name="a" buy="0" sell="1" points="1" time="10" texture="a.png" sound="a-name.ogg" phoneme="a-phoneme.ogg">a</letter>
            <letter name="c" buy="2" sell="1" points="1" time="10" texture="c.png" sound="c-name.ogg" phoneme="c-phoneme.ogg">c</letter>
            <letter name="h" buy="4" sell="3" points="1" time="10" texture="h.png" sound="h-name.ogg" phoneme="h-phoneme.ogg">h</letter>
            <letter name="t" buy="3" sell="2" points="1" time="10" texture="t.png" sound="t-name.ogg" phoneme="t-phoneme.ogg">t</letter>
        </letters>
Now that you've defined the general components, it's time to add the parts of your Locale Pack that define the language itself. This starts with the `<letters>` section, where you will define your alphabet. 

Each `<letter>` element must supply a unique `name` property, which doesn't necessarily have to be the letter itself. It will also need a `texture` image that contains the graphics that will be used to represent the letter on and off the map. Each `<letter>` also contains two audio assets, the `sound` which is the spoken name of the letter, and `phoneme` which is the sound that he letter makes.

Letters also have a few values that determine how they behave in the game, such as `buy` and `sell` prices that define the base values for the letter in game currency, as well as `points` to define the number of experience points earned by growing or collecting it. They also need a `time` which represents the time (in seconds) it takes to grow or collect (the game engine will take care of updating the blocks placed on the map according to this time).

Finally the value inside the `<letter></letter>` tags is the actual letter being defined. 

### Words
        <words>
            <word name="cat" buy="100" sell="10" points="10" construct="20"  time="60" texture="words/cat.png" sound="cat.ogg">cat</word>
            <word name="hat" buy="200" sell="20" points="20" construct="20" time="75" texture="words/hat.png" sound="hat.ogg">hat</word>
        </words>
The second half of the language definition is the selection of `<words>` that your player will be learning throughout the game. You can choose which words from your target language to use to present the best learning path for that language. It is best to stick to nouns here, because they will need to be represented in a visual way.

Just like with `<letter>` each word must supply a unique `name` for internal reference, which may or may not be the same as the word being represented. They also take a `texture` image containing all of the graphics for displaying the word. Unlike letters, words need only one audio asset, the `sound` which is the spoken word.

Again, like with `<letter>`, you will define a `buy` and `sell` value for your words, as well as `points` the player will earn from them. Words also need a `time` defining how long it takes to build it, but it additionally takes a `construct` time (also in seconds) defining how long it takes to build the map block that will be used to build the words themselves.

Again, the value inside the `<word></word>` tags is the text of the word itself.

### Levels

By now you will have defined both the look of the game as well as the language components that will be used in it. Now it's time to define the order and pace that the player will be learning them. This is the most important part of your Locale Pack, and where you will need to think specifically about how to teach a player to read in your target language. Phoenicia handles this through `<levels>` which represent each step along the learning path you are creating.

#### Intro Pages
                <intro>
                    <page sound="intro3p1.ogg">Now you're getting it! Lets grow some 'C's and you'll have enough letters to start building something</page>
                    <page sound="intro3p2.ogg" texture="levels/intro3p2.png">Use the Word images to build a 'cat' factory</page>
                </intro>

Each `<level>` starts with an `<intro>` section to tell the player what new items may have been unlocked, and provide guidance about what to do next. It does this through one or more `<page>` elements, each containing a short piece of text with a `sound` recording of the same text to be played aloud with it. The can optionally include a `texture` image containing an animation to visually demonstrate what the page text is describing.

#### Letters and Words
                <letters>a,t,c</letters>
                <words>cat</words>
Each `<level>` must provide a list of `<letters>` and `<words>` that are available to the player at that level. These are a comma-separated list that matches the unique `name` defined in the `<letter>` and `<word>` blocks at the top of the manifest. Remember that levels don't consider what was defined before them, so each one must include *every* letter and word that you want to be available in it.

#### Help
                <help>
                    <letters>t,c</letters>
                    <words>cat</words>
                </help>
Your player will need more assistance with letters and words after they've first been exposed to them, but will require less as they have had the time to become familiar with them. To facilitate this, each level can define a subset of it's `<letters>` and `<words>` in a `<help>` section, and Phoenicia will play the audio for those more often during interactions than it otherwise would. This lets you ease off the amount of hand-holding the player will be given as they master new skills.

#### Requirements
                <req>
                    <gather_word count="1">cat</gather_word>
                </req>
The final piece of the puzzle is defining *when* the player can progress from one level to the next. Phoenicia does this by putting one or more requirement in the `<req>` section of a level. Currently Phoenicia provides `<gather_letter>` and `<gather_word>` types of requirement, which take a minimum `count` for how many of that letter or word has to be collected before the requirement has been met. Once all of the requirements defined for a level have been met, the game engine with automatically advance the player to the next level, and display the new level's introduction pages.
