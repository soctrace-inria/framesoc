#!/bin/bash
#
# Upload update site.
#
# Put this script in the directory containing the generated update site
# and run it from that location.
#
# Author: Generoso Pagano
#

DEFAULT="./../../../framesoc.gh-pages"
SITE_CONTENT="features plugins web artifacts.jar content.jar index.html site.xml"

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

# remove old content
cd ${SITE}
rm -rf $SITE_CONTENT

# Copy new content
cd -
cp -r ${SITE_CONTENT} ${SITE}

# Upload new content if necessary
cd ${SITE}

CHANGES=`git status -s | wc -l`
if [ ${CHANGES} -gt 0 ]
then 
  echo "Uploading new update site"
else
  echo "There are no changes to upload"
  exit
fi

git add --all
git commit -m "New update site uploaded"
git push origin gh-pages
