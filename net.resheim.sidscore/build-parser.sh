#!/bin/bash
antlr4 -o src/main/java/net/resheim/sidscore/parser \
    -package net.resheim.sidscore.parser SIDScoreLexer.g4
antlr4 -o src/main/java/net/resheim/sidscore/parser \
    -package net.resheim.sidscore.parser SIDScoreParser.g4
