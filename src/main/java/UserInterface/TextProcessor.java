package UserInterface;

import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.DataSetUtils;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.VertexJoinFunction;
import org.apache.flink.graph.library.LabelPropagation;
import org.apache.flink.types.NullValue;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import core.nlp.GraphMLConverter;
import core.nlp.StopWords;
import core.nlp.helper.Stemmer;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

@SuppressWarnings("serial")
public class TextProcessor extends JFrame {
	
    
	private JPanel contentPane;
	private JTextField sent;
	private JTextField impl;
	//private int id = 0;
	
	
	
	SemanticGraph dependencies = null;
	List<Tuple3<String, String, String>> edges = new ArrayList<Tuple3<String, String, String>>();
	List<Tuple2<Tuple2<String, String>, Tuple2<String, Integer>>> wEdges = new ArrayList<Tuple2<Tuple2<String, String>, Tuple2<String, Integer>>>();
	List<Tuple2<Tuple2<String, String>, Tuple2<String, Integer>>> fedg = new ArrayList<Tuple2<Tuple2<String, String>, Tuple2<String, Integer>>>();
	List<String> nodesList = new ArrayList<String>();
	StopWords stop = new StopWords();
	List<IndexedWord> listOfvertices = new ArrayList<IndexedWord>();
	List<CoreMap> sentences = new ArrayList<CoreMap>();
	List<CoreMap> uSent = new ArrayList<CoreMap>();
	List<Edge<String, String>> edgelist = new ArrayList<Edge<String, String>>();
	CoreMap parsed = null;
	BufferedReader br = new BufferedReader(
			new FileReader(
					"C:\\Users\\Asus\\Desktop\\Masterarbeit\\TextProcessor\\resources\\input.txt"));
	String readString = null;
	

	// List<String> input = new ArrayList<String>();
	
	public TextProcessor() throws IOException {
		this.initWindow();
	}

	protected void initWindow() throws IOException {

		while ((readString = br.readLine()) != null) {
			sentences = performAnnotation(readString);
		}
		List<String> vertexList = new ArrayList<String>();

		vertexList = parseISentence(sentences.get(0));
		nodesList.addAll(vertexList);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 557, 235);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblSentence = new JLabel("Sentence");
		lblSentence.setForeground(Color.BLUE);
		lblSentence.setBounds(27, 32, 58, 14);
		contentPane.add(lblSentence);

		sent = new JTextField();
		lblSentence.setLabelFor(sent);
		sent.setBounds(208, 26, 323, 26);
		contentPane.add(sent);
		sent.setColumns(10);
		sent.setText(sentences.get(0).toString());

		JLabel lblWhatIsThis = new JLabel("What is this sentence about ?");
		lblWhatIsThis.setForeground(Color.BLUE);
		lblWhatIsThis.setBounds(27, 82, 171, 14);
		contentPane.add(lblWhatIsThis);

		impl = new JTextField();
		lblWhatIsThis.setLabelFor(impl);
		impl.setBounds(208, 76, 323, 26);
		contentPane.add(impl);
		impl.setColumns(10);

		Button button = new Button("Construct Graph");
		button.setForeground(Color.BLUE);
		button.setFont(new Font("Dialog", Font.BOLD, 12));
		button.setBackground(SystemColor.controlHighlight);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				constructGraph(dependencies);
				addImplicationEdges(parseISentence(sentences.get(0)),
						parseImplication(impl.getText()));
				// store the sentence and its implication in the cassadra database
				//UUID id = UUID.randomUUID();
				//session.execute("INSERT INTO mapping (id,sentence,implication) VALUES ('+id+','"+sentences.get(0)+"','"+impl.getText()+"')");
				/*Statement statement = QueryBuilder.insertInto("k", "mapping")
				        .value("id", id)
				        .value("sentence", sentences.get(0).toString())
				        .value("implication", impl.getText());
				session.execute(statement);
				id=id+1;*/
				
