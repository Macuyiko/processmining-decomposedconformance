package org.processmining.plugins;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;

@Plugin(name = "PNML export (Accepting Petri Net)", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Accepting Petri Net", "File" }, userAccessible = true)
@UIExportPlugin(description = "Accepting Petri Net", extension = "pnml")
public class ExportAcceptingPetriNetPlugin {

	@PluginVariant(variantLabel = "PNML export (Accepting Petri Net)", requiredParameterLabels = { 0, 1 })
	public void export(PluginContext context, AcceptingPetriNet net, File file) throws IOException {
		net.exportToFile(context, file);
	}
}
