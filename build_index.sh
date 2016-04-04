#!/bin/bash

if [ -d "target" ]; then
    java -jar target/graph-genome.jar index "$@"
else
    echo "No target files detected. Build the project with >mvn clean install"
fi