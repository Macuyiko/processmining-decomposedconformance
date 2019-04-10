package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.util.Set;

import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.parameters.DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters;

public class DecomposeAcceptingPetriNetUsingActivityClusterArrayDialog extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6732517188360796457L;

	public DecomposeAcceptingPetriNetUsingActivityClusterArrayDialog(AcceptingPetriNet net, Set<XEventClass> activities, DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters parameters) {		
		double size[][] = { { TableLayoutConstants.FILL}, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		add(new TransEvClassMappingPanel(activities, parameters.getMapping()), "0, 0");
	}

}
