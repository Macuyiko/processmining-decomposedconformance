package org.processmining.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JPanel;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.parameters.ConvertPetriNetToAcceptingPetriNetParameters;

public class ConvertPetriNetToAcceptingPetriNetDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8223132048730054360L;

	private PetrinetGraph net;
	private ConvertPetriNetToAcceptingPetriNetParameters parameters;
	public ConvertPetriNetToAcceptingPetriNetDialog(final UIPluginContext context, final PetrinetGraph net,
			final ConvertPetriNetToAcceptingPetriNetParameters parameters) {
		this.net = net;
		this.parameters = parameters;
	}
	
	public JPanel getPanel(int n) {
		removeAll();
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		if (n == 0) {
			MarkingPanel initialMarkingPanel = new MarkingPanel(net, parameters.getInitialMarking());
			add(initialMarkingPanel, "0, 0");
		} else if (n == 1) {
			MarkingsPanel finalMarkingsPanel = new MarkingsPanel(net, parameters.getFinalMarkings());
			add(finalMarkingsPanel, "0, 0");
		}
		return this;
	}
}
