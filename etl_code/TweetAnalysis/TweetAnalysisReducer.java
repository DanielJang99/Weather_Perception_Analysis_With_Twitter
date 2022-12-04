import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class TweetAnalysisReducer extends Reducer<Text, FloatWritable, Text, Text>{
    @Override
    protected void reduce(Text key, Iterable<FloatWritable> values, Reducer<Text, FloatWritable, Text, Text>.Context context) throws IOException, InterruptedException {
        int cnt = 0;
        float total_sentiment = 0;
        for (FloatWritable value : values) {
            cnt +=1;
            total_sentiment += value.get();
        }
        String final_val = total_sentiment / cnt + "," + cnt; // final value for each county
        context.write(key, new Text(final_val));
    }
}
