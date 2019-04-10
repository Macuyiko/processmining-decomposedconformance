package org.processmining.plugins;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNetArray;

@Plugin(name = "APNA export (Accepting Petri Net Array)", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Accepting Petri Net Array", "File" }, userAccessible = true)
@UIExportPlugin(description = "Accepting Petri Net Array", extension = "apna")
public class ExportAcceptingPetriNetArrayPlugin {

	@PluginVariant(variantLabel = "APNA export (Accepting Petri Net Array)", requiredParameterLabels = { 0, 1 })
	public void export(PluginContext context, AcceptingPetriNetArray nets, File file) throws IOException {
		nets.exportToFile(context, file);
	}
}
