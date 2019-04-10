package org.processmining.plugins.realtimedcc.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.neconformance.models.AbstractProcessReplayModel;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;

public class RealtimePetrinetReplayModel extends AbstractProcessReplayModel<Transition, String, Marking> {

	protected Petrinet net;
	protected Marking currentMarking;
	protected Random generator;
	
	protected List<Boolean> transitionIsForced;
	
	public RealtimePetrinetReplayModel(Petrinet net, Set<String> events, Marking initialMarking) {
		super(net.getTransitions(), events, PetrinetUtils.getInitialMarking(net, initialMarking));
		this.net = net;
	}
	
	public RealtimePetrinetReplayModel(RealtimePetrinetReplayModel toClone) {
		this(toClone.getPetrinet(), toClone.getLogElements(), toClone.getInitialState());
	}
	
	public void reset() {
		super.reset();
		currentMarking = new Marking();
		currentMarking.addAll(initialState.toList());
		transitionIsForced = new ArrayList<Boolean>();
		this.generator = new Random();
	}
	
	public void replay(RealtimePetrinetReplayModel previous, List<String> trace, int frozenSteps) {
		this.reset();
		int currentSequencePosition = 0;
		int previousChainCount = 0;
		int frozenStepCount = 0;
		while (currentSequencePosition < trace.size()) {
			String nextClass = (currentSequencePosition < trace.size() - 1)
					? trace.get(currentSequencePosition + 1)
					: null;
			if (previousChainCount < previous.size() && (frozenSteps == -1 || frozenStepCount < frozenSteps)) {
				do {
					this.addReplayStep(previous.getReplayMove(previousChainCount),
							previous.getModelElement(previousChainCount),
							previous.getTraceElement(previousChainCount),
							previous.getModelState(previousChainCount));
					this.currentMarking = this.getModelState(this.size()-1);		
					previousChainCount++;
				} while (this.getTraceElement(previousChainCount) != null);
				frozenStepCount++;
				currentSequencePosition++;
			} else {
				replay(trace.get(currentSequencePosition), nextClass, false);
				if (this.getTraceElement(this.size()-1) != null)
					currentSequencePosition++;
			}
		}		
	}

	public void replay(List<String> trace) {
		this.reset();
		int currentSequencePosition = 0;
		while (currentSequencePosition < trace.size()) {
			String nextClass = (currentSequencePosition < trace.size() - 1)
					? trace.get(currentSequencePosition + 1)
					: null;
			replay(trace.get(currentSequencePosition), nextClass, false);
			if (this.getTraceElement(this.size()-1) != null)
				currentSequencePosition++;
		}		
	}
	
	public ReplayMove replay(String event) {
		return replay(event, null, true);
	}
	
