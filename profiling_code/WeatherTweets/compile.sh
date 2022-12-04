rm -rf *.class
rm -rf CountWeatherTweets.jar
javac -classpath `yarn classpath` -d . CountWeatherTweetsMapper.java
javac -classpath `yarn classpath` -d . CountWeatherTweetsReducer.java
javac -classpath `yarn classpath`:. -d . CountWeatherTweets.java
jar -cvf CountWeatherTweets.jar *.class
