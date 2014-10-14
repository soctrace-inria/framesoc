#!/bin/bash -x

DEFAULT="./../../../framesoc.gh-pages"

# check args
if [ $# -lt 1 ]
then 
  echo "Specify the gh-pages checkout directory or use -d for default"
  exit
fi

OPT=$1
if [ "x${OPT}" == "x-d" ]
then
  GHPAGES=${DEFAULT}
else
  GHPAGES="${OPT}"
fi

SITE="${GHPAGES}/updatesite"

echo "Updating site in ${SITE} and uploading it"

rm -rf ${SITE}/* 
cp -r features plugins web artifacts.jar content.jar index.html site.xml ${SITE}

cd ${SITE}

git add --all
git commit -m "New update site uploaded"
git push origin gh-pages
