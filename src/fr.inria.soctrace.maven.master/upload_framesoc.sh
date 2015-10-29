#!/bin/bash
                                                                                        
#####################################################################
# Upload Framesoc update site.
# 
# This is simply a convenience shortcut for the upload-site.sh script.
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

SCRIPT="../../../soctrace-inria.github.io/updatesite/upload-site.sh"
REPO="../../framesoc/src/fr.inria.soctrace.maven.repository/target/repository/"
PROJECT="framesoc"
$SCRIPT $REPO $PROJECT
