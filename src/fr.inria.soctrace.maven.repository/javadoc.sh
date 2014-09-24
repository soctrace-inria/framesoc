#!/bin/bash
ant -buildfile javadoc.xml | grep --color -E '^|warning'
