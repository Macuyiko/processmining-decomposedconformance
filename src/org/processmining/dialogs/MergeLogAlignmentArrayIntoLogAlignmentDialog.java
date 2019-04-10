package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.parameters.MergeLogAlignmentArrayIntoLogAlignmentParameters;

public class MergeLogAlignmentArrayIntoLogAlignmentDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8697712384734043122L;

	private List<XEventClassifier> selectedClassifier;

	private XLog log;
	private MergeLogAlignmentArrayIntoLogAlignmentParameters parameters;

	public MergeLogAlignmentArrayIntoLogAlignmentDialog(XLog log, MergeLogAlignmentArrayIntoLogAlignmentParameters parameters) {
		this.log = log;
		this.parameters = parameters;
	}

	public JPanel getPanel(int n) {
		removeAll();
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		if (n == 0) {
			List<XEventClassifier> classifiers = log.getClassifiers();
			if (classifiers.isEmpty()) {
				classifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
			}
			selectedClassifier = new ArrayList<XEventClassifier>();
			selectedClassifier.add(parameters.getClassifier());
			ClassifierPanel classifierPanel = new ClassifierPanel(classifiers, selectedClassifier);
			add(classifierPanel, "0, 0");
		}
		return this;
	}

	public void finish() {
		parameters.setClassifier(selectedClassifier.get(0));
	}
}
