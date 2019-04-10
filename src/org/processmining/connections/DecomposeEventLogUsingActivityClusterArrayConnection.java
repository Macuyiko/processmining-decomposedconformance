package org.processmining.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.EventLogArray;
import org.processmining.parameters.DecomposeEventLogUsingActivityClusterArrayParameters;

public class DecomposeEventLogUsingActivityClusterArrayConnection extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String CLUSTERS = "Clusters";
	public final static String LOGS = "Logs";

	private DecomposeEventLogUsingActivityClusterArrayParameters parameters;

	public DecomposeEventLogUsingActivityClusterArrayConnection(XLog log,
			ActivityClusterArray clusters, EventLogArray logs, DecomposeEventLogUsingActivityClusterArrayParameters parameters) {
		super("Decompose Event Log Using Activity Cluster Array Connection");
		put(LOG, log);
		put(CLUSTERS, clusters);
		put(LOGS, logs);
		this.parameters = parameters;
	}

	public DecomposeEventLogUsingActivityClusterArrayParameters getParameters() {
		return parameters;
	}
}