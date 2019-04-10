package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.processmining.parameters.MineCausalActivityMatrixFromEventLogParameters;

public class MineCausalActivityMatrixFromEventLogDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3148397115646503161L;

	public MineCausalActivityMatrixFromEventLogDialog(XLog eventLog, MineCausalActivityMatrixFromEventLogParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		add(new ClassifierPanel(eventLog.getClassifiers(), parameters.getClassifierList()), "0, 0");
		
		List<String> miners = new ArrayList<String>();
		miners.add(MineCausalActivityMatrixFromEventLogParameters.STANDARD);
		miners.add(MineCausalActivityMatrixFromEventLogParameters.HEURISTICS);
		miners.add(MineCausalActivityMatrixFromEventLogParameters.FUZZY);
		Collections.sort(miners);
		add(new MinerPanel(miners, parameters.getMinerList()), "0, 1");
	}
}