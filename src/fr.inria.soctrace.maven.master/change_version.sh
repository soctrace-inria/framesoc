#!/bin/bash                                                                                                                                                                                                                               
if [ $# -lt 1 ]; then
    echo "usage: change_version.sh <new version>"
    echo "<new version>: new version in the format x.y.z"
    exit
fi

NEW=$1

FEATURE="../fr.inria.soctrace.features.framesoc/feature.xml"
CATEGORY="../fr.inria.soctrace.maven.repository/category.xml"

BV="Bundle-Version:.*.qualifier"
NBV="Bundle-Version: ${NEW}.qualifier"

echo "Updating MANIFEST.MF in all plugins"
find .. -wholename "*META-INF/MANIFEST.MF" | grep -v "linuxtools" | xargs sed -i s/"$BV"/"$NBV"/

echo "Updating framesoc feature.xml"
sed -i s/"version=\".*.qualifier\""/"version=\"${NEW}.qualifier\""/ $FEATURE 

echo "Updating repository category.xml"
sed -i s/"\_.*.qualifier.jar"/"\_${NEW}.qualifier.jar"/ $CATEGORY
sed -i s/"version=\".*.qualifier\""/"version=\"${NEW}.qualifier\""/ $CATEGORY

echo "Update pom.xml in all modules"
mvn versions:set -DnewVersion="${NEW}-SNAPSHOT" -DgenerateBackupPoms=false
