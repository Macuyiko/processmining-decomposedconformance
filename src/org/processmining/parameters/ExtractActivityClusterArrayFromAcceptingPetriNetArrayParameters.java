package org.processmining.parameters;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.dialogs.TransEvClassMappingPanel;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

public class ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters {

	private TransEvClassMapping mapping;
	private XEventClass invisibleActivity;

	public ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters(AcceptingPetriNetArray nets,
			Set<XEventClass> activities) {
		invisibleActivity = new XEventClass(TransEvClassMappingPanel.INVISIBLE, activities.size());
		mapping = new TransEvClassMapping(null, invisibleActivity);
		for (int i = 0; i < nets.getSize(); i++) {
			AcceptingPetriNet net = nets.getNet(i);
			for (Transition transition : net.getNet().getTransitions()) {
				mapping.put(transition, invisibleActivity);
				for (XEventClass activity : activities) {
					if (activity.getId().equals(transition.getLabel())) {
						mapping.put(transition, activity);
					}
				}
			}
		}
	}

	public void setMapping(TransEvClassMapping mapping) {
		this.mapping = mapping;
	}

	public TransEvClassMapping getMapping() {
		return mapping;
	}

	public void setInvisibleActivity(XEventClass invisibleActivity) {
		this.invisibleActivity = invisibleActivity;
	}

	public XEventClass getInvisibleActivity() {
		return invisibleActivity;
	}

	public boolean equals(Object object) {
		if (object == null)
			return false;
		if (object == this)
			return true;
		if (object instanceof ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters) {
			ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters parameters = (ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters) object;
			return mapping.equals(parameters.mapping);
		}
		return false;
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + this.mapping.hashCode();
		return result;
	}

}
