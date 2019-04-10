package org.processmining.connections.rpst.petrinet;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.rpst.petrinet.PetriNetRPST;
import org.processmining.parameters.rpst.petrinet.CreateAcceptingPetriNetArrayFromAllRPSTNodesParameters;

public class CreateAcceptingPetriNetArrayFromAllRPSTNodesConnection extends AbstractConnection {

	public final static String RPST = "RPST";
	public final static String NETS = "Nets";

	private CreateAcceptingPetriNetArrayFromAllRPSTNodesParameters parameters;

	public CreateAcceptingPetriNetArrayFromAllRPSTNodesConnection(PetriNetRPST rpst, AcceptingPetriNetArray nets,
			CreateAcceptingPetriNetArrayFromAllRPSTNodesParameters parameters) {
		super("Create Accepting Petri Net Array From All RPST Nodes Connection");
		put(RPST, rpst);
		put(NETS,nets);
		this.parameters = parameters;
	}

	public CreateAcceptingPetriNetArrayFromAllRPSTNodesParameters getParameters() {
		return parameters;
	}
}
