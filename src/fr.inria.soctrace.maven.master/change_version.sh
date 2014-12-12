File Edit Options Buffers Tools Sh-Script Help                                                                                                                                                                                               
#!/bin/bash -x                                                                                                                                                                                                                               

if [ $# -lt 1 ]; then
    echo "usage: change_version.sh <new version>"
    echo "<new version>: new version in the format x.y.z"
    exit
fi

NEW=$1

FEATURE="../fr.inria.soctrace.features.framesoc/feature.xml"
CATEGORY=""

BV="Bundle-Version:.*.qualifier"
NBV="Bundle-Version: ${NEW}.qualifier"

echo "Updating MANIFEST.MF in all plugins"
find .. -wholename "*META-INF/MANIFEST.MF" | grep -v "linuxtools" | xargs sed -i s/"$BV"/"$NBV"/

echo "Updating framesoc feature.xml"
sed s/"version=\".*.qualifier\""/"version=\"${NEW}.qualifier\""/ <$FEATURE > tmp
mv $FEATURE $FEATURE.bkp
mv tmp $FEATURE
rm $FEATURE.bkp

exit
echo "Update pom.xml in all modules"
mvn versions:set -DnewVersion="${NEW}-SNAPSHOT" -DgenerateBackupPoms=false
