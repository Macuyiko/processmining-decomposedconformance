package org.processmining.parameters;

import java.util.HashSet;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;

public class ConvertPetriNetToAcceptingPetriNetParameters {

	private Marking initialMarking;
	private Set<Marking> finalMarkings;
	
	public ConvertPetriNetToAcceptingPetriNetParameters(Petrinet net) {
		initialMarking = new Marking();
		finalMarkings = new HashSet<Marking>();
		for (Place place : net.getPlaces()) {
			if (net.getInEdges(place).isEmpty()) {
				initialMarking.add(place);
			}
			if (net.getOutEdges(place).isEmpty()) {
				Marking finalMarking = new Marking();
				finalMarking.add(place);
				finalMarkings.add(finalMarking);
			}
		}
		if (finalMarkings.isEmpty()) {
			finalMarkings.add(new Marking());
		}
	}
	
	public void setInitialMarking(Marking initialMarking) {
		this.initialMarking = initialMarking;
	}
	
	public Marking getInitialMarking() {
		return initialMarking;
	}
	
	public void setFinalMarkings(Set<Marking> finalMarkings) {
		this.finalMarkings = finalMarkings;
	}
	
	public Set<Marking> getFinalMarkings() {
		return finalMarkings;
	}
	
	public boolean equals(Object object) {
		if (object instanceof ConvertPetriNetToAcceptingPetriNetParameters) {
			ConvertPetriNetToAcceptingPetriNetParameters parameters = (ConvertPetriNetToAcceptingPetriNetParameters) object;
			return initialMarking.equals(parameters.initialMarking) && finalMarkings.equals(parameters.finalMarkings);
		}
		return false;
	}
}
