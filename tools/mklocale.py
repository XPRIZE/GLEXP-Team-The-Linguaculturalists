#!/usr/bin/python

import os
import sys
import math
import subprocess
import codecs
sys.stdin = codecs.getreader('UTF-8')(sys.stdin);

if len(sys.argv) < 3:
    print "./mklocale.py NAME CHARS_FILE WORDS_FILE ./OUTPUT_DIR"
    exit(1)

def read_data(source):
    data = list()
    fh = open(source, "r")
    for line in fh.readlines():
        line = line.strip()
        if line.startswith("#") or line == "":
            continue
        data.append(line)
    return data

def print_header():
    parts = locale_name.split('_')
    lang = '%s_%s' % (parts[0], parts[1])
    if len(parts) < 3:
        parts.append('Unknown')
    display = '%s %s, %s' % (parts[1], parts[0], parts[2])
    manifest_file.write("""<?xml version="1.0" encoding="utf-8"?>
<locale name="%(locale)s" lang="%(lang)s" display_name="%(display)s">
    <shell src="locales/common/textures/gameui.png" />
    <map src="locales/en_us_rural/map.tmx" />
    <music src="locales/common/sounds/forest_background.mp3" />

    <!-- Default blocks -->
    <inventory level="3" size="4x4x4" name="Storage" col="23" row="26" block="locales/common/textures/inventory.png" />
    <market level="6" size="4x4x4" name="Market" col="23" row="18" block="locales/common/textures/market.png" />
    <workshop level="9" size="3x3x3" name="Workshop" col="18" row="32" block="locales/common/textures/workshop.png" />

    <!-- People used in the marketplace and player's avatar -->
    <people>
        <person name="Roxanne" texture="locales/common/textures/persons/f1.png" />
        <person name="Michelle" texture="locales/common/textures/persons/f2.png" />
        <person name="Laura" texture="locales/common/textures/persons/f3.png" />
        <person name="Ashley" texture="locales/common/textures/persons/f4.png" />
        <person name="Ben" texture="locales/common/textures/persons/m1.png" />
        <person name="Michael" texture="locales/common/textures/persons/m2.png" />
        <person name="Dave" texture="locales/common/textures/persons/m3.png" />
        <person name="Henry" texture="locales/common/textures/persons/m4.png" />
    </people>

    <!-- Mini-games -->
    <games>
        <game level="6"  name="Word Match"  type="wordmatch"  buy="300" construct="120" time="300" reward="1.5" sprite="locales/en_us_rural/textures/games/sprites/minigame1.png" block="locales/en_us_rural/textures/games/blocks/minigame1.png" />
        <game level="12" name="Image Match" type="imagematch" buy="500" construct="300" time="600" reward="2.0" sprite="locales/en_us_rural/textures/games/sprites/minigame2.png" block="locales/en_us_rural/textures/games/blocks/minigame2.png" />
    </games>

    <!-- Decorations -->
    <decorations>
        <decoration level="5"  size="1x1" name="Lamp Post"  buy="200" sprite="locales/common/textures/decorations/sprites/lamp.png" block="locales/common/textures/decorations/blocks/lamp.png" />
        <decoration level="10" size="1x1" name="Small Tree" buy="500" sprite="locales/common/textures/decorations/sprites/smalltree.png" block="locales/common/textures/decorations/blocks/smalltree.png" />
        <decoration level="20" size="2x2" name="Fountain"   buy="1000" sprite="locales/common/textures/decorations/sprites/fountain.png" block="locales/common/textures/decorations/blocks/fountain.png" />
    </decorations>

    <!-- Tour -->
    <tour guide="Michelle">
        <stop id="welcome">
            <message sound="locales/%(locale)s/sounds/tour/welcome1.ogg">Welcome to Phoenicia!</message>
            <message sound="locales/%(locale)s/sounds/tour/welcome2.ogg">Use the Letter images at the bottom of your screen to start planting with the letter 'A'</message>
            <message sound="locales/%(locale)s/sounds/tour/welcome3.ogg">Tap a letter to start planting it</message>
            <message sound="locales/%(locale)s/sounds/tour/welcome4.ogg">Tap a place on the map to plant your new letter</message>
            <message sound="locales/%(locale)s/sounds/tour/welcome5.ogg">When your letter is fully grown, tap it again to collect it</message>
            <message sound="locales/%(locale)s/sounds/tour/welcome6.ogg">Your letters will start growing again for you to collect later</message>
        </stop>
        <stop id="inventory">
            <message sound="locales/%(locale)s/sounds/tour/inventory1.ogg">This is your storage building. Any words or letters you collect will be kept here for you to use.</message>
            <message sound="locales/%(locale)s/sounds/tour/inventory2.ogg">You can also sell an item you have at any time, just by tapping on it.</message>
        </stop>
        <stop id="words">
            <message sound="locales/%(locale)s/sounds/tour/words1.ogg">Use the Word images to build a 'cat' factory</message>
            <message sound="locales/%(locale)s/sounds/tour/words2.ogg">Tap a word image to create a building for it</message>
            <message sound="locales/%(locale)s/sounds/tour/words3.ogg">Tap a place on the map to place your building</message>
            <message sound="locales/%(locale)s/sounds/tour/words4.ogg">Once your building is ready, you can use it to start making words</message>
            <message sound="locales/%(locale)s/sounds/tour/words5.ogg">You create a word by spelling it, use the letters C A T to spell cat</message>
            <message sound="locales/%(locale)s/sounds/tour/words6.ogg">It will take time for your word to finish, once it's ready you can collect it</message>
            <message sound="locales/%(locale)s/sounds/tour/words7.ogg">Tap the finished word at the top to add it to your inventory</message>
        </stop>
        <stop id="market">
            <message sound="locales/%(locale)s/sounds/tour/market1.ogg">Now that you have some words, you can start selling them at the market.</message>
            <message sound="locales/%(locale)s/sounds/tour/market2.ogg">Here you will find friends who would like to buy the letters and words you've gathered</message>
            <message sound="locales/%(locale)s/sounds/tour/market3.ogg">Once you have everything they need, you can sell them to earn the coins you need to keep building your town</message>
        </stop>
        <stop id="workshop">
            <message sound="locales/%(locale)s/sounds/tour/workshop1.ogg">If you want to make a word that you don't have a building for yet, you can make it in your workshop</message>
            <message sound="locales/%(locale)s/sounds/tour/workshop2.ogg">Here you can make any word in the game, all you need are the letters and how to spell it</message>
            <message sound="locales/%(locale)s/sounds/tour/workshop3.ogg">Try making the word map with the letters M A P</message>
            <message sound="locales/%(locale)s/sounds/tour/workshop4.ogg">Great! But remember, you can only make one word at a time in the workshop, so keep making word buildings!</message>
        </stop>
    </tour>

    <!-- Numbers -->
    <numbers>
        <number name="0" sprite="locales/common/textures/numbers/0.png" sound="locales/%(locale)s/sounds/0.ogg">0</number>
        <number name="1" sprite="locales/common/textures/numbers/1.png" sound="locales/%(locale)s/sounds/1.ogg">1</number>
        <number name="2" sprite="locales/common/textures/numbers/2.png" sound="locales/%(locale)s/sounds/2.ogg">2</number>
        <number name="3" sprite="locales/common/textures/numbers/3.png" sound="locales/%(locale)s/sounds/3.ogg">3</number>
        <number name="4" sprite="locales/common/textures/numbers/4.png" sound="locales/%(locale)s/sounds/4.ogg">4</number>
        <number name="5" sprite="locales/common/textures/numbers/5.png" sound="locales/%(locale)s/sounds/5.ogg">5</number>
        <number name="6" sprite="locales/common/textures/numbers/6.png" sound="locales/%(locale)s/sounds/6.ogg">6</number>
        <number name="7" sprite="locales/common/textures/numbers/7.png" sound="locales/%(locale)s/sounds/7.ogg">7</number>
        <number name="8" sprite="locales/common/textures/numbers/8.png" sound="locales/%(locale)s/sounds/8.ogg">8</number>
        <number name="9" sprite="locales/common/textures/numbers/9.png" sound="locales/%(locale)s/sounds/9.ogg">9</number>
        <number name="10" sprite="locales/common/textures/numbers/10.png" sound="locales/%(locale)s/sounds/10.ogg">10</number>
    </numbers>

""" % {'locale': locale_name, 'lang': lang, 'display': display})

