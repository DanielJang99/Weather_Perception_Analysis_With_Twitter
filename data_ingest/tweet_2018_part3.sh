YEAR=2018
MONTH_END=10
for ((i=8;i<=MONTH_END;i++)); do
    if [ $i -le 9 ]; then
        M="0${i}"
    else
        M="${i}"
    fi
    if [ $i -eq 10 ]; then
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
        LINK="https://archive.org/download/archiveteam-twitter-stream-2018-${M}/twitter-2018-${M}-${D}.tar"
        curl -L -s -o ${FNAME} ${LINK} &
    done
done
