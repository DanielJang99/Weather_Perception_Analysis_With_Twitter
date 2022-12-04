#!/bin/bash

YEAR=2021
MONTH_END=2
for ((i=1;i<=MONTH_END;i++)); do
    M="0${i}"
    FNAME="${YEAR}_${M}.zip"
    LINK="https://archive.org/compress/archiveteam-twitter-stream-2021-${M}/formats=ZIP&file=/archiveteam-twitter-stream-2021-${M}.zip"
    curl -L -s -o ${FNAME} "${LINK}" & 
done
