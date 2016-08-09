#!/usr/bin/python

import sys

counts = dict()
scores = dict();
minlen = 0;

def read_data(source):
    fh = open(source, "r")
    for line in fh.readlines():
        line = line.strip()
        if line.startswith("#") or line == "":
            continue
        yield line

words_file = sys.argv[1]
words = read_data(words_file)

for word in read_data(words_file):
    word = word.strip().lower()
    if len(word) == 0: continue
    for letter in word:
        if letter not in counts:
            counts[letter] = 1
        else:
            counts[letter] += 1;
    letters = set(word)
    scores[word] = 0;
    if len(letters) < minlen or minlen == 0:
        minlen = len(letters)
            
for letter in sorted(counts, key=counts.get, reverse=True):
    count = counts[letter]

for word in scores:
    value = 0
    for letter in word:
        value += counts[letter]
    scores[word] = value / (len(word) * (len(set(word))-minlen+1))
    
for word in sorted(scores, key=scores.get, reverse=True):
    score = scores[word]
    print "%s" % (word, )
