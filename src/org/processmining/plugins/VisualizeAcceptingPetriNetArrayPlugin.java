package org.processmining.plugins;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNetArray;

@Plugin(name = "Visualize Accepting Petri Net Array", returnLabels = { "Visualized Accepting Petri Net Array" }, returnTypes = { JComponent.class }, parameterLabels = { "Accepting Petri Net Array" }, userAccessible = false)
@Visualizer
public class VisualizeAcceptingPetriNetArrayPlugin {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext context, AcceptingPetriNetArray nets) {
		
		VisualizeAcceptingPetriNetPlugin visualizer = new VisualizeAcceptingPetriNetPlugin();
		
		JTabbedPane tabbedPane = new JTabbedPane();
		for (int index = 0; index < nets.getSize(); index++) {
			String label = "Net " + (index + 1);
			tabbedPane.add(label, visualizer.visualize(context, nets.getNet(index)));
		}
		return tabbedPane;
	}
}
