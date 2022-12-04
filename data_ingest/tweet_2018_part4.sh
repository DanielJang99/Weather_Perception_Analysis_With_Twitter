YEAR=2018
MONTH_END=12
for ((i=11;i<=MONTH_END;i++)); do
    M="${i}"
    if [ $i -eq 12 ]; then 
        DAYS_END=31
    else
        DAYS_END=30
    fi
    for ((j=1;j<=DAYS_END;j++)); do
        if [ $j -le 9 ]; then
            D="0${j}"
        else
            D="${j}"
        fi
        FNAME="${YEAR}_${M}_${D}.tar"
        LINK="https://archive.org/download/archiveteam-twitter-stream-2018-${M}/twitter_stream_2018_${M}_${D}.tar"
        curl -L -s -o ${FNAME} ${LINK} & 
    done
done
wait 
ls 2018_11_*.tar |xargs -n1 tar -xf &
ls 2018_12_*.tar |xargs -n1 tar -xf &
