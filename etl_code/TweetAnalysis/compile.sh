rm -rf *.class
rm -rf TweetAnalysis.jar
javac -classpath `yarn classpath`:VaderSentimentv1.jar -d . TweetAnalysisMapper.java
javac -classpath `yarn classpath` -d . TweetAnalysisReducer.java
javac -classpath `yarn classpath`:. -d . TweetAnalysis.java
jar -cvf TweetAnalysis.jar *.class
