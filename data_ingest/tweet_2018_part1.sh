YEAR=2018
MONTH_END=4
for ((i=1;i<=MONTH_END;i++)); do
    M="0${i}"
    FNAME="${YEAR}_${M}.tar"
    LINK="https://archive.org/download/archiveteam-twitter-stream-2018-${M}/archiveteam-twitter-stream-2018-${M}.tar"
    curl -L -s -o ${FNAME} ${LINK}
done
