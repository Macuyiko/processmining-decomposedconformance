package org.processmining.plugins;

import java.awt.Color;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.semantics.petrinet.Marking;

@Plugin(name = "Visualize Accepting Petri Net", returnLabels = { "Visualized Accepting Petri Net" }, returnTypes = { JComponent.class }, parameterLabels = { "Accepting Petri Net" }, userAccessible = false)
@Visualizer
public class VisualizeAcceptingPetriNetPlugin {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext context, AcceptingPetriNet net) {
		ViewSpecificAttributeMap map = new ViewSpecificAttributeMap();
		for (Place place: net.getInitialMarking().baseSet()) {
			map.putViewSpecific(place, AttributeMap.FILLCOLOR, new Color(127, 0, 0));
		}
		for (Marking marking: net.getFinalMarkings()) {
			for (Place place: marking.baseSet()) {
				map.putViewSpecific(place, AttributeMap.FILLCOLOR, new Color(0, 0, 127));
			}
		}
		return ProMJGraphVisualizer.instance().visualizeGraph(context, net.getNet(), map);
	}
}
