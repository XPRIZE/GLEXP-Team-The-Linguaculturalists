#!/usr/bin/python

import sys

BASE_TILE_HEIGHT = 64
BASE_TILE_WIDTH = 64

size = sys.argv[1]

(cols, rows, height) = size.split('x')
cols = float(cols)
rows = float(rows)
height = float(height)

print "cols=%s, rows=%s, height=%s" % (cols, rows, height)

diagonal = (cols/2) + (rows/2)

print "diagonal: %s" % diagonal

width = BASE_TILE_WIDTH * diagonal

height = (BASE_TILE_HEIGHT*height/2) + (BASE_TILE_HEIGHT/2 * diagonal)

print "size: %sx%s" % (width, height)