				sentences.remove(0);
				try {
					getEdgeList();
					br.close();
				} catch (Exception s) {
					// TODO Auto-generated catch block
					s.printStackTrace();
				}
				System.out.println(" the list of edges is "
						+ edgelist.toString());

			}
		});
		button.setBounds(27, 142, 99, 20);
		contentPane.add(button);

		Button button_1 = new Button("New Sentence");
		button_1.setForeground(Color.BLUE);
		button_1.setFont(new Font("Dialog", Font.BOLD, 12));
		button_1.setBackground(SystemColor.controlHighlight);
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("add a new sentence ");
				List<String> vertexList = new ArrayList<String>();

				vertexList = parseISentence(sentences.get(0));
				System.out.println(" the new list is " + vertexList);
				nodesList.addAll(vertexList);
				sent.setText(sentences.get(0).toString());

			}
		});
		
		button_1.setBounds(148, 142, 99, 20);
		contentPane.add(button_1);

		Button button_2 = new Button("Visualize Graph");
		button_2.setForeground(Color.BLUE);
		button_2.setFont(new Font("Dialog", Font.BOLD, 12));
		button_2.setBackground(SystemColor.controlHighlight);
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					visualizeGraph();
				} catch (Exception s) {
					// TODO Auto-generated catch block
					s.printStackTrace();
				}

			}
		});
		button_2.setBounds(275, 142, 99, 20);
		contentPane.add(button_2);
    
		Button button_4 = new Button("Clustering");
		button_4.setForeground(Color.BLUE);
		button_4.setFont(new Font("Dialog", Font.BOLD, 12));
		button_4.setBackground(SystemColor.controlHighlight);
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					clustering();
				} catch (Exception s) {
					// TODO Auto-generated catch block
					s.printStackTrace();
				}

			}
		});
		button_4.setBounds(386, 142, 99, 20);
		contentPane.add(button_4);
		
		//MCL.train(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);

	}

	public List<CoreMap> performAnnotation(String sentence) {
		Properties props = new Properties();
		props.setProperty("annotators",
				"tokenize,ssplit,pos,lemma,ner,parse,dcoref");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation = new Annotation(sentence);
		pipeline.annotate(annotation);
		return annotation.get(CoreAnnotations.SentencesAnnotation.class);

	}

	public List<String> parseISentence(CoreMap sent) {
		List<String> vertexList = new ArrayList<String>();
		stop.stopwordsSet();
		// perform annotations

		// for (CoreMap sent : sentences) {
		dependencies = sent
				.get(CollapsedCCProcessedDependenciesAnnotation.class);
		// System.out.println(" the sentences of index " + index +"is "+
		// sentences.get(index));

		listOfvertices = dependencies.vertexListSorted();
		Stemmer s = new Stemmer();
		stop.stopwordsSet();
		// fill the list of vertices
		// List<IndexedWord> filteredList = new ArrayList<IndexedWord>();
		for (IndexedWord v : listOfvertices) {
			String lemm = v.lemma().toLowerCase();
			String stem = s.stem(lemm);
			if (!stop.getSet().contains(lemm)) {
				// filteredList.add(v);
				vertexList.add(stem);
			}

		}
		return vertexList;

		// }
	}

	public List<String> parseImplication(String sentence) {
		List<String> input = new ArrayList<String>();
		stop.stopwordsSet();
		Stemmer s = new Stemmer();
		// perform annotations
		Properties props = new Properties();
		props.setProperty("annotators",
				"tokenize,ssplit,pos,lemma,ner,parse,dcoref");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation = new Annotation(sentence);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation
				.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sent : sentences) {
			for (CoreLabel token : sent.get(TokensAnnotation.class)) {
				// this is the text of the token
				// String word = token.get(TextAnnotation.class);
				String lemma = token.get(LemmaAnnotation.class);
				// if (!stop.getSet().contains(s.stem(lemma))) {
				input.add(s.stem(lemma));
			}
		}

		return input;
	}

	
	public void addImplicationEdges(List<String> vlist, List<String> input) {
		if (!impl.getText().equals(null)) {
			parseImplication(impl.getText());
		}
		// ToDo correction not the vertex list
		for (int i = 0; i < vlist.size(); i++) {
			for (int j = 0; j < input.size(); j++) {
				Tuple3<String, String, String> e = new Tuple3<String, String, String>(
						vlist.get(i), input.get(j), "impl");
				nodesList.add(input.get(j));
				edges.add(e);

			}
		}

	}

	public void constructGraph(SemanticGraph dependencies) {
		Stemmer s = new Stemmer();
		List<IndexedWord> filteredList = new ArrayList<IndexedWord>();
		for (IndexedWord v : listOfvertices) {
			String lemm = v.lemma().toLowerCase();
			//String stem = s.stem(lemm);
			if (!stop.getSet().contains(lemm)) {
				filteredList.add(v);
				// vertexList.add(stem);

			}

		}
		// add the follow edges
		for (int i = 0; i < filteredList.size() - 1; i++) {
			Tuple3<String, String, String> e = new Tuple3<String, String, String>(
					s.stem(filteredList.get(i).lemma().toLowerCase()),
					s.stem(filteredList.get(i + 1).lemma().toLowerCase()), "f");

			edges.add(e);

		}

		// add the child edges
		List<SemanticGraphEdge> listOfEdges = dependencies.edgeListSorted();
		for (SemanticGraphEdge edge : listOfEdges) {
			String gov = edge.getGovernor().lemma().toLowerCase();
			String sgov = s.stem(gov);
			String dep = edge.getDependent().lemma().toLowerCase();
			String sdep = s.stem(dep);

			if (stop.getSet().contains(gov) || stop.getSet().contains(dep)) {

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

	

	public Graph<String, Long, String> getEdgeList() throws Exception {

		ExecutionEnvironment env = ExecutionEnvironment
				.getExecutionEnvironment();
		DataSet<Tuple3<String, String, String>> Edges = env
				.fromCollection(edges);
		// System.out.println("list of edges "+edges.toString());
	//	Graph<String, NullValue, String> graph = Graph.fromTupleDataSet(Edges,
	//			env);
		Graph<String, NullValue, String> graph = Graph.fromTupleDataSet(Edges,env);
		DataSet<Edge<String, String>> edgeSet = graph.getEdges();
		edgelist = edgeSet.collect();
		DataSet<Vertex<String, NullValue>> vertices = graph.getVertices();
		List<Vertex<String, NullValue>> list = vertices.collect();
		List<Vertex<String, Long>> vlist = new ArrayList<Vertex<String, Long>>();
	    for (int i=0 ;i<list.size(); i++ ){
	    	Vertex<String, Long> v = new Vertex<String,Long >(list.get(i).f0,1L);
	    	vlist.add(v);    	
	    }
	    DataSet<Vertex<String, Long>> vertex = env
				.fromCollection(vlist);
	    Graph<String, Long, String> ugraph = Graph.fromDataSet(vertex, edgeSet, env).getUndirected();	
		return ugraph;

	}
	

	public void clustering() throws Exception{
		
		// Initialize each vertex with a unique numeric label and run the label propagation algorithm
		Graph<String, Long, String> graph = getEdgeList();
		DataSet<Tuple2<String, Long>> idsWithInitialLabels = DataSetUtils
				.zipWithUniqueId(graph.getVertexIds())
				.map(new MapFunction<Tuple2<Long, String>, Tuple2<String, Long>>() {
					@Override
					public Tuple2<String, Long> map(Tuple2<Long, String> tuple2) throws Exception {
						return new Tuple2<String, Long>(tuple2.f1, tuple2.f0);
					}
				});
		
		System.out.println(" ids with initial label");
		
		idsWithInitialLabels.print();
	    
		DataSet<Vertex<String, Long>> verticesWithCommunity = graph.joinWithVertices(idsWithInitialLabels,
				new VertexJoinFunction<Long, Long>() {
			public Long vertexJoin(Long vertexValue, Long inputValue) {
				return inputValue;
			}

		})
		.run(new LabelPropagation<String, Long, String>(10));
		
		
		System.out.println(" the vertices and their communities   ");
		verticesWithCommunity.print();
	}
	public void visualizeGraph() throws Exception {

		GraphMLConverter gm = new GraphMLConverter();
		gm.convert(
				"C:\\Users\\Asus\\Desktop\\Masterarbeit\\TextProcessor\\resources\\finalG.graphml",
				nodesList, edgelist);
		 
		 
		// org.apache.spark.graphx.Graph<Vertex,Edge>  g = new org.apache.spark.graphx.Graph(data,vertices);

	}

	

}
