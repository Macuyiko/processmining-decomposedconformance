package org.processmining.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.LogAlignment;
import org.processmining.models.LogAlignmentArray;
import org.processmining.parameters.MergeLogAlignmentArrayIntoLogAlignmentParameters;

public class MergeLogAlignmentArrayIntoLogAlignmentConnection extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String ALIGNMENTS = "Alignments";
	public final static String ALIGNMENT = "Alignment";

	private MergeLogAlignmentArrayIntoLogAlignmentParameters parameters;

	public MergeLogAlignmentArrayIntoLogAlignmentConnection(XLog log, LogAlignmentArray alignments,
			LogAlignment alignment, MergeLogAlignmentArrayIntoLogAlignmentParameters parameters) {
		super("Merge Log Alignments Connection");
		put(LOG, log);
		put(ALIGNMENTS, alignments);
		put(ALIGNMENT, alignment);
		this.parameters = parameters;
	}

	public MergeLogAlignmentArrayIntoLogAlignmentParameters getParameters() {
		return parameters;
	}
}
