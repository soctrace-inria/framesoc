#!/bin/bash
#
# Upload update site.
#
# This script must be in the root of the maven.repository project.
#
# Author: Generoso Pagano
#

DEFAULT="./../../../framesoc.gh-pages"
REPOSITORY="./target/repository"
SITE_CONTENT="features plugins artifacts.jar content.jar"

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

# check if repository exits
if [ ! -d "${REPOSITORY}" ]; then
  echo "Directory ${REPOSITORY} does not exist. Generate it or fix the path in the script."
  exit 
fi

echo "Updating site in ${SITE} and uploading it"

# remove old content
cd ${SITE}
rm -rf $SITE_CONTENT

# Copy new content
cd -
for F in ${SITE_CONTENT}; do
  cp -r "${REPOSITORY}/${F}" "${SITE}"
done

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