def print_footer():
    manifest_file.write("</locale>\n")

def print_numbers():
    manifest_file.write("\n    <!-- Numbers -->\n")
    manifest_file.write("    <numbers>\n")
    for number in range(0,10):
        filename = numberFileName(number)
        context = {
            'number': number, 
            'name': number, 
            'filename': filename,
            'locale': locale_name
        }
        manifest_file.write('        <number name="%(name)s" sprite="locales/%(locale)s/textures/letters/sprites/%(filename)s.png" sound="locales/%(locale)s/sounds/%(filename)s.ogg">%(number)s</number>\n' % context)

        mkletterimage = ["./mknumberimage", "-d", locale_name, letter, filename]
        if rtl:
            mknumberimage.insert(1, "-rtl")
        subprocess.call(mkletterimage)

    manifest_file.write("    </numbers>\n")

def print_letters():
    manifest_file.write("\n    <!-- Alphabet -->\n")
    manifest_file.write("    <letters>\n")
    value_order = sorted(letter_values, key=letter_values.get, reverse=True)
    for letter in characters:
        try:
            letter_value = value_order.index(letter)
        except:
            letter_value = len(value_order)+1
        #round(math.pow(x+1, 2), -1)
        buy = round(math.pow(letter_value+1, 1.5), -1) or round(math.pow(letter_value+1, 1.5)) or 1
        sell = round(buy / (math.log(letter_value+1)+1), -1) or round(buy / (math.log(letter_value+1)+1)) or 1
        points = round(math.log(letter_value+1)*5, 1) or 1
        time = round(math.log(letter_value+1, 1.2)*30, -1) or 30
        if letter in words[0]:
            # reduce time to 1/3
            time = round(time/3,-1)
        elif letter in words[1]:
            #reduce time to 1/2
            time = round(time/2, -1)
        elif letter in words[2]:
            # reduce time to 2/3
            time = round(2*time/3)
        filename = letterFileName(letter)
        context = {
            'letter': letter, 
            'name': letter, 
            'filename': filename,
            "buy": buy, 
            'points': points, 
            'sell': sell, 
            'time': time,
            'locale': locale_name
        }
        manifest_file.write('        <letter name="%(name)s" buy="%(buy)d" sell="%(sell)d"  points="%(points)d" time="%(time)d" sprite="locales/common/textures/letters/sprites/%(filename)s.png" block="locales/common/textures/letters/blocks/%(filename)s.png" sound="locales/%(locale)s/sounds/%(filename)s-name.ogg" phoneme="locales/%(locale)s/sounds/%(filename)s-phoneme.ogg">%(letter)s</letter>\n' % context)

        if not manifest_only:
            mkletterimage = ["./mkletterimage", "-d", locale_name, letter, filename]
            if rtl:
                mkletterimage.insert(1, "-rtl")
            subprocess.call(mkletterimage)

    manifest_file.write("    </letters>\n")

