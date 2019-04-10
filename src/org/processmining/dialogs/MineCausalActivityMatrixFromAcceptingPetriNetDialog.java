package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.parameters.MineCausalActivityMatrixFromAcceptingPetriNetParameters;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class MineCausalActivityMatrixFromAcceptingPetriNetDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6732517188360796457L;

	public MineCausalActivityMatrixFromAcceptingPetriNetDialog(AcceptingPetriNet net, XLog log,
			final MineCausalActivityMatrixFromAcceptingPetriNetParameters parameters, int rank) {
		if (rank == 1) {
			double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
			setLayout(new TableLayout(size));
			List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>(log.getClassifiers());
			if (classifiers.isEmpty()) {
				classifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
			}
			add(new ClassifierPanel(classifiers, parameters.getClassifierList()), "0, 0");
		} else if (rank == 2) {
			XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
			parameters.setActivities(new HashSet<XEventClass>(info.getEventClasses().getClasses()));

			double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30 } };
			setLayout(new TableLayout(size));
			add(new TransEvClassMappingPanel(parameters.getActivities(), parameters.getMapping()), "0, 0");

			final JCheckBox box = SlickerFactory.instance().createCheckBox("Use coverability graph", true);
			box.setSelected(parameters.getMinerType() == MineCausalActivityMatrixFromAcceptingPetriNetParameters.CG);
			box.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (box.isSelected()) {
						parameters.getMinerTypeList().clear();
						parameters.getMinerTypeList().add(MineCausalActivityMatrixFromAcceptingPetriNetParameters.CG);
					} else {
						parameters.getMinerTypeList().clear();
						parameters.getMinerTypeList().add(MineCausalActivityMatrixFromAcceptingPetriNetParameters.PN);
					}
				}

			});
			box.setOpaque(false);
			add(box, "0, 1");
		}
	}

}