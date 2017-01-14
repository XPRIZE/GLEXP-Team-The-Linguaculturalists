#!/usr/bin/python

import os
import sys
import codecs
sys.stdin = codecs.getreader('UTF-8')(sys.stdin);

from lxml import etree

locale_path = sys.argv[1]
if locale_path.endswith('/'):
    locale_path = locale_path[:-1]
locale = os.path.basename(locale_path)
manifest = os.path.join(locale_path, 'manifest.xml')
locale_base = sys.argv[2]
ERROR = 0;

def warn(msg):
    global ERROR
    print msg
    ERROR += 1

def exists(filename):
    local_filename = filename.replace("locales/%s/"%locale, "")
    if os.path.exists(os.path.join(locale, local_filename)):
        return True
    if os.path.exists(os.path.join(locale_base, filename)):
        return True
        
def check_letters(letters):
    for letter in letters:
        name = letter.attrib['name']
        sprite = letter.attrib['sprite']
        if not exists(sprite):
            warn("Letter %s Sprite not found: %s" % (name, sprite))
        block = letter.attrib['block']
        if not exists(block):
            warn("Letter %s Block not found: %s" % (name, block))
        sound = letter.attrib['sound']
        if not exists(sound):
            warn("Letter %s Sound not found: %s" % (name, sound))
        phoneme = letter.attrib['phoneme']
        if not exists(phoneme):
            warn("Letter %s Phoneme not found: %s" % (name, phoneme))
    
def check_words(words):
    for word in words:
        name = word.attrib['name']
        sprite = word.attrib['sprite']
        if not exists(sprite):
            warn("Word %s Sprite not found: %s" % (name, sprite))
        block = word.attrib['block']
        if not exists(block):
            warn("Word %s Block not found: %s" % (name, block))
        sound = word.attrib['sound']
        if not exists(sound):
            warn("Word %s Sound not found: %s" % (name, sound))
    
def check_tour(tour):
    for stop in tour:
        name = stop.attrib['id']
        index = 0
        for msg in stop:
            index += 1
            sound = msg.attrib['sound']
            if sound and not exists(sound):
                warn("Tour stop '%s' message %s Sound not found: %s" % (name, index, sound))
    
print "Checking %s against %s" % (manifest, locale_base)
data = open(manifest, 'r')
tree = etree.parse(data)
root = tree.getroot()

check_letters(root.find("letters"))
check_words(root.find("words"))
check_tour(root.find("tour"))

if ERROR:
    print "%s errors" % ERROR
else:
    print "Ok!"
exit(ERROR)
