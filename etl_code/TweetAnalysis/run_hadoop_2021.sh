for ((i=10;i<=12;i++)); do
    if [ $i -le 9 ]; then
        M="0${i}"
    else
        M="${i}"
    fi
    hadoop jar TweetAnalysis.jar TweetAnalysis -libjars VaderSentimentv1.jar /user/hsj276/tweets/2021/${M}.json /user/hsj276/sentiment_data/2021_${M}
done 
