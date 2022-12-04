import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class CountWeatherTweetsMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        String line = value.toString();
        Object obj;
        JSONParser jsonParser = new JSONParser();
        try{
            obj = jsonParser.parse(line);
            JSONObject jsonObject = (JSONObject) obj;
            String tweet = (String) jsonObject.get("text");
            if(tweet != null){
                if (containsWeatherKeyword(tweet)) {
                    context.write(new Text("Weather Tweets "), new IntWritable(1));
                }
            }
        } catch (ParseException ignored) {
        }
    }

    /**
     * Checks whether string contains weather-related keywords
     * @param s: string to be checked
     * @return boolean
     */
    private boolean containsWeatherKeyword(String s) {
        if (s.length() > 0) {
            String[] keywords = {"blizzard", "breeze", "chilly", "clear", "clouds", "cloudy", "cold", "damp", "dew", "downpour", "drizzle", "drought", "dry","flurry", "fog", "freezing", "frigid", "frostbite", "frosty", "gail", "gust", "hail", "heat", "hot", "humid", "hurricane", "icy", "lightning", "misty", "moist", "monsoon", "muddy", "overcast", "pouring", "precipitation", "rain", "rainbow", "showers", "sleet", "snowflakes", "soggy", "sprinkle", "sunny", "thunder", "thunderstorm", "typhoon", "weather", "wet", "wind", "windstorm", "windy"};
            for (String keyword : keywords) {
                if (s.contains(keyword.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}
