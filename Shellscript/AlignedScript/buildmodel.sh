#!/bin/bash
echo "align in progress.."
array=($(ls | grep .sto | sort -n -k 1))

for i in ${array[@]}
do
    #printf '%d.sto' $i
    echo $i
    clustalo -i $i --force -o $i
    #clustalo -i $i.sto --force -o $i.sto --outfmt=stockholm
    
done
echo "align success."

for i in ${array[@]}
do
    #printf '%d.sto' $i
    echo $i
    var=$i+""
    filename=${var%.*}
    hmmbuild $filename.hmm $i
    
done


cat *.hmm > hmmdb
hmmpress hmmdb
rm -rf *.hmm
echo "done"

echo "Making queires..."
mkdir result
./phmmresult.sh
