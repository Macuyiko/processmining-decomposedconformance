package org.processmining.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.parameters.ConvertPetriNetToAcceptingPetriNetParameters;

public class ConvertPetriNetToAcceptingPetriNetConnection extends AbstractConnection {

	public final static String NET = "PetriNet";
	public final static String ACCEPTINGNET = "AcceptingPetriNet";

	private ConvertPetriNetToAcceptingPetriNetParameters parameters;

	public ConvertPetriNetToAcceptingPetriNetConnection(Petrinet net,
			AcceptingPetriNet acceptingNet, ConvertPetriNetToAcceptingPetriNetParameters parameters) {
		super("Convert Petri Net To Accepting Petri Net Connection");
		put(NET, net);
		put(ACCEPTINGNET, acceptingNet);
		this.parameters = parameters;
	}

	public ConvertPetriNetToAcceptingPetriNetParameters getParameters() {
		return parameters;
	}
}
