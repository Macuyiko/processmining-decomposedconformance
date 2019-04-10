package org.processmining.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.ActivityClusterArray;
import org.processmining.parameters.DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters;

public class DecomposeAcceptingPetriNetUsingActivityClusterArrayConnection extends AbstractConnection {

	public final static String NET = "Net";
	public final static String CLUSTERS = "Clusters";
	public final static String NETS = "Nets";

	private DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters parameters;

	public DecomposeAcceptingPetriNetUsingActivityClusterArrayConnection(AcceptingPetriNet net,
			AcceptingPetriNetArray nets, ActivityClusterArray clusters, DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters parameters) {
		super("Decompose Accepting Petri Net Using Activity Cluster Array Connection");
		put(NET, net);
		put(CLUSTERS, clusters);
		put(NETS, nets);
		this.parameters = parameters;
	}

	public DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters getParameters() {
		return parameters;
	}
}

