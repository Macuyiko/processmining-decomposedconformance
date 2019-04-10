package org.processmining.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.parameters.MergeAcceptingPetriNetArrayIntoAcceptingPetriNetParameters;

public class MergeAcceptingPetriNetArrayIntoAcceptingPetriNetConnection extends AbstractConnection {

	public final static String NETS = "Nets";
	public final static String NET = "Net";

	private MergeAcceptingPetriNetArrayIntoAcceptingPetriNetParameters parameters;

	public MergeAcceptingPetriNetArrayIntoAcceptingPetriNetConnection(AcceptingPetriNetArray nets,
			AcceptingPetriNet net, MergeAcceptingPetriNetArrayIntoAcceptingPetriNetParameters parameters) {
		super("Merge Accepting Petri Net Array Into Accepting Petri Net Connection");
		put(NETS, nets);
		put(NET, net);
		this.parameters = parameters;
	}

	public MergeAcceptingPetriNetArrayIntoAcceptingPetriNetParameters getParameters() {
		return parameters;
	}
}
