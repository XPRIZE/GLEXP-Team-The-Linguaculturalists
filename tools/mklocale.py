#!/usr/bin/python

import os
import sys
import math
import codecs
sys.stdin = codecs.getreader('UTF-8')(sys.stdin);
#sys.stdout = codecs.getwriter('UTF-8')(sys.stdout);

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
    
    print """<?xml version="1.0" encoding="utf-8"?>
<locale name="%(locale)s" lang="en_us" display_name="US English, Rural Setting">
    <shell src="locales/common/textures/gameui.png" />
    <map src="locales/%(locale)s/map.tmx" />
    <music src="locales/%(locale)s/sounds/background.mp3" />

    <!-- Default blocks -->
    <inventory size="1x2" name="Barn" col="37" row="43" block="locales/%(locale)s/textures/biginventory.png" />
    <market size="2x1" name="Market" col="42" row="40" block="locales/%(locale)s/textures/bigmarket.png" />
    <workshop name="Workshop" col="38" row="40" block="locales/%(locale)s/textures/workshop.png" />

    <!-- People used in the marketplace and player's avatar -->
    <people>
        <person name="Dave" texture="locales/common/textures/persons/Dave.png" />
        <person name="Roxanne" texture="locales/common/textures/persons/Roxanne.png" />
        <person name="Ben" texture="locales/common/textures/persons/Ben.png" />
        <person name="Laura" texture="locales/common/textures/persons/Laura.png" />
    </people>

    <!-- Mini-games -->
    <games>
        <game name="Blue Tent"  type="wordmatch"  points="100" buy="300" construct="120" time="300" reward="1.5" sprite="locales/%(locale)s/textures/games/sprites/minigame1.png" block="locales/%(locale)s/textures/games/blocks/minigame1.png" />
        <game name="Green Tent" type="imagematch" points="500" buy="500" construct="300" time="600" reward="2.0" sprite="locales/%(locale)s/textures/games/sprites/minigame2.png" block="locales/%(locale)s/textures/games/blocks/minigame2.png" />
    </games>

    <!-- Decorations -->
    <decorations>
        <decoration size="1x1" name="Lamp Post"  points="100" buy="200" sprite="locales/common/textures/decorations/sprites/lamp.png" block="locales/common/textures/decorations/blocks/lamp.png" />
        <decoration size="1x1" name="Small Tree" points="300" buy="500" sprite="locales/common/textures/decorations/sprites/smalltree.png" block="locales/common/textures/decorations/blocks/smalltree.png" />
        <decoration size="2x2" name="Fountain"   points="500" buy="1000" sprite="locales/common/textures/decorations/sprites/fountain.png" block="locales/common/textures/decorations/blocks/fountain.png" />
    </decorations>
""" % {'locale': locale_name}

def print_footer():
    print "</locale>"

def print_letters():
    print "    <!-- Alphabet -->"
    print "    <letters>"
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
        time = round(math.log(letter_value+1, 1.2)*60, -1) or 60
        context = {
            'name': letter, 
            "buy": buy, 
            'points': points, 
            'sell': sell, 
            'time': time,
            'locale': locale_name
        }
        print '        <letter name="%(name)s" buy="%(buy)d" sell="%(sell)d"  points="%(points)d" time="%(time)d" sprite="locales/%(locale)s/textures/letters/sprites/%(name)s.png" block="locales/%(locale)s/textures/letters/blocks/%(name)s.png" sound="locales/%(locale)s/sounds/%(name)s-name.ogg" phoneme="locales/%(locale)s/sounds/%(name)s-phoneme.ogg">%(name)s</letter>' % context
    print "    </letters>"

def print_words():
    print "\n    <!-- Words -->"
    print "    <words>"
    for word in sorted(words):
        if word not in word_values:
            word_values[word] = len(word_values)
        buy = round(math.pow(word_values[word]+1, 1.1)*100, -1) or 100
        sell = round(math.pow(word_values[word]+1, 1.001)*10, -1) or 10
        points = round(math.log(word_values[word]+1)*50, 1) or 10
        time = round(math.pow(word_values[word]+1, 1.5), -1)*60 or 120
        construct = round(math.pow(word_values[word]+1, 1.8), -1)*90 or 180
        context = {
            'name': word, 
            "buy": buy, 
            'points': points, 
            'sell': sell, 
            'time': time, 
            'construct': construct,
            'locale': locale_name
        }
        print '        <word name="%(name)s" size="1x1" buy="%(buy)d" sell="%(sell)d" points="%(points)d" construct="%(construct)d" time="%(time)d" sprite="locales/%(locale)s/textures/words/sprites/%(name)s.png" block="locales/%(locale)s/textures/words/blocks/%(name)s.png" sound="locales/%(locale)s/sounds/%(name)s.ogg">%(name)s</word>' % context
    print "    </words>"

def print_levels():
    print "\n    <!-- Levels -->"
    print "    <levels>"
    level_number = 1
    market = 0;
    market
    for level, words in enumerate(level_words):
        coins = round(math.pow(level_number, 0.6)*100, -2) or 100
        points = round(math.pow(level_number, 0.6)*200, -1) or 200
        market = round(math.log(level_number, len(word_values))*4) or 0
        print '        <level name="%(name)s" coins="%(coins)d" points="%(points)d" market="%(market)d">' % {'name': level_number, 'coins': coins, 'points': points, 'market': market}
        print '            <intro>'
        print '            </intro>'
        print '            <letters>%s</letters>' % ','.join(level_letters[level])
        print '            <words>%s</words>' % ','.join(level_words[level])
        print '            <help>'
        print '                <letters>%s</letters>' % ','.join(level_letters[level])
        print '                <words>%s</words>' % ','.join(level_words[level])
        print '            </help>'
        print '            <req>'
        for letter in level_letters[level]:
            print '                <gather_letter count="1">%s</gather_letter>' % letter
        for word in level_words[level]:
            print '                <gather_word count="1">%s</gather_word>' % word
        print '            </req>'
        print '        </level>'
        level_number += 1
    print "    </levels>"
    
locale_name = sys.argv[1]
characters_file = sys.argv[2]
words_file = sys.argv[3]

character_exposure = set()
character_order = list()

letter_values = dict()
word_values = dict()

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
    for letter in word:
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
    
print_header()
print_letters()
print_words()
print_levels()
print_footer()
