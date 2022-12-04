import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class CountWeatherTweetsReducer extends Reducer<Text, IntWritable, Text, Text>{

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Reducer<Text, IntWritable, Text, Text>.Context context) throws IOException, InterruptedException {
        int cnt = 0;
        for (IntWritable value : values) {
            cnt +=1;
        }
        context.write(key, new Text(String.valueOf(cnt)));
    }
}
