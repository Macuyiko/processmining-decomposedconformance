package org.processmining.parameters;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.models.EventLogArray;

public class DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters {

	public final static String ALPHAMINER = "Alpha Miner";
	public final static String ILPMINER = "ILP Miner";
	
	private List<XEventClassifier> classifiers;
	private List<String> miners;

	public DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters(EventLogArray logs) {
		classifiers = new ArrayList<XEventClassifier>();
		List<XEventClassifier> availableClassifiers = new ArrayList<XEventClassifier>();
		for (int i = 0; i < logs.getSize(); i++) {
			if (i == 0) {
				availableClassifiers.addAll(logs.getLog(i).getClassifiers());
			} else {
				availableClassifiers.retainAll(logs.getLog(i).getClassifiers());
			}
			if (availableClassifiers.isEmpty()) {
				availableClassifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
			}
			classifiers.add(availableClassifiers.get(0));
		}
		miners = new ArrayList<String>();
		miners.add(ALPHAMINER);
	}

	public void setClassifier(XEventClassifier classifier) {
		classifiers.clear();
		classifiers.add(classifier);
	}

	public XEventClassifier getClassifier() {
		return classifiers.get(0);
	}

	public List<XEventClassifier> getClassifierList() {
		return classifiers;
	}

	public void SetMiner(String miner) {
		miners.clear();
		miners.add(miner);
	}
	
	public String getMiner() {
		return miners.get(0);
	}
	
	public List<String> getMinerList() {
		return miners;
	}
	
	public boolean equals(Object object) {
		if (object instanceof DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters) {
			DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters parameters = (DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters) object;
			return classifiers.equals(parameters.classifiers) && miners.equals(parameters.miners);
		}
		return false;
	}
}
