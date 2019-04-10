package org.processmining.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.CausalActivityGraph;
import org.processmining.models.CausalActivityMatrix;
import org.processmining.parameters.ConvertCausalActivityMatrixToCausalActivityGraphParameters;

public class ConvertCausalActivityMatrixToCausalActivityGraphConnection extends AbstractConnection {

	public final static String MATRIX = "Matrix";
	public final static String GRAPH = "Graph";

	private ConvertCausalActivityMatrixToCausalActivityGraphParameters parameters;

	public ConvertCausalActivityMatrixToCausalActivityGraphConnection(CausalActivityMatrix matrix,
			CausalActivityGraph graph, ConvertCausalActivityMatrixToCausalActivityGraphParameters parameters) {
		super("Convert Causal Activity Matrix To Causal Activity Graph Connection");
		put(MATRIX, matrix);
		put(GRAPH, graph);
		this.parameters = parameters;
	}

	public ConvertCausalActivityMatrixToCausalActivityGraphParameters getParameters() {
		return parameters;
	}
}
