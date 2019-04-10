package org.processmining.plugins;

import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.CausalActivityGraph;
import org.processmining.models.impl.DivideAndConquerFactory;

@Plugin(name = "Import Causal Activity Graph from CAG file", parameterLabels = { "Filename" }, returnLabels = { "Causal Activity Graph" }, returnTypes = { CausalActivityGraph.class })
@UIImportPlugin(description = "CAG files", extensions = { "cag" })
public class ImportCausalActivityGraphPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("CAG files", "cag");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		CausalActivityGraph graph = DivideAndConquerFactory.createCausalActivityGraph();
		graph.importFromStream(input);
		return graph;
	}
}
