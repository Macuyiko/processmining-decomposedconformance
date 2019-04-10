package org.processmining.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.CausalActivityMatrix;
import org.processmining.parameters.MineCausalActivityMatrixFromEventLogParameters;

public class MineCausalActivityMatrixFromEventLogConnection extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String MATRIX = "Matrix";

	private MineCausalActivityMatrixFromEventLogParameters parameters;

	public MineCausalActivityMatrixFromEventLogConnection(XLog log, CausalActivityMatrix matrix,
			MineCausalActivityMatrixFromEventLogParameters parameters) {
		super("Mine Causal Activity Matrix From Event Log Connection");
		put(LOG, log);
		put(MATRIX, matrix);
		this.parameters = parameters;
	}

	public MineCausalActivityMatrixFromEventLogParameters getParameters() {
		return parameters;
	}
}
