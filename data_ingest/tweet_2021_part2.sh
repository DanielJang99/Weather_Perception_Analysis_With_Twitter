YEAR=2021
MONTH_END=8
for ((i=3;i<=MONTH_END;i++)); do
    M="0${i}"
    if [ $i -eq 3 -o $i -eq 5 -o $i -eq 7 -o $i -eq 8 ]; then 
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
        LINK="https://archive.org/download/archiveteam-twitter-stream-2021-${M}/twitter-stream-2021-${M}-${D}.zip"
        FNAME="${YEAR}_${M}_${D}.zip"
        curl -L -s -o ${FNAME} "${LINK}" &
    done
done
