

import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.JoinOperator.DefaultJoin;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.DataSetUtils;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.library.LabelPropagation;
import org.apache.flink.types.NullValue;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.FilterFunction;

import tu.master.utils.GraphMLConverter;
import tu.master.utils.Stemmer;
import tu.master.utils.StopWords;
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

/**
 * @author Nesrine
 *
 */
@SuppressWarnings("serial")
public class TextProcessor extends JFrame {
	final static Logger logger = Logger.getLogger(TextProcessor.class);
	private JPanel contentPane;
	private JTextField sent;
	private JTextField impl;

	Stemmer s = new Stemmer();
	SemanticGraph dependencies = null;

	static Map<Long, List<String>> vertMapping = new HashMap<Long, List<String>>();
	List<Tuple3<String, String, String>> edges = new ArrayList<Tuple3<String, String, String>>();
	Set<String> impSet = new HashSet<String>();
	StopWords stop = new StopWords();
	List<CoreMap> uSent = new ArrayList<CoreMap>();
	List<Edge<String, String>> edgelist = new ArrayList<Edge<String, String>>();
	CoreMap parsed = null;
	private BufferedReader br;
	private List<CoreMap> sentences = new ArrayList<CoreMap>();
	private List<String> nodesList = new ArrayList<String>();
	private List<IndexedWord> listOfvertices = new ArrayList<IndexedWord>();
	ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

	public static Map<Long, List<String>> getVertMapping() {
		return vertMapping;
	}

	public void setVertMapping(Map<Long, List<String>> vertMapping) {
		TextProcessor.vertMapping = vertMapping;
	}

	public List<IndexedWord> getListOfvertices() {
		return listOfvertices;
	}

	public void setListOfvertices(List<IndexedWord> listOfvertices) {
		this.listOfvertices = listOfvertices;
	}

	public List<String> getNodesList() {
		return nodesList;
	}

	public void setNodesList(List<String> nodesList) {
		this.nodesList = nodesList;
	}

	public List<CoreMap> getSentences() {
		return sentences;
	}

	public void setSentences(List<CoreMap> sentences) {
		this.sentences = sentences;
	}

	public Set<String> getImpSet() {
		return impSet;
	}

	public void setImpSet(Set<String> impSet) {
		this.impSet = impSet;
	}

	/**
	 * @throws IOException
	 */
	public TextProcessor() throws IOException {
		this.initWindow();
	}

	/**
	 * creates the window and calls the methods corresponding to the actions to
	 * be performed when the buttons are clicked
	 * 
	 * @throws IOException
	 */
	protected void initWindow() throws IOException {

		initialNodesList();

		// Layout
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
		sent.setText(getSentences().get(0).toString());

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
				addImplicationEdges(parseISentence(getSentences().get(0)),
						parseImplication(impl.getText()));
				getSentences().remove(0);
				try {
					initialGraph();
					br.close();
				} catch (Exception s) {
					// TODO Auto-generated catch block
					s.printStackTrace();
				}
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
				logger.info("add a new sentence ");
				List<String> vertexList = new ArrayList<String>();
				vertexList = parseISentence(getSentences().get(0));
				logger.info(" the new list is " + vertexList);
				getNodesList().addAll(vertexList);
				sent.setText(getSentences().get(0).toString());

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

	}

	/**
	 * creates the node list corresponding the first sentence
	 * 
	 * @throws IOException
	 */
	private void initialNodesList() throws IOException {
		// TODO Auto-generated method stub
		br = new BufferedReader(new FileReader("resources\\annotated_texts.txt"));
		String readString = null;
		while ((readString = br.readLine()) != null) {
			setSentences(performAnnotation(readString));
		}
		List<String> vertexList = new ArrayList<String>();
		vertexList = parseISentence(getSentences().get(0));
		getNodesList().addAll(vertexList);

	}

	/**
	 * THis method annotates a sentence using the nlp core framework
	 * 
	 * @param sentence
	 *            : a sentence from the input file
	 * @return annotated sentence
	 */
	public List<CoreMap> performAnnotation(String sentence) {
		Properties props = new Properties();
		props.setProperty("annotators",
				"tokenize,ssplit,pos,lemma,ner,parse,dcoref");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation = new Annotation(sentence);
		pipeline.annotate(annotation);
		return annotation.get(CoreAnnotations.SentencesAnnotation.class);

	}

