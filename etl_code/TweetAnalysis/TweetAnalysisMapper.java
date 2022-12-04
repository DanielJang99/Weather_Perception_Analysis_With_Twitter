import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.vader.sentiment.analyzer.SentimentAnalyzer;
import com.vader.sentiment.analyzer.SentimentPolarities;

import java.io.IOException;

public class TweetAnalysisMapper extends Mapper<LongWritable, Text, Text, FloatWritable> {
    CloseableHttpClient httpClient = HttpClients.createDefault();

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, FloatWritable>.Context context) throws IOException, InterruptedException {
        String line = value.toString();
        Object obj;
        JSONParser jsonParser = new JSONParser();
        try{
            obj = jsonParser.parse(line);
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject geo = (JSONObject) jsonObject.get("geo");
            if (geo != null) {
                JSONArray coordinates = (JSONArray) geo.get("coordinates");
                String latitude = coordinates.get(0).toString();
                String longitude = coordinates.get(1).toString();
                String countyName = requestCountyName(latitude, longitude, jsonParser);
                if (!countyName.equals("Not US County")) {
                    String tweet = (String) jsonObject.get("text");
                    if (containsWeatherKeyword(tweet)) {
                        float sentiment = calculateSentiment(tweet);
                        context.write(new Text(countyName), new FloatWritable(sentiment));
                    }
                }
            }
        } catch (ParseException ignored) {
        }
    }

    /**
     * Returns county name and state of the given coordinate by sending an API request to FCC
     * @param latitude: coordinate latitude
     * @param longitude: coordinate longitude
     * @param jsonParser: JSONParser
     * @return county_name, state_code
     */
    private String requestCountyName(String latitude, String longitude, JSONParser jsonParser) {
        String url = "https://geo.fcc.gov/api/census/block/find?latitude=" + latitude + "&longitude=" + longitude + "&censusYear=2020&format=json";
        HttpGet httpGet = new HttpGet(url);
        try(CloseableHttpResponse response = httpClient.execute(httpGet)){
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                JSONObject obj = (JSONObject) jsonParser.parse(result);
                JSONObject county = (JSONObject) obj.get("County");
                JSONObject state = (JSONObject) obj.get("State");
                if (county.get("name") != null && state.get("code") != null) {
                    return county.get("name") + ", " + state.get("code");  // e.g) Queens County, NY
                } else {
                    return "Not US County";
                }
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    /**
     * Checks whether string contains weather-related keywords
     * @param s: string to be checked
     * @return boolean
     */
    private boolean containsWeatherKeyword(String s) {
        String[] keywords = {"blizzard", "breeze", "chilly", "clear", "clouds", "cloudy", "cold", "damp", "dew", "downpour", "drizzle", "drought", "dry","flurry", "fog", "freezing", "frigid", "frostbite", "frosty", "gail", "gust", "hail", "heat", "hot", "humid", "hurricane", "icy", "lightning", "misty", "moist", "monsoon", "muddy", "overcast", "pouring", "precipitation", "rain", "rainbow", "showers", "sleet", "snowflakes", "soggy", "sprinkle", "sunny", "thunder", "thunderstorm", "typhoon", "weather", "wet", "wind", "windstorm", "windy"};
        for (String keyword : keywords) {
            if (s.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Quantifies the sentiment of weather-related tweet using Vader Lexicon
     * @param s: weather-related text
     * @return int
     */
    private float calculateSentiment(String s) {
        SentimentPolarities sentimentPolarities = SentimentAnalyzer.getScoresFor(s);
        return sentimentPolarities.getCompoundPolarity();
    }
}
