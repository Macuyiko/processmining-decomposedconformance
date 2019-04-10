package org.processmining.plugins;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.CausalActivityMatrix;

@Plugin(name = "CAM export (Causal Activity Matrix)", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Causal Activity Matrix", "File" }, userAccessible = true)
@UIExportPlugin(description = "Causal Activity Matrix", extension = "cam")
public class ExportCausalActivityMatrixPlugin {

	@PluginVariant(variantLabel = "CAM export (Causal Activity Matrix)", requiredParameterLabels = { 0, 1 })
	public void export(PluginContext context, CausalActivityMatrix matrix, File file) throws IOException {
		matrix.exportToFile(file);
	}
}
