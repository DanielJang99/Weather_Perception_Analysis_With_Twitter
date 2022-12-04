
YEAR=2020
MONTH_END=6
for ((i=1;i<=MONTH_END;i++)); do
    if [ $i -le 9 ]; then
        M="0${i}"
    else
        M="${i}"
    fi
    if [ $i -eq 1 -o $i -eq 3 -o $i -eq 5 -o $i -eq 7 -o $i -eq 8 -o $i -eq 10 -o $i -eq 12 ]; then 
        DAYS_END=31
    elif [ $i -eq 2 ];then 
        DAYS_END=28
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
	LINK="https://archive.org/download/archiveteam-twitter-stream-2020-${M}/twitter_stream_2020_${M}_${D}.tar"
	curl -L -s -o ${FNAME} "${LINK}" &
    done
done

