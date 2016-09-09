#!/usr/bin/python

import sys

counts = dict()
scores = dict();
minlen = 0;

for game in sys.stdin.readlines():
    game = game.strip().lower()
    if len(game) == 0: continue
    for letter in game:
        if letter not in counts:
            counts[letter] = 1
        else:
            counts[letter] += 1;
    letters = set(game)
    scores[game] = 0;
    if len(letters) < minlen or minlen == 0:
        minlen = len(letters)
            
print "Letter frequency:"
for letter in sorted(counts, key=counts.get, reverse=True):
    count = counts[letter]
    print "%s\t%s" % (letter, count)

for game in scores:
    value = 0
    for letter in game:
        value += counts[letter]
    scores[game] = value / (len(game) * (len(set(game))-minlen+1))
    
print "\nWord Scores:"
for game in sorted(scores, key=scores.get, reverse=True):
    score = scores[game]
    print "%s\t%s" % (game, score)
