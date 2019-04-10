package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.ReplayResultArray;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.parameters.ConvertReplayResultToLogAlignmentParameters;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

public class ConvertReplayResultToLogAlignmentDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7981926888579119733L;

	private List<XEventClassifier> selectedClassifier;
	private Map<String, XEventClass> activities;
	private XEventClass invisibleActivity;
	private TransEvClassMapping mapping;

//	private ReplayResultArray replays;
	private EventLogArray logs;
	private AcceptingPetriNetArray nets;
	private ConvertReplayResultToLogAlignmentParameters parameters;

	public ConvertReplayResultToLogAlignmentDialog(ReplayResultArray replays, EventLogArray logs,
			AcceptingPetriNetArray nets, ConvertReplayResultToLogAlignmentParameters parameters) {
//		this.replays = replays;
		this.logs = logs;
		this.nets = nets;
		this.parameters = parameters;
	}

	public JPanel getPanel(int n) {
		removeAll();
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		if (n == 0) {
			List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();
			for (int i = 0; i < logs.getSize(); i++) {
				if (i == 0) {
					classifiers.addAll(logs.getLog(i).getClassifiers());
				} else {
					classifiers.retainAll(logs.getLog(i).getClassifiers());
				}
			}
			if (classifiers.isEmpty()) {
				classifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
			}
			selectedClassifier = new ArrayList<XEventClassifier>();
			selectedClassifier.add(parameters.getClassifier(0));
			ClassifierPanel classifierPanel = new ClassifierPanel(classifiers, selectedClassifier);
			add(classifierPanel, "0, 0");
		} else if (n == 1) {
			for (int i = 0; i < logs.getSize(); i++) {
				parameters.setClassifier(i, selectedClassifier.get(0));
			}
			activities = new HashMap<String, XEventClass>();
			for (int i = 0; i < logs.getSize(); i++) {
				XLogInfo info = XLogInfoFactory.createLogInfo(logs.getLog(i), parameters.getClassifier(i));
				for (XEventClass activity : info.getEventClasses().getClasses()) {
					activities.put(activity.getId(), activity);
				}
			}
			invisibleActivity = new XEventClass(TransEvClassMappingPanel.INVISIBLE, activities.size());
			mapping = new TransEvClassMapping(null, invisibleActivity);
			for (int i = 0; i < nets.getSize(); i++) {
				parameters.setMapping(i, new TransEvClassMapping(null, invisibleActivity));
				for (Transition transition : nets.getNet(i).getNet().getTransitions()) {
					mapping.put(transition, invisibleActivity);
					if (!transition.isInvisible() && activities.containsKey(transition.getLabel())) {
						mapping.put(transition, activities.get(transition.getLabel()));
					}
				}
			}
			TransEvClassMappingPanel mapPanel = new TransEvClassMappingPanel(new HashSet<XEventClass>(
					activities.values()), mapping);
			add(mapPanel, "0, 0");
		}
		return this;
	}

	public void finish() {
		for (int i = 0; i < nets.getSize(); i++) {
			//			System.out.println("Net " + i);
			for (Transition transition : nets.getNet(i).getNet().getTransitions()) {
				//				System.out.println(transition.getLabel() + " -> " + mapping.get(transition));
				parameters.getMapping(i).put(transition, mapping.get(transition));
			}
		}
	}
}
