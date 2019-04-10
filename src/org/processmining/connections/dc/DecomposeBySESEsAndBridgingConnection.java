package org.processmining.connections.dc;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.parameters.dc.DecomposeBySESEsAndBridgingParameters;

public class DecomposeBySESEsAndBridgingConnection extends AbstractConnection {

	public final static String NET = "Net";
	public final static String NETS = "Nets";

	private DecomposeBySESEsAndBridgingParameters parameters;

	public DecomposeBySESEsAndBridgingConnection(AcceptingPetriNet net, AcceptingPetriNetArray nets,
			DecomposeBySESEsAndBridgingParameters parameters) {
		super("Decompose By SESEs and Bridging Connection");
		put(NET, net);
		put(NETS,nets);
		this.parameters = parameters;
	}

	public DecomposeBySESEsAndBridgingParameters getParameters() {
		return parameters;
	}
}