def print_words():
    manifest_file.write("\n    <!-- Words -->\n")
    manifest_file.write("    <words>\n")
    for word in sorted(words):
        if word not in word_values:
            word_values[word] = len(word_values)
        buy = round(math.pow(word_values[word]+1, 1.1)*100, -1) or 100
        sell = round(math.pow(word_values[word]+1, 1.001)*10, -1) or 10
        points = round(math.log(word_values[word]+1)*50, 1) or 10
        time = round(math.pow(word_values[word]+1, 1.5), 0)*60 or 120
        construct = round(math.pow(word_values[word]+1, 1.6), 0)*90 or 180
        filename = wordFileName(word)
        context = {
            'word': word, 
            'name': word, 
            'filename': filename, 
            "buy": buy, 
            'points': points, 
            'sell': sell, 
            'time': time, 
            'construct': construct,
            'locale': locale_name
        }
        manifest_file.write('        <word name="%(name)s" size="2x2x2" buy="%(buy)d" sell="%(sell)d" points="%(points)d" construct="%(construct)d" time="%(time)d" sprite="locales/%(locale)s/textures/words/sprites/%(filename)s.png" block="locales/%(locale)s/textures/words/blocks/%(filename)s.png" sound="locales/%(locale)s/sounds/%(filename)s.ogg">%(word)s</word>\n' % context)
        
        if not manifest_only:
            mkwordimage = ["./mkwordimage", "-d", locale_name, word, filename]
            if rtl:
                mkwordimage.insert(1, "-rtl")
            subprocess.call(mkwordimage)
    manifest_file.write("    </words>\n")

