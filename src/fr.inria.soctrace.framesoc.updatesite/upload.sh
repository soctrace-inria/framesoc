#!/bin/bash -x

# path of gh-pages checkout
GHPAGES="./../../framesoc.gh-pages"

rm -rf ${GHPAGES}/* 
cp features plugins web artifacts.jar content.jar index.html site.xml ${GHPAGES}

cd ${GHPAGES}

git add --all
git commit -m "New update site uploaded"
git push origin gh-pages