	/**
	 * this method parses a sentence from the input file and returns the list of
	 * the vertices
	 * 
	 * @param sentence
	 *            a sentence from the input file
	 * @reurturn the vertex list
	 */
	public List<String> parseISentence(CoreMap sentence) {
		List<String> vertexList = new ArrayList<String>();
		stop.stopwordsSet();
		dependencies = sentence
				.get(CollapsedCCProcessedDependenciesAnnotation.class);
		setListOfvertices(dependencies.vertexListSorted());
		for (IndexedWord v : getListOfvertices()) {
			String lemm = v.lemma().toLowerCase();
			String stem = s.stem(lemm);
			if (!stop.getSet().contains(lemm)) {
				vertexList.add(stem);
			}

		}
		return vertexList;

	}

	/**
	 * @param sentence
	 * @return
	 */
	public List<String> parseImplication(String sentence) {
		List<String> input = new ArrayList<String>();
		stop.stopwordsSet();
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
			 if (!stop.getSet().contains(s.stem(lemma))) {
				input.add(s.stem(lemma));
			}
			}
		}

		return input;
	}

	/**
	 * @param vlist
	 * @param input
	 *            impSet contains the set of implications
	 */
	public void addImplicationEdges(List<String> vlist, List<String> input) {
		if (!impl.getText().equals(null)) {
			parseImplication(impl.getText());
		}
		// ToDo correction not the vertex list
		for (int i = 0; i < vlist.size(); i++) {
			for (int j = 0; j < input.size(); j++) {
				Tuple3<String, String, String> e = new Tuple3<String, String, String>(
						vlist.get(i), input.get(j), "i");
				getNodesList().add(input.get(j));
				getImpSet().add(input.get(j));
				edges.add(e);

			}
		}

	}

	/**
	 * constructs the graph without the implications
	 * 
	 * @param dependencies
	 */
	public void constructGraph(SemanticGraph dependencies) {
		List<IndexedWord> filteredList = new ArrayList<IndexedWord>();
		for (IndexedWord v : getListOfvertices()) {
			String lemm = v.lemma().toLowerCase();
			if (!stop.getSet().contains(lemm)) {
				filteredList.add(v);

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

			else {
				// add the word to the edge collection
				Tuple3<String, String, String> e = new Tuple3<String, String, String>(
						sgov, sdep, "c");
				edges.add(e);

			}
		}

	}

	/**
	 * @return
	 * @throws Exception
	 */
	public Graph<String, Long, String> initialGraph() throws Exception {

		DataSet<Tuple3<String, String, String>> Edges = env
				.fromCollection(edges);
		Graph<String, NullValue, String> graph = Graph.fromTupleDataSet(Edges,
				env);
		DataSet<Edge<String, String>> edgeSet = graph.getEdges();
		edgelist = edgeSet.collect();
		DataSet<Vertex<String, NullValue>> vertices = graph.getVertices();
		List<Vertex<String, NullValue>> list = vertices.collect();
		List<Vertex<String, Long>> vlist = new ArrayList<Vertex<String, Long>>();
		for (int i = 0; i < list.size(); i++) {
			Vertex<String, Long> v = new Vertex<String, Long>(list.get(i).f0,
					1L);
			vlist.add(v);
		}
		DataSet<Vertex<String, Long>> vertex = env.fromCollection(vlist);
		Graph<String, Long, String> fgraph = Graph.fromDataSet(vertex, edgeSet,
				env).getUndirected();
		return fgraph;
	}

	/**
	 * performs the vertices clustering using the label propagation protocol
	 * @throws Exception
	 */
	public void clustering() throws Exception {

		// Initialize each vertex with a unique numeric label and run the label
		// propagation algorithm
		Graph<String, Long, String> graph = initialGraph();
		/*graph.subgraph(new FilterFunction<Edge<String, String>>() {

			@Override
			public boolean filter(Edge<String, String> edge) throws Exception {
				// TODO Auto-generated method stub
				return (edge.getValue().equals("f"));
			}});*/
		DataSet<Tuple2<String, Long>> idsWithInitialLabels = DataSetUtils
				.zipWithUniqueId(graph.getVertexIds())
				.map(new MapFunction<Tuple2<Long, String>, Tuple2<String, Long>>() {
					public Tuple2<String, Long> map(Tuple2<Long, String> tuple2)
							throws Exception {
						return new Tuple2<String, Long>(tuple2.f1, tuple2.f0);
					}
				});

		logger.info(" ids with initial label" + idsWithInitialLabels.toString());

		DataSet<Vertex<String, Long>> verticesWithCommunity = graph
				.joinWithVertices(idsWithInitialLabels,
						(Long v1, Long v2) -> v2).run(
						new LabelPropagation<String, Long, String>(1000));

		logger.info(" the vertices and their communities   "
				+ verticesWithCommunity.toString());

		// order the vertices,degree dataset
		DataSet<Tuple2<String, Long>> degree = graph.getDegrees();

		// store the vertices and their communities in a dataset to be able to
		// perform the join transformation
		List<Vertex<String, Long>> vList = verticesWithCommunity.collect();
		List<Tuple2<String, Long>> vertices = new ArrayList<Tuple2<String, Long>>();
		for (Vertex<String, Long> item : vList) {
			Tuple2<String, Long> tup = new Tuple2<String, Long>(item.f0,
					item.f1);
			vertices.add(tup);
		}

		DataSet<Tuple2<String, Long>> vert = env.fromCollection(vertices);

		// join the vertex, degree and the vertex community datasets
		DefaultJoin<Tuple2<String, Long>, Tuple2<String, Long>> j = degree
				.join(vert).where(0).equalTo(0);

		List<Tuple2<Tuple2<String, Long>, Tuple2<String, Long>>> joinList = j
				.collect();
		// transform the result in a tuple list vertex id, degree, comm
		List<Tuple3<String, Long, Long>> tuplList = new ArrayList<Tuple3<String, Long, Long>>();
		for (Tuple2<Tuple2<String, Long>, Tuple2<String, Long>> tpl : joinList) {
			Tuple3<String, Long, Long> t = new Tuple3<String, Long, Long>(
					tpl.f0.f0, tpl.f0.f1, tpl.f1.f1);
			tuplList.add(t);
		}
		// order the list according to the node degree
		Comparator<Tuple3<String, Long, Long>> compa = new Comparator<Tuple3<String, Long, Long>>() {

			@Override
			public int compare(Tuple3<String, Long, Long> v1,
					Tuple3<String, Long, Long> v2) {
				// TODO Auto-generated method stub
				return v2.f1.compareTo(v1.f1);
			}

		};
		Collections.sort(tuplList, compa);
		logger.info(" the list  ordered  according to the degree");
		tuplList.forEach(logger::info);
		logger.info("\n--------------------------------------- the ordered map ---------------------------------------\n");
		Map<Long, List<String>> map2 = NodesMapping(tuplList);
		//Map<Long, List<String>> map3 = new HashMap<Long, List<String>>();
		List<Tuple3<Long, List<String>, List<String>>> clustersList2 = new ArrayList<Tuple3<Long, List<String>, List<String>>>();
		//map3.putAll(map2);
		setVertMapping(map2);
		Iterator<Map.Entry<Long, List<String>>> p = map2.entrySet().iterator();
		while (p.hasNext()) {
			Long key = p.next().getKey();
			logger.info(key + ", " + map2.get(key));
			List<String> impList = new ArrayList<String>();
			for (int k = 0; k < map2.get(key).size(); k++) {
				if (getImpSet().contains(map2.get(key).get(k))) {
					impList.add(map2.get(key).get(k));
					//map3.get(key).remove(map2.get(key).get(k));
				}

			}

			Tuple3<Long, List<String>, List<String>> tpl = new Tuple3<Long, List<String>, List<String>>(
					key, impList, map2.get(key));
			clustersList2.add(tpl);
		}
		logger.info(" the List < clusterID, Implications , Vertices > ");
		clustersList2.forEach(logger::info);
	}

	/**
	 * @param tuplList list of tuples; vertex, degree, clusterID
	 * @return a map key: clusterID, value: 
	 */
	private Map<Long, List<String>> NodesMapping(
			List<Tuple3<String, Long, Long>> tuplList) {
		List<Tuple2<String, Long>> verList = new ArrayList<Tuple2<String, Long>>();
		for (Tuple3<String, Long, Long> tpl : tuplList) {
			Tuple2<String, Long> t = new Tuple2<String, Long>(tpl.f0, tpl.f2);
			verList.add(t);
		}
		logger.info(" the list  of vertices ordered  according to the degree");
		verList.forEach(logger::info);
		Map<Long, List<String>> map = new HashMap<Long, List<String>>();
		for (Tuple2<String, Long> item : verList) {

			List<String> list = map.get(item.f1);
			if (list == null) {
				list = new ArrayList<String>();
				map.put(item.f1, list);
			}
			list.add(item.f0);
		}
		return map;

	}

	/**
	 * @return a list of colors to be used for the clusterColoring
	 */
	public static List<String> colorsSet() {
		List<String> colors = new ArrayList<String>();
		// Set of stop words
		colors.add("#2F4F4F");
		colors.add("#A52A2A");
		colors.add("#D2691E");
		colors.add("#800000");
		colors.add("#D2B48C");
		colors.add("#DEB887");
		colors.add("#708090");
		colors.add("#FF4500");
		colors.add("#3CB371");
		colors.add("#FF6347");
		colors.add("#FF66FF");
		colors.add("#FF3333");
		colors.add("#66FFCC");
		colors.add("#CCFF00");
		colors.add("#660066");
		colors.add("#330099");
		colors.add("#0099CC");
		colors.add("#006600");
		colors.add("#333366");
		colors.add("#33CC99");
		colors.add("#660066");
		colors.add("#339999");
		colors.add("#999900");
		colors.add("#FFFF00");
		colors.add("#FFDAB9");
		colors.add("#BDB76B");
		colors.add("#DDA0DD");
		colors.add("#EE82EE");
		colors.add("#BA55D3");
		colors.add("#8A2BE2");
		colors.add("#483D8B");
		colors.add("#E0FFFF");
		colors.add("#4682B4");
		colors.add("#B0C4DE");
		

		return colors;

	}
	
	
	
	
	static String gencode()
    {
        String[] letters = new String[15];
        letters = "0123456789ABCDEF".split("");
        String code ="#";
        for(int i=0;i<6;i++)
        {
            double ind = Math.random() * 15;
            int index = (int)Math.round(ind);
            code += letters[index]; 
        }
        return code;
    }
	
	public static List<String> colorsList() {
		List<String> colors = new ArrayList<String>();
		// Set of stop words
		for (int i=0 ; i<100 ; i++){
			colors.add(gencode());
		}

		return colors;

	}

	/**
	 * @return a map cotaining a mapping between the cluster ID and the color
	 * @throws IOException
	 */
	public static Map<Long, String> clusterColoring() throws IOException {
		Map<Long, String> map = new HashMap<Long, String>();
		List<String> colors = colorsSet();

		Iterator<Map.Entry<Long, List<String>>> p = getVertMapping().entrySet()
				.iterator();
		while (p.hasNext()) {
			Long key = p.next().getKey();
			// clusterId, color
			//map.put(key, gencode());
			map.put(key, colors.get(0));
			colors.remove(0);
		}

		return map;
	}

	/**
	 * creates the graphml file to be used by yED 
	 * @throws Exception
	 */
	public void visualizeGraph() throws Exception {
		
		System.out.println(" size of the map " + getVertMapping().size());
		Map<Long, String> color = TextProcessor.clusterColoring();
		Iterator<Map.Entry<Long, String>> p = color.entrySet()
				.iterator();
		while (p.hasNext()) {
			Long key = p.next().getKey();
			System.out.println(" the key of the map " + key + " the color is " + color.get(key));
		}
		System.out.println(" size of the color  map " + color.size());

		GraphMLConverter gm = new GraphMLConverter();
		gm.convert("resources\\first.graphml", getNodesList(), edgelist);

	}

}
