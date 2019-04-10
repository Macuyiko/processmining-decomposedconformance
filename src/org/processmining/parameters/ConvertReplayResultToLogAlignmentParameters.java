package org.processmining.parameters;

import java.util.ArrayList;
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
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.EventLogArray;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;


public class ConvertReplayResultToLogAlignmentParameters {

	private List<List<XEventClassifier>> classifiers;
	private List<TransEvClassMapping> mappings;

	public ConvertReplayResultToLogAlignmentParameters(EventLogArray logs, AcceptingPetriNetArray nets) {
		int size = (nets.getSize() < logs.getSize() ? nets.getSize() : logs.getSize());
		classifiers = new ArrayList<List<XEventClassifier>>();
		mappings = new ArrayList<TransEvClassMapping>();
		
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
		}
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

	public boolean equals(Object object) {
		if (object instanceof ConvertReplayResultToLogAlignmentParameters) {
			ConvertReplayResultToLogAlignmentParameters parameters = (ConvertReplayResultToLogAlignmentParameters) object;
			return classifiers.equals(parameters.classifiers) && mappings.equals(parameters.mappings);
		}
		return false;
	}
}
