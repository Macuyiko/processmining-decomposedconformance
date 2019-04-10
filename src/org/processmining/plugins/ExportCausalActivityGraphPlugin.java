package org.processmining.plugins;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.CausalActivityGraph;

@Plugin(name = "CAG export (Causal Activity Graph)", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Causal Activity Graph", "File" }, userAccessible = true)
@UIExportPlugin(description = "Causal Activity Graph", extension = "cag")
public class ExportCausalActivityGraphPlugin {

	@PluginVariant(variantLabel = "CAG export (Causal Activity Graph)", requiredParameterLabels = { 0, 1 })
	public void export(PluginContext context, CausalActivityGraph graph, File file) throws IOException {
		graph.exportToFile(file);
	}
}
