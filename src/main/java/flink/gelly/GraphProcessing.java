package flink.gelly;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.graph.library.ConnectedComponents;
import org.apache.flink.types.NullValue;

/**
 * This is the skeleton code for the Gellyschool.com Tutorial#1.
 *
 * <p>
 * This program:
 * <ul>
 * <li>reads a list edges
 * <li>creates a graph from the edge data
 * <li>calls Gelly's Connected Components library method on the graph
 * <li>prints the result to stdout
 * </ul>
 *
 */
public class GraphProcessing {

	public static void main(String[] args) throws Exception {
		Integer maxIterations = 1;

		// set up the execution environment
		final ExecutionEnvironment env = ExecutionEnvironment
				.getExecutionEnvironment();

		// Step #1: Load the data in a DataSet

		// directed network containing information about who follows whom on
		// Twitter.
		// Nodes represent users and an edge shows that the left user follows
		// the right one.

		DataSet<Tuple2<Long, Long>> twitterEdges = env
				.readCsvFile(
						"C:\\Users\\Asus\\Desktop\\Masterarbeit\\TextProcessor\\resources\\graph.csv")
				.fieldDelimiter(" ") // node IDs are separated by spaces
				.ignoreComments("%") // comments start with "%"
				.types(Long.class, Long.class); // read the node IDs as Longs

		// Step #2: Create a Graph and initialize vertex values
		// In Gelly, a Graph is represented by a DataSet of vertices and a
		// DataSet of edges.
		// from a DataSet of Tuple2 representing the edges. Gelly will convert
		// each Tuple2 to an Edge, where the first field will be the source ID
		// and the second field will be the target ID. Both vertex and edge
		// values will be set to NullValue
		Graph<Long, Long, NullValue> graph = Graph.fromTuple2DataSet(
				twitterEdges, new InitVertices(), env);

		// Step #3: Run Connected Components
		// A weakly connected component is a maximal subgraph of a directed
		// graph such that for every pair of vertices u, v in the subgraph,
		// there is an undirected path from u to v and a directed path from v to
		// u
		// Upon convergence, two vertices belong to the same component, if there
		// is a path from one to the other, without taking edge direction into
		// account.
		// The algorithm is implemented using scatter-gather iterations. This
		// implementation uses a comparable vertex value as initial component
		// identifier (ID). Vertices propagate their current value in each
		// iteration. Upon receiving component IDs from its neighbors, a vertex
		// adopts a new component ID if its value is lower than its current
		// component ID. The algorithm converges when vertices no longer update
		// their component ID value or when the maximum number of iterations has
		// been reached.
		DataSet<Vertex<Long, Long>> verticesWithComponents = graph
				.run(new ConnectedComponents<Long, NullValue>(maxIterations));

		// Print the result
		verticesWithComponents.print();

	}

	//
	// User Functions
	//

	/**
	 * Initializes the vertex values with the vertex ID
	 */
	@SuppressWarnings("serial")
	public static final class InitVertices implements MapFunction<Long, Long> {

		public Long map(Long vertexId) {
			return vertexId;
		}
	}
}
