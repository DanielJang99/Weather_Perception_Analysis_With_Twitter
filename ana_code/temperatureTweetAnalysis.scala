val args = sc.getConf.get("spark.driver.args").split("\\s+")
val year = args(0)
val month = args(1)

val coRDD = sc.textFile(f"climate_opinions/$year%s.csv")
val filteredCounties = coRDD.filter(r => r.split(",")(0) == "County").map{r=> r.split(",")}.map{r => (r(1).replaceAll("\"", "").toLowerCase, r(2).toLowerCase.replaceAll("\"", "").replaceAll(" ",""), r(4), r(5))}
val coDF = filteredCounties.toDF("county", "state_fullname", "cc_happening", "cc_not_happening").withColumn("cc_happening", col("cc_happening").cast("float")).withColumn("cc_not_happening", col("cc_not_happening").cast("float"))

val stateName2Code = sc.textFile("state2code.csv")
val stateName2CodeRDD = stateName2Code.map{r => r.split(",")}.map{r => (r(0).replaceAll("\"", "").replaceAll(" ","").toLowerCase, r(2).replaceAll("\"", ""))}
val stateName2CodeDF = stateName2CodeRDD.toDF("state_fullname", "state")
val climateOpinionDF = coDF.join(stateName2CodeDF, coDF("state_fullname") === stateName2CodeDF("state_fullname"), "left").select(coDF("county"), stateName2CodeDF("state"),coDF("cc_happening"),coDF("cc_not_happening"))


val weatherRDD = sc.textFile(f"weather_data/weather_$year%s$month%s.csv")
val weatherHeaders = weatherRDD.take(4)

val weatherFiltered = weatherRDD.filter(d => !weatherHeaders.contains(d) && !d.contains("-99")).map{r=> r.split(",")}.map{r => (r(0).split("-")(0).replaceAll(" ",""), r(1).replaceAll("\"", "").toLowerCase, r(4))}
val weatherDF = weatherFiltered.toDF("state", "county", "anomaly")

val tweetDF = sc.textFile(f"sentiment_data/$year%s_$month%s/output.txt")
val countySent = tweetDF.map(line => (line.split("\t")(0).split(",")(1).replaceAll(" ",""), line.split("\t")(0).split(",")(0).toLowerCase(), line.split("\t")(1).split(",")(0), line.split("\t")(1).split(",")(1)))
val countySentDF = countySent.toDF("state", "county", "sentiment", "count")

val joinedDF = weatherDF.join(countySentDF, weatherDF("state") === countySentDF("state") && weatherDF("county") === countySentDF("county"), "left").join(climateOpinionDF, weatherDF("state") === climateOpinionDF("state") && weatherDF("county") === climateOpinionDF("county"), "left").select(weatherDF("state"), weatherDF("county"), weatherDF("anomaly"), countySentDF("sentiment"), countySentDF("count"), climateOpinionDF("cc_happening"), climateOpinionDF("cc_not_happening")).withColumn("anomaly",col("anomaly").cast("float")).withColumn("sentiment",col("sentiment").cast("float")).withColumn("count", col("count").cast("int"))

val validDF = joinedDF.filter(!joinedDF("cc_happening").isNull && !joinedDF("sentiment").isNull)

val num_rows = validDF.count()
println(f"Total tweets analyzed for $year%s $month%s: "+num_rows)

val anomaly_sentiment_corr = validDF.stat.corr("anomaly", "sentiment")
val cc_sentiment_corr = validDF.stat.corr("cc_happening", "sentiment")
val count_sentiment_corr = validDF.stat.corr("count", "sentiment")
val cc_count_corr = validDF.stat.corr("count", "cc_happening")
val anomaly_count_corr = validDF.stat.corr("anomaly", "count")
val anomaly_cc_corr = validDF.stat.corr("anomaly", "cc_happening")


println("temperature anomaly <-> tweet sentiment: "+anomaly_sentiment_corr)
println("% climate change believers <-> tweet sentiment: "+cc_sentiment_corr)
println("# weather tweets <-> tweet sentiment: " + count_sentiment_corr)
println("# weather tweets <-> % climate change believers: "+cc_count_corr)
println("temperature anomaly <-> # weather tweets: "+anomaly_count_corr)
println("temperature anomaly <-> % climate change believers: "+anomaly_cc_corr)


validDF.repartition(1).write.option("header","false").csv(f"hdfs://horton.hpc.nyu.edu:8020/user/hsj276/analysisOutput/$year%s_$month%s")
sc.stop()
System.exit(0)