#!/bin/bash

num=50
mismatches=0
prob=0.00

for var in "$@"
do
    /bin/bash test-dataset.sh $var $num $mismatches $prob
done