	public ReplayMove replay(String event, String nextClass, boolean fireAfterInvisible) {
		Set<Transition> enabledCandidates = new HashSet<Transition>();
		Set<Transition> allCandidates = new HashSet<Transition>();
		Set<Transition> filteredEnabledCandidates = new HashSet<Transition>();
		Set<Transition> filteredForcedCandidates = new HashSet<Transition>();

		enabledCandidates = PetrinetReplayUtils.getEnabledMappedTransitions(net, currentMarking, event);
		allCandidates = PetrinetReplayUtils.getMappedTransitions(net, event);
		filteredEnabledCandidates = filterOnNextTaskEnabler(enabledCandidates, event, nextClass);
		filteredForcedCandidates = filterOnNextTaskEnabler(allCandidates, event, nextClass);
			
		List<Transition> invisiblePath = 
			PetrinetReplayUtils.getInvisibleTaskReachabilityPaths(net, event, currentMarking);
			
		ReplayMove move = null;
		Transition t = null;
		String c = null;
		if (enabledCandidates.size() > 0) {
			if (filteredEnabledCandidates.size() > 0)
				t = getRandomTransition(filteredEnabledCandidates);
			else
				t = getRandomTransition(enabledCandidates);		
			c = event;
			move = ReplayMove.BOTH_SYNCHRONOUS;
		} else if (invisiblePath != null) {
			// Add all transitions until last one
			for (int i = 0; i < invisiblePath.size() - 1; i++) {
				Marking invm = currentMarking;
				invm = PetrinetReplayUtils.getMarkingAfterFire(net, invm, invisiblePath.get(i));
				this.addReplayStep(ReplayMove.MODELONLY_UNOBSERVABLE, invisiblePath.get(i), null, invm);
				this.transitionIsForced.add(false);
				this.currentMarking = invm;
			}
			t = invisiblePath.get(invisiblePath.size()-1);
			c = null;
			move = ReplayMove.MODELONLY_UNOBSERVABLE;
		} else if (allCandidates.size() > 0) {
			if (filteredForcedCandidates.size() > 0)
				t = getRandomTransition(filteredForcedCandidates);
			else
				t = getRandomTransition(allCandidates);		
			c = event;
			move = ReplayMove.BOTH_FORCED;
			this.transitionIsForced.add(true);
		} else {
			// Move the log only
			// Event was probably not mapped
			t = null;
			c = event;
			move = ReplayMove.LOGONLY_INSERTED;
		} 

		Marking m = currentMarking;
		if (t != null) // Get new marking
			m = PetrinetReplayUtils.getMarkingAfterFire(net, m, t);
		this.addReplayStep(move, t, c, m);
		this.currentMarking = this.getModelState(this.size()-1);
		
		if (fireAfterInvisible && c == null) {
			// Perform second-chance attempt
			return replay(event, nextClass, false);
		}
		
		return move;
	}
	
	private Transition getRandomTransition(Set<Transition> choices) {
		int index = generator.nextInt(choices.size());
		return (Transition) choices.toArray()[index];
	}

	public Petrinet getPetrinet() {
		return net;
	}

	private Set<Transition> filterOnNextTaskEnabler(Set<Transition> fireCandidates,
			String currentClass, String nextClass) {
		HashSet<Transition> filtered = new HashSet<Transition>();
		
		for (Transition t : fireCandidates) {
			Set<Transition> nextTaskEnabledTransitions = null;
			Marking nextState = PetrinetReplayUtils.getMarkingAfterFire(net, currentMarking, t);
			
			if (PetrinetReplayUtils.isInvisibleTransition(t))
				nextTaskEnabledTransitions = 
					PetrinetReplayUtils.getEnabledMappedTransitions(net, nextState, currentClass);
			else if(nextClass != null && !PetrinetReplayUtils.isInvisibleTransition(t))
				nextTaskEnabledTransitions = 
					PetrinetReplayUtils.getEnabledMappedTransitions(net, nextState, nextClass);
			
			if (nextTaskEnabledTransitions == null || nextTaskEnabledTransitions.size() > 0)
				filtered.add(t);
		}
		
		return filtered;
	}

	public boolean isExecutableModelElement(Transition element, Marking state) {
		return PetrinetReplayUtils.getEnabledTransitions(net, state).contains(element);
	}
	
	public boolean isExecutableLogElement(String element, Marking state) {
		return PetrinetReplayUtils.isTaskReachableByInvisiblePaths(net, element, state);		
	}

	public Set<Transition> getOrphanedModelElements() {
		Set<Transition> orphans = new HashSet<Transition>();
		for (Transition tr : net.getTransitions()) {
			if (!PetrinetReplayUtils.isInvisibleTransition(tr) && !this.getLogElements().contains(tr.getLabel()))
				orphans.add(tr);
		}
		return orphans;
	}

	public Set<String> getOrphanedLogElements() {
		Set<String> orphans = new HashSet<String>();
		for (Transition tr : net.getTransitions()) {
			String ec = tr.getLabel();
			if (!this.getLogElements().contains(ec))
				orphans.add(ec);
		}
		return orphans;
	}

	public ProcessReplayModel<Transition, String, Marking> copy() {
		return new RealtimePetrinetReplayModel(this);
	}
	
}
