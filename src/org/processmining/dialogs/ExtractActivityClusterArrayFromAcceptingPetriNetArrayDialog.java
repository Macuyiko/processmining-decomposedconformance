package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.util.Set;

import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.parameters.ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters;

public class ExtractActivityClusterArrayFromAcceptingPetriNetArrayDialog extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -560514328184733130L;

	public ExtractActivityClusterArrayFromAcceptingPetriNetArrayDialog(Set<XEventClass> activities, ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters parameters) {		
		double size[][] = { { TableLayoutConstants.FILL}, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		add(new TransEvClassMappingPanel( activities, parameters.getMapping()), "0, 0");
	}

}