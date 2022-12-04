YEAR=2018
MONTH_END=8
for ((i=5;i<=MONTH_END;i++)); do
    M="0${i}"
    FNAME="${YEAR}_${M}.zip"
    LINK="https://archive.org/compress/archiveteam-twitter-stream-2018-${M}/formats=TAR&file=/archiveteam-twitter-stream-2018-${M}.zip"
    curl -L -s -o ${FNAME} ${LINK}
done
