package flink.examples;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

public class WordCountExample {

		public static void main(String[] args) throws Exception {
			
			//1.	Obtain an execution environment,
	        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

	        //2.	Load/create the initial data,
	        DataSet<String> text = env.fromElements(
	            "black cat ",
	            "I think I hear them. Stand, ho! Who's there?");
	        //env.readTextFile("C:\\Users\\Asus\\Desktop\\Masterarbeit\\TextProcessor\\resources\\input.txt");

	        //3.	Specify transformations on this data,
	        
	        //The FlatMap transformation applies a user-defined flat-map function on each element of a DataSet. This variant of a map function can return arbitrary many result elements (including none) for each input element.
	        DataSet<Tuple2<String, Integer>> wordCounts = text
	            .flatMap(new LineSplitter())
	            .groupBy(0) // group the tuples with the same first index
	            .sum(1); // The index of the Tuple field on which the aggregation function is applied.
            
	        //4.	Specify where to put the results of your computations
	        wordCounts.print();
	    }

	    public static class LineSplitter implements FlatMapFunction<String, Tuple2<String, Integer>> {
	        
			private static final long serialVersionUID = 1L;

			public void flatMap(String line, Collector<Tuple2<String, Integer>> out) {
	            for (String word : line.split(" ")) {
	                out.collect(new Tuple2<String, Integer>(word, 1));
	            }
	        }
	    }
	}


