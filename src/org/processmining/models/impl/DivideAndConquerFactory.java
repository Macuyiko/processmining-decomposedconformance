package org.processmining.models.impl;

import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.CausalActivityGraph;
import org.processmining.models.CausalActivityMatrix;
import org.processmining.models.EventLogArray;
import org.processmining.models.LogAlignment;
import org.processmining.models.LogAlignmentArray;
import org.processmining.models.ReplayResultArray;
import org.processmining.models.TraceAlignment;

public class DivideAndConquerFactory {

	public static AcceptingPetriNetArray createAcceptingPetriNetArray() {
		return new AcceptingPetriNetArrayImpl();
	}

	public static AcceptingPetriNet createAcceptingPetriNet() {
		return new AcceptingPetriNetImpl();
	}

	public static ActivityClusterArray createActivityClusterArray() {
		return new ActivityClusterArrayImpl();
	}

	public static CausalActivityGraph createCausalActivityGraph() {
		return new CausalActivityGraphImpl();
	}

	public static CausalActivityMatrix createCausalActivityMatrix() {
		return new CausalActivityMatrixImpl();
	}

	public static EventLogArray createEventLogArray() {
		return new EventLogArrayImpl();
	}
	
	public static ReplayResultArray createReplayResultArray() {
		return new ReplayResultArrayImpl();
	}
	
	public static TraceAlignment createTraceAlignment() {
		return new TraceAlignmentImpl();
	}
	
	public static LogAlignment createLogAlignment() {
		return new LogAlignmentImpl();
	}
	
	public static LogAlignmentArray createLogAlignmentArray() {
		return new LogAlignmentArrayImpl();
	}
}
