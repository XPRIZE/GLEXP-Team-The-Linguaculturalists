#!/usr/bin/python

import os
import sys
import math
import subprocess

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
    <map src="locales/%(locale)s/map.tmx" />
    <music src="locales/%(locale)s/sounds/background.mp3" />

    <!-- Default blocks -->
    <inventory level="1" size="1x2" name="Barn" col="37" row="43" block="locales/%(locale)s/textures/biginventory.png" />
    <market level="4" size="2x1" name="Market" col="42" row="40" block="locales/%(locale)s/textures/bigmarket.png" />
    <workshop level="6" name="Workshop" col="38" row="40" block="locales/%(locale)s/textures/workshop.png" />

    <!-- People used in the marketplace and player's avatar -->
    <people>
        <person name="Dave" texture="locales/common/textures/persons/Dave.png" />
        <person name="Roxanne" texture="locales/common/textures/persons/Roxanne.png" />
        <person name="Ben" texture="locales/common/textures/persons/Ben.png" />
        <person name="Laura" texture="locales/common/textures/persons/Laura.png" />
    </people>

    <!-- Mini-games -->
    <games>
        <game name="Blue Tent"  type="wordmatch"  level="6" buy="300" construct="120" time="300" reward="1.5" sprite="locales/%(locale)s/textures/games/sprites/minigame1.png" block="locales/%(locale)s/textures/games/blocks/minigame1.png" />
        <game name="Green Tent" type="imagematch" level="12" buy="500" construct="300" time="600" reward="2.0" sprite="locales/%(locale)s/textures/games/sprites/minigame2.png" block="locales/%(locale)s/textures/games/blocks/minigame2.png" />
    </games>

    <!-- Decorations -->
    <decorations>
        <decoration size="1x1" name="Lamp Post"  level="5" buy="200" sprite="locales/common/textures/decorations/sprites/lamp.png" block="locales/common/textures/decorations/blocks/lamp.png" />
        <decoration size="1x1" name="Small Tree" level="10" buy="500" sprite="locales/common/textures/decorations/sprites/smalltree.png" block="locales/common/textures/decorations/blocks/smalltree.png" />
        <decoration size="2x2" name="Fountain"   level="20" buy="1000" sprite="locales/common/textures/decorations/sprites/fountain.png" block="locales/common/textures/decorations/blocks/fountain.png" />
    </decorations>
""" % {'locale': locale_name, 'lang': lang, 'display': display})

def print_footer():
    manifest_file.write("</locale>\n")

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
        time = round(math.log(letter_value+1, 1.2)*60, -1) or 60
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
        manifest_file.write('        <letter name="%(name)s" buy="%(buy)d" sell="%(sell)d"  points="%(points)d" time="%(time)d" sprite="locales/%(locale)s/textures/letters/sprites/%(filename)s.png" block="locales/%(locale)s/textures/letters/blocks/%(filename)s.png" sound="locales/%(locale)s/sounds/%(filename)s-name.ogg" phoneme="locales/%(locale)s/sounds/%(filename)s-phoneme.ogg">%(letter)s</letter>\n' % context)

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
        time = round(math.pow(word_values[word]+1, 1.5), -1)*60 or 120
        construct = round(math.pow(word_values[word]+1, 1.8), -1)*90 or 180
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
        manifest_file.write('        <word name="%(name)s" size="1x1" buy="%(buy)d" sell="%(sell)d" points="%(points)d" construct="%(construct)d" time="%(time)d" sprite="locales/%(locale)s/textures/words/sprites/%(filename)s.png" block="locales/%(locale)s/textures/words/blocks/%(filename)s.png" sound="locales/%(locale)s/sounds/%(filename)s.ogg">%(word)s</word>\n' % context)
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
        market = round(math.log(level_number, len(word_values))*4) or 0
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

rtl=False
if sys.argv[1] == "-rtl":
    sys.argv = sys.argv[1:]
    rtl=True
    
locale_name = sys.argv[1]
characters_file = sys.argv[2]
words_file = sys.argv[3]

character_exposure = set()
character_order = list()

letter_values = dict()
word_values = dict()

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
print_letters()
print_words()
print_levels()
print_footer()

manifest_file.close()

#generate_letter_images()
#generate_word_images()