def print_levels():
    manifest_file.write("\n    <!-- Levels -->\n")
    manifest_file.write("    <levels>\n")
    level_number = 1
    market = 0;
    market
    for level, words in enumerate(level_words):
        coins = round(math.pow(level_number, 0.6)*100, -2) or 100
        points = round(math.pow(level_number, 0.6)*200, -1) or 200
        market = round(math.log(level_number, len(word_values))*6) or 0
        letters = ','.join(level_letters[level])
        words = ','.join(level_words[level])
        context = {
            'name': level_number, 
            'coins': coins, 
            'points': points, 
            'market': market,
        }
        manifest_file.write('        <level name="%(name)s" coins="%(coins)d" points="%(points)d" market="%(market)d">\n' % {'name': level_number, 'coins': coins, 'points': points, 'market': market})
        manifest_file.write('            <intro>\n')
        manifest_file.write('            </intro>\n')
        manifest_file_unicode.write('            <letters>%s</letters>\n' % letters)
        manifest_file.write('            <words>%s</words>\n' % words)
        manifest_file.write('            <help>\n')
        manifest_file_unicode.write('                <letters>%s</letters>\n' % ','.join(level_letters[level]))
        manifest_file.write('                <words>%s</words>\n' % ','.join(level_words[level]))
        manifest_file.write('            </help>\n')
        manifest_file.write('            <req>\n')
        for letter in level_letters[level]:
            manifest_file_unicode.write('                <gather_letter count="1">%s</gather_letter>\n' % letter)
        for word in level_words[level]:
            manifest_file.write('                <gather_word count="1">%s</gather_word>\n' % word)
        manifest_file.write('            </req>\n')
        manifest_file.write('        </level>\n')
        level_number += 1
    manifest_file.write("    </levels>\n")
    
def numberFileName(number):
    try:
        return str(number)
    except:
        name = 'number_%s' % len(number_names)
        number_names[name] = number
        return name

def letterFileName(letter):
    try:
        return letter.decode('ascii')
    except:
        name = 'letter_%s' % len(letter_names)
        letter_names[name] = letter
        return name

def wordFileName(word):
    try:
        return word.decode('ascii')
    except:
        name = 'word_%s' % len(word_names)
        word_names[name] = word
        return name

manifest_only=False
if sys.argv[1] == "--manifest-only":
    manifest_only=True
    sys.argv = sys.argv[1:]
    
rtl=False
if sys.argv[1] == "--rtl":
    rtl=True
    sys.argv = sys.argv[1:]
    
locale_name = sys.argv[1]
characters_file = sys.argv[2]
words_file = sys.argv[3]

character_exposure = set()
character_order = list()

letter_values = dict()
word_values = dict()

number_names = dict()
letter_names = dict()
word_names = dict()

characters = read_data(characters_file)
words = read_data(words_file)

level_letters = list()
level_words = list()

value = 0
level=0
#level_words.append([])
for word in words:
    level_words.append((word,))
    add_letters = list()
    word_values[word] = value
    value += 1
    print "Evaluating word %s" % word
    for letter in word.decode("utf-8"):
        if letter not in character_exposure:
            add_letters.append(letter)
            character_order.append(letter)
            character_exposure.add(letter)
            # if this is the first word, do one letter per level
            if len(word_values) == 1:
                level_words.insert(0, [])
                level_letters.append(add_letters)
                add_letters = list()
        if letter not in letter_values:
            letter_values[letter] = 1
        else:
            letter_values[letter] += 1;
    if len(word_values) > 1:
        level_letters.append(add_letters)
level_letters.append([])

for letter in characters:
    if letter not in letter_values:
        letter_values[letter] = 0

if os.path.exists(locale_name):
    if not os.path.isdir(locale_name):
        print "Path %s exists and is not a directory!" % locale_name
        exit(1)
else:
    os.mkdir(locale_name)
    os.makedirs(os.path.join(locale_name, 'sounds'))
    os.makedirs(os.path.join(locale_name, 'textures', 'numbers'))
    os.makedirs(os.path.join(locale_name, 'textures', 'letters', 'sprites'))
    os.makedirs(os.path.join(locale_name, 'textures', 'letters', 'blocks'))
    os.makedirs(os.path.join(locale_name, 'textures', 'words', 'sprites'))
    os.makedirs(os.path.join(locale_name, 'textures', 'words', 'blocks'))
    
manifest_file_name = os.path.join(locale_name, 'manifest.xml')
manifest_file = open(manifest_file_name, 'w')
from codecs import getwriter
UnicodeWriter = getwriter('utf-8')
manifest_file_unicode = UnicodeWriter(manifest_file)

print "Creating %s" % manifest_file_name
print_header()
print_numbers()
print_letters()
print_words()
print_levels()
print_footer()

manifest_file.close()

#generate_letter_images()
#generate_word_images()
