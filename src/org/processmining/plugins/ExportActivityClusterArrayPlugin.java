package org.processmining.plugins;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.ActivityClusterArray;

@Plugin(name = "ACA export (Activity Cluster Array)", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Activity Cluster Array", "File" }, userAccessible = true)
@UIExportPlugin(description = "Activity Cluster Array", extension = "aca")
public class ExportActivityClusterArrayPlugin {

	@PluginVariant(variantLabel = "ACA export (Activity Cluster Array)", requiredParameterLabels = { 0, 1 })
	public void export(PluginContext context, ActivityClusterArray array, File file) throws IOException {
		array.exportToFile(file);
	}
}
