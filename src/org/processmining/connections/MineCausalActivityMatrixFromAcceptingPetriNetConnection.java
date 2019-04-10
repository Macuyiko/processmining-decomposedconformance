package org.processmining.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.CausalActivityMatrix;
import org.processmining.parameters.MineCausalActivityMatrixFromAcceptingPetriNetParameters;

public class MineCausalActivityMatrixFromAcceptingPetriNetConnection extends AbstractConnection {

	public final static String NET = "Net";
	public final static String MATRIX = "Matrix";

	private MineCausalActivityMatrixFromAcceptingPetriNetParameters parameters;

	public MineCausalActivityMatrixFromAcceptingPetriNetConnection(AcceptingPetriNet net, CausalActivityMatrix matrix,
			MineCausalActivityMatrixFromAcceptingPetriNetParameters parameters) {
		super("Mine Causal Activity Matrix From Accepting Petri Net Connection");
		put(NET, net);
		put(MATRIX, matrix);
		this.parameters = parameters;
	}

	public MineCausalActivityMatrixFromAcceptingPetriNetParameters getParameters() {
		return parameters;
	}
}
