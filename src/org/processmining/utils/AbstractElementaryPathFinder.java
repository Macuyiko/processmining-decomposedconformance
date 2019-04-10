package org.processmining.utils;

import java.util.Collection;
import java.util.HashSet;

import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.transitionsystem.CoverabilityGraph;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;

public abstract class AbstractElementaryPathFinder {

	public Collection<Pair<PetrinetNode, PetrinetNode>> find(PetrinetGraph pn) {
		Collection<Pair<PetrinetNode, PetrinetNode>> nodePairs = new HashSet<Pair<PetrinetNode, PetrinetNode>>();
		Collection<PetrinetNode> nodes = pn.getNodes();
		for (PetrinetNode node : nodes) {
			if (isStartNode(node)) {
				Collection<PetrinetNode> sourceNodes = new HashSet<PetrinetNode>();
				Collection<PetrinetNode> seenNodes = new HashSet<PetrinetNode>();
				sourceNodes.add(node);
				find(pn, node, sourceNodes, nodePairs, seenNodes);
			}
		}
		return nodePairs;
	}
	
	public Collection<Pair<PetrinetNode, PetrinetNode>> find(CoverabilityGraph cg) {
		Collection<Pair<PetrinetNode, PetrinetNode>> nodePairs = new HashSet<Pair<PetrinetNode, PetrinetNode>>();
		Collection<Transition> transitions = cg.getEdges();
		for (Transition transition : transitions) {
			if (isStartTransition(transition)) {
				Collection<Transition> sourceTransitions = new HashSet<Transition>();
				Collection<Transition> seenTransitions = new HashSet<Transition>();
				sourceTransitions.add(transition);
				find(cg, transition, sourceTransitions, nodePairs, seenTransitions);
			}
		}
		return nodePairs;
	}
	
	private void find(PetrinetGraph pn, PetrinetNode startNode, Collection<PetrinetNode> sourceNodes, Collection<Pair<PetrinetNode, PetrinetNode>> nodePairs, Collection<PetrinetNode> seenNodes) {
		while (!sourceNodes.isEmpty()) {
			PetrinetNode sourceNode = sourceNodes.iterator().next();
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = pn.getOutEdges(sourceNode);
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> outEdge : outEdges) {
				PetrinetNode targetNode = outEdge.getTarget();
				if (isEndNode(targetNode)) {
					nodePairs.add(new Pair<PetrinetNode, PetrinetNode>(startNode, targetNode));
				} else {
					sourceNodes.add(targetNode);
				}
			}
			seenNodes.add(sourceNode);
			sourceNodes.removeAll(seenNodes);
		}
	}
	
	private void find(CoverabilityGraph cg, Transition startTransition, Collection<Transition> sourceTransitions, Collection<Pair<PetrinetNode, PetrinetNode>> nodePairs, Collection<Transition> seenTransitions) {
		while (!sourceTransitions.isEmpty()) {
			Transition sourceTransition = sourceTransitions.iterator().next();
			State state = sourceTransition.getTarget();
			Collection<Transition> targetTransitions = cg.getOutEdges(state);
			for (Transition targetTransition : targetTransitions) {
				if (isEndTransition(targetTransition)) {
					PetrinetNode sourceNode = (PetrinetNode) startTransition.getIdentifier();
					PetrinetNode targetNode = (PetrinetNode) targetTransition.getIdentifier();
					nodePairs.add(new Pair<PetrinetNode, PetrinetNode>(sourceNode, targetNode));
				} else {
					sourceTransitions.add(targetTransition);
				}
			}
			seenTransitions.add(sourceTransition);
			sourceTransitions.removeAll(seenTransitions);
		}
	}
	
	public abstract boolean isStartNode(PetrinetNode node);
	
	public abstract boolean isEndNode(PetrinetNode node);

	public abstract boolean isStartTransition(Object transition);
	
	public abstract boolean isEndTransition(Object transition);
}
