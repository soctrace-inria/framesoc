#!/bin/bash                                                                                                                                                         
# Usage help function
function usage() {
    echo "usage: ./change_version.sh <new version>"
    echo "<new version>: new version in the format x.y.z (where x,y,z are positive integers)"
}                                     
                     
# Parameter check            
if [ $# -lt 1 ]; then
    usage
    exit
fi

NEW=$1

if [[ ! $NEW =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Wrong version format: $NEW"
    usage
    exit
fi

# Constants
FEATURE="../fr.inria.soctrace.features.framesoc/feature.xml"
CATEGORY="../fr.inria.soctrace.maven.repository/category.xml"
BV="Bundle-Version:.*.qualifier"
NBV="Bundle-Version: ${NEW}.qualifier"

# Body
echo "Updating MANIFEST.MF in all plugins"
find .. -wholename "*META-INF/MANIFEST.MF" | grep -v "linuxtools" | xargs sed -i s/"$BV"/"$NBV"/

echo "Updating framesoc feature.xml"
sed -i s/"version=\".*.qualifier\""/"version=\"${NEW}.qualifier\""/ $FEATURE 

echo "Updating repository category.xml"
sed -i s/"\_.*.qualifier.jar"/"\_${NEW}.qualifier.jar"/ $CATEGORY
sed -i s/"version=\".*.qualifier\""/"version=\"${NEW}.qualifier\""/ $CATEGORY

echo "Update pom.xml in all modules"
mvn versions:set -DnewVersion="${NEW}-SNAPSHOT" -DgenerateBackupPoms=false
