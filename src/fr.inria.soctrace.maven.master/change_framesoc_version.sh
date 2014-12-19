#!/bin/bash
                                                                                        
#####################################################################
# Change the version number to framesoc.
# 
# This is simply a convenience shortcut for the change_version.sh
# script.
#
# Author: Generoso Pagano
#####################################################################

SCRIPT="./change_version.sh"
MASTER="."
FEATURE="../fr.inria.soctrace.features.framesoc/feature.xml"
CATEGORY="../fr.inria.soctrace.maven.repository/category.xml"

# parameter check is done in the change_version.sh script
NEW=$1
$SCRIPT $MASTER $FEATURE $CATEGORY $NEW

