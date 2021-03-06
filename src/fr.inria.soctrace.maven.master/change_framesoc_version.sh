#!/bin/bash
                                                                                        
#####################################################################
# Change the version number to framesoc.
# 
# This is simply a convenience shortcut for the change_version.sh
# script.
#
# IMPORTANT
#
# It works having the framesoc and the soctrace-inria.github.io 
# clones in the same root directory:
# ./somedir/framesoc
# ./somedir/soctrace-inria.github.io
#
# Author: Generoso Pagano
#####################################################################

SCRIPT="../../../soctrace-inria.github.io/updatesite/change_version.sh"
MASTER="."
FEATURE="../fr.inria.soctrace.features.framesoc/feature.xml"
CATEGORY="../fr.inria.soctrace.maven.repository/category.xml"

# parameter check is done in the change_version.sh script
NEW=$1
$SCRIPT $MASTER $FEATURE $CATEGORY $NEW

