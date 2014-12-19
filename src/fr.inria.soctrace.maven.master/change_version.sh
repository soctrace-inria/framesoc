#!/bin/bash
                                                                                        
#####################################################################
# Change the version number to a given project, modifying both Maven
# and Eclipse configuration files.
#
# Author: Generoso Pagano
#####################################################################

# Constants
BV="Bundle-Version:.*.qualifier"
NBV="Bundle-Version: ${NEW}.qualifier"

# Input parameters
MASTER=""
FEATURE=""
CATEGORY=""
NEW=""
                                             
# Usage help function
function usage() {
    echo "usage: ./change_version.sh <maven master> <feature> <category> <new version>"
    echo "<maven master>: absolute path of the maven master project folder"
    echo "<feature>: absolute path of the project feature.xml"
    echo "<category>: absolute path of the project update site category.xml"
    echo "<new version>: new version in the format x.y.z (where x,y,z are positive integers)"
}                                     

# Return 0 if the params are OK, 1 otherwise
function check_params() {                    
    
    # Parameter check            
    if [ $# -lt 4 ]; then
	return 1
    fi

    MASTER=$1
    FEATURE=$2
    CATEGORY=$3
    NEW=$4

    if [ ! -d "$MASTER" ]; then
	echo "Error: directory $MASTER does not exist."
	return 1
    fi

    if [ ! -f "$FEATURE" ]; then
	echo "Error: file $FEATURE does not exist."
	return 1
    fi

    if [ ! -f "$CATEGORY" ]; then
	echo "Error: file $CATEGORY does not exist."
	return 1
    fi

    if [[ ! $NEW =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
	echo "Wrong version format: $NEW"
	return 1
    fi

    return 0
}

function print_params() {
    echo "MASTER: $MASTER"
    echo "FEATURE: $FEATURE"
    echo "CATEGORY: $CATEGORY"
    echo "VERSION: $NEW"
}

function main() {

    OLDDIR=`pwd`
    cd $MASTER

    echo "Updating MANIFEST.MF in all plugins"
    find .. -wholename "*META-INF/MANIFEST.MF" | grep -v "linuxtools" | xargs sed -i s/"$BV"/"$NBV"/

    echo "Updating framesoc feature.xml"
    sed -i s/"version=\".*.qualifier\""/"version=\"${NEW}.qualifier\""/ $FEATURE 

    echo "Updating repository category.xml"
    sed -i s/"\_.*.qualifier.jar"/"\_${NEW}.qualifier.jar"/ $CATEGORY
    sed -i s/"version=\".*.qualifier\""/"version=\"${NEW}.qualifier\""/ $CATEGORY

    echo "Update pom.xml in all modules"
    mvn versions:set -DnewVersion="${NEW}-SNAPSHOT" -DgenerateBackupPoms=false

    cd $OLDDIR
}

# ENTRY POINT

check_params $@
if [ $? -ne 0 ]; then
    usage
    exit
fi

print_params

main

