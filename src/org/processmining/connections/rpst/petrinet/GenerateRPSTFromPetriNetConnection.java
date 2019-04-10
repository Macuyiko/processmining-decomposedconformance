package org.processmining.connections.rpst.petrinet;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.rpst.petrinet.PetriNetRPST;
import org.processmining.parameters.rpst.petrinet.GenerateRPSTFromPetriNetParameters;

public class GenerateRPSTFromPetriNetConnection extends AbstractConnection {

	public final static String NET = "Net";
	public final static String RPST = "RPST";

	private GenerateRPSTFromPetriNetParameters parameters;

	public GenerateRPSTFromPetriNetConnection(AcceptingPetriNet net,
			PetriNetRPST rpst, GenerateRPSTFromPetriNetParameters parameters) {
		super("Generate RPST from Petri Net Connection");
		put(NET, net);
		put(RPST, rpst);
		this.parameters = parameters;
	}

	public GenerateRPSTFromPetriNetParameters getParameters() {
		return parameters;
	}
}
