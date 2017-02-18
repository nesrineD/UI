package tu.master.utils;

/*
 * GraphMLConverter.java
 *  
 */

import java.util.*;
import java.io.*;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Vertex;
import org.apache.flink.types.NullValue;
import org.jdom2.*;
import org.jdom2.output.*;

import tu.master.ConceptDetection.TextProcessor;

import java.net.*;
/**
 * Util to convert nodes and edges into grapml document
 * @author rbolze
 */
public class GraphMLConverter {
    
    // no constructor
    // only use static method of this class
	public GraphMLConverter(){
		
	}
    

    /**
     * method which write in the xml file the graphML structure of the list of nodes and edges
     * @param fileName the xml file name
     * @param nodes the vector of URL whcih represent the nodes explored
     * @param edges the Vector of String the format of the contains string is source+"->"+target
     * @throws IOException 
     */
    public void convert(String fileName,Map<Long, List<String>> map,List<Edge<String, String>> edges) throws IOException {
       
      
    	
        Element graphml = new Element("graphml","http://graphml.graphdrawing.org/xmlns");
        Document document = new Document(graphml);        
        Namespace xsi = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
        Namespace schemLocation = Namespace.getNamespace("schemLocation","http://graphml.graphdrawing.org/xmlns \n \t http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd");
        Namespace y = Namespace.getNamespace("y","http://www.yworks.com/xml/graphml");
        
        // add Namespace
        graphml.addNamespaceDeclaration(xsi);
        graphml.addNamespaceDeclaration(y);
        graphml.addNamespaceDeclaration(schemLocation);
     
        // keys for graphic representation
        Element key_d0 = new Element("key",graphml.getNamespace());
        key_d0.setAttribute("id","d0");
        key_d0.setAttribute("for","node");
        key_d0.setAttribute("yfiles.type","nodegraphics");
        graphml.addContent(key_d0);
  
        
        Element key_d4 = new Element("key",graphml.getNamespace());
        key_d4.setAttribute("id","d4");
        key_d4.setAttribute("for","edge");
        key_d4.setAttribute("yfiles.type","edgegraphics");
        graphml.addContent(key_d4);
        
        Element graph = new Element("graph",graphml.getNamespace());
        graph.setAttribute("id","G");
        graph.setAttribute("edgedefault","directed");
        graphml.addContent(graph);
        
        
        Iterator<Map.Entry<Long, List<String>>> p = map.entrySet().iterator();
		while (p.hasNext()) {
			
		    // cluster id
			Long key = p.next().getKey();
			
			List<String> nodes = map.get(key);
			for (int k = 0; k < nodes.size(); k++) {
				int id = nodes.indexOf(nodes.get(k));
	        	//System.out.println(" id of the node "+nodes.get(k) + "num " +k + " is " + id );
	          	String node  = nodes.get(k);
	        	addNode(id,node,key,graph,graphml);

			}
        
        
    for (int i = 0; i < edges.size(); i++) {
    	Edge<String, String> edge = edges.get(i);
    	int id = edges.indexOf(edge);
    	//System.out.println(" id of the edge "+edges.get(i) + " index " +i + " is " + id);
            String source = edge.getSource();
			//URL urlsrc = new URL ("http://"+source) ;
			String target = edge.getTarget();
			//URL urltarget = new URL ("http://"+target) ;
			String value = edge.getValue();
			int idSource = nodes.indexOf(source);                
			int idTarget = nodes.indexOf(target);
			//System.out.println("index of  source is  "+ nodes.indexOf(target) );
			if (idSource<0 || idTarget <0){
			    System.err.println("bad edge: "+edge);
			}
			if(idSource<0){                    
			    System.err.println("bad source :"+source);
			}
			if(idTarget<0){                    
			    System.err.println("bad target :"+target);
			}
			addEdge(id,idSource,idTarget,source,target,value,graph,graphml);
            
        }
		}
       // printAll(document);
        save(fileName,document);        
    }
    
    
    /**
     * add a edge to the graphML document
     * @param id the id of the edge
     * @param idSource the id of the node source
     * @param idTarget the id of the node target
     * @param source the URL of the source
     * @param target the URL of the target
     * @param graph the graph element of the graphML document
     * @param graphml the graphml element of the graphML document
     */
    public static void addEdge(int id,int idSource,int idTarget, String source, String target,String value,Element graph,Element graphml){
        Element edge = new Element("edge",graphml.getNamespace());
        edge.setAttribute("id","e"+id);
        edge.setAttribute("source","n"+idSource);
        edge.setAttribute("target","n"+idTarget);
        Element data4 = new Element("data",graphml.getNamespace());
        data4.setAttribute("key","d4");
        edge.addContent(data4);
        Namespace yns = graphml.getNamespace("y");
        Element shapeNode = new Element("PolyLineEdge",yns);
        Element arrow = new Element("Arrows",yns);
        arrow.setAttribute("source","none");
        arrow.setAttribute("target","standard");
        shapeNode.addContent(arrow);      
        data4.addContent(shapeNode);
        Element edgeLabel = new Element("EdgeLabel",yns);
        edgeLabel.setAttribute("visible","true");
        edgeLabel.setAttribute("autoSizePolicy","content");
        edgeLabel.setText(value);
        shapeNode.addContent(edgeLabel);
        Element lineStyle = new Element("LineStyle",yns);
        lineStyle.setAttribute("color","#800000");
        shapeNode.addContent(lineStyle);
        graph.addContent(edge);
    }
    
