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
import org.processmining.models.EventLogArray;
import org.processmining.parameters.DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters;

public class DiscoverAcceptingPetriNetArrayFromEventLogArrayDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4544473255468256618L;

	public DiscoverAcceptingPetriNetArrayFromEventLogArrayDialog(EventLogArray logs, DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
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
		}
		add(new ClassifierPanel(availableClassifiers, parameters.getClassifierList()), "0, 0");
		List<String> availableMiners = new ArrayList<String>();
		availableMiners.add(DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters.ALPHAMINER);
		availableMiners.add(DiscoverAcceptingPetriNetArrayFromEventLogArrayParameters.ILPMINER);
		add(new MinerPanel(availableMiners, parameters.getMinerList()), "0, 1");
	}
}