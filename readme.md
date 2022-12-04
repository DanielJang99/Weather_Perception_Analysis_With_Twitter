# Influence of Temperature Anomalies and Remarkability of Local Weather to Climate Change Opinions

In this project, I aimed to analyze the relationships among temperature anomalies, public perception of weather, and public belief in climate change.

A specific walkthrough of the steps I took for this project are detailed in [this medium post](https://medium.com/@hsj276/using-twitter-to-correlate-remarkability-of-local-weather-and-opinions-of-climate-change-80b95f170831).

## Architecture

![architecture](https://drive.google.com/uc?export=view&id=18jdPGbwW1d8bYTQ7IDnDFhz-GRHDYYEs)

## Directories

### data_ingest

This folder contains scripts that download monthly tweet archives and county-level temperature anomaly datasets using the linux **curl** command.

#### Tweet Archive

The following are shell scripts that curls monthly tweet archive provided by **_archive.org_**

| File Name               | Description                                                                                                                                                                     |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **tweet_2018_part1.sh** | Downloads archive of tweets posted from 2018 January - April, compressed to a single zip file for each month (_2018_01.zip_, _2018_02.zip_, _2018_03.zip_, _2018_04.zip_)       |
| **tweet_2018_part2.sh** | Downloads archive of tweets posted from 2018 May - August, compressed to a single zip file for each month (_2018_05.zip_, _2018_06.zip_, _2018_07.zip_, _2018_08.zip_)          |
| **tweet_2018_part3.sh** | Downloads archive of tweets posted from 2018 September - October, compressed to a single tar file for each day within the time period (_2018_09_01.tar_ ~ _2018_10_31.tar_)     |
| **tweet_2018_part4.sh** | Downloads archive of tweets posted from 2018 November - December, compressed to a single tar file for each day within the time period (_2018_11_01.tar_ ~ _2018_12_31.tar_)     |
| **tweet_2019_part1.sh** | Downloads archive of tweets posted from 2019 January - August, compressed to a single zip file for each day within the time period (_2019_01_01.zip_ ~ _2019_08_31.zip_)        |
| **tweet_2019_part2.sh** | Downloads archive of tweets posted from 2019 September - December, compressed to a single tar file for each day within the time period (_2019_10_01.tar_ ~ _2019_12_31.tar_)    |
| **tweet_2020_part1.sh** | Downloads archive of tweets posted from 2020 January - June, compressed to a single tar file for each day within the time period (_2020_01_01.tar_ ~ _2019_06_30.tar_)          |
| **tweet_2020_part2.sh** | Downloads archive of tweets posted from 2020 July - December, compressed to a single zip file for each day within the time period (_2020_07_01.zip_ ~ _2020_12_31.zip_)         |
| **tweet_2021_part1.sh** | Downloads archive of tweets posted from 2021 January - February, compressed to a single zip file for each month (_2021_01.zip_, _2021_02.zip_)                                  |
| **tweet_2021_part2.sh** | Downloads archive of tweets posted from 2021 March - August, compressed to a single zip file for each day within the time period (_2021_03_01.zip_ ~ _2021_08_31.zip_)          |
| **tweet_2021_part3.sh** | Downloads archive of tweets posted from 2021 September - December, compressed to a single tar file for each day within in the time period (_2021_09_01.tar_ ~ _2021_12_31.tar_) |

Multiple shell scripts had to be made to fully download tweet archive from 2018-2021. This was to account for differences in compression format (zip, tar), or download links.

Also note that tweet archive for September 2019 is unavailable at this time of writing.

Once the zip/tar files were downloaded, they were decompressed, which produced compressed json files with the _.json.bz2_ extensions. The bz2 files in a directory can be recursively decompressed to json files with `find {directory} -type f -name "*.bz2" -exec bzip2 -d {} \;`.

Be aware that these archives are extremely big in size, totaling up to 23.9 TB. I uploaded the json files containing the tweet data to HDFS, but even then I had to reduce the replication factor of certain files to 2 as I was limited to 60 TB of storage in HDFS.

#### Temperature Anomaly Data

This shell script simply curls county-level temperature anomaly dataset for every month from 2018 to 2021. The output is a monthly csv file, named _weather\_{year}\_{month}.csv_

#### Climate Change Perception Data

This project also utilized annual dataset on county-level perception of climate change, provided by Yale Program on Climate Change. However, the dataset could only be received via email after subscribing to their website, hence there isn't a separate shell script.

### etl_code

#### TweetAnalysis

This is a MR directory for calculating the sentiment of each tweet on monthly basis. All java, class, and jar files are provided, but they can be recompiled by running `./compile.sh` should there be any modifications.

The MR for each month can be executed with

`hadoop jar TweetAnalysis.jar TweetAnalysis -libjars VaderSentimentv1.jar {MONTHLY_TWEETS.json} {HDFS_OUTPUT_DIR}`.

**VaderSentimentv1.jar** must be included in the class path, as this MR uses VADER as the sentiment analysis tool.

The output maps each county by their number of weather-related Tweets posted and their average sentiment. For instance, this is a part of the output from MR for 2018 March.

```
...
Allegheny County, PA	0.1073111,18
Cherokee County, GA	0.6358,2
...
```

The above indicates that throughout this month, Allegheny County in Pennslevania had 18 weather-related tweets with average sentiment of 0.10731, while Cherokee County in Georgia only had 2 weather-related tweets with higher average sentiment of 0.6358.

### ana_code

This is a directory containing scala script I used for data analysis.

#### temperatureTweetAnalysis

This is a scala script which joined 3 datasets - **twitter sentiment dataset** (from TweetAnalysis MR), **Temperature Anomaly**, and **Climate Change Perception** - for a particular month within 2018-2021.

The script can be executed with `spark-shell --deploy-mode client -i temperatureTweetAnalysis.scala --conf spark.driver.args="{YEAR} {MONTH}"` command, where {MONTH} and {YEAR} are command line arguments. Note that for January-September, the month must start with 0 (01, 02, 03, ... 09).

The output is a table with the following schema:

```
state (string) : state code (AZ, GA, NY, etc)
county (string) : name of county
anomaly (float) : county's monthly temperature anomaly w.r.t. 20th century
sentiment (float) : county's average of weather-related tweet sentiment
count (int) : number of weather-related tweets posted in that county during the given year and month
cc_happening (float) : percentage of county population believing that climate change is happening
cc_not_happening (float) : percentage of county population believing that climate change is NOT happening
```

The table is written to a csv file, which was then saved in one of my HDFS directories.

During execution, script also prints the pearson correlation between the numerical columns (_anomaly_, _sentiment_, _count_, _cc_happening_). These values were used for analytics.

### profiling_code

#### Total Tweets

This is a MR that simply counts the number of tweets in the archive of a given {MONTH} in {YEAR}. Run `./compile.sh` to compile the code. To execute MR, run `hadoop jar CountTotalTweets.jar CountTotalTweets {MONTHLY_TWEETS.json} {HDFS_OUTPUT_DIR}`.

### vis

This directory contains the python script used for simple analysis and visualization (heatmap) of correlation coefficients calculated in _temperatureTweetAnalysis.scala_
