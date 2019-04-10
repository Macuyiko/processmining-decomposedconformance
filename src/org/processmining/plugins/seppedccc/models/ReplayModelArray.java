package org.processmining.plugins.seppedccc.models;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;

public class ReplayModelArray extends ArrayList<List<ProcessReplayModel<Transition, XEventClass, Marking>>> {
	private static final long serialVersionUID = 3911815361421554561L;
	private AcceptingPetriNetArray acceptingPetrinetArray;
	private EventLogArray eventLogArray;
	private PetrinetLogMapper[] mapperArray;
	
	public ReplayModelArray(AcceptingPetriNetArray apna, EventLogArray ela, PetrinetLogMapper[] ma) {
		this.acceptingPetrinetArray = apna;
		this.eventLogArray = ela;
		this.mapperArray = ma;
	}
	
	public AcceptingPetriNetArray getAcceptingPetrinetArray() {
		return acceptingPetrinetArray;
	}
	
	public EventLogArray getEventLogArray() {
		return eventLogArray;
	}
	
	public PetrinetLogMapper[] getMapperArray() {
		return mapperArray;
	}
}
