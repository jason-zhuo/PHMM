#!/bin/bash
echo "check in progress.."
mkdir result
arrayQuery=($(ls | grep .query | sort -n -k 1))
 
for i in ${arrayQuery[@]}
do
    printf 'query %s\n' $i
    var=$i+""
    filename=${var%.*}
    hmmscan hmmdb $i > result/out_$filename
    
done
python resultStat.py result
echo "done"
