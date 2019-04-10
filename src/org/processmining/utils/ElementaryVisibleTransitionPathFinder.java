package org.processmining.utils;

import java.util.HashSet;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

public class ElementaryVisibleTransitionPathFinder extends AbstractElementaryPathFinder {

	private Set<org.processmining.models.graphbased.directed.petrinet.elements.Transition> transitions;

	public ElementaryVisibleTransitionPathFinder(
			Set<org.processmining.models.graphbased.directed.petrinet.elements.Transition> visibleTransitions) {
		transitions = new HashSet<org.processmining.models.graphbased.directed.petrinet.elements.Transition>(
				visibleTransitions);
	}

	public boolean isStartNode(PetrinetNode node) {
		if (node instanceof org.processmining.models.graphbased.directed.petrinet.elements.Transition) {
			org.processmining.models.graphbased.directed.petrinet.elements.Transition transition = (org.processmining.models.graphbased.directed.petrinet.elements.Transition) node;
			return transitions.contains(transition);
		}
		return false;
	}

	public boolean isEndNode(PetrinetNode node) {
		return isStartNode(node);
	}

	public boolean isStartTransition(Object object) {
		if (object instanceof org.processmining.models.graphbased.directed.transitionsystem.Transition) {
			org.processmining.models.graphbased.directed.transitionsystem.Transition cgTransition = (org.processmining.models.graphbased.directed.transitionsystem.Transition) object;
			org.processmining.models.graphbased.directed.petrinet.elements.Transition transition = (org.processmining.models.graphbased.directed.petrinet.elements.Transition) cgTransition
					.getIdentifier();
			return transitions.contains(transition);
		}
		return false;
	}

	public boolean isEndTransition(Object object) {
		return isStartTransition(object);
	}
}