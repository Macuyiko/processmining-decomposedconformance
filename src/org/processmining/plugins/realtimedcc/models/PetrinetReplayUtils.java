package org.processmining.plugins.realtimedcc.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;

public class PetrinetReplayUtils extends PetrinetUtils {
	
	public static boolean isInvisibleTransition(Transition t) {
		return t.isInvisible();
	}
	
	public static Marking getMarkingAfterFire(Petrinet net, Marking current, Transition fired) {
		PetrinetSemantics semantics = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);
		semantics.initialize(net.getTransitions(), current);
		
		Set<Place> missingPlaces = PetrinetReplayUtils.getPlacesBeforeTransition(net, fired);
		missingPlaces.removeAll(current);
		
		Marking forcedMarking = new Marking(semantics.getCurrentState());
		forcedMarking.addAll(missingPlaces);
		semantics.setCurrentState(forcedMarking);
		
		try {
			semantics.executeExecutableTransition(fired);
		} catch (IllegalTransitionException e) {
			System.err.println("DANGER! Illegal transition exception occurred when trying to derive next state!");
			System.err.println("Current marking is:");
			System.err.println(current);
			System.err.println("Missing places are:");
			System.err.println(missingPlaces);
			System.err.println("All before places are:");
			System.err.println(PetrinetReplayUtils.getPlacesBeforeTransition(net, fired));
			System.err.println("Transition was:");
			System.err.println(fired);
			System.err.println("I'm putting the output places in the current marking...");
			Marking newMarking = current;
			newMarking.addAll(PetrinetReplayUtils.getPlacesAfterTransition(net, fired));
			return newMarking;
		}
		
		Marking newMarking = semantics.getCurrentState();
		
		return newMarking;
	}
	
	public static Set<Transition> getMappedTransitions(Petrinet net, String task) {
		Set<Transition> mt = new HashSet<Transition>();
		for (Transition t : net.getTransitions())
			if (t.getLabel().equals(task) && !PetrinetReplayUtils.isInvisibleTransition(t))
				mt.add(t);
		return mt;
	}
	
	public static Set<String> getVisibleTransitionLabels(Petrinet net) {
		Set<String> mt = new HashSet<String>();
		for (Transition t : net.getTransitions())
			if (!PetrinetReplayUtils.isInvisibleTransition(t))
				mt.add(t.getLabel());
		return mt;
	}

	public static Collection<Transition> getEnabledTransitions(Petrinet net, Marking current) {
		PetrinetSemantics semantics = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);
		semantics.initialize(net.getTransitions(), current);
		Collection<Transition> fireables = semantics.getExecutableTransitions();
		return fireables;
	}

	public static Set<Transition> getEnabledMappedTransitions(Petrinet net, Marking current, String task) {
		Collection<Transition> allTransitions = PetrinetReplayUtils.getMappedTransitions(net, task);
		allTransitions.retainAll(PetrinetReplayUtils.getEnabledTransitions(net, current));
		return new HashSet<Transition>(allTransitions);
	}

	public static Set<Transition> getEnabledInvisibleTransitions(Petrinet net, Marking current) {
		Collection<Transition> enabled = PetrinetReplayUtils.getEnabledTransitions(net, current);
		HashSet<Transition> invisibles = new HashSet<Transition>();
		for (Transition t : enabled) {
			if (PetrinetReplayUtils.isInvisibleTransition(t))
				invisibles.add(t);
		}
		return invisibles;
	}
		
	public static List<Transition> getInvisibleTaskReachabilityPaths(
			Petrinet petriNet,
			String task,
			Marking marking) {
		Marking initialMarking = getInitialMarking(petriNet, marking);
		return getInvisibleTaskReachabilityPaths(
				petriNet,
				task,
				initialMarking,
				50,
				true, true, false);
	}
	
	public static List<Transition> getInvisibleTaskReachabilityPaths(
			Petrinet petriNet,
			String task,
			Marking marking,
			int maxStates,
			boolean useImmediate,
			boolean useOne,
			boolean useBFS) {
		
		// Is the task reachable now?
		if (useImmediate && getEnabledMappedTransitions(petriNet, marking, task).size() > 0)
			return new ArrayList<Transition>();
		
		// Is the task reachable in one step -- quick optimization
		List<Transition> single = getSingleInvisibleTaskReachabilityPath(petriNet, task, marking);
		if (useOne && single != null)
			return single;
		
		// Check complete exploration -- limited number of states
		return getInvisibleTaskReachabilityPath(petriNet, task, marking, maxStates, useBFS);
		
		//return null;
	}

	public static List<Transition> getInvisibleTaskReachabilityPath(
			Petrinet petriNet,
			String task,
			Marking marking,
			int maxStates,
			boolean useBFS) {
		Set<Marking> visitedMarkings = new HashSet<Marking>();
		visitedMarkings.add(marking);
		List<Marking> queuedMarkings = new ArrayList<Marking>();
		queuedMarkings.add(marking);
		Map<Marking, List<Transition>> pathToMarking = new HashMap<Marking, List<Transition>>();
		pathToMarking.put(marking, new ArrayList<Transition>());
		
		int statesChecked = 0;
		
		while (queuedMarkings.size() > 0) {
			statesChecked++;
			if (maxStates > 0 && statesChecked > maxStates) {
				//System.err.println("* Max state exploration limit ("+maxStates+") reached: "+task.toString());
				return null;
			}
			
			Marking markingTodo = queuedMarkings.remove(0);

			// Is the task enabled in this state?
			if (getEnabledMappedTransitions(petriNet, markingTodo, task).size() > 0)
				return pathToMarking.get(markingTodo);
			
			// Try other states
			Set<Transition> fireableTransitions = getEnabledInvisibleTransitions(petriNet, markingTodo);
			for (Transition fire : fireableTransitions) {
				Marking after = getMarkingAfterFire(petriNet, markingTodo, fire);
				if (!visitedMarkings.contains(after)) {
					if (useBFS) {
						queuedMarkings.add(after); // BFS: append
					} else {
						queuedMarkings.add(0, after); // DFS: prepend
					}
					List<Transition> newPath = new ArrayList<Transition>(pathToMarking.get(markingTodo));
					newPath.add(fire);
					pathToMarking.put(after, newPath);
					visitedMarkings.add(after);
				}
			}
	
		}
		
		return null;
	}

	public static List<Transition> getSingleInvisibleTaskReachabilityPath(
			Petrinet petriNet,
			String task,
			Marking marking) {
		List<Transition> path = new ArrayList<Transition>();
		
		Set<Transition> fireableTransitions = getEnabledInvisibleTransitions(petriNet, marking);
		for (Transition fire : fireableTransitions) {
			Marking after = getMarkingAfterFire(petriNet, marking, fire);
			if (getEnabledMappedTransitions(petriNet, after, task).size() > 0) {
				path.add(fire);
				return path;
			}
		}
		return null;
	}

	public static boolean isTaskReachableByInvisiblePaths(Petrinet net, String element, Marking state) {
		return getInvisibleTaskReachabilityPaths(net, element, state) != null;
	}
	
}
