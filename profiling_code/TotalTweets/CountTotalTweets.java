import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.Path;


public class CountTotalTweets extends Configured implements Tool {
    public static void main(String[] args) throws Exception {
        int r = ToolRunner.run(new Configuration(), new CountTotalTweets(), args);
        System.exit(r);
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = getConf();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: Total Tweet Count <input path> <output path>");
            System.exit(-1);
        }

        Job job = new Job(conf, "Count Total Tweets Job");
        job.setJarByClass(CountTotalTweets.class);
        job.setJobName("CountTotalTweets");
        if(!otherArgs[0].endsWith(".json")){
            FileInputFormat.setInputDirRecursive(job, true);
        }
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(CountTotalTweetsMapper.class);
        job.setReducerClass(CountTotalTweetsReducer.class);
        job.setNumReduceTasks(1);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        return job.waitForCompletion(true) ? 0 : 1;
    }
}
