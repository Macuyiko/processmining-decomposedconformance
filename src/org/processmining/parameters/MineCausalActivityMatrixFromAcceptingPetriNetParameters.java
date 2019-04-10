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
import org.deckfour.xes.model.XLog;
import org.processmining.dialogs.TransEvClassMappingPanel;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

public class MineCausalActivityMatrixFromAcceptingPetriNetParameters {

	/*
	 * Use structure only.
	 */
	public final static int PN = 0;
	/*
	 * Use coverability graph.
	 */
	public final static int CG = 1;
	
	private List<Integer> minerTypeList;
	private TransEvClassMapping map;
	private Set<XEventClass> activities;
	private XEventClass invisibleActivity;
	private List<XEventClassifier> classifier;
	private AcceptingPetriNet net;
	
	public MineCausalActivityMatrixFromAcceptingPetriNetParameters(AcceptingPetriNet net, XLog log) {
		this.net = net;
		classifier = new ArrayList<XEventClassifier>(); 
		if (log.getClassifiers().isEmpty()) {
			classifier.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
		} else {
			classifier.add(log.getClassifiers().get(0));
		}
		XLogInfo info = XLogInfoFactory.createLogInfo(log, classifier.get(0));
		activities = new HashSet<XEventClass>(info.getEventClasses().getClasses());
		invisibleActivity = new XEventClass(TransEvClassMappingPanel.INVISIBLE, activities.size());
		map = (new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(net, activities, classifier.get(0))).getMapping();
		minerTypeList = new ArrayList<Integer>();
		minerTypeList.add(PN);
		
	}
	
	public MineCausalActivityMatrixFromAcceptingPetriNetParameters(AcceptingPetriNet net) {
		this.net = net;
		classifier = null;
		activities = new HashSet<XEventClass>();
		for (Transition transition : net.getNet().getTransitions()) {
			if (!transition.isInvisible()) {
				XEventClass activity = new XEventClass(transition.getLabel(), activities.size());
				if (!activities.contains(activity)) {
					activities.add(activity);
				}
			}
		}		
		invisibleActivity = new XEventClass(TransEvClassMappingPanel.INVISIBLE, activities.size());
		map = (new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(net, activities, null)).getMapping();
		minerTypeList = new ArrayList<Integer>();
		minerTypeList.add(PN);
		
	}

	public void setMinerType(int minerType) {
		minerTypeList.clear();
		minerTypeList.add(minerType);
	}

	public int getMinerType() {
		return minerTypeList.get(0);
	}	

	public List<Integer> getMinerTypeList() {
		return minerTypeList;
	}
	
	public void setMapping(TransEvClassMapping mapping) {
		this.map = mapping;
	}
	
	public TransEvClassMapping getMapping() {
		return map;
	}
	
	public void setActivities(Set<XEventClass> activities) {
		this.activities.clear();
		this.activities.addAll(activities);
		map = (new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(net, activities, classifier.get(0))).getMapping();
	}
	
	public Set<XEventClass> getActivities() {
		return activities;
	}
	
	public void setInvisibleActivity(XEventClass invisibleActivity) {
		this.invisibleActivity = invisibleActivity;
	}

	public XEventClass getInvisibleActivity() {
		return invisibleActivity;
	}

	public boolean equals(Object object) {
		if (object instanceof MineCausalActivityMatrixFromAcceptingPetriNetParameters) {
			MineCausalActivityMatrixFromAcceptingPetriNetParameters parameters = (MineCausalActivityMatrixFromAcceptingPetriNetParameters) object;
			return minerTypeList.equals(parameters.minerTypeList) && map.equals(parameters.map) && classifier.equals(parameters.classifier);
		}
		return false;		
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier.clear();
		this.classifier.add(classifier);
	}

	public XEventClassifier getClassifier() {
		return classifier.get(0);
	}

	public List<XEventClassifier> getClassifierList() {
		return classifier;
	}
}
