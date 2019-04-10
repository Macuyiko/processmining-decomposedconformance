package org.processmining.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.EventLogArray;
import org.processmining.parameters.DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters;

public class DiscoverAcceptingPetriNetArrayFromEventLogArrayConnection extends AbstractConnection {

	public final static String LOGS = "Logs";
	public final static String NETS = "Nets";

	private DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters parameters;

	public DiscoverAcceptingPetriNetArrayFromEventLogArrayConnection(EventLogArray logs,
			AcceptingPetriNetArray nets, DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters parameters) {
		super("Discover Accepting Petri Net Array From Event Log Array Connection");
		put(LOGS, logs);
		put(NETS, nets);
		this.parameters = parameters;
	}

	public DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters getParameters() {
		return parameters;
	}
}
