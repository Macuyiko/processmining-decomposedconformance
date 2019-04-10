package org.processmining.plugins;

import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.CausalActivityMatrix;
import org.processmining.models.impl.DivideAndConquerFactory;

@Plugin(name = "Import Causal Activity Matrix from CAM file", parameterLabels = { "Filename" }, returnLabels = { "Causal Activity Matrix" }, returnTypes = { CausalActivityMatrix.class })
@UIImportPlugin(description = "CAM files", extensions = { "cam" })
public class ImportCausalActivityMatrixPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("CAM files", "cam");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		CausalActivityMatrix matrix = DivideAndConquerFactory.createCausalActivityMatrix();
		matrix.importFromStream(input);
		return matrix;
	}
}
