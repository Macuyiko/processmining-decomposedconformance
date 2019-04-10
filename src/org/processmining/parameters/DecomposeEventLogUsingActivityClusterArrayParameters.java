package org.processmining.parameters;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;

public class DecomposeEventLogUsingActivityClusterArrayParameters {

	private List<XEventClassifier> classifiers;
	private boolean removeEmptyTraces;
	
	public DecomposeEventLogUsingActivityClusterArrayParameters(XLog eventLog) {
		classifiers = new ArrayList<XEventClassifier>();
		if (eventLog.getClassifiers().isEmpty()) {
			classifiers.clear();
			classifiers.add(new XEventAndClassifier(new XEventNameClassifier(),
					new XEventLifeTransClassifier()));
		} else {
			classifiers.clear();
			classifiers.add(eventLog.getClassifiers().iterator().next());
		}
		removeEmptyTraces = false;
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
	
	public boolean equals(Object object) {
		if (object instanceof DecomposeEventLogUsingActivityClusterArrayParameters) {
			DecomposeEventLogUsingActivityClusterArrayParameters parameters = (DecomposeEventLogUsingActivityClusterArrayParameters) object;
			return classifiers.equals(parameters.classifiers);
		}
		return false;
	}

	public void setRemoveEmptyTraces(Boolean removeEmptyTraces) {
		this.removeEmptyTraces = removeEmptyTraces;
	}

	public Boolean getRemoveEmptyTraces() {
		return removeEmptyTraces;
	}
}
