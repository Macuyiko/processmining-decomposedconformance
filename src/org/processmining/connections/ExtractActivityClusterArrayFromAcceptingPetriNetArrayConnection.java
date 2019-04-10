package org.processmining.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.ActivityClusterArray;
import org.processmining.parameters.ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters;

public class ExtractActivityClusterArrayFromAcceptingPetriNetArrayConnection extends AbstractConnection {

	public final static String NETS = "Nets";
	public final static String CLUSTERS = "Clusters";

	private ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters parameters;

	public ExtractActivityClusterArrayFromAcceptingPetriNetArrayConnection(AcceptingPetriNetArray nets,
			ActivityClusterArray clusters, ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters parameters) {
		super("Extract Activity Cluster Array from Accepting Petri Net Array Connection");
		put(NETS, nets);
		put(CLUSTERS, clusters);
		this.parameters = parameters;
	}

	public ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters getParameters() {
		return parameters;
	}
}
