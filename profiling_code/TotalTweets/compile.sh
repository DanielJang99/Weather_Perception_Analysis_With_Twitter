rm -rf *.class
rm -rf CountTotalTweets.jar
javac -classpath `yarn classpath` -d . CountTotalTweetsMapper.java
javac -classpath `yarn classpath` -d . CountTotalTweetsReducer.java
javac -classpath `yarn classpath`:. -d . CountTotalTweets.java
jar -cvf CountTotalTweets.jar *.class
