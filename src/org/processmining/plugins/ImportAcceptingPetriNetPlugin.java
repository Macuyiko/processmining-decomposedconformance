package org.processmining.plugins;

import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.impl.DivideAndConquerFactory;

@Plugin(name = "Import Accepting Petri Net from PNML file", parameterLabels = { "Filename" }, returnLabels = { "Accepting Petri Net" }, returnTypes = { AcceptingPetriNet.class })
@UIImportPlugin(description = "PNML Accepting Petri Net files", extensions = { "pnml" })
public class ImportAcceptingPetriNetPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("PNML files", "pnml");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		AcceptingPetriNet net = DivideAndConquerFactory.createAcceptingPetriNet();
		net.importFromStream(context, input);
		return net;
	}
}
