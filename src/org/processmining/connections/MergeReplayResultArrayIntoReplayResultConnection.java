package org.processmining.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.ReplayResultArray;
import org.processmining.parameters.MergeReplayResultArrayIntoReplayResultParameters;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class MergeReplayResultArrayIntoReplayResultConnection extends AbstractConnection {

	public final static String REPLAYS = "Replays";
	public final static String REPLAY = "Replay";

	private MergeReplayResultArrayIntoReplayResultParameters parameters;

	public MergeReplayResultArrayIntoReplayResultConnection(ReplayResultArray replays,
			PNRepResult replay, MergeReplayResultArrayIntoReplayResultParameters parameters) {
		super("Merge Replay Result Array Into Replay Result Connection");
		put(REPLAYS, replays);
		put(REPLAY, replay);
		this.parameters = parameters;
	}

	public MergeReplayResultArrayIntoReplayResultParameters getParameters() {
		return parameters;
	}
}

