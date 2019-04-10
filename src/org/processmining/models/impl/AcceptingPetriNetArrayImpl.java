package org.processmining.models.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.plugins.ImportAcceptingPetriNetPlugin;

import com.csvreader.CsvWriter;

public class AcceptingPetriNetArrayImpl extends ObjectArrayImpl<AcceptingPetriNet> implements AcceptingPetriNetArray {

	@Deprecated
	public int addNet(AcceptingPetriNet net) {
		return addElement(net);
	}

	@Deprecated
	public int removeNet(AcceptingPetriNet net) {
		return removeElement(net);
	}

	@Deprecated
	public void addNet(int index, AcceptingPetriNet net) {
		addElement(index, net);
	}

	@Deprecated
	public void removeNet(int index) {
		removeElement(index);
	}

	@Deprecated
	public AcceptingPetriNet getNet(int index) {
		return getElement(index);
	}

	public void importFromStream(PluginContext context, InputStream input, String parent) throws Exception {
		importFromStream(context, input, parent, new ImportAcceptingPetriNetPlugin());
	}

	public void exportToFile(PluginContext context, File file) throws IOException {
		Writer fileWriter = new FileWriter(file);
		CsvWriter csvWriter = new CsvWriter(fileWriter, ',');
		int n = 1;
		for (AcceptingPetriNet acceptingNet: list) {
			String fileName = file.getName();
			File dir = file.getParentFile();
			String prefix = fileName.substring(0, fileName.indexOf("."));
			File netFile = File.createTempFile(prefix + "." + n + ".", ".pnml", dir);
			csvWriter.write(netFile.getName());
			csvWriter.endRecord();
			System.out.println("Exporting Accepting Petri Net to " + netFile.getName());
			acceptingNet.exportToFile(context, netFile);
			n++;
		}
		csvWriter.close();
	}

}
