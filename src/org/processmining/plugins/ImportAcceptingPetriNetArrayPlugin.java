package org.processmining.plugins;

import java.io.File;
import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.impl.DivideAndConquerFactory;

@Plugin(name = "Import Accepting Petri Net Array from APNA file", parameterLabels = { "Filename" }, returnLabels = { "Accepting Petri Net Array" }, returnTypes = { AcceptingPetriNetArray.class })
@UIImportPlugin(description = "APNA Accepting Petri Net Array files", extensions = { "apna" })
public class ImportAcceptingPetriNetArrayPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("APNA files", "apna");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		AcceptingPetriNetArray nets = DivideAndConquerFactory.createAcceptingPetriNetArray();
		File file = getFile();
		String parent = (file == null ? null : file.getParent());
		nets.importFromStream(context, input, parent);
		return nets;
	}
}
