package core.nlp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.types.NullValue;

import core.nlp.helper.Stemmer;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

/**
 * Hello world!
 *
 */
public class TextProcessing {
	public static void main(String[] args) throws Exception {
		StopWords stop = new StopWords();
		stop.stopwordsSet();

		List<Tuple3<String, String, String>> edges = new ArrayList<Tuple3<String, String, String>>();

		List<String> vertexList = new ArrayList<String>();

		ExecutionEnvironment env = ExecutionEnvironment
				.getExecutionEnvironment();
		// add the annotations
		Properties props = new Properties();
		props.setProperty("annotators",
				"tokenize,ssplit,pos,lemma,ner,parse,dcoref,stopword");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		PrintWriter out = new PrintWriter(
				"C:\\Users\\Asus\\Desktop\\Masterarbeit\\TextProcessor\\resources\\annotated.txt");
		BufferedReader br = new BufferedReader(
				new FileReader(
						"C:\\Users\\Asus\\Desktop\\Masterarbeit\\TextProcessor\\resources\\input.txt"));

		SemanticGraph dependencies = performAnnotation(br, out, pipeline);

		constructGraph(dependencies, stop, vertexList, edges);
				
		br.close();	

		// Create the Gelly graph

		DataSet<Tuple3<String, String, String>> Edges = env
				.fromCollection(edges);
		Graph<String, NullValue, String> graph = Graph.fromTupleDataSet(Edges,
				env);
		DataSet<Edge<String, String>> edgeSet = graph.getEdges();
		List<Edge<String, String>> edgelist = edgeSet.collect();
		

		GraphMLConverter gm = new GraphMLConverter();
		gm.convert(
				"C:\\Users\\Asus\\Desktop\\Masterarbeit\\TextProcessor\\resources\\finalG.graphml",
				vertexList, edgelist);

		// System.out.println (" follow list  " +followlist.toString());

		System.out.println("Done ..");

	}

	private static void constructGraph(SemanticGraph dependencies,StopWords stop, List<String> vertexList,List<Tuple3<String, String, String>> edges ) {
		// TODO Auto-generated method stub
		List<IndexedWord> listOfvertices = dependencies
				.vertexListSorted();
		Stemmer s = new Stemmer();

		List<IndexedWord> filteredList = new ArrayList<IndexedWord>();
		for (IndexedWord v : listOfvertices) {
			String lemm = v.lemma().toLowerCase();
			String stem = s.stem(lemm);
			if (!stop.getSet().contains(lemm)) {
				filteredList.add(v);
				vertexList.add(stem);

			}

		}
		// add the follow edges
		for (int i = 0; i < filteredList.size() - 1; i++) {
			Tuple3<String, String, String> e = new Tuple3<String, String, String>(
					s.stem(filteredList.get(i).lemma().toLowerCase()),
					s.stem(filteredList.get(i + 1).lemma()
							.toLowerCase()), "follow");
			edges.add(e);

		}

		// add the child edges
		List<SemanticGraphEdge> listOfEdges = dependencies
				.edgeListSorted();
		for (SemanticGraphEdge edge : listOfEdges) {
			String gov = edge.getGovernor().lemma().toLowerCase();
			String sgov = s.stem(gov);
			String dep = edge.getDependent().lemma().toLowerCase();
			String sdep = s.stem(dep);

			if (stop.getSet().contains(gov)
					|| stop.getSet().contains(dep)) {

			}
			// if the set doesn't contain gov nor dep then add the edge
			// to the graph
			else {
				// add the word to the edge collection

				Tuple3<String, String, String> e = new Tuple3<String, String, String>(
						sgov, sdep, "child");
				edges.add(e);

			}
		}

	}

	private static SemanticGraph performAnnotation(BufferedReader br, PrintWriter out,
			StanfordCoreNLP pipeline) throws IOException {
		// TODO Auto-generated method stub

		Annotation annotation = null;
		String readString = null;
		SemanticGraph dependencies =null;

		while ((readString = br.readLine()) != null) {

			annotation = new Annotation(readString);
			pipeline.annotate(annotation);
			pipeline.prettyPrint(annotation, out);
			List<CoreMap> sentences = annotation
					.get(CoreAnnotations.SentencesAnnotation.class);
			for (CoreMap sentence : sentences) {
				dependencies = sentence
						.get(CollapsedCCProcessedDependenciesAnnotation.class);
				// dependencies.prettyPrint();

			}
		}
		return dependencies;
	}
}
