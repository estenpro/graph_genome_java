
runs=$1
index=$2
reads=$3
out=$4
total_fuzzy_0=0
total_fuzzy_1=0
total_fuzzy_2=0
total_po_msa=0

for i in `seq 1 $runs`;
do
    echo "Run $i"
    sequence=$(tail -$i $reads | head -1)
    #java -jar -Xmx4096m -Xms4096m ../target/graph-genome.jar align --index=$index -as=$sequence > temp.stats
    #fuzzy_time_0=($(tail -2 temp.stats | head -1))
    #total_fuzzy_0=$(($total_fuzzy_0+${fuzzy_time_0[1]}))
    #echo "Finished fuzzy, em=0: ${fuzzy_time_0[1]}"
    #echo "Total: $total_fuzzy_0"
    #java -jar -Xmx4096m -Xms4096m ../target/graph-genome.jar align --index=$index -as=$sequence -em=1 > temp.stats
    #fuzzy_time_1=($(tail -2 temp.stats | head -1))
    #total_fuzzy_1=$(($total_fuzzy_1+${fuzzy_time_1[1]}))
    #echo "Finished fuzzy, em=1: ${fuzzy_time_1[1]}"
    #java -jar -Xmx4096m -Xms4096m ../target/graph-genome.jar align --index=$index -as=$sequence -em=2 > temp.stats
    #fuzzy_time_2=($(tail -2 temp.stats | head -1))
    #total_fuzzy_2=$(($total_fuzzy_2+${fuzzy_time_2[1]}))
    #echo "Finished fuzzy, em=2: ${fuzzy_time_2[1]}"
    java -jar -Xmx4096m -Xms4096m ../target/graph-genome.jar align --index=$index  -as=$sequence -t=po_msa > temp.stats
    po_msa_time=($(tail -2 temp.stats | head -1))
    total_po_msa=$(($total_po_msa+${po_msa_time[1]}))
    echo "Finished po-msa: ${po_msa_time[1]}"
done 

echo "Avg fuzzy, em=0: $(($total_fuzzy_0 / $runs))" > $out
echo "Avg fuzzy, em=1: $(($total_fuzzy_1 / $runs))" >> $out
echo "Avg fuzzy, em=2: $(($total_fuzzy_2 / $runs))" >> $out
echo "Avg po-msa: $(($total_po_msa / $runs))" >> $out
