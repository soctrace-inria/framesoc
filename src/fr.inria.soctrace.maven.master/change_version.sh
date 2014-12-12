#!/bin/bash -x

if [ $# -lt 1 ]; then
    echo "usage: change_version.sh <new version>"
    echo "<new version>: new version in the format x.y.z"
    exit
fi

NEW=$1

BV="Bundle-Version:.*.qualifier"
NBV="Bundle-Version: ${NEW}.qualifier"

echo "Updating MANIFEST.MF in all plugins"
find .. -wholename "*META-INF/MANIFEST.MF" | grep -v "linuxtools" | xargs sed -i s/"$BV"/"$NBV"/

echo "Update pom.xml in all modules"
mvn versions:set -DnewVersion="${NEW}-SNAPSHOT"
