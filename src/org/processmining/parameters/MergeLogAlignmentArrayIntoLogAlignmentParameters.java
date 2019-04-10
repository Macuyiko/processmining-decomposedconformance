package org.processmining.parameters;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.models.LogAlignmentArray;

public class MergeLogAlignmentArrayIntoLogAlignmentParameters {

	private List<XEventClassifier> classifiers;

	public MergeLogAlignmentArrayIntoLogAlignmentParameters(XLog log, LogAlignmentArray alignments) {
		classifiers = new ArrayList<XEventClassifier>(1);
		if (log.getClassifiers().isEmpty()) {
			setClassifier(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
		} else {
			setClassifier(log.getClassifiers().iterator().next());
		}
	}

	public XEventClassifier getClassifier() {
		return classifiers.get(0);
	}

	public void setClassifier(XEventClassifier classifier) {
		classifiers.clear();
		classifiers.add(classifier);
	}

	public List<XEventClassifier> getClassifiers() {
		return classifiers;
	}

	public boolean equals(Object object) {
		if (object instanceof MergeLogAlignmentArrayIntoLogAlignmentParameters) {
			MergeLogAlignmentArrayIntoLogAlignmentParameters parameters = (MergeLogAlignmentArrayIntoLogAlignmentParameters) object;
			return classifiers.equals(parameters.classifiers);
		}
		return false;
	}
}
