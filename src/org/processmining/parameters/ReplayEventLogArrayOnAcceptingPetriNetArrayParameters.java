package org.processmining.parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.dialogs.TransEvClassMappingPanel;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;

public class ReplayEventLogArrayOnAcceptingPetriNetArrayParameters {

	private List<List<XEventClassifier>> classifiers;
	private List<TransEvClassMapping> mappings;
	protected List<IPNReplayAlgorithm> replayers;
	protected List<IPNReplayParameter> replayParameters;

	public ReplayEventLogArrayOnAcceptingPetriNetArrayParameters(EventLogArray logs, AcceptingPetriNetArray nets) {
		int size = (nets.getSize() < logs.getSize() ? nets.getSize() : logs.getSize());
		classifiers = new ArrayList<List<XEventClassifier>>();
		mappings = new ArrayList<TransEvClassMapping>();
		replayers = new ArrayList<IPNReplayAlgorithm>();
		replayParameters = new ArrayList<IPNReplayParameter>();
		
		for (int index = 0; index < size; index++) {
			classifiers.add(index, new ArrayList<XEventClassifier>());
			if (logs.getLog(index).getClassifiers().isEmpty()) {
				setClassifier(index, new XEventAndClassifier(new XEventNameClassifier(),
						new XEventLifeTransClassifier()));
			} else {
				setClassifier(index, logs.getLog(0).getClassifiers().iterator().next());
			}
			Set<XEventClass> activities = new HashSet<XEventClass>();
			XLogInfo info = XLogInfoFactory.createLogInfo(logs.getLog(index), getClassifier(index));
			activities.addAll(info.getEventClasses().getClasses());
			mappings.add(index, new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(nets.getNet(index), activities, getClassifier(index)).getMapping());
			replayers.add(index, new PetrinetReplayerWithILP());
			replayParameters.add(index, createReplayParameters(activities, new XEventClass(TransEvClassMappingPanel.INVISIBLE,
					info.getEventClasses().size()), nets.getNet(index)));
		}
	}
	
	public IPNReplayParameter createReplayParameters(Collection<XEventClass> activities, XEventClass invisibleActivity, AcceptingPetriNet net) {
		IPNReplayParameter parameters = new CostBasedCompleteParam(activities, new XEventClass(TransEvClassMappingPanel.INVISIBLE,
				activities.size()), net.getNet().getTransitions());
		parameters.setInitialMarking(net.getInitialMarking());
		Set<Marking> finalMarkings = net.getFinalMarkings();
		if (finalMarkings.isEmpty()) {
			finalMarkings = new HashSet<Marking>();
			finalMarkings.add(new Marking());
		}
		parameters.setFinalMarkings(finalMarkings.toArray(new Marking[0]));
		return parameters;
	}
	
	public void setClassifier(int index, XEventClassifier classifier) {
		classifiers.get(index).clear();
		classifiers.get(index).add(classifier);
	}

	public XEventClassifier getClassifier(int index) {
		return classifiers.get(index).get(0);
	}

	public List<XEventClassifier> getClassifiers(int index) {
		return classifiers.get(index);
	}

	public void setMapping(int index, TransEvClassMapping mapping) {
		mappings.remove(index);
		mappings.add(index, mapping);
	}

	public TransEvClassMapping getMapping(int index) {
		return mappings.get(index);
	}

	public void setReplayer(int index, IPNReplayAlgorithm replayer) {
		replayers.remove(index);
		replayers.add(index, replayer);
	}

	public IPNReplayAlgorithm getReplayer(int index) {
		return replayers.get(index);
	}

	public void setReplayParameters(int index, IPNReplayParameter parameters) {
		replayParameters.remove(index);
		replayParameters.add(index, parameters);
	}

	public IPNReplayParameter getReplayParameters(int index) {
		return replayParameters.get(index);
	}

	public boolean equals(Object object) {
		if (object instanceof ReplayEventLogArrayOnAcceptingPetriNetArrayParameters) {
			ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters = (ReplayEventLogArrayOnAcceptingPetriNetArrayParameters) object;
			return classifiers.equals(parameters.classifiers) && mappings.equals(parameters.mappings)
					&& replayers.equals(parameters.replayers) && replayParameters.equals(parameters.replayParameters);
		}
		return false;
	}
}
