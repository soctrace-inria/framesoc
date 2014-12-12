File Edit Options Buffers Tools Sh-Script Help                                                                                                                                                                                               
#!/bin/bash -x                                                                                                                                                                                                                               

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
sed s/"version=\".*.qualifier\""/"version=\"${NEW}.qualifier\""/ <$FEATURE > tmp
mv $FEATURE $FEATURE.bkp
mv tmp $FEATURE
rm $FEATURE.bkp

echo "Updating repository category.xml"
sed s/"\_.*.qualifier.jar"/"\_${NEW}.qualifier.jar"/ <$CATEGORY > tmp
sed s/"version=\".*.qualifier\""/"version=\"${NEW}.qualifier\""/ <tmp > $CATEGORY
rm tmp

echo "Update pom.xml in all modules"
mvn versions:set -DnewVersion="${NEW}-SNAPSHOT" -DgenerateBackupPoms=false
