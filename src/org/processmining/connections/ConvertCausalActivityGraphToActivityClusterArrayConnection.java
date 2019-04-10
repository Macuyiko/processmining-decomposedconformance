package org.processmining.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.CausalActivityGraph;
import org.processmining.parameters.ConvertCausalActivityGraphToActivityClusterArrayParameters;

public class ConvertCausalActivityGraphToActivityClusterArrayConnection extends AbstractConnection {

	public final static String GRAPH = "Graph";
	public final static String CLUSTERS = "Clusters";

	private ConvertCausalActivityGraphToActivityClusterArrayParameters parameters;

	public ConvertCausalActivityGraphToActivityClusterArrayConnection(CausalActivityGraph graph,
			ActivityClusterArray clusters, ConvertCausalActivityGraphToActivityClusterArrayParameters parameters) {
		super("Convert Causal Activity Graph To Activity Cluster Array Connection");
		put(GRAPH, graph);
		put(CLUSTERS, clusters);
		this.parameters = parameters;
	}

	public ConvertCausalActivityGraphToActivityClusterArrayParameters getParameters() {
		return parameters;
	}
}
