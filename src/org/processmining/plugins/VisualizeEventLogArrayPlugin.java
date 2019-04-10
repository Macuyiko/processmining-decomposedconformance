package org.processmining.plugins;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.EventLogArray;
import org.processmining.plugins.log.ui.logdialog.SlickerOpenLogSettings;

@Plugin(name = "Visualize Event Log Array", returnLabels = { "Visualized Event Log Array" }, returnTypes = { JComponent.class }, parameterLabels = { "Event Log Array" }, userAccessible = false)
@Visualizer
public class VisualizeEventLogArrayPlugin {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext context, EventLogArray logs) {
		
		JTabbedPane tabbedPane = new JTabbedPane();
		for (int index = 0; index < logs.getSize(); index++) {
			String label = "Log " + (index + 1);
			tabbedPane.add(label, (new SlickerOpenLogSettings()).showLogVis(context, logs.getLog(index)));
		}
		return tabbedPane;
	}
}
