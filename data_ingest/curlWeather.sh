for ((i=2018;i<=2021;i++)); do
    for ((j=1;j<=12;j++)); do
        if [ $j -le 9 ];
        then
            M="0${j}"
        else
            M="${j}"
        fi
        FNAME="weather_${i}${M}.csv"
        LINK="https://www.ncei.noaa.gov/access/monitoring/climate-at-a-glance/county/mapping/110-tavg-${i}${M}-1.csv"
        curl -L -o ${FNAME} ${LINK}
    done
done