package org.processmining.parameters;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;

public class MineCausalActivityMatrixFromEventLogParameters {

	private List<XEventClassifier> classifiers;
	private List<String> miners;
	
	public static final String STANDARD = "Standard miner";
	public static final String HEURISTICS = "Heuristics miner";
	public static final String FUZZY = "Fuzzy miner";
	
	public MineCausalActivityMatrixFromEventLogParameters(XLog eventLog) {
		classifiers = new ArrayList<XEventClassifier>();
		if (eventLog.getClassifiers().isEmpty()) {
			classifiers.clear();
			classifiers.add(new XEventAndClassifier(new XEventNameClassifier(),
					new XEventLifeTransClassifier()));
		} else {
			classifiers.clear();
			classifiers.add(eventLog.getClassifiers().iterator().next());
		}
		miners = new ArrayList<String>();
		miners.add(STANDARD);
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
	
	public void setMiner(String miner) {
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
		if (object instanceof MineCausalActivityMatrixFromEventLogParameters) {
			MineCausalActivityMatrixFromEventLogParameters parameters = (MineCausalActivityMatrixFromEventLogParameters) object;
			return classifiers.equals(parameters.classifiers) && miners.equals(parameters.miners);
		}
		return false;
	}
}
