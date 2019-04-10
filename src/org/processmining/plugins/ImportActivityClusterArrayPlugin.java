package org.processmining.plugins;

import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.impl.DivideAndConquerFactory;

@Plugin(name = "Import Activity Cluster Array from ACA file", parameterLabels = { "Filename" }, returnLabels = { "Activity Cluster Array" }, returnTypes = { ActivityClusterArray.class })
@UIImportPlugin(description = "ACA files", extensions = { "aca" })
public class ImportActivityClusterArrayPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("ACA files", "aca");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		ActivityClusterArray array = DivideAndConquerFactory.createActivityClusterArray();
		array.importFromStream(input);
		return array;
	}
}