    /**
     * add a node to the graphML document
     * @param id the id of the node
     * @param node2 the URL of the node
     * @param key 
     * @param graph the graph element of the graphML document
     * @param graphml the graphml element of the graphML document
     * @throws IOException 
     */
    public void addNode(int id,String node2,Long key, Element graph,Element graphml) throws IOException{
    	//TextProcessor proc = new TextProcessor();
    	//Map<Long, String> map = proc.clusterColoring() ;
    	
    	/*Iterator<Map.Entry<Long, String>> p = map.entrySet().iterator();
    		while (p.hasNext()) {
			Long k = p.next().getKey();
			System.out.println(key + ", " + map.get(k));

			}*/
        Element node = new Element("node",graphml.getNamespace());
        node.setAttribute("id","n"+id);
        Element data0 = new Element("data",graphml.getNamespace());
        data0.setAttribute("key","d0");
        node.addContent(data0);
        Namespace yns = graphml.getNamespace("y");
        Element shapeNode = new Element("ShapeNode",yns);
        data0.addContent(shapeNode);
        Element nodeLabel = new Element("NodeLabel",yns);
        nodeLabel.setAttribute("visible","true");
        nodeLabel.setAttribute("autoSizePolicy","content");
        nodeLabel.setText(node2);
        //System.out.println(url);
        shapeNode.addContent(nodeLabel);
       // <y:Fill color="#FFCC00" transparent="false"/>
       // Element Fill = new Element("Fill",yns);
       // System.out.println(" the cluster id is " + key + " the color is " + map.get(key));
      /*  Iterator<Map.Entry<Long, String>> p = map.entrySet().iterator();
		while (p.hasNext()) {
		Long k = p.next().getKey();
		System.out.println(" the cluster id is "+ k +  " the color is " + map.get(k));*/
		//shapeNode.addContent(Fill);
		//Fill.setAttribute("color",map.get(k));
		
		//System.out.println(" the node cluster id is "+ key + " the node color is "+ map.get(key));
        
        
        graph.addContent(node);
    }
    /**
     * print the content of the document
     * @param doc xml document
     */
   /* public static void printAll(Document doc) {
        try{
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(doc, System.out);
        } catch (java.io.IOException e){
            e.printStackTrace();
        }
    }*/
    
    /**
     * write the xml document into file
     * @param file the file name
     * @param doc xml document
     */
    public static void save(String file,Document doc) {
        System.out.println("### document saved in : "+file);
        try {
            XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
            sortie.output(doc, new java.io.FileOutputStream(file));
        } catch (java.io.IOException e){}
    }
    
}